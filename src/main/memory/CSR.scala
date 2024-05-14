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
        val have_int = Output(Bool())
        val counter_H   = Output(UInt(LENX.W))
        val counter_L   = Output(UInt(LENX.W))
        val counter_id  = Output(UInt(LENX.W))
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

    val stable_counter = RegInit(0.U(64.W))
    stable_counter := Mux(stable_counter === "hffffffffffffffff".U, 0.U, stable_counter + 1.U)

    io.counter_H  := stable_counter(63, 32)
    io.counter_L  := stable_counter(31, 0)
    io.counter_id := TID.info.tid

    io.csr.rdata := 0.U
    for (x <- csrlist) {
        when (x.id === io.csr.raddr) {
            io.csr.rdata := Mux(x.id === CSR.TICLR, 0.U(LENX.W), x.info.asUInt)
        }
    }

    io.flush_pc := io.csr.excp =/= 0.U
    io.next_pc  := Mux(io.csr.excp === 1.U, EENTRY.info.asUInt, ERA.info.asUInt)
    io.have_int := ((Cat(ESTAT.info.is_12, ESTAT.info.is_11, ESTAT.info.is_9_2, ESTAT.info.is_1_0) & Cat(ECFG.info.lie_12_11, ECFG.info.lie_9_0)).orR.asBool) && CRMD.info.ie
    when (io.csr.excp === 1.U) {
        CRMD.info.plv := "b00".U
        CRMD.info.ie  := false.B
        PRMD.info.pplv := CRMD.info.plv
        PRMD.info.pie  := CRMD.info.ie
        ERA.info.pc := io.csr.pc
        ESTAT.info.esubcode := Mux(io.csr.Ecode === ECodes.ADEM, 1.U(9.W), 0.U(9.W))
        ESTAT.info.ecode := io.csr.Ecode
        when (io.csr.badv) { BADV.write(io.csr.badaddr) }
    }.elsewhen (io.csr.excp === 2.U) {
        CRMD.info.plv := PRMD.info.pplv
        CRMD.info.ie  := PRMD.info.pie
        when (ESTAT.info.ecode === "h3F".U(6.W)) {
            CRMD.info.da := false.B
            CRMD.info.pg := true.B
        }
    }

    val MASK = Mux(io.csr.usemask, io.csr.mask, "b1111_1111_1111_1111_1111_1111_1111_1111".U)

    //TVAL
    when (io.csr.wen && io.csr.waddr === TCFG.id) {
        val rval = ((MASK & io.csr.wdata) | (~MASK & TCFG.info.asUInt))
        TVAL.info.timeval := rval(TIMER_X - 1, 2) ## 1.U(2.W)
    }.elsewhen (TCFG.info.en) {
        when (TVAL.info.timeval === 0.U) {
            TVAL.info.timeval := Mux(TCFG.info.preiodic, Cat(TCFG.info.initval, 0.U(2.W)), 0.U(TIMER_X.W))
        }.otherwise {
            TVAL.info.timeval := TVAL.info.timeval - 1.U
        }
    }

    val TVAL_edge = ShiftRegister(TVAL.info.timeval, 1)
    when (TCFG.info.en && TVAL.info.timeval === 0.U && TVAL_edge === 1.U) {
        ESTAT.info.is_11 := true.B
    }
    
    when (io.csr.wen) {
        for (x <- csrlist) {
            when (x.id === io.csr.waddr) {
                val rval = ((MASK & io.csr.wdata) | (~MASK & x.info.asUInt))
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