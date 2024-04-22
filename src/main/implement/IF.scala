package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._
import common.Instructions._

class IF_Stage extends Module {
    val io = IO(new Bundle {
        val inst    = new RAM_IO()
        val to_ds   = new ID_INFO()
        val br      = Flipped(new BR_INFO())
        val ds_allowin = Input(UInt(1.W))
    })
    val to_fs_valid    = ~reset.asUInt
    val fs_ready_go    = 1.U(1.W)
    val fs_valid       = RegInit(0.U(1.W))
    val fs_allowin     = (~fs_valid) | (io.ds_allowin & fs_ready_go)
    when (fs_allowin === 1.U) {fs_valid := to_fs_valid}
    val pc          = RegInit("h1bfffffc".asUInt(32.W))
    val pc_plus_4   = pc + 4.U(32.W)
    val next_pc     = MuxCase(pc_plus_4, Seq(
        io.br.flg  ->  io.br.target
    ))
    io.inst.wen     := 0.U(4.W)
    io.inst.wdata   := 233.U
    io.inst.en      := (to_fs_valid & fs_allowin)
    io.inst.addr    := next_pc

    io.to_ds.valid  := fs_valid & fs_ready_go & (~io.br.flg.asUInt)
    io.to_ds.pc     := pc
    io.to_ds.inst   := io.inst.rdata

    when ((to_fs_valid & fs_allowin) === 1.U) {pc := next_pc}
}