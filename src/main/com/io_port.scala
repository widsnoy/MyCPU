package ioport

import chisel3._
import chisel3.util._
import const.R._
import const._

class ds_gpr extends Bundle {
    val rr1  = Input(UInt(gpr_addr.W))
    val rr2  = Input(UInt(gpr_addr.W))
    val rr3  = Input(UInt(gpr_addr.W))
    val rd1  = Output(UInt(data_len.W))
    val rd2  = Output(UInt(data_len.W))
    val rd3  = Output(UInt(data_len.W))
}
class ws_gpr extends Bundle {
    val wen = Input(Bool())
    val wr  = Input(UInt(gpr_addr.W))
    val wd  = Input(UInt(data_len.W))
}

class pf_ram extends Bundle {
    val req     = Output(Bool())
    val wr      = Output(Bool())
    val size    = Output(UInt(3.W))
    val addr    = Output(UInt(addr_len.W))
    val addr_ok = Input(Bool())
}
class fs_ram extends Bundle {
    val data_ok = Input(Bool())
    val rdata   = Input(UInt(data_len.W))
}
class inst_sram_slave_axi extends Bundle {
    val araddr  = Output(UInt(data_len.W))
    val arsize  = Output(UInt(3.W))
    val arvalid = Output(Bool())
    val arready = Input(Bool())
    val rready  = Output(Bool())
    val rdata   = Input(UInt(data_len.W))
    val rvalid  = Input(Bool())
}

class es_ram extends Bundle {
    val req     = Output(Bool())
    val wr      = Output(Bool())
    val size    = Output(UInt(3.W))
    val addr    = Output(UInt(addr_len.W))
    val wstrb   = Output(UInt(4.W))
    val wdata   = Output(UInt(data_len.W))
    val addr_ok = Input(Bool())
}
class ms_ram extends Bundle {
    val data_ok = Input(Bool())
    val rdata   = Input(UInt(data_len.W))
}
class data_sram_slave_axi extends Bundle {
    val araddr  = Output(UInt(data_len.W))
    val arsize  = Output(UInt(3.W))
    val arvalid = Output(Bool())
    val arready = Input(Bool())
    val awaddr  = Output(UInt(data_len.W))
    val awsize  = Output(UInt(3.W))
    val awvalid = Output(Bool())
    val awready = Input(Bool())
    val rready  = Output(Bool())
    val rdata   = Input(UInt(data_len.W))
    val rvalid  = Input(Bool())
    val wdata   = Output(UInt(data_len.W))
    val wstrb   = Output(UInt(4.W))
    val wvalid  = Output(Bool())
    val wready  = Input(Bool())
    val bvalid  = Input(Bool())
    val bready  = Output(Bool())
}
class axi_interface extends Bundle {
    val arid    = Output(UInt(4.W))
    val araddr  = Output(UInt(data_len.W))
    val arlen   = Output(UInt(8.W))
    val arsize  = Output(UInt(3.W))
    val arburst = Output(UInt(2.W))
    val arlock  = Output(UInt(2.W))
    val arcache = Output(UInt(4.W))
    val arprot  = Output(UInt(3.W))
    val arvalid = Output(Bool())
    val arready = Input(Bool())

    val rid     = Input(UInt(4.W))
    val rready  = Output(Bool())
    val rdata   = Input(UInt(data_len.W))
    val rvalid  = Input(Bool())

    val awid    = Output(UInt(4.W))
    val awaddr  = Output(UInt(data_len.W))
    val awlen   = Output(UInt(8.W))
    val awsize  = Output(UInt(3.W))
    val awburst = Output(UInt(2.W))
    val awlock  = Output(UInt(2.W))
    val awcache = Output(UInt(4.W))
    val awprot  = Output(UInt(3.W))
    val awvalid = Output(Bool())
    val awready = Input(Bool())
    
    val wid     = Output(UInt(4.W))
    val wdata   = Output(UInt(data_len.W))
    val wstrb   = Output(UInt(4.W))
    val wlast   = Output(Bool())
    val wvalid  = Output(Bool())
    val wready  = Input(Bool())

    val bvalid  = Input(Bool())
    val bready  = Output(Bool())
}

class branch extends Bundle {
    val flag   = Output(Bool())
    val target = Output(UInt(pc_len.W))
}
class to_fs_bus extends Bundle {
    val pc      = Output(UInt(pc_len.W))
    val evalid  = Output(Bool())
    val ecode   = Output(UInt(6.W))
}
class to_ds_bus extends Bundle {
    val pc     = UInt(pc_len.W)
    val inst   = UInt(data_len.W)
    val evalid  = Output(Bool())
    val ecode   = Output(UInt(6.W))
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
    val dest   = UInt(gpr_addr.W)
    val evalid  = Output(Bool())
    val ecode   = Output(UInt(6.W))
    val csrnum  = Output(UInt(14.W))
}
class to_ms_bus extends Bundle {
    val pc      = UInt(pc_len.W)
    val w_tp    = UInt(5.W)
    val funct   = UInt(func.len.W)
    val mod4    = UInt(2.W)
    val res     = UInt(data_len.W)
    val dest    = UInt(gpr_addr.W)
}
class to_ws_bus extends Bundle {
    val pc      = UInt(pc_len.W)
    val w_tp    = UInt(5.W)
    val res     = UInt(data_len.W)
    val dest    = UInt(gpr_addr.W)
}
class debug_interface extends Bundle {
    val wb_pc       = UInt(pc_len.W)
    val wb_rf_wen   = UInt(4.W)
    val wb_rf_wnum  = UInt(gpr_addr.W)
    val wb_rf_wdata = UInt(data_len.W)
}
class bypass extends Bundle {
    val valid   = Output(Bool())
    val stall   = Output(Bool())
    val dest    = Output(UInt(gpr_addr.W))
    val value   = Output(UInt(data_len.W))
}
class calc_interface extends Bundle {
    val en      = Input(Bool())
    val signed  = Input(Bool())
    val op1     = Input(UInt(32.W))
    val op2     = Input(UInt(32.W))
    val result  = Output(UInt(64.W))
    val ready   = Output(Bool())
}

class csr_interface extends Bundle {
    val opt       = Output(UInt(2.W))
    val wvalid    = Output(Bool())
    val waddr     = Output(UInt(14.W))
    val wmask     = Output(UInt(data_len.W))
    val wdata     = Output(UInt(data_len.W))
    val pc        = Output(UInt(pc_len.W))
    val badv      = Output(UInt(data_len.W))
    val raddr     = Output(UInt(14.W))
    val evalid    = Output(Bool())
    val ecode     = Output(UInt(6.W))
    val br_target = Input(UInt(pc_len.W))
    val rdata     = Input(UInt(data_len.W))
}
class csr_to_ds extends Bundle {
    val snow = Output(Bool())
    val tid  = Output(UInt(data_len.W))
}