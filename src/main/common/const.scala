package common

import chisel3._

object Consts {
    val LENX    = 32
    val LENx    = 5

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

    val OP1_LEN = 2
    val OP1_RS1 = 0.U(OP1_LEN.W)
    val OP1_PC  = 1.U(OP1_LEN.W)
    val OP1_X   = 2.U(OP1_LEN.W)

    val OP2_LEN = 4
    val OP2_X   = 0.U(OP2_LEN.W)
    val OP2_RS2 = 1.U(OP2_LEN.W)
    val OP2_UI5 = 2.U(OP2_LEN.W)
    val OP2_SI12 = 3.U(OP2_LEN.W)
    val OP2_SI20_SEX = 4.U(OP2_LEN.W)
    val OP2_OF16 = 5.U(OP2_LEN.W)
    val OP2_OF26 = 6.U(OP2_LEN.W)
    val OP2_RD   = 7.U(OP2_LEN.W)
    val OP2_OF16_SEX    = 8.U(OP2_LEN.W)
    val OP2_OF26_SEX    = 9.U(OP2_LEN.W)

    val MEN_LEN = 1
    val MEN_X   = 0.U(MEN_LEN.W)
    val MEN_S   = 1.U(MEN_LEN.W)

    val REN_LEN = 1
    val REN_X   = 0.U(REN_LEN.W)
    val REN_S   = 1.U(REN_LEN.W)

    val WB_SEL_LEN = 2
    val WB_X       = 0.U(WB_SEL_LEN.W)
    val WB_ALU     = 1.U(WB_SEL_LEN.W)
    val WB_MEM     = 2.U(WB_SEL_LEN.W)
    val WB_PC      = 3.U(WB_SEL_LEN.W)
}