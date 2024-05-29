package regfile

import chisel3._
import chisel3.util._
import const.R._

class Regfile extends Module {
    val io = IO(new Bundle {
        val reg = new ioport.reg()
    })
    val regfile = RegInit(VecInit(Seq.fill(reg_count)(0.U(data_len.W))))
    io.reg.rd1 :=  Mux(io.reg.r1 =/= 0.U, regfile(io.reg.r1), 0.U(32.W))
    io.reg.rd2 :=  Mux(io.reg.r2 =/= 0.U, regfile(io.reg.r2), 0.U(32.W))
    io.reg.rd3 :=  Mux(io.reg.r3 =/= 0.U, regfile(io.reg.r3), 0.U(32.W))
    when (io.reg.wen === 1.U) {regfile(io.reg.wr) := io.reg.wd}
}