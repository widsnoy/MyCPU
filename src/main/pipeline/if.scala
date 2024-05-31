package pipeline

import chisel3._
import chisel3.util._
import const.R._

class IF extends Module {
    val io = IO(new Bundle {
        val ram         = new ioport.fs_ram()
        val fr_pf_valid = Input(Bool())
        val fr_pf       = Input(new ioport.to_fs_bus())
        val yuki        = Output(Bool())
        val fs_allowin  = Output(Bool())

        val to_ds_valid = Output(Bool())
        val to_ds       = new ioport.to_ds_bus()
        val ds_allowin  = Input(Bool())
        val rain        = Input(Bool())
    })

    val ram_data_ok = RegInit(false.B)
    val ram_rdata   = RegInit(0.U(data_len.W))
    val throw_data  = RegInit(false.B)     // 中断例外，跳转分支，都在 EXE 级处理，保证至多丢弃一次数据
    val fs_valid    = RegInit(false.B)
    val fs_ready    = (io.ram.data_ok || ram_data_ok) && !throw_data
    val fs_allowin  = !fs_valid || (fs_ready && io.ds_allowin)
    val fs_bus      = RegInit(0.U.asTypeOf(new ioport.to_fs_bus())) 

    when (io.rain) {
        throw_data  := io.fr_pf_valid || (!fs_allowin && !fs_ready)
        fs_valid    := false.B
        ram_data_ok := false.B
    }.elsewhen (fs_allowin) {
        fs_valid    := io.fr_pf_valid
        fs_bus      := io.fr_pf
        ram_data_ok := false.B
    }
    when (io.ram.data_ok) {
        when (throw_data) {
            throw_data  := false.B
        }.otherwise {
            ram_data_ok := true.B
            ram_rdata   := io.ram.rdata
        }
    }

    io.yuki         := io.rain
    io.fs_allowin   := fs_allowin

    io.to_ds_valid  := fs_valid && fs_ready && !io.rain
    io.to_ds.pc     := fs_bus.pc
    io.to_ds.inst   := Mux(ram_data_ok, ram_rdata, io.ram.rdata)
}