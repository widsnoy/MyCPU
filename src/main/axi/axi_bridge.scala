package axi

import chisel3._
import chisel3.util._
import const.R._

class axi_bridge extends Module {
    val io = IO(new Bundle {
        val inst = Flipped(new ioport.inst_sram_slave_axi())
        val data = Flipped(new ioport.data_sram_slave_axi())
    
        val axi  = new ioport.axi_interface()
    })

    // keep lock from req to addr_ok  (next posedge) 
    val ar_sel_lock = RegInit(false.B)
    val ar_sel_val  = RegInit(false.B)
    val ar_id       = Mux(ar_sel_lock, ar_sel_val, io.data.arvalid)
    val r_id        = io.axi.rid(0).asBool
    when (io.axi.arvalid) {
        when(io.axi.arready) {
            ar_sel_lock := false.B
            ar_sel_val  := false.B
        }.otherwise {
            ar_sel_lock := true.B
            ar_sel_val  := ar_id
        }
    }

    io.axi.arid     := ar_id
    io.axi.araddr   := Mux(ar_id, io.data.araddr, io.inst.araddr)
    io.axi.arlen    := 0.U
    io.axi.arsize   := Mux(ar_id, io.data.arsize, io.inst.arsize)
    io.axi.arburst  := 1.U
    io.axi.arlock   := 0.U
    io.axi.arcache  := 0.U
    io.axi.arprot   := 0.U
    io.axi.arvalid  := Mux(ar_id, io.data.arvalid, io.inst.arvalid)
    io.inst.arready := !ar_id && io.axi.arready
    io.data.arready := ar_id && io.axi.arready

    io.axi.rready   := Mux(r_id, io.data.rready, io.inst.rready)
    io.data.rdata   := io.axi.rdata
    io.inst.rdata   := io.axi.rdata
    io.data.rvalid  := r_id && io.axi.rvalid
    io.inst.rvalid  := !r_id && io.axi.rvalid

    io.axi.awid     := 1.U
    io.axi.awaddr   := io.data.awaddr
    io.axi.awlen    := 0.U
    io.axi.awsize   := io.data.awsize
    io.axi.awburst  := 1.U
    io.axi.awlock   := 0.U
    io.axi.awcache  := 0.U
    io.axi.awprot   := 0.U
    io.axi.awvalid  := io.data.awvalid
    io.data.awready := io.axi.awready

    io.axi.wid      := 1.U
    io.axi.wdata    := io.data.wdata
    io.axi.wstrb    := io.data.wstrb
    io.axi.wlast    := 1.U
    io.axi.wvalid   := io.data.wvalid
    io.data.wready  := io.axi.wready

    io.axi.bready   := io.data.bready
    io.data.bvalid  := io.axi.bvalid 
}