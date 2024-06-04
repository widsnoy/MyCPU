package mycpu

import chisel3._
import chisel3.util._
import pipeline._
import gpr._
import ioport._
import axi._
import calc._
import csr._

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
    val gpr = Module(new GPR()).io
    val mul = Module(new multiplier()).io
    val div = Module(new divider()).io
    val csr = Module(new CSR()).io

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
    ds.gpr          <> gpr.ds
    ds.es_bypass    <> es.bypass    
    ds.ms_bypass    <> ms.bypass
    ds.ws_bypass    <> ws.bypass
    ds.csr          <> csr.ds

    es.ram          <> data_sram.es
    es.to_ms_valid  <> ms.fr_es_valid
    es.to_ms        <> ms.fr_es
    es.ms_allowin   <> ms.ms_allowin
    es.mul          <> mul.yu
    es.div          <> div.yu
    es.csr          <> csr.csr

    ms.ram          <> data_sram.ms
    ms.to_ws_valid  <> ws.fr_ms_valid
    ms.to_ws        <> ws.fr_ms
    ms.ws_allowin   <> ws.ws_allowin

    ws.gpr          <> gpr.ws
    ws.debug        <> io.debug
}