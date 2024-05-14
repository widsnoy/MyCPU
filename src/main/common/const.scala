package common

import chisel3._

object Consts {
    val vivado_build : Boolean = false

    val LENX        = 32
    val LENx        = 5
    val HILO        = 64
    val mulClockNum = 2
    val divClockNum = 8
    val TIMER_X     = 25
    val CSR_LENx    = 14

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
    val BREAK       =  36.U(EXE_FUN_LEN.W)

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
    val OP2_RDCNTID     = 10.U(OP2_LEN.W)
    val OP2_COUNTER_L   = 11.U(OP2_LEN.W)
    val OP2_COUNTER_H   = 12.U(OP2_LEN.W)


    // store
    val MEN_LEN = 3
    val MEN_X   = "b000".U
    val MEN_S   = "b100".U
    val MEN_H   = "b110".U
    val MEN_B   = "b111".U

    // load
    val REN_LEN = 3
    val REN_X   = "b010".U
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
}
object CSR {
    val CRMD        = 0x0.U(14.W)
    val PRMD        = 0x1.U(14.W)
    val EUEN        = 0x2.U(14.W)
    val ECFG        = 0x4.U(14.W)
    val ESTAT       = 0x5.U(14.W)
    val ERA         = 0x6.U(14.W)
    val BADV        = 0x7.U(14.W)
    val EENTRY      = 0xc.U(14.W)
    val TLBIDX      = 0x10.U(14.W)
    val TLBEHI      = 0x11.U(14.W)
    val TLBELO0     = 0x12.U(14.W)
    val TLBELO1     = 0x13.U(14.W)
    val ASID        = 0x18.U(14.W)
    val PGDL        = 0x19.U(14.W)
    val PGDH        = 0x1a.U(14.W)
    val PGD         = 0x1b.U(14.W)
    val CPUID       = 0x20.U(14.W)
    val SAVE0       = 0x30.U(14.W)
    val SAVE1       = 0x31.U(14.W)
    val SAVE2       = 0x32.U(14.W)
    val SAVE3       = 0x33.U(14.W)
    val TID         = 0x40.U(14.W)
    val TCFG        = 0x41.U(14.W)
    val TVAL        = 0x42.U(14.W)
    val TICLR       = 0x44.U(14.W)
    val LLBCTL      = 0x60.U(14.W)
    val TLBRENTRY   = 0x88.U(14.W)
    val CTAG        = 0x98.U(14.W)
    val DMW0        = 0x180.U(14.W)
    val DMW1        = 0x181.U(14.W)
}

object ECodes {
    val INT     = 0x00.U(6.W) 
    val PIL     = 0x01.U(6.W)
    val PIS     = 0x02.U(6.W)
    val PIF     = 0x03.U(6.W)
    val PME     = 0x04.U(6.W)
    val PPI     = 0x07.U(6.W)
    val ADEF    = 0x08.U(6.W)
    val ADEM    = 0x24.U(6.W)
    val ALE     = 0x09.U(6.W)
    val SYS     = 0x0b.U(6.W)
    val BRK     = 0x0c.U(6.W)
    val INE     = 0x0d.U(6.W)
    val IPE     = 0x0e.U(6.W)
    val FPD     = 0x0f.U(6.W)
    val FPE     = 0x12.U(6.W)
    val TLBR    = 0x3F.U(6.W)
    val NONE    = 0x25.U(6.W)
    val ERTN    = 0x26.U(6.W)
}

object WireBreak {
    val IPI    = 12.U(7.W)
    val TI     = 11.U(7.W) 
    val PMI    = 10.U(7.W) 
    val HWI0   = 9.U(7.W) 
    val HWI1   = 8.U(7.W)
    val HWI2   = 7.U(7.W)
    val HWI3   = 6.U(7.W)
    val HWI4   = 5.U(7.W)
    val HWI5   = 4.U(7.W)
    val HWI6   = 3.U(7.W)
    val HWI7   = 2.U(7.W)
    val SWI0   = 1.U(7.W)
    val SWI1   = 0.U(7.W)
}