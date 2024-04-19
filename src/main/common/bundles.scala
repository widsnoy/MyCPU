package common

import chisel3._
import chisel3.util._
import common.Consts._

class REG_IO extends Bundle {
    val r1 = Input(UInt(LENx.W))
    val r2 = Input(UInt(LENx.W))
    val r3 = Input(UInt(LENx.W))
    val rdata1 = Output(UInt(LENX.W))
    val rdata2 = Output(UInt(LENX.W))
    val rdata3 = Output(UInt(LENX.W))
    val wen = Input(UInt(1.W))
    val wr  = Input(UInt(LENx.W))
    val wdata = Input(UInt(LENX.W))
}
class RAM_IO extends Bundle {
    val en    = Output(UInt(1.W))
    val wen   = Output(UInt(4.W))
    val addr  = Output(UInt(LENX.W))
    val wdata = Output(UInt(LENX.W))
    val rdata = Input(UInt(LENX.W))
}
class DEBUG extends Bundle {
    val wb_pc     = Output(UInt(LENX.W))
    val wb_rf_wen = Output(UInt(4.W))
    val wb_rf_wnum = Output(UInt(5.W))
    val wb_rf_wdata = Output(UInt(LENX.W))
}
class ID_INFO extends Bundle {
    val valid = Output(UInt(1.W))
    val inst  = Output(UInt(LENX.W))
    val pc    = Output(UInt(LENX.W))
}
class BR_INFO extends Bundle {
    val flg   = Output(Bool())
    val target= Output(UInt(LENX.W))
}
class EX_INFO extends Bundle {
    val valid = Output(UInt(1.W))
    val exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val op1_data = Output(UInt(LENX.W))
    val op2_data = Output(UInt(LENX.W))
    val mem_wen = Output(UInt(MEN_LEN.W))
    val rf_wen  = Output(UInt(REN_LEN.W))
    val wb_sel = Output(UInt(WB_SEL_LEN.W))
    val pc = Output(UInt(LENX.W))
    val rs3_rd = Output(UInt(LENX.W))
    val dest  = Output(UInt(LENx.W))
}
class MEM_INFO extends Bundle {
    val valid = Output(UInt(1.W))
    val exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val rf_wen  = Output(UInt(REN_LEN.W))
    val wb_sel = Output(UInt(WB_SEL_LEN.W))
    val mem_wen = Output(UInt(MEN_LEN.W))
    val rs3_rd = Output(UInt(LENX.W))
    val pc = Output(UInt(LENX.W))
    val alu_out = Output(UInt(LENX.W))
    val dest  = Output(UInt(LENx.W))
}
class WB_INFO extends Bundle {
    val valid = Output(UInt(1.W))
    val exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val rf_wen  = Output(UInt(REN_LEN.W))
    val wb_sel = Output(UInt(WB_SEL_LEN.W))
    val pc = Output(UInt(LENX.W))
    val alu_out = Output(UInt(LENX.W))
    val dest  = Output(UInt(LENx.W))
}