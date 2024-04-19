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
        val data_rdata = Input(UInt(LENX.W))
        val reg_wen = Output(UInt(1.W))
        val reg_wr  = Output(UInt(LENx.W))
        val reg_wdata = Output(UInt(LENX.W))
    })
    val ws_ready_go    = 1.U(1.W)
    val ws_valid       = RegInit(0.U(1.W))
    val ws_allowin     = (~ws_valid) | (ws_ready_go)
    when (ws_allowin === 1.U) {ws_valid := io.ms.valid}
    io.ws_allowin     := ws_allowin

    val dest    = RegInit(0.U(LENx.W))
    val exe_fun = RegInit(0.U(EXE_FUN_LEN.W))
    val rf_wen  = RegInit(0.U(REN_LEN.W))
    val wb_sel  = RegInit(0.U(WB_SEL_LEN.W))
    val pc      = RegInit(0.U(LENX.W))
    val alu_out = RegInit(0.U(LENX.W))
    when ((ws_allowin & io.ms.valid).asUInt === 1.U) {
        dest        := io.ms.dest
        exe_fun     := io.ms.exe_fun
        rf_wen      := io.ms.rf_wen
        wb_sel      := io.ms.wb_sel
        pc          := io.ms.pc
        alu_out     := io.ms.alu_out
    }
    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_MEM) -> io.data_rdata,
        (wb_sel === WB_PC)  -> (pc + 4.U(LENX.W))
    ))
    val wb_addr = Mux(exe_fun === BR_BL, 1.U(LENx.W), dest)
    
    io.reg_wen := rf_wen
    io.reg_wr := wb_addr
    io.reg_wdata := wb_data

    io.debug.wb_pc      := pc
    io.debug.wb_rf_wen  := Fill(4, rf_wen(0))
    io.debug.wb_rf_wnum := wb_addr
    io.debug.wb_rf_wdata:= wb_data
}