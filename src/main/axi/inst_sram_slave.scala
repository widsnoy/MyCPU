package axi

import chisel3._
import chisel3.util._
import const.R._

class inst_sram_slave extends Module {
    val io = IO(new Bundle {
        val pf   = Flipped(new ioport.pf_ram())
        val fs   = Flipped(new ioport.fs_ram())
        
        val axi = new ioport.inst_sram_slave_axi
    })

    val araddr     = RegInit(0.U(data_len.W))
    val arsize     = RegInit(0.U(3.W))
    val arvalid    = RegInit(false.B)
    val rready     = RegInit(false.B)
    val pf_addr_ok = RegInit(false.B)

    io.axi.araddr   <> araddr
    io.axi.arsize   <> arsize
    io.axi.arvalid  <> arvalid
    io.axi.rready   <> rready
    io.pf.addr_ok   := pf_addr_ok
    io.fs.data_ok   := io.axi.rvalid
    io.fs.rdata     := io.axi.rdata

    // idle addr data
    val idle :: raddr :: rdata :: Nil = Enum(3)
    val state = RegInit(idle)
    switch (state) {
        is (idle) {
            when (io.pf.req) {
                arvalid    := true.B
                araddr     := io.pf.addr
                arsize     := io.pf.size
                state      := raddr
                pf_addr_ok := true.B
            }
        }
        is (raddr) {
            pf_addr_ok := false.B
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
    }
}