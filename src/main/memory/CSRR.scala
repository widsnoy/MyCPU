package csr

import chisel3._
import chisel3.util._
import common._
import common.Consts._

trait base {
  val id: UInt
  val info: Data
  val rw: UInt
  def write(value: UInt) = {
    info := ((~rw & info.asUInt) | (rw & value)).asTypeOf(info)
  }
}

class CRMD_info extends Bundle {
  val zero = UInt(23.W)
  val datm = UInt(2.W) 
  val datf = UInt(2.W) 
  val pg   = Bool()    
  val da   = Bool()    
  val ie   = Bool()    
  val plv  = UInt(2.W)
}

class CRMD extends base {
  override val info = RegInit({
    val init = WireDefault(0.U.asTypeOf(new CRMD_info))
    init.da := true.B
    init
  })
  override val id = CSR.CRMD
  override val rw = "b0000_0000_0000_0000_0000_0001_1111_1111".U
}

class PRMD_info extends Bundle {
  val zero = UInt(29.W)
  val pie  = Bool()
  val pplv = UInt(2.W)
}

class PRMD extends base {
  override val info = RegInit(0.U.asTypeOf(new PRMD_info))
  override val id   = CSR.PRMD
  override val rw   = "b0000_0000_0000_0000_0000_0000_0000_0111".U
}

class EUEN_info extends Bundle {
  val zero = UInt(31.W)
  val fpe = Bool()
}

class EUEN extends base {
  override val info = RegInit(0.U.asTypeOf(new EUEN_info))
  override val id   = CSR.EUEN
  override val rw   = "b0000_0000_0000_0000_0000_0000_0000_0001".U
}

class ECFG_info extends Bundle {
  val zero2 = UInt(19.W)
  val lie_12_11 = UInt(2.W)
  val zero1     = Bool()
  val lie_9_0   = UInt(10.W)
}

class ECFG extends base {
  override val info = RegInit(0.U.asTypeOf(new ECFG_info))
  override val id   = CSR.ECFG
  override val rw   = "b0000_0000_0000_0000_0001_1011_1111_1111".U
}

class ESTAT_info extends Bundle {
  val zero3    = Bool()
  val esubcode = UInt(9.W)
  val ecode    = UInt(6.W)
  val zero2    = UInt(3.W)
  val is_12    = Bool()
  val is_11    = Bool()
  val zero1    = Bool()
  val is_9_2   = UInt(8.W)
  val is_1_0   = UInt(2.W)
}

class ESTAT extends base {
  override val info = RegInit(0.U.asTypeOf(new ESTAT_info))
  override val id   = CSR.ESTAT
  override val rw   = "b0000_0000_0000_0000_0000_0000_0000_0011".U
}

class ERA_info extends Bundle {
  val pc = UInt(LENX.W)
}

class ERA extends base {
  override val info = RegInit(0.U.asTypeOf(new ERA_info))
  override val id   = CSR.ERA
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class BADV_info extends Bundle {
  val vaddr = UInt(LENX.W)
}

class BADV extends base {
  override val info = RegInit(0.U.asTypeOf(new BADV_info))
  override val id   = CSR.BADV
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class EENTRY_info extends Bundle {
  val va   = UInt(26.W)
  val zero = UInt(6.W)
}

class EENTRY extends base {
  override val info = RegInit(0.U.asTypeOf(new EENTRY_info))
  override val id   = CSR.EENTRY
  override val rw   = "b1111_1111_1111_1111_1111_1111_1100_0000".U
}

class SAVE0_info extends Bundle {
  val data = UInt(LENX.W)
}

class SAVE0 extends base {
  override val info = RegInit(0.U.asTypeOf(new SAVE0_info))
  override val id   = CSR.SAVE0
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class SAVE1_info extends Bundle {
  val data = UInt(LENX.W)
}

class SAVE1 extends base {
  override val info = RegInit(0.U.asTypeOf(new SAVE1_info))
  override val id   = CSR.SAVE1
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class SAVE2_info extends Bundle {
  val data = UInt(LENX.W)
}

class SAVE2 extends base {
  override val info = RegInit(0.U.asTypeOf(new SAVE2_info))
  override val id   = CSR.SAVE2
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class SAVE3_info extends Bundle {
  val data = UInt(LENX.W)
}

class SAVE3 extends base {
  override val info = RegInit(0.U.asTypeOf(new SAVE3_info))
  override val id   = CSR.SAVE3
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class TID_info extends Bundle {
  val tid = UInt(32.W)
}

class TID extends base {
  override val info = RegInit(0.U.asTypeOf(new TID_info))
  override val id   = CSR.TID
  override val rw   = "b1111_1111_1111_1111_1111_1111_1111_1111".U
}

class TCFG_info extends Bundle {
  val zero     = UInt((32 - COUNT_N).W)
  val initval  = UInt((COUNT_N - 2).W)
  val preiodic = Bool()
  val en       = Bool()
}

class TCFG extends base {
  override val info     = RegInit(0.U.asTypeOf(new TCFG_info))
  override val id       = CSR.TCFG
  override val rw       = "b0000_1111_1111_1111_1111_1111_1111_1111".U
}

class TVAL_info extends Bundle {
  val zero    = UInt((32 - COUNT_N).W)
  val timeval = UInt(COUNT_N.W)
}

class TVAL extends base {
  override val info = RegInit(0.U.asTypeOf(new TVAL_info))
  override val id   = CSR.TVAL
  override val rw   = "b0000_0000_0000_0000_0000_0000_0000_0000".U
}

class TICLR_info extends Bundle {
  val zero = UInt(31.W)
  val clr  = Bool()
}

class TICLR extends base {
  override val info = RegInit(0.U.asTypeOf(new TICLR_info))
  override val id   = CSR.TICLR
  override val rw   = "b0000_0000_0000_0000_0000_0000_0000_0001".U
}