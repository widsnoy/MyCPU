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
class wb_reg extends Bundle {
    val wen = Input(Bool())
    val wr  = Input(UInt(reg_addr.W))
    val wd  = Input(UInt(data_len.W))
}
// class sram extends Bundle {
//     val req    = Output(Bool())
//     val wr     = Output(Bool())
//     val size   = Output(UInt(2.W))
//     val addr   = Output(UInt(addr_len.W))
//     val wstrb  = Output(UInt(4.W))
//     val wdata  = Output(UInt(data_len.W))
//     val addr_ok= Input(Bool())
//     val data_ok= Input(Bool())
//     val rdata  = Input(UInt(data_len.W))
// }
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
    val src1   = UInt(data_len.W)
    val src2   = UInt(data_len.W)
    val src3   = UInt(data_len.W)
    val w_tp   = UInt(6.W)
    val dest   = UInt(data_len.W) // regfile num
}