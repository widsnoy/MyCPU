package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class MEM_Stage extends Module {
    val io = IO(new Bundle {
        val es = Flipped(new MEM_INFO())
        val ms_allowin = Output(UInt(1.W))
        val ws_allowin = Input(UInt(1.W))
        val to_wb = new WB_INFO()
        val data = new RAM_IO()
    })

    val ms_ready_go    = 1.U(1.W)
    val ms_valid       = RegInit(0.U(1.W))
    val ms_allowin     = (~ms_valid) | (io.ws_allowin & ms_ready_go)
    when (ms_allowin === 1.U) {ms_valid := io.es.valid}
    io.to_wb.valid       := ms_valid & ms_ready_go
    io.ms_allowin     := ms_allowin

    val dest    = RegInit(0.U(LENx.W))
    val exe_fun = RegInit(0.U(EXE_FUN_LEN.W))
    val rf_wen  = RegInit(0.U(REN_LEN.W))
    val wb_sel  = RegInit(0.U(WB_SEL_LEN.W))
    val mem_wen = RegInit(0.U(MEN_LEN.W))
    val rs3_rd  = RegInit(0.U(LENX.W))
    val pc      = RegInit(0.U(LENX.W))
    val alu_out = RegInit(0.U(LENX.W))
    when ((ms_allowin & io.es.valid).asUInt === 1.U) {
        dest        := io.es.dest
        exe_fun     := io.es.exe_fun
        mem_wen     := io.es.mem_wen
        rf_wen      := io.es.rf_wen
        wb_sel      := io.es.wb_sel
        pc          := io.es.pc
        rs3_rd      := io.es.rs3_rd
        alu_out     := io.es.alu_out
    }
    io.data.wen := mem_wen
    io.data.wdata := rs3_rd
    io.data.addr := alu_out
    io.data.en   := 1.U(1.W)

    io.to_wb.exe_fun   := exe_fun
    io.to_wb.rf_wen    := rf_wen
    io.to_wb.wb_sel    := wb_sel
    io.to_wb.pc        := pc
    io.to_wb.alu_out   := alu_out
    io.to_wb.dest      := dest
}