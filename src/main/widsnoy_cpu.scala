package mycpu

import chisel3._
import chisel3.util._
import common._
import csr._
class widsnoy_cpu extends Module {
    val io = IO(new Bundle {
        val inst = new RAM_IO()
        val data = new RAM_IO()
        val debug = new DEBUG()
    })
    val IF  = Module(new IF_Stage()).io
    val ID  = Module(new ID_Stage()).io
    val EX  = Module(new EXE_Stage()).io
    val MEM = Module(new MEM_Stage()).io
    val WB  = Module(new WB_Stage()).io
    val reg = Module(new Regfile()).io
    val csr = Module(new CSR()).io
    IF.inst <> io.inst
    IF.to_ds <> ID.fs
    IF.br <> ID.br
    IF.ds_allowin <> ID.ds_allowin
    IF.ds_flush   <> ID.ds_flush
    IF.flush_pc   <> csr.flush_pc
    IF.next_pc    <> csr.next_pc

    ID.es_allowin <> EX.es_allowin
    ID.to_exe <> EX.ds
    ID.reg_r1 <> reg.reg.r1
    ID.reg_r2 <> reg.reg.r2
    ID.reg_r3 <> reg.reg.r3
    ID.reg_rdata1 <> reg.reg.rdata1
    ID.reg_rdata2 <> reg.reg.rdata2
    ID.reg_rdata3 <> reg.reg.rdata3
    ID.wr_EX     <> EX.wrf
    ID.wr_MEM    <> MEM.wrf
    ID.wr_WB     <> WB.wrf
    ID.csr       <> EX.rsc
    ID.es_flush  <> EX.es_flush
    ID.have_int  <> csr.have_int
    ID.counter_H <> csr.counter_H
    ID.counter_L <> csr.counter_L
    ID.counter_id <> csr.counter_id

    EX.ms_allowin <> MEM.ms_allowin
    EX.to_mem     <> MEM.es
    EX.data       <> io.data
    EX.csr       <> MEM.rsc
    EX.ms_flush  <> MEM.ms_flush

    MEM.ws_allowin <> WB.ws_allowin
    MEM.to_wb      <> WB.ms
    MEM.data_rdata <> io.data.rdata
    MEM.csr       <> WB.rsc
    MEM.wb_flush  <> WB.wb_flush

    WB.debug       <> io.debug
    WB.reg_wen     <> reg.reg.wen
    WB.reg_wr      <> reg.reg.wr
    WB.reg_wdata   <> reg.reg.wdata
    
    WB.csr.excp     <> csr.csr.excp
    WB.csr.Ecode    <> csr.csr.Ecode
    WB.csr.pc       <> csr.csr.pc
    WB.csr.usemask  <> csr.csr.usemask
    WB.csr.wen      <> csr.csr.wen
    WB.csr.waddr    <> csr.csr.waddr
    WB.csr.wdata    <> csr.csr.wdata
    WB.csr.mask     <> csr.csr.mask
    WB.csr.raddr    <> csr.csr.raddr
    WB.csr.rdata    <> csr.csr.rdata
    WB.csr.badv     <> csr.csr.badv
    WB.csr.badaddr  <> csr.csr.badaddr
}