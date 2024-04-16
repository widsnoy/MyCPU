package mycpu

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._

class mycpu extends Module {
    val io = IO(new Bundle {
        // 指令寄存器
        val inst_sram_en    = Output(UInt(1.W))
        val inst_sram_wen   = Output(UInt(1.W))
        val inst_sram_addr  = Output(UInt(32.W))
        val inst_sram_wdata = Output(UInt(32.W))
        val inst_sram_rdata = Input(UInt(32.W))
        // 数据寄存器
        val data_sram_en    = Output(UInt(1.W))
        val data_sram_wen   = Output(UInt(1.W))
        val data_sram_addr  = Output(UInt(32.W))
        val data_sram_wdata = Output(UInt(32.W))
        val data_sram_rdata = Input(UInt(32.W))
        // trace debug
        val debug_wb_pc     = Output(UInt(32.W))
        val debug_wb_rf_wen = Output(UInt(4.W))
        val debug_wb_rf_wnum = Output(UInt(5.W))
        val debug_wb_rf_wdata = Output(UInt(32.W))
    })
    val regfile = Mem(32, UInt(32.W))
    // IF
    io.inst_sram_wen := 0.U
    io.inst_sram_wdata := 233.U
    io.inst_sram_en  := 1.U
    val pc      = RegInit("h1bfffffc".asUInt(32.W))
    val inst    = io.inst_sram_rdata
    val pc_seq  = pc + 4.U(32.W)
    val br_flg  = Wire(Bool())
    val br_target = Wire(UInt(32.W))
    val nextpc  = MuxCase(pc_seq, Seq(
        br_flg  ->  br_target
    ))
    io.inst_sram_addr := pc
    pc  := nextpc

    // ID
    val rd      = inst(4, 0)
    val rj      = inst(9, 5)
    val rk      = inst(14, 10)
    val rs1_rd  = Mux(rj =/= 0.U, regfile(rj), 0.U(32.W))
    val rs2_rd  = Mux(rk =/= 0.U, regfile(rk), 0.U(32.W))
    val rs3_rd  = Mux(rd =/= 0.U, regfile(rd), 0.U(32.W))
    val ui5     = inst(14, 10)
    val i12     = inst(21, 10)
    val i16     = inst(25, 10)
    val i20     = inst(24, 5)
    val i26     = Cat(inst(9, 0), inst(25, 10))
    val i12_sex = Cat(Fill(20, i12(11)), i12)
    val of16_sex = Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W))
    val of26_sex = Cat(Fill(4, inst(9)), Cat(inst(9, 0), inst(25, 10)), 0.U(2.W))

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
            ld_w    -> List(ALU_SL, OP1_RS1, OP2_SI12, MEN_X, REN_S, WB_MEM),
            st_w    -> List(ALU_SL, OP1_RS1, OP2_SI12, MEN_S, REN_X, WB_X),
            jirl    -> List(BR_JIRL, OP1_RS1, OP2_OF16_SEX, MEN_X, REN_S, WB_PC),
            inst_b  -> List(BR_B, OP1_PC, OP2_OF26_SEX, MEN_X, REN_X, WB_X),
            inst_bl -> List(BR_BL, OP1_PC, OP2_OF26_SEX, MEN_X, REN_S, WB_PC),
            beq     -> List(BR_BEQ, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            bne     -> List(BR_BNE, OP1_PC, OP2_OF16_SEX, MEN_X, REN_X, WB_X),
            lu12i_w -> List(ALU_LU12I, OP1_X, OP2_SI20, MEN_X, REN_S, WB_ALU)
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
        (op2_sel === OP2_SI20) -> i20,
        (op2_sel === OP2_RD)   -> rs3_rd,
        (op2_sel === OP2_OF26) -> i26,
        (op2_sel === OP2_OF16) -> i16,
        (op2_sel === OP2_OF26_SEX) -> of26_sex,
        (op2_sel === OP2_OF16_SEX) -> of16_sex
    ))
    // EX
    val alu_out = MuxCase(0.U(32.W), Seq(
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
        (exe_fun === ALU_SL)    -> (op1_data + op2_data),
        (exe_fun === BR_JIRL)  -> (op1_data + op2_data),
        (exe_fun === BR_B)     -> (op1_data + op2_data),
        (exe_fun === BR_BL)     -> (op1_data + op2_data),
        (exe_fun === BR_BEQ)   -> (op1_data + op2_data),
        (exe_fun === BR_BNE)  -> (op1_data + op2_data),
        (exe_fun === ALU_LU12I) -> (Cat(op2_data, 0.U(12.W)))
    ))
    //branch
    br_flg := MuxCase(false.B, Seq(
        (exe_fun === BR_BL) -> true.B,
        (exe_fun === BR_B)  -> true.B,
        (exe_fun === BR_JIRL)  -> true.B,
        (exe_fun === BR_BNE)   -> (rs1_rd =/= rs3_rd),
        (exe_fun === BR_BEQ)   -> (rs1_rd === rs3_rd)
    ))
    br_target := alu_out
    
    // MEM
    io.data_sram_wen := mem_wen
    io.data_sram_wdata := rs3_rd
    io.data_sram_addr := alu_out
    io.data_sram_en   := 1.U
    val Disk = io.data_sram_rdata

    // WB
    val wb_data = MuxCase(alu_out, Seq(
        (wb_sel === WB_MEM) -> Disk,
        (wb_sel === WB_PC)  -> pc_seq
    ))
    val wb_addr = Mux(exe_fun === BR_BL, 1.U(5.W), rd)
    when (rf_wen === REN_S) {
        regfile(wb_addr) := wb_data
    }
    io.debug_wb_pc      := pc
    io.debug_wb_rf_wen  := Fill(4, rf_wen(0))
    io.debug_wb_rf_wnum := wb_addr
    io.debug_wb_rf_wdata:= wb_data
}
