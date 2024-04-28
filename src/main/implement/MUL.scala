package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class SignedMul extends BlackBox with HasBlackBoxResource {
    val io = IO(new Bundle {
        val CLK = Input(Clock())
        val CE  = Input(Bool())
        val A   = Input(UInt((LENX + 1).W))
        val B   = Input(UInt((LENX + 1).W))
        val P   = Output(UInt((HILO + 2).W))
    })
}

class MUL extends Module {
    val io = IO(new Bundle {
        val en    = Input(Bool())
        val signed= Input(Bool())
        val op1   = Input(UInt(LENX.W))
        val op2   = Input(UInt(LENX.W))
        val result= Output(UInt(HILO.W))
        val ready = Output(Bool())
    })
    val mul = Module(new SignedMul).io
    mul.CLK := clock
    mul.CE  := io.en
    mul.A   := Mux(io.signed, Cat(io.op1(31), io.op1), Cat(0.U(1.W), io.op1))
    mul.B   := Mux(io.signed, Cat(io.op2(31), io.op2), Cat(0.U(1.W), io.op2))
    val cnt = RegInit(0.U(2.W))
    cnt := Mux(io.en && !io.ready, cnt + 1.U, 0.U(2.W))
    io.ready := cnt >= mulClockNum.U
    io.result := mul.P(63, 0)
}
