package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class Regfile extends Module {
    val io = IO(new Bundle {
        val reg = new REG_IO()
    })
    val regfile = RegInit(VecInit(Seq.fill(LENX)(0.U(LENX.W))))
    io.reg.rdata1 :=  Mux(io.reg.r1 =/= 0.U, regfile(io.reg.r1), 0.U(32.W))
    io.reg.rdata2 :=  Mux(io.reg.r2 =/= 0.U, regfile(io.reg.r2), 0.U(32.W))
    io.reg.rdata3 :=  Mux(io.reg.r3 =/= 0.U, regfile(io.reg.r3), 0.U(32.W))
    when (io.reg.wen === 1.U) {regfile(io.reg.wr) := io.reg.wdata}
}