package pipeline

import chisel3._
import chisel3.util._
import const.R._

class PreIF extends Module {
    val io = IO(new Bundle {
        val ram         = new ioport.pf_ram()
        val br          = Flipped(new ioport.branch())
        val to_fs       = Output(new ioport.to_fs_bus())
        val to_fs_valid = Output(Bool())
        val fs_allowin  = Input(Bool())
        val rain        = Input(Bool())
    })

    val req         = RegNext(io.ram.req)
    val to_pf_valid = RegNext(!reset.asBool)
    val pf_ready    = io.ram.addr_ok && req
    val pc          = RegInit("h1bfffffc".asUInt(pc_len.W))
    val br_flag     = RegInit(false.B)
    val br_target   = RegInit(0.U(pc_len.W))
    val next_pc     = MuxCase(pc + 4.U, Seq(
        io.br.flag  -> io.br.target,
        br_flag     -> br_target
    ))

    when (io.br.flag) {
        br_flag    := true.B
        br_target  := io.br.target
    }

    io.ram.wr      := 0.U
    io.ram.size    := 2.U
    io.ram.req     := to_pf_valid && !pf_ready && io.fs_allowin && !io.rain
    io.ram.addr    := next_pc

    io.to_fs_valid    := pf_ready
    io.to_fs.pc       := next_pc

    when (to_pf_valid && pf_ready && io.fs_allowin && !io.br.flag) {
        pc        := next_pc
        br_flag   := false.B
        br_target := 0.U
    }
}