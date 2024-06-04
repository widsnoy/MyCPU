package gpr

import chisel3._
import chisel3.util._
import const.R._

class GPR extends Module {
    val io = IO(new Bundle {
        val ds = new ioport.ds_gpr()
        val ws = new ioport.ws_gpr()
    })
    val gpr = RegInit(VecInit(Seq.fill(gpr_count)(0.U(data_len.W))))
    io.ds.rd1 :=  Mux(io.ds.rr1 =/= 0.U, gpr(io.ds.rr1), 0.U(data_len.W))
    io.ds.rd2 :=  Mux(io.ds.rr2 =/= 0.U, gpr(io.ds.rr2), 0.U(data_len.W))
    io.ds.rd3 :=  Mux(io.ds.rr3 =/= 0.U, gpr(io.ds.rr3), 0.U(data_len.W))
    when (io.ws.wen) {gpr(io.ws.wr) := io.ws.wd}
}