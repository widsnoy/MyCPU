package common

import chisel3._

object Consts {
    val vivado_build : Boolean = false

    val LENX        = 32
    val LENx        = 5
    val HILO        = 64
    val mulClockNum = 2
    val divClockNum = 8

    val EXE_FUN_LEN = 6
    val ALU_X       =  0.U(EXE_FUN_LEN.W)
    val ALU_ADD     =  1.U(EXE_FUN_LEN.W)
    val ALU_SUB     =  2.U(EXE_FUN_LEN.W)
    val ALU_AND     =  3.U(EXE_FUN_LEN.W)
    val ALU_NOR     =  4.U(EXE_FUN_LEN.W)
    val ALU_OR      =  5.U(EXE_FUN_LEN.W)
    val ALU_XOR     =  6.U(EXE_FUN_LEN.W)
    val ALU_SLL     =  7.U(EXE_FUN_LEN.W)
    val ALU_SRL     =  8.U(EXE_FUN_LEN.W)
    val ALU_SRA     =  9.U(EXE_FUN_LEN.W)
    val ALU_SLT     =  10.U(EXE_FUN_LEN.W)
    val ALU_SLTU    =  11.U(EXE_FUN_LEN.W)
    val ALU_LU12I   =  12.U(EXE_FUN_LEN.W)
    val BR_JIRL     =  13.U(EXE_FUN_LEN.W)
    val BR_B        =  14.U(EXE_FUN_LEN.W)
    val BR_BL       =  15.U(EXE_FUN_LEN.W)
    val BR_BEQ      =  16.U(EXE_FUN_LEN.W)
    val BR_BNE      =  17.U(EXE_FUN_LEN.W)
    val LD          =  18.U(EXE_FUN_LEN.W)
    val ST          =  19.U(EXE_FUN_LEN.W)
    val ALU_MULH    =  20.U(EXE_FUN_LEN.W)
    val ALU_MULL    =  21.U(EXE_FUN_LEN.W)
    val ALU_MULHU   =  22.U(EXE_FUN_LEN.W)
    val ALU_DIVS    =  23.U(EXE_FUN_LEN.W)
    val ALU_DIVU    =  24.U(EXE_FUN_LEN.W)
    val ALU_MODS    =  25.U(EXE_FUN_LEN.W)
    val ALU_MODU    =  26.U(EXE_FUN_LEN.W)
    val BR_BLT      =  27.U(EXE_FUN_LEN.W)
    val BR_BLTU     =  28.U(EXE_FUN_LEN.W)
    val BR_BGE      =  29.U(EXE_FUN_LEN.W)
    val BR_BGEU     =  30.U(EXE_FUN_LEN.W)
    val CSRRD       =  31.U(EXE_FUN_LEN.W)
    val CSRWR       =  32.U(EXE_FUN_LEN.W)
    val CSRXCHG     =  33.U(EXE_FUN_LEN.W)
    val ERTN        =  34.U(EXE_FUN_LEN.W)
    val SYSCALL     =  35.U(EXE_FUN_LEN.W)

    val OP1_LEN = 3
    val OP1_RS1 = 0.U(OP1_LEN.W)
    val OP1_PC  = 1.U(OP1_LEN.W)
    val OP1_X   = 2.U(OP1_LEN.W)

    val OP2_LEN         = 4
    val OP2_X           = 0.U(OP2_LEN.W)
    val OP2_RS2         = 1.U(OP2_LEN.W)
    val OP2_UI5         = 2.U(OP2_LEN.W)
    val OP2_RD          = 3.U(OP2_LEN.W)
    val OP2_SI12_SEX    = 4.U(OP2_LEN.W)
    val OP2_SI20_SEX    = 5.U(OP2_LEN.W)
    val OP2_OF16_SEX    = 6.U(OP2_LEN.W)
    val OP2_OF26_SEX    = 7.U(OP2_LEN.W)
    val OP2_SI12_UEX    = 8.U(OP2_LEN.W)
    val OP2_CSR_NUM     = 9.U(OP2_LEN.W)

    val MEN_LEN = 3
    val MEN_X   = "b000".U
    val MEN_S   = "b100".U
    val MEN_H   = "b110".U
    val MEN_B   = "b111".U

    val REN_LEN = 3
    val REN_X   = "b000".U
    val REN_S   = "b000".U
    val REN_H   = "b100".U
    val REN_B   = "b110".U
    val REN_HU  = "b101".U
    val REN_BU  = "b111".U

    val WB_SEL_LEN = 3
    val WB_X       = "b000".U(WB_SEL_LEN.W)
    val WB_MEM     = "b001".U(WB_SEL_LEN.W)
    val WB_CSR     = "b101".U(WB_SEL_LEN.W)
    val WB_BOTH    = "b111".U(WB_SEL_LEN.W)
    val WB_ALU     = "b011".U(WB_SEL_LEN.W)

    val CSR_LENx     = 14
    val CSR_SIZE     = 9
    val CSR_SIZE_LOG = 4
    val CSR_BADVADDR = 15

    val CRMD    = 0.U
    val CRMD_x  = "h0".U
    val PRMD    = 1.U
    val PRMD_x  = "h1".U
    val ESTAT   = 2.U
    val ESTAT_x = "h5".U
    val ERA     = 3.U
    val ERA_x   = "h6".U
    val EENTRY  = 4.U
    val EENTRY_x= "hc".U
    val SAVE0   = 5.U
    val SAVE0_x = "h30".U
    val SAVE1   = 6.U
    val SAVE1_x = "h31".U
    val SAVE2   = 7.U
    val SAVE2_x = "h32".U
    val SAVE3   = 8.U
    val SAVE3_x = "h33".U
}