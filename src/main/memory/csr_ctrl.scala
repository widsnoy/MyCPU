package csr

import chisel3._
import chisel3.util._
import const.R._
import const._

class CSR extends Module {
    val io = IO(new Bundle {
        val ds  = new ioport.csr_to_ds()
        val csr = Flipped(new ioport.csr_interface())
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

    io.ds.tid    := TID.info.tid
    io.csr.rdata := 0.U
    for (x <- csrlist) { 
        when (x.id === io.csr.raddr) {
            io.csr.rdata := Mux(x.id === CSR.TICLR, 0.U(data_len.W), x.info.asUInt)
        }
    }

    io.csr.br_target  := Mux(io.csr.opt === 1.U, EENTRY.info.asUInt, ERA.info.asUInt)
    io.ds.snow        := ((Cat(ESTAT.info.is_12, ESTAT.info.is_11, ESTAT.info.is_9_2, ESTAT.info.is_1_0) & Cat(ECFG.info.lie_12_11, ECFG.info.lie_9_0)).orR.asBool) && CRMD.info.ie
    when (io.csr.opt === 1.U) {
        CRMD.info.plv       := "b00".U
        CRMD.info.ie        := false.B
        PRMD.info.pplv      := CRMD.info.plv
        PRMD.info.pie       := CRMD.info.ie
        ERA.info.pc         := io.csr.pc
        ESTAT.info.esubcode := Mux(io.csr.ecode === ECODE.ADEM, 1.U(9.W), 0.U(9.W))
        ESTAT.info.ecode    := io.csr.ecode
        when (io.csr.ecode === ECODE.ALE) {
            BADV.write(io.csr.badv)
        }.elsewhen (io.csr.ecode === ECODE.ADEF) {
            BADV.write(io.csr.pc)
        }
    }.elsewhen (io.csr.opt === 2.U) {
        CRMD.info.plv := PRMD.info.pplv
        CRMD.info.ie  := PRMD.info.pie
        when (ESTAT.info.ecode === "h3F".U(6.W)) {
            CRMD.info.da := false.B
            CRMD.info.pg := true.B
        }
    }

    //TVAL
    when (io.csr.wvalid && io.csr.waddr === TCFG.id) {
        val rval = ((io.csr.wmask & io.csr.wdata) | (~io.csr.wmask & TCFG.info.asUInt))
        TVAL.info.timeval := rval(timer_len - 1, 2) ## 1.U(2.W)
    }.elsewhen (TCFG.info.en) {
        when (TVAL.info.timeval === 0.U) {
            TVAL.info.timeval := Mux(TCFG.info.preiodic, Cat(TCFG.info.initval, 0.U(2.W)), 0.U(timer_len.W))
        }.otherwise {
            TVAL.info.timeval := TVAL.info.timeval - 1.U
        }
    }

    val TVAL_edge = ShiftRegister(TVAL.info.timeval, 1)
    when (TCFG.info.en && TVAL.info.timeval === 0.U && TVAL_edge === 1.U) {
        ESTAT.info.is_11 := true.B
    }
    when (io.csr.wvalid) {
        for (x <- csrlist) {
            when (x.id === io.csr.waddr) {
                val rval = ((io.csr.wmask & io.csr.wdata) | (~io.csr.wmask & x.info.asUInt))
                x.write(rval)
                when (x.id === CSR.CRMD && rval(4) === 1.U) {
                    CRMD.info.datf := "b01".U
                    CRMD.info.datm := "b01".U
                }
                when (x.id === CSR.TICLR && rval(0) === 1.U) {
                    ESTAT.info.is_11 := false.B
                }
            }
        }
    }
    
}