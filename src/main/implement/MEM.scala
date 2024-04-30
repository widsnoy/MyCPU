package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class MEM_Stage extends Module {
    val io = IO(new Bundle {
        val es = Flipped(new MEM_INFO())
        val ms_allowin = Output(Bool())
        val ws_allowin = Input(Bool())
        val data_rdata = Input(UInt(LENX.W))
        val to_wb = new WB_INFO()
        val wrf  = new WRF_INFO()
    })

    val ms_ready_go    = true.B
    val ms_valid       = RegInit(false.B)
    val ms_allowin     = !ms_valid || (io.ws_allowin && ms_ready_go)
    when (ms_allowin === 1.U) {ms_valid := io.es.valid}
    io.to_wb.valid    := ms_valid && ms_ready_go
    io.ms_allowin     := ms_allowin
    
    val dest    = RegInit(0.U(LENx.W))
    val exe_fun = RegInit(0.U(EXE_FUN_LEN.W))
    val rf_wen  = RegInit(0.U(REN_LEN.W))
    val wb_sel  = RegInit(0.U(WB_SEL_LEN.W))
    val mem_wen = RegInit(0.U(MEN_LEN.W))
    val rs3_rd  = RegInit(0.U(LENX.W))
    val pc      = RegInit(0.U(LENX.W))
    val alu_out = RegInit(0.U(LENX.W))
    when (ms_allowin && io.es.valid) {
        dest        := io.es.dest
        exe_fun     := io.es.exe_fun
        mem_wen     := io.es.mem_wen
        rf_wen      := io.es.rf_wen
        wb_sel      := io.es.wb_sel
        pc          := io.es.pc
        rs3_rd      := io.es.rs3_rd
        alu_out     := io.es.alu_out
    }
    val addr_mod_4  = alu_out(1, 0)
    val rdata       = io.data_rdata
    val _TB = MuxCase(0.U(8.W), Seq(
        (addr_mod_4 === 0.U) -> rdata(7, 0),
        (addr_mod_4 === 1.U) -> rdata(15, 8),
        (addr_mod_4 === 2.U) -> rdata(23, 16),
        (addr_mod_4 === 3.U) -> rdata(31, 24),
    ))
    val _TH = MuxCase(0.U(16.W), Seq(
        (addr_mod_4 === 0.U) -> rdata(15, 0),
        (addr_mod_4 === 2.U) -> rdata(31, 16)
    ))
    val _T  = Mux((rf_wen === REN_H) || (rf_wen === REN_HU), _TH, _TB)
    val temp = Mux(wb_sel === WB_MEM, Mux(rf_wen === REN_S, rdata, _T), alu_out)
    io.wrf.valid := rf_wen(3).asBool && ms_valid && (dest =/= 0.U(32.W))
    io.wrf.ready := 1.U
    io.wrf.dest  := dest
    io.wrf.wdata := temp

    io.to_wb.exe_fun   := exe_fun
    io.to_wb.rf_wen    := rf_wen
    io.to_wb.wb_sel    := wb_sel
    io.to_wb.pc        := pc
    io.to_wb.alu_out   := temp
    io.to_wb.dest      := dest
}