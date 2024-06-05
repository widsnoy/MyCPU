package pipeline

import chisel3._
import chisel3.util._
import const.R._
import const._

class WB extends Module {
    val io = IO(new Bundle {
        val debug       = Output(new ioport.debug_interface())

        val fr_ms_valid = Input(Bool())
        val fr_ms       = Input(new ioport.to_ws_bus())
        val ws_allowin  = Output(Bool())
        val bypass      = new ioport.bypass()

        val gpr         = Flipped(new ioport.ws_gpr())
    })

    val www        = RegInit(0.U.asTypeOf(new ioport.to_ws_bus))
    val ws_valid   = RegInit(false.B)
    val ws_ready   = true.B
    val ws_allowin = !ws_valid || ws_ready

    when (ws_allowin) {
        ws_valid := io.fr_ms_valid
        www      := io.fr_ms
    }

    io.gpr.wen   := ws_valid && www.w_tp(4).asBool
    io.gpr.wr    := www.dest
    io.gpr.wd    := www.res

    io.ws_allowin := ws_allowin

    io.debug.wb_pc       := www.pc
    io.debug.wb_rf_wen   := Fill(4, www.w_tp(4) & ws_valid.asUInt)
    io.debug.wb_rf_wnum  := www.dest
    io.debug.wb_rf_wdata := www.res

    io.bypass.valid := ws_valid && www.w_tp(4).asBool && www.dest =/= 0.U
    io.bypass.stall := true.B
    io.bypass.dest  := www.dest
    io.bypass.value := www.res
}