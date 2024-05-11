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
        val ds_allowin = Input(Bool())
        val ds_flush   = Input(Bool())
        val flush_pc = Input(Bool())
        val next_pc  = Input(UInt(LENX.W))
    })
    val to_fs_valid    = RegNext(!reset.asBool)
    val fs_ready_go    = true.B
    val fs_valid       = RegInit(false.B)
    val fs_allowin     = (!fs_valid || (io.ds_allowin && fs_ready_go)) && !io.ds_flush
    when (fs_allowin) {fs_valid := to_fs_valid}
    val pc          = RegInit("h1bfffffc".asUInt(32.W))
    val csr_pc_flush= RegNext(io.flush_pc)
    val csr_next_pc = RegNext(io.next_pc)
    val pc_plus_4   = pc + 4.U(32.W)
    val next_pc     = MuxCase(pc_plus_4, Seq(
        csr_pc_flush->  csr_next_pc,
        io.br.flg   ->  io.br.target
    ))
    io.inst.wen     := 0.U(4.W)
    io.inst.wdata   := 233.U
    io.inst.en      := to_fs_valid && fs_allowin
    io.inst.addr    := next_pc

    io.to_ds.valid  := fs_valid && fs_ready_go && !io.br.flg && !csr_pc_flush && !io.ds_flush
    io.to_ds.pc     := pc
    io.to_ds.inst   := Mux(io.to_ds.valid, io.inst.rdata, 0.U(32.W))

    when (to_fs_valid && fs_allowin) {pc := next_pc}
}