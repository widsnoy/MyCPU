package ioport

import chisel3._
import chisel3.util._
import const.R._

class reg extends Bundle {
    val r1  = Input(UInt(reg_addr.W))
    val r2  = Input(UInt(reg_addr.W))
    val r3  = Input(UInt(reg_addr.W))
    val rd1 = Output(UInt(reg_addr.W))
    val rd2 = Output(UInt(reg_addr.W))
    val rd3 = Output(UInt(reg_addr.W))
    val wen = Input(Bool())
    val wr  = Input(UInt(reg_addr.W))
    val wd  = Input(UInt(reg_addr.W))
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