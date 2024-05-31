package ioport

import chisel3._
import chisel3.util._
import const.R._
import const._

class ds_reg extends Bundle {
    val rr1  = Input(UInt(reg_addr.W))
    val rr2  = Input(UInt(reg_addr.W))
    val rr3  = Input(UInt(reg_addr.W))
    val rd1  = Output(UInt(data_len.W))
    val rd2  = Output(UInt(data_len.W))
    val rd3  = Output(UInt(data_len.W))
}
class ws_reg extends Bundle {
    val wen = Input(Bool())
    val wr  = Input(UInt(reg_addr.W))
    val wd  = Input(UInt(data_len.W))
}

class pf_ram extends Bundle {
    val req     = Output(Bool())
    val wr      = Output(Bool())
    val size    = Output(UInt(2.W))
    val addr    = Output(UInt(addr_len.W))
    val addr_ok = Input(Bool())
}
class fs_ram extends Bundle {
    val data_ok = Input(Bool())
    val rdata   = Input(UInt(data_len.W))
}
class es_ram extends Bundle {
    val req     = Output(Bool())
    val wr      = Output(Bool())
    val size    = Output(UInt(2.W))
    val addr    = Output(UInt(addr_len.W))
    val wstrb   = Output(UInt(4.W))
    val wdata   = Output(UInt(data_len.W))
    val addr_ok = Input(Bool())
}
class ms_ram extends Bundle {
    val data_ok = Input(Bool())
    val rdata   = Input(UInt(data_len.W))
}
class branch extends Bundle {
    val flag   = Output(Bool())
    val target = Output(UInt(pc_len.W))
}
class to_fs_bus extends Bundle {
    val pc = Output(UInt(pc_len.W))
}
class to_ds_bus extends Bundle {
    val pc     = UInt(pc_len.W)
    val inst   = UInt(data_len.W)
}
class to_es_bus extends Bundle {
    val pc     = UInt(pc_len.W)
    val funct  = UInt(func.len.W)
    val op1    = UInt(data_len.W)
    val op2    = UInt(data_len.W)
    val src1   = UInt(data_len.W)
    val src2   = UInt(data_len.W)
    val src3   = UInt(data_len.W)
    val w_tp   = UInt(5.W)
    val dest   = UInt(reg_addr.W)
}
class to_ms_bus extends Bundle {
    val pc      = UInt(pc_len.W)
    val w_tp    = UInt(5.W)
    val funct   = UInt(func.len.W)
    val mod4    = UInt(2.W)
    val res     = UInt(data_len.W)
    val dest    = UInt(reg_addr.W)
}
class to_ws_bus extends Bundle {
    val pc      = UInt(pc_len.W)
    val w_tp    = UInt(5.W)
    val res     = UInt(data_len.W)
    val dest    = UInt(reg_addr.W)
}
class debug_interface extends Bundle {
    val wb_pc       = UInt(pc_len.W)
    val wb_rf_wen   = UInt(4.W)
    val wb_rf_wnum  = UInt(reg_addr.W)
    val wb_rf_wdata = UInt(data_len.W)
}