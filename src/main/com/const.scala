package const

import chisel3._

object R {
    val vivado_build = false
    val data_len     = 32
    val addr_len     = 32
    val pc_len       = 32
    val reg_count    = 32
    val reg_addr     = 5
}

object func {
    val len     =  6
    val null    =  0.U(len.W)
    val add     =  1.U(len.W)
    val sub     =  2.U(len.W)
    val and     =  3.U(len.W)
    val nor     =  4.U(len.W)
    val or      =  5.U(len.W)
    val xor     =  6.U(len.W)
    val sll     =  7.U(len.W)
    val srl     =  8.U(len.W)
    val sra     =  9.U(len.W)
    val slt     =  10.U(len.W)
    val sltu    =  11.U(len.W)
    val lu12i   =  12.U(len.W)
    val jirl    =  13.U(len.W)
    val br_b    =  14.U(len.W)
    val br_bl   =  15.U(len.W)
    val br_beq  =  16.U(len.W)
    val br_bne  =  17.U(len.W)
    val load    =  18.U(len.W)
    val store   =  19.U(len.W)
    val mulh    =  20.U(len.W)
    val mull    =  21.U(len.W)
    val mulhu   =  22.U(len.W)
    val div_sig =  23.U(len.W)
    val div_uns =  24.U(len.W)
    val mod_sig =  25.U(len.W)
    val mod_uns =  26.U(len.W)
    val br_blt  =  27.U(len.W)
    val br_bltu =  28.U(len.W)
    val br_bge  =  29.U(len.W)
    val br_bgeu =  30.U(len.W)
    val csrrd   =  31.U(len.W)
    val csrwr   =  32.U(len.W)
    val csrxchg =  33.U(len.W)
    val srtn    =  34.U(len.W)
    val syscall =  35.U(len.W)
    val break   =  36.U(len.W)
}

object op1 {
    val len  = 2
    val null = 0.U(len.W)
    val pc   = 1.U(len.W)
    val rj   = 2.U(len.W)
}

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

object op2 {
    val len         = 4
    val null        = 0.U(len.W)
    val rk          = 1.U(len.W)
    val rd          = 2.U(len.W)
    val ui5         = 3.U(len.W)    
    val si12        = 4.U(len.W)        
    val si12u       = 5.U(len.W)        
    val si20        = 6.U(len.W)        
    val of16        = 7.U(len.W)        
    val of26        = 8.U(len.W)        
    val csrnum      = 9.U(len.W)        
    val rdcntid     = 10.U(len.W)        
    val counterl    = 11.U(len.W)            
    val counterh    = 12.U(len.W)            
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