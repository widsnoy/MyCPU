package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

/*
unsigned
s_axis_dividend_tdata[31:0]
s_axis_dividend_tready
s_axis_dividend_tvalid

signed


*/

class EXE_Stage extends Module {
    val io = IO(new Bundle {
        val ds = Flipped(new EX_INFO)
        val es_allowin = Output(Bool())
        val ms_allowin = Input(Bool())
        val to_mem     = new MEM_INFO
        val data = new RAM_IO()
        val wrf  = new WRF_INFO()
    })
    val div = Module(new DIV()).io
    val mul = Module(new MUL()).io
    val es_ready_go    = (!div.en || div.ready) && (!mul.en || mul.ready)
    val es_valid       = RegInit(false.B)
    val es_allowin     = !es_valid || (io.ms_allowin && es_ready_go)
    when (es_allowin) {es_valid := io.ds.valid}
    io.to_mem.valid   := es_valid && es_ready_go
    io.es_allowin     := es_allowin
    
    val dest        = RegInit(0.U(LENx.W))
    val exe_fun     = RegInit(0.U(EXE_FUN_LEN.W))
    val op1_data    = RegInit(0.U(LENX.W))
    val op2_data    = RegInit(0.U(LENX.W))
    val mem_wen     = RegInit(false.B)
    val rf_wen      = RegInit(false.B)
    val wb_sel      = RegInit(0.U(WB_SEL_LEN.W))
    val pc          = RegInit(0.U(LENX.W))
    val rs3_rd      = RegInit(0.U(LENX.W))
    when (es_allowin && io.ds.valid) {
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
    div.en      := ((exe_fun === ALU_MODS) || (exe_fun === ALU_MODU) || (exe_fun === ALU_DIVU) || (exe_fun === ALU_DIVS))
    div.signed  := ((exe_fun === ALU_MODS) || (exe_fun === ALU_DIVS))
    div.op1     := op1_data
    div.op2     := op2_data
    val div_result = div.result

    mul.en      := ((exe_fun === ALU_MULH) || (exe_fun === ALU_MULL) || (exe_fun === ALU_MULHU))
    mul.signed  := (exe_fun =/= ALU_MULHU)
    mul.op1     := op1_data
    mul.op2     := op2_data
    val mul_result = mul.result

    val sum     = op1_data + op2_data
    val sub     = op1_data - op2_data
    val slt     = (op1_data.asSInt < op2_data.asSInt).asUInt
    val sltu    = (op1_data < op2_data).asUInt
    val nor     = ~(op1_data | op2_data)
    val and     = op1_data & op2_data
    val or      = op1_data | op2_data
    val xor     = op1_data ^ op2_data
    val sll     = (op1_data << op2_data(4, 0))(31, 0).asUInt
    val srl     = (op1_data >> op2_data(4, 0))(31, 0).asUInt
    val sra     = (op1_data.asSInt >> op2_data(4, 0))(31, 0).asUInt
    val br_t    = pc + 4.U(32.W)
    val rem     = div_result(31, 0)
    val div_res = div_result(63, 32)

    val alu_out = MuxCase(sum, Seq(
        (exe_fun === ALU_SUB)   -> sub,
        (exe_fun === ALU_SLT)   -> slt,
        (exe_fun === ALU_SLTU)  -> sltu,
        (exe_fun === ALU_NOR)   -> nor,
        (exe_fun === ALU_AND)   -> and,   
        (exe_fun === ALU_OR)    -> or,
        (exe_fun === ALU_XOR)   -> xor,
        (exe_fun === ALU_SLL)   -> sll,
        (exe_fun === ALU_SRL)   -> srl,
        (exe_fun === ALU_SRA)   -> sra,
        (exe_fun === ALU_LU12I) -> op2_data,
        (exe_fun === BR_JIRL)   -> br_t,
        (exe_fun === BR_BL)     -> br_t,
        (exe_fun === ALU_MULL)  -> mul_result(31, 0),
        (exe_fun === ALU_MULH)  -> mul_result(63, 32),
        (exe_fun === ALU_MULHU) -> mul_result(63, 32),
        (exe_fun === ALU_DIVS)  -> div_res,
        (exe_fun === ALU_DIVU)  -> div_res,
        (exe_fun === ALU_MODS)  -> rem,
        (exe_fun === ALU_MODU)  -> rem
    ))

    io.wrf.valid := rf_wen && es_valid && (dest =/= 0.U(32.W))
    io.wrf.ready := (exe_fun =/= LD)
    io.wrf.dest  := dest
    io.wrf.wdata := alu_out

    io.data.wen  := Fill(4, (mem_wen && es_valid).asUInt) 
    io.data.wdata:= rs3_rd
    io.data.addr := alu_out
    io.data.en   := true.B

    io.to_mem.exe_fun   := exe_fun
    io.to_mem.rf_wen    := rf_wen
    io.to_mem.wb_sel    := wb_sel
    io.to_mem.mem_wen   := mem_wen
    io.to_mem.rs3_rd    := rs3_rd
    io.to_mem.pc        := pc
    io.to_mem.alu_out   := alu_out
    io.to_mem.dest      := dest
}