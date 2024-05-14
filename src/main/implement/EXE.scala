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

        val rsc  = Flipped(new OP_CSR_INFO())
        val csr  = new OP_CSR_INFO()
        val es_flush = Output(Bool())
        val ms_flush = Input(Bool())
    })
    val div = Module(new DIV()).io
    val mul = Module(new MUL()).io
    val es_ready_go    = (!div.en || div.ready) && (!mul.en || mul.ready)
    val es_valid       = RegInit(false.B)
    val es_allowin     = !es_valid || (io.ms_allowin && es_ready_go)
    when (es_allowin || io.ms_flush) {es_valid := io.ds.valid && !io.ms_flush}
    io.to_mem.valid   := es_valid && es_ready_go && !io.ms_flush
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

    io.es_flush := io.ms_flush || (es_valid && csr_excp =/= 0.U)

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

    io.wrf.valid := wb_sel(0).asBool && es_valid && (dest =/= 0.U(32.W)) && !io.ms_flush
    io.wrf.ready := exe_fun =/= LD && !wb_sel(2).asBool
    io.wrf.dest  := dest
    io.wrf.wdata := alu_out
    
    val rd_h        = Fill(2, rs3_rd(15, 0))
    val rd_b        = Fill(4, rs3_rd(7, 0))
    val addr_mod_4  = alu_out(1, 0)
    val wen_H       = Mux(addr_mod_4 === 0.U, "b0011".U, "b1100".U)
    val wen_B       = (1.U(32.W) << addr_mod_4)(3, 0)

    val p_exc     = MuxCase("b000".U(3.W), Seq(
       (csr_excp === 0.U && rf_wen === REN_S && alu_out(1, 0) =/= 0.U)      -> "b001".U(3.W),
       (csr_excp === 0.U && rf_wen(2, 1) === "b10".U && alu_out(0) =/= 0.U) -> "b010".U(3.W),
       (csr_excp === 0.U && mem_wen === MEN_S && alu_out(1, 0) =/= 0.U)     -> "b011".U(3.W),
       (csr_excp === 0.U && mem_wen === MEN_H && alu_out(0) =/= 0.U)        -> "b100".U(3.W)
    ))
    val check_exc = ListLookup(p_exc, List(csr_excp, csr_Ecode, csr_badv, csr_badaddr), Array(
       BitPat("b001".U(3.W)) -> List(1.U(2.W), ECodes.ALE, true.B, alu_out),
       BitPat("b010".U(3.W)) -> List(1.U(2.W), ECodes.ALE, true.B, alu_out),
       BitPat("b011".U(3.W)) -> List(1.U(2.W), ECodes.ALE, true.B, alu_out),
       BitPat("b100".U(3.W)) -> List(1.U(2.W), ECodes.ALE, true.B, alu_out)
    ))
    val have_exc :: cecode :: cbadv :: cbadaddr :: Nil = check_exc
    
    io.data.wen  := Mux(!(mem_wen(2).asBool && es_valid) || (have_exc =/= 0.U) || io.ms_flush, 0.U(4.W), MuxCase("b1111".U, Seq(
        (mem_wen === MEN_H) -> wen_H,
        (mem_wen === MEN_B) -> wen_B
    )))
    io.data.wdata:= MuxCase(rs3_rd, Seq(
        (mem_wen === MEN_H) -> rd_h,
        (mem_wen === MEN_B) -> rd_b
    ))
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

    io.csr.excp   := have_exc
    io.csr.Ecode  := cecode
    io.csr.pc     := csr_pc
    io.csr.usemask:= csr_usemask  
    io.csr.wen    := csr_wen
    io.csr.waddr  := csr_waddr 
    io.csr.wdata  := csr_wdata 
    io.csr.mask   := csr_mask 
    io.csr.raddr  := csr_raddr
    io.csr.badv   := cbadv
    io.csr.badaddr:= cbadaddr
}