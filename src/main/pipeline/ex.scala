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
        val bypass      = new ioport.bypass()

        val to_ms_valid = Output(Bool())
        val to_ms       = Output(new ioport.to_ms_bus())
        val ms_allowin  = Input(Bool())

        val mul         = Flipped(new ioport.calc_interface())
        val div         = Flipped(new ioport.calc_interface())
    })
    
    val div_rvalid = RegInit(false.B)
    val div_rdata  = RegInit(0.U(64.W))
    val mul_rvalid = RegInit(false.B)
    val mul_rdata  = RegInit(0.U(64.W))
    val emo         = RegInit(0.U.asTypeOf(new ioport.to_es_bus()))
    val es_valid    = RegInit(false.B)
    val es_ready    = !es_valid || ((!(emo.funct === func.store || emo.funct === func.load) || io.ram.addr_ok) && (!io.div.en || io.div.ready || div_rvalid) && (!io.mul.en || io.mul.ready || mul_rvalid))
    val es_allowin  = !es_valid || (es_ready && io.ms_allowin)

    when (es_allowin) {
        es_valid    := io.fr_ds_valid 
        emo         := io.fr_ds
        div_rvalid  := false.B
        mul_rvalid  := false.B
    }
    // 如果保证所有冲刷流水线的操作都不在
    // mem, wb
    // 就不会有 ex 发出的读写请求被丢弃
    // 所以处理例外和中断，分支跳转都在 ex 级处理

    val sum_t     = emo.op1 + emo.op2
    val sub_t     = emo.op1 - emo.op2
    val slt_t     = (emo.op1.asSInt < emo.op2.asSInt).asUInt
    val sltu_t    = (emo.op1 < emo.op2).asUInt
    val nor_t     = ~(emo.op1 | emo.op2)
    val and_t     = emo.op1 & emo.op2
    val or_t      = emo.op1 | emo.op2
    val xor_t     = emo.op1 ^ emo.op2
    val sll_t     = (emo.op1 << emo.op2(4, 0))(31, 0).asUInt
    val srl_t     = (emo.op1 >> emo.op2(4, 0))(31, 0).asUInt
    val sra_t     = (emo.op1.asSInt >> emo.op2(4, 0))(31, 0).asUInt
    val br_t      = emo.pc + 4.U(pc_len.W)


    io.div.en        := ((emo.funct === func.mod_sig) || (emo.funct === func.mod_uns) || (emo.funct === func.div_uns) || (emo.funct === func.div_sig))
    io.div.signed    := ((emo.funct === func.mod_sig) || (emo.funct === func.div_sig))
    io.div.op1       := emo.op1
    io.div.op2       := emo.op2
    when (io.div.ready) {
        div_rvalid := true.B
        div_rdata  := io.div.result
    }
    val div_emo    = Mux(io.div.ready, io.div.result, div_rdata)

    io.mul.en        := ((emo.funct === func.mulh) || (emo.funct === func.mull) || (emo.funct === func.mulhu))
    io.mul.signed    := (emo.funct =/= func.mulhu)
    io.mul.op1       := emo.op1
    io.mul.op2       := emo.op2
    when (io.mul.ready) {
        mul_rvalid := true.B
        mul_rdata  := io.mul.result
    }
    val mul_emo    = Mux(io.mul.ready, io.mul.result, mul_rdata)

    val rem     = div_emo(31, 0)
    val div     = div_emo(63, 32)
    val mull    = mul_emo(31, 0)
    val mulh    = mul_emo(63, 32)

    val alu_out = MuxCase(sum_t, Seq(
        (emo.funct === func.sub)       -> sub_t ,
        (emo.funct === func.slt)       -> slt_t ,
        (emo.funct === func.sltu)      -> sltu_t,
        (emo.funct === func.nor)       -> nor_t ,
        (emo.funct === func.and)       -> and_t ,   
        (emo.funct === func.or)        -> or_t  ,
        (emo.funct === func.xor)       -> xor_t ,
        (emo.funct === func.sll)       -> sll_t ,
        (emo.funct === func.srl)       -> srl_t ,
        (emo.funct === func.sra)       -> sra_t ,
        (emo.funct === func.jirl)      -> br_t  ,
        (emo.funct === func.bl)        -> br_t  ,
        (emo.funct === func.mulh || emo.funct === func.mulhu) -> mulh,
        (emo.funct === func.mull)      -> mull  ,
        (emo.funct === func.div_sig || emo.funct === func.div_uns) -> div,
        (emo.funct === func.mod_sig || emo.funct === func.mod_uns) -> rem
    ))

    // branch
    io.br.flag   := es_valid && MuxCase(false.B, Seq(
        (emo.funct === func.bl)    -> true.B,
        (emo.funct === func.b)     -> true.B,
        (emo.funct === func.jirl)  -> true.B,
        (emo.funct === func.bne)   -> (emo.src1 =/= emo.src3),
        (emo.funct === func.beq)   -> (emo.src1 === emo.src3),
        (emo.funct === func.blt)   -> (emo.src1.asSInt < emo.src3.asSInt),
        (emo.funct === func.bltu)  -> (emo.src1.asUInt < emo.src3.asUInt),
        (emo.funct === func.bge)   -> (emo.src1.asSInt >= emo.src3.asSInt),
        (emo.funct === func.bgeu)  -> (emo.src1.asUInt >= emo.src3.asUInt)
    ))
    io.br.target := sum_t
    io.yuki      := io.br.flag
    
    // load and store
    val wd_h      = Fill(2, emo.src3(15, 0))
    val wd_b      = Fill(4, emo.src3(7, 0))
    val mod4      = alu_out(1, 0)
    val wstrb_h   = Mux(mod4 === 0.U, "b0011".U, "b1100".U)
    val wstrb_b   = (1.U(32.W) << mod4)(3, 0)
    io.ram.req   := es_valid && (emo.funct === func.store || emo.funct === func.load) && io.ms_allowin
    io.ram.wr    := emo.funct === func.store
    io.ram.addr  := sum_t
    io.ram.size  := Mux(emo.w_tp(1, 0) === 0.U, 4.U(3.W), 0.U(1.W) ## emo.w_tp(1, 0))
    io.ram.wstrb := MuxCase("b1111".U(4.W), Seq(
        (emo.w_tp === 2.U) -> wstrb_h,
        (emo.w_tp === 1.U) -> wstrb_b
    ))
    io.ram.wdata := MuxCase(emo.src3, Seq(
        (emo.w_tp === 2.U) -> wd_h,
        (emo.w_tp === 1.U) -> wd_b
    ))

    io.es_allowin  := es_allowin

    io.to_ms_valid := es_valid && es_ready
    io.to_ms.pc    := emo.pc
    io.to_ms.funct := emo.funct
    io.to_ms.w_tp  := emo.w_tp
    io.to_ms.mod4  := mod4
    io.to_ms.res   := alu_out
    io.to_ms.dest  := emo.dest

    io.bypass.valid   := es_valid && emo.w_tp(4).asBool
    io.bypass.stall   := emo.funct =/= func.load
    io.bypass.dest    := emo.dest
    io.bypass.value   := alu_out
}