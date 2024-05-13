package csr

import chisel3._
import chisel3.util._
import common._
import common.Consts._

class CSR extends Module {
    val io = IO(new Bundle {
        val csr = Flipped(new CSR_IO())
        val flush_pc = Output(Bool())
        val next_pc  = Output(UInt(LENX.W))
    })

    val CRMD   = new CRMD
    val PRMD   = new PRMD
    val EUEN   = new EUEN
    val ECFG   = new ECFG
    val ESTAT  = new ESTAT
    val ERA    = new ERA
    val BADV   = new BADV
    val EENTRY = new EENTRY
    // val TLBIDX    =
    // val TLBEHI    =
    // val TLBELO0   =
    // val TLBELO1   =
    // val ASID      =
    // val PGDL      =
    // val PGDH      =
    // val PGD       =
    // val CPUID     =
    val SAVE0 = new SAVE0
    val SAVE1 = new SAVE1
    val SAVE2 = new SAVE2
    val SAVE3 = new SAVE3
    val TID   = new TID
    val TCFG  = new TCFG
    val TVAL  = new TVAL
    val TICLR = new TICLR
    // val LLBCTL    =
    // val TLBRENTRY =
    // val CTAG      =
    // val DMW0      =
    // val DMW1      =

    val csrlist = Seq(
        CRMD,
        PRMD,
        EUEN,
        ECFG,
        ESTAT,
        ERA,
        BADV,
        EENTRY,
        // TLBIDX,
        // TLBEHI,
        // TLBELO0,
        // TLBELO1,
        // ASID,
        // PGDL,
        // PGDH,
        // PGD,
        // CPUID,
        SAVE0,
        SAVE1,
        SAVE2,
        SAVE3,
        TID,
        TCFG,
        TVAL,
        TICLR,
        // LLBCTL,
        // TLBRENTRY,
        // CTAG,
        // DMW0,
        // DMW1,
    )

//    val stable_counter = RegInit(0.U(64.W))
//    stable_counter := Mux(stable_counter === ALL_MASK.U, 0.U, stable_counter + 1.U)

    io.csr.rdata := 0.U
    for (x <- csrlist) {
        when (x.id === io.csr.raddr) {
            io.csr.rdata := x.info.asUInt
        }
    }

    io.flush_pc         := io.csr.excp =/= 0.U
    io.next_pc          := Mux(io.csr.excp === 1.U, EENTRY.info.asUInt, ERA.info.asUInt)
    when (io.csr.excp === 1.U) {
        CRMD.info.plv := "b00".U
        CRMD.info.ie  := false.B
        PRMD.info.pplv := CRMD.info.plv
        PRMD.info.pie  := CRMD.info.ie
        ERA.write(io.csr.pc)
        ESTAT.info.esubcode := io.csr.Esubcode
        ESTAT.info.ecode := io.csr.Ecode
    }.elsewhen (io.csr.excp === 2.U) {
        CRMD.info.plv := PRMD.info.pplv
        CRMD.info.ie  := PRMD.info.pie
        when (ESTAT.info.ecode === "h3F".U(6.W)) {
            CRMD.info.da := false.B
            CRMD.info.pg := true.B
        }
    }
    when (io.csr.wen) {
        val MASK = Mux(io.csr.usemask, io.csr.mask, "b1111_1111_1111_1111_1111_1111_1111_1111".U)
        for (x <- csrlist) {
            when (x.id === io.csr.waddr) {
                val rval = ((MASK & io.csr.wdata) | (~MASK & x.info.asUInt))
                x.write(rval)
                when (x.id === CSR.CRMD && rval(4) === 1.U) {
                    CRMD.info.datf := "b01".U
                    CRMD.info.datm := "b01".U
                }
            }
        }
    }
}