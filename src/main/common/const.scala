package common

import chisel3._

object Consts {
    val vivado_build : Boolean = false

    val LENX        = 32
    val LENx        = 5
    val HILO        = 64
    val mulClockNum = 2
    val divClockNum = 8

    val EXE_FUN_LEN = 5
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

    val MEN_LEN = 3
    val MEN_X   = "b000".U
    val MEN_S   = "b100".U
    val MEN_H   = "b110".U
    val MEN_B   = "b111".U

    val REN_LEN = 4
    val REN_X   = "b0000".U
    val REN_S   = "b1000".U
    val REN_H   = "b1100".U
    val REN_B   = "b1110".U
    val REN_HU  = "b1101".U
    val REN_BU  = "b1111".U

    val WB_SEL_LEN = 3
    val WB_X       = 0.U(WB_SEL_LEN.W)
    val WB_ALU     = 1.U(WB_SEL_LEN.W)
    val WB_MEM     = 2.U(WB_SEL_LEN.W)
    val WB_PC      = 3.U(WB_SEL_LEN.W)
}