package regfile

import chisel3._
import chisel3.util._
import const.R._

class Regfile extends Module {
    val io = IO(new Bundle {
        val ds = new ioport.ds_reg()
        val ws = new ioport.ws_reg()
    })
    val regfile = RegInit(VecInit(Seq.fill(reg_count)(0.U(data_len.W))))
    io.ds.rd1 :=  Mux(io.ds.rr1 =/= 0.U, regfile(io.ds.rr1), 0.U(data_len.W))
    io.ds.rd2 :=  Mux(io.ds.rr2 =/= 0.U, regfile(io.ds.rr2), 0.U(data_len.W))
    io.ds.rd3 :=  Mux(io.ds.rr3 =/= 0.U, regfile(io.ds.rr3), 0.U(data_len.W))
    when (io.ws.wen) {regfile(io.ws.wr) := io.ws.wd}
}