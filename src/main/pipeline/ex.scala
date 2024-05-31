package pipeline

import chisel3._
import chisel3.util._
import const.R._
import const._

class EX extends Module {
    val io = IO(new Bundle {
        val ram         = new ioport.es_ram()
        val br          = new ioport.branch()
        
        val fr_ds_valid = Input(Bool())
        val fr_ds       = Input(new ioport.to_es_bus())
        val yuki        = Output(Bool())
        val es_allowin  = Output(Bool())

        val to_ms_valid = Output(Bool())
        val to_ms       = Output(new ioport.to_ms_bus())
        val ms_allowin  = Input(Bool())
    })
    
    val es_valid    = RegInit(false.B)
    val es_ready    = true.B // mul division
    val es_allowin  = !es_valid || (es_ready && io.ms_allowin)
    val es_bus      = RegInit(0.U.asTypeOf(new ioport.to_es_bus()))

    when (es_allowin) {
        es_valid := io.fr_ds_valid 
        es_bus   := io.fr_ds
    }
    // 如果保证所有冲刷流水线的操作都不在
    // mem, wb
    // 就不会有 ex 发出的读写请求被丢弃
    // 所以处理例外和中断，分支跳转都在 ex 级处理

    val sum_t     = es_bus.src1 + es_bus.src2
    val sub_t     = es_bus.src1 - es_bus.src2
    val slt_t     = (es_bus.src1.asSInt < es_bus.src2.asSInt).asUInt
    val sltu_t    = (es_bus.src1 < es_bus.src2).asUInt
    val nor_t     = ~(es_bus.src1 | es_bus.src2)
    val and_t     = es_bus.src1 & es_bus.src2
    val or_t      = es_bus.src1 | es_bus.src2
    val xor_t     = es_bus.src1 ^ es_bus.src2
    val sll_t     = (es_bus.src1 << es_bus.src2(4, 0))(31, 0).asUInt
    val srl_t     = (es_bus.src1 >> es_bus.src2(4, 0))(31, 0).asUInt
    val sra_t     = (es_bus.src1.asSInt >> es_bus.src2(4, 0))(31, 0).asUInt
    val br_t      = pc + 4.U(pc_len.W)

    val alu_out = MuxCase(sum, Seq(
        (es_bus.funct === func.sub)   -> sub,
        (es_bus.funct === func.slt)   -> slt,
        (es_bus.funct === func.sltu)  -> sltu,
        (es_bus.funct === func.nor)   -> nor,
        (es_bus.funct === func.and)   -> and,   
        (es_bus.funct === func.or)    -> or,
        (es_bus.funct === func.xor)   -> xor,
        (es_bus.funct === func.sll)   -> sll,
        (es_bus.funct === func.srl)   -> srl,
        (es_bus.funct === func.sra)   -> sra,
        (es_bus.funct === func.jirl)  -> br_t,
        (es_bus.funct === func.bl)    -> br_t
    ))

}