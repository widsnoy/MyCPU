package pipeline

import chisel3._
import chisel3.util._
import const.R._
import const._
import inst._

class ID extends Module {
    val io = IO(new Bundle {
        val fr_fs_valid = Input(Bool())
        val fr_fs       = Input(new ioport.to_ds_bus())
        val yuki        = Output(Bool())
        val ds_allowin  = Output(Bool())

        val to_es_valid = Output(Bool())
        val to_es_bus   = Output(new ioport.to_es_bus())
        val ds_allowin  = Input(Bool())
        val rain        = Input(Bool())
    })

    val ds_valid = RegInit(false.B)
    val ds_ready = 
}