package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class SignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    // 除数
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(LENX.W))
    // 被除数
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(LENX.W))
    // 结果
    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(HILO.W))
  })
}

class UnsignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    // 除数
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(LENX.W))
    // 被除数
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(LENX.W))
    // 结果
    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(HILO.W))
  })
}

class DIV extends Module {
    val io = IO(new Bundle {
        val en    = Input(Bool())
        val signed= Input(Bool())
        val op1   = Input(UInt(LENX.W))
        val op2   = Input(UInt(LENX.W))
        val result= Output(UInt(HILO.W))
        val ready = Output(Bool())
    })
    val udiv = Module(new UnsignedDiv()).io
    val sdiv = Module(new SignedDiv()).io
    sdiv.aclk                   := clock
    sdiv.s_axis_dividend_tdata  := io.op1
    sdiv.s_axis_divisor_tdata   := io.op2
    udiv.aclk                   := clock
    udiv.s_axis_dividend_tdata  := io.op1
    udiv.s_axis_divisor_tdata   := io.op2

    val udiv_op1_sent   = RegInit(false.B)
    val udiv_op2_sent   = RegInit(false.B)
    when (io.en && udiv.s_axis_dividend_tready && udiv.s_axis_dividend_tvalid) {
        udiv_op1_sent := true.B
    }.elsewhen (io.ready) {
        udiv_op1_sent := false.B
    }
    when (io.en && udiv.s_axis_divisor_tready && udiv.s_axis_divisor_tvalid) {
        udiv_op2_sent := true.B
    }.elsewhen (io.ready) {
        udiv_op2_sent := false.B
    }
    udiv.s_axis_dividend_tvalid := io.en && !udiv_op1_sent
    udiv.s_axis_divisor_tvalid  := io.en && !udiv_op2_sent

    val sdiv_op1_sent   = RegInit(false.B)
    val sdiv_op2_sent   = RegInit(false.B)
    when (io.en && sdiv.s_axis_dividend_tready && sdiv.s_axis_dividend_tvalid) {
        sdiv_op1_sent := true.B
    }.elsewhen (io.ready) {
        sdiv_op1_sent := false.B
    }
    when (io.en && sdiv.s_axis_divisor_tready && sdiv.s_axis_divisor_tvalid) {
        sdiv_op2_sent := true.B
    }.elsewhen (io.ready) {
        sdiv_op2_sent := false.B
    }
    sdiv.s_axis_dividend_tvalid := io.en && !sdiv_op1_sent
    sdiv.s_axis_divisor_tvalid  := io.en && !sdiv_op2_sent

    io.ready := Mux(io.signed, sdiv.m_axis_dout_tvalid, udiv.m_axis_dout_tvalid)
    io.result:= Mux(io.signed, sdiv.m_axis_dout_tdata, udiv.m_axis_dout_tdata)
}