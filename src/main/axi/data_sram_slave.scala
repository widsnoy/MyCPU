package axi

import chisel3._
import chisel3.util._
import const.R._

class data_sram_slave extends Module {
    val io = IO(new Bundle {
        val es   = Flipped(new ioport.es_ram())
        val ms   = Flipped(new ioport.ms_ram())
        
        val axi = new ioport.data_sram_slave_axi
    })

    val araddr     = RegInit(0.U(data_len.W))
    val arsize     = RegInit(0.U(2.W))
    val arvalid    = RegInit(false.B)
    val awaddr     = RegInit(0.U(data_len.W))
    val awsize     = RegInit(0.U(3.W))
    val awvalid    = RegInit(false.B)
    val rready     = RegInit(false.B)
    val wdata      = RegInit(0.U(data_len.W))
    val wstrb      = RegInit(0.U(4.W))
    val wvalid     = RegInit(false.B)
    val bready     = RegInit(false.B)
    val es_addr_ok = RegInit(false.B)

    io.axi.araddr  <> araddr 
    io.axi.arsize  <> arsize
    io.axi.arvalid <> arvalid
    io.axi.rready  <> rready
    io.axi.awaddr  <> awaddr
    io.axi.awsize  <> awsize
    io.axi.awvalid <> awvalid 
    io.axi.wdata   <> wdata
    io.axi.wstrb   <> wstrb
    io.axi.wvalid  <> wvalid
    io.axi.bready  <> bready
    
    // www1/awready  www2/wready
    val idle :: raddr :: rdata :: waddr :: www1 :: www2 :: www3 :: Nil = Enum(7)
    val state = RegInit(idle)
    switch (state) {
        is (idle) {
            when (io.es.req) {
                arvalid    := !io.es.wr
                araddr     := io.es.addr
                arsize     := io.es.size
                awvalid    := io.es.wr
                awaddr     := io.es.addr
                awsize     := io.es.size
                wvalid     := io.es.wr
                wdata      := io.es.wdata
                wstrb      := io.es.wstrb
                es_addr_ok := true.B
                state      := Mux(io.es.wr, waddr, raddr)
            }
        }
        is (raddr) {
            es_addr_ok := false.B
            when (io.axi.arready) {
                arvalid := false.B
                rready  := true.B
                state   := rdata
            }
        }
        is (rdata) {
            when (io.axi.rvalid) {
                rready  := false.B
                state   := idle
            }
        }
        is (waddr) {
            es_addr_ok := false.B
            when (io.axi.wready && io.axi.awready) {
                awvalid := false.B
                wvalid  := false.B
                bready  := true.B
                state   := www3
            }.elsewhen (io.axi.awready) {
                awvalid := false.B
                state   := www1
            }.elsewhen (io.axi.wready) {
                wvalid := false.B
                state   := www2
            }
        }
        is (www1) {
            when (io.axi.wready) {
                wvalid := false.B
                bready  := true.B
                state   := www3
            }
        }
        is (www2) {
            when (io.axi.awready) {
                awvalid := false.B
                bready  := true.B
                state   := www3
            }
        }
        is (www3) {
            when (io.axi.bvalid) {
                bready  := false.B
                state   := idle
            }
        }
    }

    io.es.addr_ok  := es_addr_ok
    io.ms.data_ok  := (state === rdata && io.axi.rvalid) || (state === www3 && io.axi.bvalid)
    io.ms.rdata    := io.axi.rdata
}