package calc

import chisel3._
import chisel3.util._
import const.R._

class SignedMul extends BlackBox with HasBlackBoxResource {
    val io = IO(new Bundle {
        val CLK = Input(Clock())
        val CE  = Input(Bool())
        val A   = Input(UInt(33.W))
        val B   = Input(UInt(33.W))
        val P   = Output(UInt(66.W))
    })
}

class multiplier extends Module {
    val mulClockNum = 2
    val io = IO(new Bundle {
        val yu = new ioport.calc_interface()
    })
    if (vivado_build) {
        val mul  = Module(new SignedMul()).io
        mul.CLK   := clock
        mul.CE    := io.yu.en
        mul.A     := Mux(io.yu.signed, Cat(io.yu.op1(31), io.yu.op1), Cat(0.U(1.W), io.yu.op1))
        mul.B     := Mux(io.yu.signed, Cat(io.yu.op2(31), io.yu.op2), Cat(0.U(1.W), io.yu.op2))
        val cnt    = RegInit(0.U(2.W))
        cnt       := Mux(io.yu.en && !io.yu.ready, cnt + 1.U, 0.U(2.W))
        io.yu.ready  := cnt >= mulClockNum.U
        io.yu.result := mul.P(63, 0)
    } else {
        val A      = Mux(io.yu.signed, Cat(io.yu.op1(31), io.yu.op1), Cat(0.U(1.W), io.yu.op1)).asSInt
        val B      = Mux(io.yu.signed, Cat(io.yu.op2(31), io.yu.op2), Cat(0.U(1.W), io.yu.op2)).asSInt
        val P      = A * B
        val cnt    = RegInit(0.U(2.W))
        val result = RegInit(0.S(66.W))
        when (io.yu.en) { result := P }
        cnt          := Mux(io.yu.en && !io.yu.ready, cnt + 1.U, 0.U(2.W))
        io.yu.ready  := cnt >= mulClockNum.U
        io.yu.result := result(63, 0)
    }
}
