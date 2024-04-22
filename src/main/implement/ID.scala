package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._
import common.Instructions._

class ID_Stage extends Module {
    val io = IO(new Bundle {
        val fs = Flipped(new ID_INFO())
        val ds_allowin = Output(UInt(1.W))
        val es_allowin = Input(UInt(1.W))
        val br      = new BR_INFO()
        val to_exe = new EX_INFO()
        val reg_r1 = Output(UInt(LENx.W))
        val reg_r2 = Output(UInt(LENx.W))
        val reg_r3 = Output(UInt(LENx.W))
        val reg_rdata1 = Input(UInt(LENX.W))
        val reg_rdata2 = Input(UInt(LENX.W))
        val reg_rdata3 = Input(UInt(LENX.W))
        val wr_EX      = Flipped(new WRF_INFO())
        val wr_MEM      = Flipped(new WRF_INFO())
        val wr_WB      = Flipped(new WRF_INFO())
    })
    val ds_ready_go    = Wire(UInt(1.W))
    val ds_valid       = RegInit(0.U(1.W))
    val ds_allowin     = (~ds_valid) | (io.es_allowin & ds_ready_go)
    when (ds_allowin === 1.U) {ds_valid := io.fs.valid}
    io.to_exe.valid   := ds_valid & ds_ready_go
    io.ds_allowin     := ds_allowin
    val inst = RegInit(0.U(LENX.W))
    val pc   = RegInit(0.U(LENX.W))
    when ((ds_allowin & io.fs.valid) === 1.U) {
        inst := io.fs.inst
        pc   := io.fs.pc
    }
    val rd      = inst(4, 0)
    val rj      = inst(9, 5)
    val rk      = inst(14, 10)
    io.reg_r1   := rj
    io.reg_r2   := rk
    io.reg_r3   := rd
    val rs1_rd  = io.reg_rdata1
    val rs2_rd  = io.reg_rdata2
    val rs3_rd  = io.reg_rdata3
    val ui5     = inst(14, 10)
    val i12     = inst(21, 10)
    val i16     = inst(25, 10)
    val i20     = inst(24, 5)
    val i26     = Cat(inst(9, 0), inst(25, 10))
    val i12_sex = Cat(Fill(20, i12(11)), i12)
    val of16_sex = Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W))
    val of26_sex = Cat(Fill(4, inst(9)), Cat(inst(9, 0), inst(25, 10)), 0.U(2.W))
    val i20_sex  = Cat(inst(24, 5), 0.U(12.W))
    val ID_signals = ListLookup(inst, 
        List(ALU_X, OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X),
        Array (
            add_w   -> List(ALU_ADD, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            sub_w   -> List(ALU_SUB, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            slt     -> List(ALU_SLT, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            sltu    -> List(ALU_SLTU, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            nor     -> List(ALU_NOR, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            and     -> List(ALU_AND, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            or      -> List(ALU_OR, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            xor     -> List(ALU_XOR, OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU),
            slli_w  -> List(ALU_SLL, OP1_RS1, OP2_UI5, MEN_X, REN_S, WB_ALU),
            srli_w  -> List(ALU_SRL, OP1_RS1, OP2_UI5, MEN_X, REN_S, WB_ALU),
            srai_w  -> List(ALU_SRA, OP1_RS1, OP2_UI5, MEN_X, REN_S, WB_ALU),
            addi_w  -> List(ALU_ADD, OP1_RS1, OP2_SI12, MEN_X, REN_S, WB_ALU),
            ld_w    -> List(LD, OP1_RS1, OP2_SI12, MEN_X, REN_S, WB_MEM),
            st_w    -> List(ST, OP1_RS1, OP2_SI12, MEN_S, REN_X, WB_X),
            jirl    -> List(BR_JIRL, OP1_RS1, OP2_OF16_SEX, MEN_X, REN_S, WB_PC),
            inst_b  -> List(BR_B, OP1_PC, OP2_OF26_SEX, MEN_X, REN_X, WB_X),
            inst_bl -> List(BR_BL, OP1_PC, OP2_OF26_SEX, MEN_X, REN_S, WB_PC),
            beq     -> List(BR_BEQ, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            bne     -> List(BR_BNE, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            lu12i_w -> List(ALU_LU12I, OP1_X, OP2_SI20_SEX, MEN_X, REN_S, WB_ALU)
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
        (op2_sel === OP2_SI12) -> i12_sex,
        (op2_sel === OP2_SI20_SEX) -> i20_sex,
        (op2_sel === OP2_RD)   -> rs3_rd,
        (op2_sel === OP2_OF26) -> i26,
        (op2_sel === OP2_OF16) -> i16,
        (op2_sel === OP2_OF26_SEX) -> of26_sex,
        (op2_sel === OP2_OF16_SEX) -> of16_sex
    ))

    ds_ready_go := MuxCase(1.U(1.W), Seq(
        (op1_sel === OP1_RS1 && io.wr_EX.valid === 1.U && io.wr_EX.dest =/= 0.U(LENx.W) && io.wr_EX.dest === rj) -> 0.U(1.W),
        (op1_sel === OP1_RS1 && io.wr_MEM.valid === 1.U && io.wr_MEM.dest =/= 0.U(LENx.W) && io.wr_MEM.dest === rj) -> 0.U(1.W),
        (op1_sel === OP1_RS1 && io.wr_WB.valid === 1.U && io.wr_WB.dest =/= 0.U(LENx.W) && io.wr_WB.dest === rj) -> 0.U(1.W),
        (op2_sel === OP2_RS2 && io.wr_EX.valid === 1.U && io.wr_EX.dest =/= 0.U(LENx.W) && io.wr_EX.dest === rk) -> 0.U(1.W),
        (op2_sel === OP2_RS2 && io.wr_MEM.valid === 1.U && io.wr_MEM.dest =/= 0.U(LENx.W) && io.wr_MEM.dest === rk) -> 0.U(1.W),
        (op2_sel === OP2_RS2 && io.wr_WB.valid === 1.U && io.wr_WB.dest =/= 0.U(LENx.W) && io.wr_WB.dest === rk) -> 0.U(1.W),
        ((exe_fun === BR_BEQ || exe_fun === BR_BNE) && io.wr_EX.valid === 1.U && io.wr_EX.dest =/= 0.U(LENx.W) && io.wr_EX.dest === rj) -> 0.U(1.W),
        ((exe_fun === BR_BEQ || exe_fun === BR_BNE) && io.wr_MEM.valid === 1.U && io.wr_MEM.dest =/= 0.U(LENx.W) && io.wr_MEM.dest === rj) -> 0.U(1.W),
        ((exe_fun === BR_BEQ || exe_fun === BR_BNE) && io.wr_WB.valid === 1.U && io.wr_WB.dest =/= 0.U(LENx.W) && io.wr_WB.dest === rj) -> 0.U(1.W),
        ((exe_fun === BR_BEQ || exe_fun === BR_BNE) && io.wr_EX.valid === 1.U && io.wr_EX.dest =/= 0.U(LENx.W) && io.wr_EX.dest === rd) -> 0.U(1.W),
        ((exe_fun === BR_BEQ || exe_fun === BR_BNE) && io.wr_MEM.valid === 1.U && io.wr_MEM.dest =/= 0.U(LENx.W) && io.wr_MEM.dest === rd) -> 0.U(1.W),
        ((exe_fun === BR_BEQ || exe_fun === BR_BNE) && io.wr_WB.valid === 1.U && io.wr_WB.dest =/= 0.U(LENx.W) && io.wr_WB.dest === rd) -> 0.U(1.W),
        (exe_fun === ST && io.wr_EX.valid === 1.U && io.wr_EX.dest =/= 0.U(LENx.W) && io.wr_EX.dest === rd) -> 0.U(1.W),
        (exe_fun === ST && io.wr_MEM.valid === 1.U && io.wr_MEM.dest =/= 0.U(LENx.W) && io.wr_MEM.dest === rd) -> 0.U(1.W),
        (exe_fun === ST && io.wr_WB.valid === 1.U && io.wr_WB.dest =/= 0.U(LENx.W) && io.wr_WB.dest === rd) -> 0.U(1.W)
    ))

    //branch
    io.br.flg := Mux(ds_valid === 0.U, false.B, MuxCase(false.B, Seq(
        (exe_fun === BR_BL) -> true.B,
        (exe_fun === BR_B)  -> true.B,
        (exe_fun === BR_JIRL)  -> true.B,
        (exe_fun === BR_BNE)   -> (rs1_rd =/= rs3_rd),
        (exe_fun === BR_BEQ)   -> (rs1_rd === rs3_rd)
    )))
    io.br.target := op1_data + op2_data

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