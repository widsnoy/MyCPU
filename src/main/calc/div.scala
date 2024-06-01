package calc

import chisel3._
import chisel3.util._

class SignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    // 除数
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(32.W))
    // 被除数
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(32.W))
    // 结果
    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(64.W))
  })
}

class UnsignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    // 除数
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(32.W))
    // 被除数
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(32.W))
    // 结果
    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(64.W))
  })
}

class DIV extends Module {
    val divClockNum = 8
    val io = IO(new Bundle {
        val yu = new ioport.calc_interface()
    })
    if (vivado_build) {
        val udiv = Module(new UnsignedDiv()).io
        val sdiv = Module(new SignedDiv()).io
        sdiv.aclk                   := clock
        sdiv.s_axis_dividend_tdata  := io.yu.op1
        sdiv.s_axis_divisor_tdata   := io.yu.op2
        udiv.aclk                   := clock
        udiv.s_axis_dividend_tdata  := io.yu.op1
        udiv.s_axis_divisor_tdata   := io.yu.op2

        val udiv_op1_sent   = RegInit(false.B)
        val udiv_op2_sent   = RegInit(false.B)
        when (io.yu.en && udiv.s_axis_dividend_tready && udiv.s_axis_dividend_tvalid) {
            udiv_op1_sent := true.B
        }.elsewhen (io.yu.ready) {
            udiv_op1_sent := false.B
        }
        when (io.yu.en && udiv.s_axis_divisor_tready && udiv.s_axis_divisor_tvalid) {
            udiv_op2_sent := true.B
        }.elsewhen (io.yu.ready) {
            udiv_op2_sent := false.B
        }
        udiv.s_axis_dividend_tvalid := io.yu.en && !udiv_op1_sent
        udiv.s_axis_divisor_tvalid  := io.yu.en && !udiv_op2_sent

        val sdiv_op1_sent   = RegInit(false.B)
        val sdiv_op2_sent   = RegInit(false.B)
        when (io.yu.en && sdiv.s_axis_dividend_tready && sdiv.s_axis_dividend_tvalid) {
            sdiv_op1_sent := true.B
        }.elsewhen (io.yu.ready) {
            sdiv_op1_sent := false.B
        }
        when (io.yu.en && sdiv.s_axis_divisor_tready && sdiv.s_axis_divisor_tvalid) {
            sdiv_op2_sent := true.B
        }.elsewhen (io.yu.ready) {
            sdiv_op2_sent := false.B
        }
        sdiv.s_axis_dividend_tvalid := io.yu.en && !sdiv_op1_sent
        sdiv.s_axis_divisor_tvalid  := io.yu.en && !sdiv_op2_sent

        io.yu.ready  := Mux(io.yu.signed, sdiv.m_axis_dout_tvalid, udiv.m_axis_dout_tvalid)
        io.yu.result := Mux(io.yu.signed, sdiv.m_axis_dout_tdata, udiv.m_axis_dout_tdata)
    } else {
        val cnt     = RegInit(0.U(4.W))
        cnt        := Mux(io.yu.en && !io.yu.ready, cnt + 1.U, 0.U(2.W))
        val signed  = io.yu.signed
        val op1_rev = (signed & io.yu.op1(31)).asBool
        val op2_rev = (signed & io.yu.op2(31)).asBool
        val op1_abs = Mux(op1_rev, (-io.yu.op1).asUInt, io.yu.op1)
        val op2_abs = Mux(op2_rev, -io.yu.op2, io.yu.op2)
        val quo_abs = op1_abs / op2_abs
        val rem_abs = op1_abs - quo_abs * op2_abs
        val quo_f   = (signed & (io.yu.op1(31) ^ io.yu.op2(31))).asBool
        val rem_f   = (signed & io.yu.op1(31)).asBool
        val quo     = RegInit(0.S(32.W))
        val rem     = RegInit(0.S(32.W))
        when (io.yu.en) {
            quo     := Mux(quo_f, (-quo_abs).asSInt, quo_abs.asSInt)
            rem     := Mux(rem_f, (-rem_abs).asSInt, rem_abs.asSInt)
        }
        io.yu.ready    := cnt >= divClockNum.U
        io.yu.result   := Cat(quo, rem)
    }
}