package mycpu

import chisel3._
import chisel3.util._
import common._
class widsnoy_cpu extends Module {
    val io = IO(new Bundle {
        val inst = new RAM_IO()
        val data = new RAM_IO()
        val debug = new DEBUG()
    })
    // val core = Module(new Core())
    // val regfile = Module(new Regfile())
    // core.io.reg <> regfile.io.reg
    // core.io.inst <> io.inst
    // core.io.data <> io.data
    // core.io.debug <> io.debug
    val IF = Module(new IF_Stage())
    val ID = Module(new ID_Stage())
    val EX = Module(new EXE_Stage())
    val MEM = Module(new MEM_Stage())
    val WB = Module(new WB_Stage())
    val regfile = Module(new Regfile())
    IF.io.inst <> io.inst
    IF.io.to_ds <> ID.io.fs
    IF.io.br <> ID.io.br
    IF.io.ds_allowin <> ID.io.ds_allowin

    ID.io.es_allowin <> EX.io.es_allowin
    ID.io.to_exe <> EX.io.ds
    ID.io.reg_r1 <> regfile.io.reg.r1
    ID.io.reg_r2 <> regfile.io.reg.r2
    ID.io.reg_r3 <> regfile.io.reg.r3
    ID.io.reg_rdata1 <> regfile.io.reg.rdata1
    ID.io.reg_rdata2 <> regfile.io.reg.rdata2
    ID.io.reg_rdata3 <> regfile.io.reg.rdata3
    ID.io.wr_EX     <> EX.io.wrf
    ID.io.wr_MEM    <> MEM.io.wrf
    ID.io.wr_WB     <> WB.io.wrf

    EX.io.ms_allowin <> MEM.io.ms_allowin
    EX.io.to_mem     <> MEM.io.es
    EX.io.data       <> io.data

    MEM.io.ws_allowin <> WB.io.ws_allowin
    MEM.io.to_wb      <> WB.io.ms
    MEM.io.data_rdata  <> io.data.rdata

    WB.io.debug       <> io.debug
    WB.io.reg_wen     <> regfile.io.reg.wen
    WB.io.reg_wr      <> regfile.io.reg.wr
    WB.io.reg_wdata   <> regfile.io.reg.wdata
}