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
    val wen    = Input(Bool())
    val wr     = Input(UInt(LENx.W))
    val wdata  = Input(UInt(LENX.W))
}
class CSR_IO extends Bundle {    
    val excp     = Output(UInt(2.W))
    val Ecode    = Output(UInt(6.W))
    val Esubcode = Output(UInt(9.W))
    val pc       = Output(UInt(LENX.W))
    val usemask  = Output(Bool()) // 0 直接覆盖，1 考虑掩码
    val wen      = Output(Bool())
    val waddr    = Output(UInt(CSR_LENx.W))
    val wdata    = Output(UInt(LENX.W))
    val mask     = Output(UInt(LENX.W))
    val raddr    = Output(UInt(CSR_LENx.W))
    val rdata    = Input(UInt(LENX.W))
}
class RAM_IO extends Bundle {
    val en    = Output(Bool())
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
    val valid = Output(Bool())
    val inst  = Output(UInt(LENX.W))
    val pc    = Output(UInt(LENX.W))
}
class BR_INFO extends Bundle {
    val flg   = Output(Bool())
    val target= Output(UInt(LENX.W))
}
class EX_INFO extends Bundle {
    val valid = Output(Bool())
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
    val valid = Output(Bool())
    val exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val mem_wen = Output(UInt(MEN_LEN.W))
    val rf_wen  = Output(UInt(REN_LEN.W))
    val wb_sel = Output(UInt(WB_SEL_LEN.W))
    val rs3_rd = Output(UInt(LENX.W))
    val pc = Output(UInt(LENX.W))
    val alu_out = Output(UInt(LENX.W))
    val dest  = Output(UInt(LENx.W))
}
class WB_INFO extends Bundle {
    val valid = Output(Bool())
    val exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val rf_wen  = Output(UInt(REN_LEN.W))
    val wb_sel = Output(UInt(WB_SEL_LEN.W))
    val pc = Output(UInt(LENX.W))
    val alu_out = Output(UInt(LENX.W))
    val dest  = Output(UInt(LENx.W))
}
class WRF_INFO extends Bundle {
    val valid = Output(Bool())
    val ready = Output(Bool())
    val dest  = Output(UInt(LENx.W))
    val wdata = Output(UInt(LENX.W))
}
class OP_CSR_INFO extends Bundle {
    val excp     = Output(UInt(2.W)) // 0 -> do nothing, 1 -> excp, 2 -> ertn
    val Ecode    = Output(UInt(6.W))
    val Esubcode = Output(UInt(9.W))
    val usemask  = Output(Bool()) // 0 直接覆盖，1 考虑掩码
    val pc       = Output(UInt(LENX.W))
    val wen      = Output(Bool())
    val waddr    = Output(UInt(CSR_LENx.W))
    val wdata    = Output(UInt(LENX.W))
    val mask     = Output(UInt(LENX.W))
    val raddr    = Output(UInt(CSR_LENx.W))
}