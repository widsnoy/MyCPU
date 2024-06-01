package mycpu

import chisel3._
import chisel3.util._
import pipeline._
import regfile._
import ioport._
import axi._

class widsnoy_cpu extends Module {
    val io = IO(new Bundle {
        val axi   = new axi_interface()
        val debug = Output(new debug_interface())
    })
    
    val inst_sram = Module(new inst_sram_slave()).io
    val data_sram = Module(new data_sram_slave()).io
    val axi       = Module(new axi_bridge()).io

    inst_sram.axi <> axi.inst
    data_sram.axi <> axi.data
    axi.axi       <> io.axi

    val pf  = Module(new PreIF()).io
    val fs  = Module(new IF()).io
    val ds  = Module(new ID()).io
    val es  = Module(new EX()).io
    val ms  = Module(new MEM()).io
    val ws  = Module(new WB()).io
    val reg = Module(new Regfile()).io
    
    pf.ram          <> inst_sram.pf
    pf.br           <> es.br
    pf.to_fs        <> fs.fr_pf
    pf.to_fs_valid  <> fs.fr_pf_valid
    pf.fs_allowin   <> fs.fs_allowin
    pf.rain         <> fs.yuki

    fs.ram          <> inst_sram.fs
    fs.to_ds_valid  <> ds.fr_fs_valid
    fs.to_ds        <> ds.fr_fs
    fs.ds_allowin   <> ds.ds_allowin
    fs.rain         <> ds.yuki

    ds.to_es_valid  <> es.fr_ds_valid
    ds.to_es        <> es.fr_ds
    ds.es_allowin   <> es.es_allowin
    ds.rain         <> es.yuki
    ds.reg          <> reg.ds

    es.ram          <> data_sram.es
    es.to_ms_valid  <> ms.fr_es_valid
    es.to_ms        <> ms.fr_es
    es.ms_allowin   <> ms.ms_allowin

    ms.ram          <> data_sram.ms
    ms.to_ws_valid  <> ws.fr_ms_valid
    ms.to_ws        <> ws.fr_ms
    ms.ws_allowin   <> ws.ws_allowin

    ws.reg          <> reg.ws
    ws.debug        <> io.debug
}