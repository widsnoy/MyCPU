package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class WB_Stage extends Module {
    val io = IO(new Bundle {
        val ms = Flipped(new WB_INFO())
        val ws_allowin = Output(UInt(1.W))
        val debug   = new DEBUG()
        
        val reg_wen = Output(UInt(1.W))
        val reg_wr  = Output(UInt(LENx.W))
        val reg_wdata = Output(UInt(LENX.W))
        val wrf  = new WRF_INFO()
        val rsc  = Flipped(new OP_CSR_INFO())
        val csr  = new CSR_IO()
        val wb_flush = Output(Bool())
    })
    val ws_ready_go    = true.B
    val ws_valid       = RegInit(false.B)
    val ws_allowin     = !ws_valid || ws_ready_go
    when (ws_allowin) {ws_valid := io.ms.valid}
    io.ws_allowin     := ws_allowin

    val dest    = RegInit(0.U(LENx.W))
    val exe_fun = RegInit(0.U(EXE_FUN_LEN.W))
    val rf_wen  = RegInit(0.U(REN_LEN.W))
    val wb_sel  = RegInit(0.U(WB_SEL_LEN.W))
    val pc      = RegInit(0.U(LENX.W))
    val alu_out = RegInit(0.U(LENX.W))

    val csr_excp        = RegInit(0.U(2.W))
    val csr_Ecode       = RegInit(0.U(6.W))
    val csr_pc          = RegInit(0.U(LENX.W))
    val csr_usemask     = RegInit(false.B)
    val csr_wen         = RegInit(false.B)
    val csr_waddr       = RegInit(0.U(CSR_LENx.W))
    val csr_wdata       = RegInit(0.U(LENX.W))
    val csr_mask        = RegInit(0.U(LENX.W))
    val csr_raddr       = RegInit(0.U(CSR_LENx.W))
    val csr_badv        = RegInit(false.B)
    val csr_badaddr     = RegInit(0.U(LENX.W))

    when (ws_allowin && io.ms.valid) {
        dest        := io.ms.dest
        exe_fun     := io.ms.exe_fun
        rf_wen      := io.ms.rf_wen
        wb_sel      := io.ms.wb_sel
        pc          := io.ms.pc
        alu_out     := io.ms.alu_out
        csr_excp        := io.rsc.excp
        csr_Ecode       := io.rsc.Ecode
        csr_usemask     := io.rsc.usemask
        csr_wen         := io.rsc.wen
        csr_waddr       := io.rsc.waddr
        csr_wdata       := io.rsc.wdata
        csr_mask        := io.rsc.mask
        csr_raddr       := io.rsc.raddr
        csr_pc          := io.rsc.pc
        csr_badv        := io.rsc.badv
        csr_badaddr     := io.rsc.badaddr
    }

    io.wb_flush := ws_valid && csr_excp =/= 0.U
    // CSR
    io.csr.excp   := csr_excp & Fill(2, ws_valid.asUInt)
    io.csr.Ecode  := csr_Ecode
    io.csr.pc     := csr_pc
    io.csr.usemask:= csr_usemask  
    io.csr.wen    := csr_wen && ws_valid
    io.csr.waddr  := csr_waddr 
    io.csr.wdata  := csr_wdata 
    io.csr.mask   := csr_mask 
    io.csr.raddr  := csr_raddr
    io.csr.badv   := csr_badv
    io.csr.badaddr:= csr_badaddr

    // 这里可以少做一次多路选择
    val alu_UH = Cat(0.U(16.W), alu_out(15, 0))
    val alu_UB = Cat(0.U(24.W), alu_out(7, 0))
    val alu_SH = Cat(Fill(16, alu_out(15)), alu_out(15, 0))
    val alu_SB = Cat(Fill(24, alu_out(7)), alu_out(7, 0))
    
    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_BOTH) -> io.csr.rdata,
        (wb_sel === WB_CSR)  -> io.csr.rdata,
        (rf_wen === REN_H) -> alu_SH,
        (rf_wen === REN_B) -> alu_SB,
        (rf_wen === REN_HU)-> alu_UH,
        (rf_wen === REN_BU)-> alu_UB,
    ))
    val wb_addr = dest

    io.wrf.valid := io.reg_wen
    io.wrf.ready := true.B
    io.wrf.dest  := dest
    io.wrf.wdata := wb_data

    io.reg_wen := wb_sel(0).asBool && ws_valid && (dest =/= 0.U(32.W)) && (csr_excp === 0.U)
    io.reg_wr := wb_addr
    io.reg_wdata := wb_data

    io.debug.wb_pc      := pc
    io.debug.wb_rf_wen  := Fill(4, io.reg_wen.asUInt)
    io.debug.wb_rf_wnum := wb_addr
    io.debug.wb_rf_wdata:= wb_data
}