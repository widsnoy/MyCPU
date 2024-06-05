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
        val to_es       = Output(new ioport.to_es_bus())
        val es_allowin  = Input(Bool())
        val rain        = Input(Bool())

        // gpr
        val gpr         = Flipped(new ioport.ds_gpr())

        // bypass
        val es_bypass   = Flipped(new ioport.bypass())
        val ms_bypass   = Flipped(new ioport.bypass())
        val ws_bypass   = Flipped(new ioport.bypass())

        // int
        val csr         = Flipped(new ioport.csr_to_ds())
    })

    def ffstop(dest: UInt): Bool = {
        return (io.es_bypass.valid && io.es_bypass.dest === dest && !io.es_bypass.stall) || (io.ms_bypass.valid && io.ms_bypass.dest === dest && !io.ms_bypass.stall)
    }
    def foword(dest: UInt, ini: UInt): UInt = {
        return MuxCase(ini, Seq(
            (io.es_bypass.valid && io.es_bypass.dest === dest) -> io.es_bypass.value,
            (io.ms_bypass.valid && io.ms_bypass.dest === dest) -> io.ms_bypass.value,
            (io.ws_bypass.valid && io.ws_bypass.dest === dest) -> io.ws_bypass.value
        ))
    }

    val stable_counter = RegInit(0.U(64.W))
    stable_counter := Mux(stable_counter === "hffffffffffffffff".U, 0.U, stable_counter + 1.U)

    val ds_bus      = RegInit(0.U.asTypeOf(new ioport.to_ds_bus()))
    val rd          = ds_bus.inst(4, 0)
    val rj          = ds_bus.inst(9, 5)
    val rk          = ds_bus.inst(14, 10)

    val ds_valid    = RegInit(false.B)
    val ds_ready    = !ffstop(rj) && !ffstop(rk) && !ffstop(rd)
    val ds_allowin  = !ds_valid || (ds_ready && io.es_allowin)
    
    when (io.rain) {
        ds_valid    := false.B
    }.elsewhen (ds_allowin) {
        ds_valid    := io.fr_fs_valid
        ds_bus      := io.fr_fs
    }

    io.gpr.rr1   := rj
    io.gpr.rr2   := rk
    io.gpr.rr3   := rd
    val rj_data  = foword(rj, io.gpr.rd1)
    val rk_data  = foword(rk, io.gpr.rd2)
    val rd_data  = foword(rd, io.gpr.rd3)

    val ui5     = ds_bus.inst(14, 10)
    val si12    = Cat(Fill(20, ds_bus.inst(21)), ds_bus.inst(21, 10))
    val of16    = Cat(Fill(14, ds_bus.inst(25)), ds_bus.inst(25, 10), 0.U(2.W))
    val of26    = Cat(Fill(4, ds_bus.inst(9)), Cat(ds_bus.inst(9, 0), ds_bus.inst(25, 10)), 0.U(2.W))
    val si20    = Cat(ds_bus.inst(24, 5), 0.U(12.W))
    val si12u   = Cat(0.U(20.W), ds_bus.inst(21, 10)) 

    /*
    high -> low
   0      0    0
   gpr  csr  1sign / 0unsign
    1 0   4byte
    0 1   2byte
    0 0   1byte
    特别的，置 00111 表示触发例外
    */

    val Decode = ListLookup(ds_bus.inst, List(func.noth, op1.noth, op2.noth,"b00111".U(5.W)), Array (     
        inst.add_w       -> List(func.add,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.sub_w       -> List(func.sub,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.slt         -> List(func.slt,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.sltu        -> List(func.sltu,     op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.slti        -> List(func.slt,      op1.rj,     op2.si12,       "b10010".U(5.W)),
        inst.sltui       -> List(func.sltu,     op1.rj,     op2.si12,       "b10010".U(5.W)),
        inst.nor         -> List(func.nor,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.and         -> List(func.and,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.or          -> List(func.or,       op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.xor         -> List(func.xor,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.andi        -> List(func.and,      op1.rj,     op2.si12u,      "b10010".U(5.W)),
        inst.ori         -> List(func.or,       op1.rj,     op2.si12u,      "b10010".U(5.W)),
        inst.xori        -> List(func.xor,      op1.rj,     op2.si12u,      "b10010".U(5.W)),
        inst.slli_w      -> List(func.sll,      op1.rj,     op2.ui5,        "b10010".U(5.W)),
        inst.srli_w      -> List(func.srl,      op1.rj,     op2.ui5,        "b10010".U(5.W)),
        inst.sra_w       -> List(func.sra,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.sll_w       -> List(func.sll,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.srl_w       -> List(func.srl,      op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.srai_w      -> List(func.sra,      op1.rj,     op2.ui5,        "b10010".U(5.W)),
        inst.addi_w      -> List(func.add,      op1.rj,     op2.si12,       "b10010".U(5.W)),
        inst.ld_w        -> List(func.load,     op1.rj,     op2.si12,       "b10010".U(5.W)),
        inst.st_w        -> List(func.store,    op1.rj,     op2.si12,       "b00010".U(5.W)),
        inst.jirl        -> List(func.jirl,     op1.rj,     op2.of16,       "b10010".U(5.W)),
        inst.b           -> List(func.b,        op1.pc,     op2.of26,       "b00010".U(5.W)),
        inst.bl          -> List(func.bl,       op1.pc,     op2.of26,       "b10010".U(5.W)),
        inst.beq         -> List(func.beq,      op1.pc,     op2.of16,       "b00010".U(5.W)),
        inst.bne         -> List(func.bne,      op1.pc,     op2.of16,       "b00010".U(5.W)),
        inst.lu12i_w     -> List(func.lu12i,    op1.noth,   op2.si20,       "b10010".U(5.W)),
        inst.pcaddu12i   -> List(func.add,      op1.pc,     op2.si20,       "b10010".U(5.W)),
        inst.mul_w       -> List(func.mull,     op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.mulh_w      -> List(func.mulh,     op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.mulh_wu     -> List(func.mulhu,    op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.div_w       -> List(func.div_sig,  op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.div_wu      -> List(func.div_uns,  op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.mod_w       -> List(func.mod_sig,  op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.mod_wu      -> List(func.mod_uns,  op1.rj,     op2.rk,         "b10010".U(5.W)),
        inst.blt         -> List(func.blt,      op1.pc,     op2.of16,       "b00010".U(5.W)),
        inst.bge         -> List(func.bge,      op1.pc,     op2.of16,       "b00010".U(5.W)),
        inst.bltu        -> List(func.bltu,     op1.pc,     op2.of16,       "b00010".U(5.W)),
        inst.bgeu        -> List(func.bgeu,     op1.pc,     op2.of16,       "b00010".U(5.W)),
        inst.ld_b        -> List(func.load,     op1.rj,     op2.si12,       "b10100".U(5.W)),
        inst.ld_h        -> List(func.load,     op1.rj,     op2.si12,       "b10101".U(5.W)),
        inst.st_b        -> List(func.store,    op1.rj,     op2.si12,       "b00000".U(5.W)),
        inst.st_h        -> List(func.store,    op1.rj,     op2.si12,       "b00001".U(5.W)),
        inst.ld_bu       -> List(func.load,     op1.rj,     op2.si12,       "b10000".U(5.W)),
        inst.ld_hu       -> List(func.load,     op1.rj,     op2.si12,       "b10001".U(5.W)),
        inst.csrrd       -> List(func.csrrd,    op1.noth,   op2.noth,       "b10011".U(5.W)),
        inst.csrwr       -> List(func.csrwr,    op1.noth,   op2.noth,       "b11011".U(5.W)),
        inst.csrxchg     -> List(func.csrxchg,  op1.noth,   op2.noth,       "b11011".U(5.W)),
        inst.syscall     -> List(func.syscall,  op1.noth,   op2.noth,       "b00111".U(5.W)),
        inst.ertn        -> List(func.ertn,     op1.noth,   op2.noth,       "b00001".U(5.W)),
        inst.break       -> List(func.break,    op1.noth,   op2.noth,       "b00111".U(5.W)),
        inst.rdcntid     -> List(func.add,      op1.noth,   op2.rdcntid,    "b10011".U(5.W)),
        inst.rdcntvlw    -> List(func.add,      op1.noth,   op2.counterl,   "b10011".U(5.W)),
        inst.rdcntvhw    -> List(func.add,      op1.noth,   op2.counterh,   "b10011".U(5.W))
    ))
    val funct :: op1_tp :: op2_tp :: w_tp :: Nil = Decode

    val op1_v = MuxCase(0.U(data_len.W), Seq(
        (op1_tp === op1.rj) -> rj_data,
        (op1_tp === op1.pc) -> ds_bus.pc
    ))
    val op2_v = MuxCase(0.U(data_len.W), Seq(
        (op2_tp === op2.rk)       -> rk_data,
        (op2_tp === op2.ui5)      -> ui5,
        (op2_tp === op2.si12)     -> si12,
        (op2_tp === op2.si12u)    -> si12u,
        (op2_tp === op2.si20)     -> si20,
        (op2_tp === op2.of16)     -> of16,
        (op2_tp === op2.of26)     -> of26,
        (op2_tp === op2.rdcntid)  -> io.csr.tid,
        (op2_tp === op2.counterh) -> stable_counter(63, 32),
        (op2_tp === op2.counterl) -> stable_counter(31, 0)       
        
    ))

    io.yuki         := io.rain
    io.ds_allowin   := ds_allowin
    val snow         = RegNext(io.csr.snow && !(io.to_es_valid && io.es_allowin))

    io.to_es_valid  := ds_valid && ds_ready && !io.rain
    io.to_es.pc     := ds_bus.pc
    io.to_es.funct  := funct
    io.to_es.op1    := op1_v
    io.to_es.op2    := op2_v
    io.to_es.src1   := rj_data
    io.to_es.src2   := rk_data
    io.to_es.src3   := rd_data
    io.to_es.w_tp   := w_tp
    io.to_es.dest   := MuxCase(rd, Seq(
        (funct === func.bl)      -> 1.U(gpr_addr.W),
        (op2_tp === op2.rdcntid) -> rj
    ))
    io.to_es.evalid := ds_valid && (ds_bus.evalid || w_tp === "b00111".U || snow || io.csr.snow)
    io.to_es.ecode  := MuxCase(ECODE.NONE, Seq(
        (io.csr.snow || snow)    -> ECODE.INT,
        (ds_bus.evalid)          -> ds_bus.ecode,
        (funct === func.noth)    -> ECODE.INE,
        (funct === func.syscall) -> ECODE.SYS,
        (funct === func.break)   -> ECODE.BRK
    ))
    io.to_es.csrnum := ds_bus.inst(23, 10)
}