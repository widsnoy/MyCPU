package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class EXE_Stage extends Module {
    val io = IO(new Bundle {
        val ds = Flipped(new EX_INFO)
        val es_allowin = Output(UInt(1.W))
        val ms_allowin = Input(UInt(1.W))
        val to_mem      = new MEM_INFO
        val data = new RAM_IO()
        val wrf  = new WRF_INFO()
    })
    val es_valid       = RegInit(0.U(1.W))
    val es_ready_go    = 1.U(1.W)
    val es_allowin     = (~es_valid) | (io.ms_allowin & es_ready_go)
    when (es_allowin === 1.U) {es_valid := io.ds.valid}
    io.to_mem.valid      := es_valid & es_ready_go
    io.es_allowin     := es_allowin
    
    val dest        = RegInit(0.U(LENx.W))
    val exe_fun     = RegInit(0.U(EXE_FUN_LEN.W))
    val op1_data    = RegInit(0.U(LENX.W))
    val op2_data    = RegInit(0.U(LENX.W))
    val mem_wen     = RegInit(0.U(MEN_LEN.W))
    val rf_wen      = RegInit(0.U(REN_LEN.W))
    val wb_sel      = RegInit(0.U(WB_SEL_LEN.W))
    val pc          = RegInit(0.U(LENX.W))
    val rs3_rd      = RegInit(0.U(LENX.W))
    when ((es_allowin & io.ds.valid).asUInt === 1.U) {
        dest        := io.ds.dest
        exe_fun     := io.ds.exe_fun
        op1_data    := io.ds.op1_data
        op2_data    := io.ds.op2_data
        mem_wen     := io.ds.mem_wen
        rf_wen      := io.ds.rf_wen
        wb_sel      := io.ds.wb_sel
        pc          := io.ds.pc
        rs3_rd      := io.ds.rs3_rd
    }

    val alu_out = MuxCase(op1_data + op2_data, Seq(
        (exe_fun === ALU_ADD)   -> (op1_data + op2_data),
        (exe_fun === ALU_SUB)   -> (op1_data - op2_data),
        (exe_fun === ALU_SLT)   -> (op1_data.asSInt < op2_data.asSInt).asUInt,
        (exe_fun === ALU_SLTU)  -> (op1_data < op2_data).asUInt,
        (exe_fun === ALU_NOR)   -> ~(op1_data | op2_data),
        (exe_fun === ALU_AND)   -> (op1_data & op2_data),   
        (exe_fun === ALU_OR)    -> (op1_data | op2_data),
        (exe_fun === ALU_XOR)   -> (op1_data ^ op2_data),
        (exe_fun === ALU_SLL)   -> (op1_data << op2_data(4, 0))(31, 0).asUInt,
        (exe_fun === ALU_SRL)   -> (op1_data >> op2_data(4, 0))(31, 0).asUInt,
        (exe_fun === ALU_SRA)   -> (op1_data.asSInt >> op2_data(4, 0))(31, 0).asUInt,
        (exe_fun === ALU_LU12I) -> op2_data
    ))

    io.wrf.valid := rf_wen & es_valid & (dest =/= 0.U(32.W)).asUInt
    io.wrf.dest  := dest

    io.data.wen := Fill(4, (mem_wen & es_valid)) 
    io.data.wdata := rs3_rd
    io.data.addr := alu_out
    io.data.en   := 1.U(1.W)

    io.to_mem.exe_fun   := exe_fun
    io.to_mem.rf_wen    := rf_wen
    io.to_mem.wb_sel    := wb_sel
    io.to_mem.mem_wen   := mem_wen
    io.to_mem.rs3_rd    := rs3_rd
    io.to_mem.pc        := pc
    io.to_mem.alu_out   := alu_out
    io.to_mem.dest      := dest
}