package pipeline

import chisel3._
import chisel3.util._
import const.R._
import const._

class MEM extends Module {
    val io = IO(new Bundle {
        val ram         = new ioport.ms_ram()

        val fr_es_valid = Input(Bool())
        val fr_es       = Input(new ioport.to_ms_bus())
        val ms_allowin  = Output(Bool())
        val bypass      = new ioport.bypass()

        val to_ws_valid = Output(Bool())
        val to_ws       = Output(new ioport.to_ws_bus())
        val ws_allowin  = Input(Bool())
    })

    val momo        = RegInit(0.U.asTypeOf(new ioport.to_ms_bus()))
    val ms_valid    = RegInit(false.B)
    val ms_ready    = !ms_valid || (!(momo.funct === func.store || momo.funct === func.load) || io.ram.data_ok)
    val ms_allowin  = !ms_valid || (ms_ready && io.ws_allowin)
    when (ms_allowin) {
        ms_valid := io.fr_es_valid
        momo     := io.fr_es
    }
    val rdata    = io.ram.rdata
    val hi_b     = momo.w_tp(2) & MuxCase(rdata(7), Seq(
        (momo.mod4 === 1.U) -> rdata(15),
        (momo.mod4 === 2.U) -> rdata(23),
        (momo.mod4 === 3.U) -> rdata(31)
    ))
    val hi_h     = momo.w_tp(2) & Mux(momo.mod4 === 0.U, rdata(15), rdata(31)) 
    val rdata_b  = Cat(Fill(24, hi_b), MuxCase(rdata(7, 0), Seq(
        (momo.mod4 === 1.U) -> rdata(15, 8),
        (momo.mod4 === 2.U) -> rdata(23, 16),
        (momo.mod4 === 3.U) -> rdata(31, 24)
    )))
    val rdata_h  = Cat(Fill(16, hi_h), Mux(momo.mod4 === 0.U, rdata(15, 0), rdata(31, 16)))

    val real_dat = MuxCase(rdata, Seq(
        (momo.w_tp(1, 0) === 1.U) -> rdata_b,
        (momo.w_tp(1, 0) === 2.U) -> rdata_h
    ))

    io.ms_allowin   := ms_allowin

    io.to_ws_valid  := ms_valid && ms_ready
    io.to_ws.pc     := momo.pc
    io.to_ws.w_tp   := momo.w_tp
    io.to_ws.res    := Mux(momo.funct === func.load, real_dat, momo.res)
    io.to_ws.dest   := momo.dest

    io.bypass.valid := ms_valid && momo.w_tp(4).asBool
    io.bypass.stall := momo.funct =/= func.load || ms_ready
    io.bypass.dest  := momo.dest
    io.bypass.value := Mux(momo.funct === func.load, real_dat, momo.res)
}