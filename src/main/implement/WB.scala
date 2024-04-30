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
    when (ws_allowin && io.ms.valid) {
        dest        := io.ms.dest
        exe_fun     := io.ms.exe_fun
        rf_wen      := io.ms.rf_wen
        wb_sel      := io.ms.wb_sel
        pc          := io.ms.pc
        alu_out     := io.ms.alu_out
    }
    // 这里可以少做一次多路选择
    val alu_UH = Cat(0.U(16.W), alu_out(15, 0))
    val alu_UB = Cat(0.U(24.W), alu_out(7, 0))
    val alu_SH = Cat(Fill(16, alu_out(15)), alu_out(15, 0))
    val alu_SB = Cat(Fill(24, alu_out(7)), alu_out(7, 0))
    
    val wb_data = MuxCase(alu_out, Seq(
        (rf_wen === REN_H) -> alu_SH,
        (rf_wen === REN_B) -> alu_SB,
        (rf_wen === REN_HU)-> alu_UH,
        (rf_wen === REN_BU)-> alu_UB
    ))
    val wb_addr = dest

    io.wrf.valid := rf_wen(3).asBool && ws_valid && (dest =/= 0.U(32.W))
    io.wrf.ready := 1.U
    io.wrf.dest  := dest
    io.wrf.wdata := wb_data

    io.reg_wen := rf_wen(3).asBool && ws_valid && (dest =/= 0.U(32.W))
    io.reg_wr := wb_addr
    io.reg_wdata := wb_data

    io.debug.wb_pc      := pc
    io.debug.wb_rf_wen  := Fill(4, (rf_wen(3).asBool && ws_valid && (dest =/= 0.U(32.W))).asUInt)
    io.debug.wb_rf_wnum := wb_addr
    io.debug.wb_rf_wdata:= wb_data
}