package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._
import common.Instructions._

class ID_Stage extends Module {
    val io = IO(new Bundle {
        val fs = Flipped(new ID_INFO())
        val ds_allowin = Output(Bool())
        val es_allowin = Input(Bool())
        val br     = new BR_INFO()
        val to_exe = new EX_INFO()
        val reg_r1 = Output(UInt(LENx.W))
        val reg_r2 = Output(UInt(LENx.W))
        val reg_r3 = Output(UInt(LENx.W))
        val reg_rdata1 = Input(UInt(LENX.W))
        val reg_rdata2 = Input(UInt(LENX.W))
        val reg_rdata3 = Input(UInt(LENX.W))
        val wr_EX      = Flipped(new WRF_INFO())
        val wr_MEM     = Flipped(new WRF_INFO())
        val wr_WB      = Flipped(new WRF_INFO())

        val csr        = new OP_CSR_INFO()
        val ds_flush = Output(Bool())
        val es_flush = Input(Bool())
    })
    val ds_ready_go    = (!io.wr_EX.valid || io.wr_EX.ready) && (!io.wr_MEM.valid || io.wr_MEM.ready) && (!io.wr_WB.valid || io.wr_WB.ready)
    val ds_valid       = RegInit(false.B)
    val ds_allowin     = !ds_valid || (io.es_allowin && ds_ready_go)
    when (ds_allowin || io.es_flush) {ds_valid := io.fs.valid && !io.es_flush}
    io.to_exe.valid   := ds_valid && ds_ready_go && !io.es_flush
    io.ds_allowin     := ds_allowin
    val inst = RegInit(0.U(LENX.W))
    val pc   = RegInit(0.U(LENX.W))
    when (ds_allowin && io.fs.valid) {
        inst := io.fs.inst
        pc   := io.fs.pc
    }
    val rd      = inst(4, 0)
    val rj      = inst(9, 5)
    val rk      = inst(14, 10)
    io.reg_r1   := rj
    io.reg_r2   := rk
    io.reg_r3   := rd
    
    def get_reg(dest: UInt, ini: UInt): UInt = {
        return MuxCase(ini, Seq(
            (io.wr_EX.valid && (io.wr_EX.dest === dest)) -> io.wr_EX.wdata,
            (io.wr_MEM.valid && (io.wr_MEM.dest === dest)) -> io.wr_MEM.wdata,
            (io.wr_WB.valid && (io.wr_WB.dest === dest)) -> io.wr_WB.wdata
        ))
    }

    val rs1_rd  = get_reg(rj, io.reg_rdata1)
    val rs2_rd  = get_reg(rk, io.reg_rdata2)
    val rs3_rd  = get_reg(rd, io.reg_rdata3)

    val ui5      = inst(14, 10)
    val i12_sex  = Cat(Fill(20, inst(21)), inst(21, 10))
    val of16_sex = Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W))
    val of26_sex = Cat(Fill(4, inst(9)), Cat(inst(9, 0), inst(25, 10)), 0.U(2.W))
    val i20_sex  = Cat(inst(24, 5), 0.U(12.W))
    val i12_uex  = Cat(0.U(20.W), inst(21, 10)) 
    val csr_num  = inst(23, 10)

    val ID_signals = ListLookup(inst, List(ALU_X, OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X),
        Array (
            add_w       -> List(ALU_ADD, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            sub_w       -> List(ALU_SUB, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            slt         -> List(ALU_SLT, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            sltu        -> List(ALU_SLTU, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            slti        -> List(ALU_SLT, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_S, WB_ALU),
            sltui       -> List(ALU_SLTU, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_S, WB_ALU),
            nor         -> List(ALU_NOR, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            and         -> List(ALU_AND, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            or          -> List(ALU_OR, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            xor         -> List(ALU_XOR, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            andi        -> List(ALU_AND, OP1_RS1, OP2_SI12_UEX, MEN_X, REN_S, WB_ALU),
            ori         -> List(ALU_OR, OP1_RS1, OP2_SI12_UEX, MEN_X, REN_S, WB_ALU),
            xori        -> List(ALU_XOR, OP1_RS1, OP2_SI12_UEX, MEN_X, REN_S, WB_ALU),
            slli_w      -> List(ALU_SLL, OP1_RS1, OP2_UI5, MEN_X, REN_S, WB_ALU),
            srli_w      -> List(ALU_SRL, OP1_RS1, OP2_UI5, MEN_X, REN_S, WB_ALU),
            sra_w       -> List(ALU_SRA, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            sll_w       -> List(ALU_SLL, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            srl_w       -> List(ALU_SRL, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            srai_w      -> List(ALU_SRA, OP1_RS1, OP2_UI5, MEN_X, REN_S, WB_ALU),
            addi_w      -> List(ALU_ADD, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_S, WB_ALU),
            ld_w        -> List(LD, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_S, WB_MEM),
            st_w        -> List(ST, OP1_RS1, OP2_SI12_SEX, MEN_S, REN_X, WB_X),
            jirl        -> List(BR_JIRL, OP1_RS1, OP2_OF16_SEX, MEN_X, REN_S, WB_ALU),
            inst_b      -> List(BR_B, OP1_PC, OP2_OF26_SEX, MEN_X, REN_X, WB_X),
            inst_bl     -> List(BR_BL, OP1_PC, OP2_OF26_SEX, MEN_X, REN_S, WB_ALU),
            beq         -> List(BR_BEQ, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            bne         -> List(BR_BNE, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            lu12i_w     -> List(ALU_LU12I, OP1_X, OP2_SI20_SEX, MEN_X, REN_S, WB_ALU),
            pcaddu12i   -> List(ALU_ADD, OP1_PC, OP2_SI20_SEX, MEN_X, REN_S, WB_ALU),
            mul_w       -> List(ALU_MULL, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            mulh_w      -> List(ALU_MULH, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            mulh_wu     -> List(ALU_MULHU, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            div_w       -> List(ALU_DIVS, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            div_wu      -> List(ALU_DIVU, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            mod_w       -> List(ALU_MODS, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            mod_wu      -> List(ALU_MODU, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            blt         -> List(BR_BLT, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            bge         -> List(BR_BGE, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            bltu        -> List(BR_BLTU, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            bgeu        -> List(BR_BGEU, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            ld_b        -> List(LD, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_B, WB_MEM),
            ld_h        -> List(LD, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_H, WB_MEM),
            st_b        -> List(ST, OP1_RS1, OP2_SI12_SEX, MEN_B, REN_X, WB_X),
            st_h        -> List(ST, OP1_RS1, OP2_SI12_SEX, MEN_H, REN_X, WB_X),
            ld_bu       -> List(LD, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_BU, WB_MEM),
            ld_hu       -> List(LD, OP1_RS1, OP2_SI12_SEX, MEN_X, REN_HU, WB_MEM),
            csrrd       -> List(CSRRD, OP1_X, OP2_X, MEN_X, REN_S, WB_CSR),
            csrwr       -> List(CSRWR, OP1_X, OP2_X, MEN_X, REN_S, WB_BOTH),
            csrxchg     -> List(CSRXCHG, OP1_X, OP2_X, MEN_X, REN_S, WB_BOTH),
            syscall     -> List(SYSCALL, OP1_X, OP2_X, MEN_X, REN_X, WB_X),
            ertn        -> List(ERTN, OP1_X, OP2_X, MEN_X, REN_X, WB_X)
        )
    )
    val exe_fun :: op1_sel :: op2_sel :: mem_wen :: rf_wen :: wb_sel :: Nil = ID_signals
    
    val op1_data = MuxCase(0.U(32.W), Seq(
        (op1_sel === OP1_RS1) -> rs1_rd,
        (op1_sel === OP1_PC)  -> pc
    ))
    val op2_data = MuxCase(0.U(32.W), Seq(
        (op2_sel === OP2_RS2)  -> rs2_rd,
        (op2_sel === OP2_UI5)  -> ui5,
        (op2_sel === OP2_SI12_SEX) -> i12_sex,
        (op2_sel === OP2_SI20_SEX) -> i20_sex,
        (op2_sel === OP2_RD)   -> rs3_rd,
        (op2_sel === OP2_OF26_SEX) -> of26_sex,
        (op2_sel === OP2_OF16_SEX) -> of16_sex,
        (op2_sel === OP2_SI12_UEX) -> i12_uex,
        (op2_sel === OP2_CSR_NUM)  -> csr_num
    ))

    //branch
    io.br.flg := ds_valid && MuxCase(false.B, Seq(
        (exe_fun === BR_BL) -> true.B,
        (exe_fun === BR_B)  -> true.B,
        (exe_fun === BR_JIRL)  -> true.B,
        (exe_fun === BR_BNE)   -> (rs1_rd =/= rs3_rd),
        (exe_fun === BR_BEQ)   -> (rs1_rd === rs3_rd),
        (exe_fun === BR_BLT)   -> (rs1_rd.asSInt < rs3_rd.asSInt),
        (exe_fun === BR_BLTU)  -> (rs1_rd.asUInt < rs3_rd.asUInt),
        (exe_fun === BR_BGE)   -> (rs1_rd.asSInt >= rs3_rd.asSInt),
        (exe_fun === BR_BGEU)  -> (rs1_rd.asUInt >= rs3_rd.asUInt)
    ))
    io.br.target := op1_data + op2_data
    io.csr.excp := MuxCase(0.U(2.W), Seq(
        (exe_fun === SYSCALL) -> 1.U(2.W),
        (exe_fun === ERTN)    -> 2.U(2.W)
    ))
    io.csr.Ecode := MuxCase(0.U(6.W), Seq(
        (exe_fun === SYSCALL) -> ECodes.SYS
    ))
    io.csr.Esubcode := MuxCase(0.U(9.W), Seq(
        (exe_fun === SYSCALL) -> 0.U(9.W)
    ))
    io.csr.pc     := pc
    io.csr.usemask:= (exe_fun === CSRXCHG)
    io.csr.wen    := (wb_sel === WB_BOTH) && (io.csr.excp === 0.U)
    io.csr.waddr  := csr_num
    io.csr.wdata  := rs3_rd
    io.csr.mask   := rs1_rd
    io.csr.raddr  := csr_num

    io.ds_flush   := io.es_flush || (ds_valid && io.csr.excp =/= 0.U)

    io.to_exe.exe_fun := exe_fun
    io.to_exe.op1_data := op1_data
    io.to_exe.op2_data := op2_data
    io.to_exe.wb_sel := wb_sel
    io.to_exe.mem_wen := mem_wen
    io.to_exe.rf_wen  := rf_wen
    io.to_exe.pc := pc
    io.to_exe.rs3_rd := rs3_rd
    io.to_exe.dest  := Mux(exe_fun === BR_BL, 1.U(LENx.W), rd)
}