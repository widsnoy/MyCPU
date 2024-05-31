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
        val es_allowin  = Input(Bool())
        val rain        = Input(Bool())

        // regfile
        val reg         = Flipped(new ioport.ds_reg())

        // bypass

    })

    val ds_valid    = RegInit(false.B)
    val ds_ready    = 
    val ds_allowin  = !ds_valid || (ds_ready && io.es_allowin)
    val ds_bus      = RegInit(0.U.asTypeOf(new ioport.to_ds_bus()))

    when (io.rain) {
        ds_valid    := false.B
    }.elsewhen (ds_allowin) {
        ds_valid    := io.fr_fs_valid
        ds_bus      := io.fr_fs
    }

    def get_reg(dest: UInt, ini: UInt): UInt = {
        return ini
        // return MuxCase(ini, Seq(
        //     (io.wr_EX.valid && (io.wr_EX.dest === dest)) -> io.wr_EX.wdata,
        //     (io.wr_MEM.valid && (io.wr_MEM.dest === dest)) -> io.wr_MEM.wdata,
        //     (io.wr_WB.valid && (io.wr_WB.dest === dest)) -> io.wr_WB.wdata
        // ))
    }
    val rd      = inst(4, 0)
    val rj      = inst(9, 5)
    val rk      = inst(14, 10)
    io.reg.r1   := rj
    io.reg.r2   := rk
    io.reg.r3   := rd
    val rj_data  = get_reg(rj, io.reg.rd1)
    val rk_data  = get_reg(rk, io.reg.rd2)
    val rd_data  = get_reg(rd, io.reg.rd3)

    val ui5     = ds_bus.inst(14, 10)
    val si12    = Cat(Fill(20, ds_bus.inst(21)), ds_bus.inst(21, 10))
    val of16    = Cat(Fill(14, ds_bus.inst(25)), ds_bus.inst(25, 10), 0.U(2.W))
    val of26    = Cat(Fill(4, ds_bus.inst(9)), Cat(ds_bus.inst(9, 0), ds_bus.inst(25, 10)), 0.U(2.W))
    val si20    = Cat(ds_bus.inst(24, 5), 0.U(12.W))
    val si12u   = Cat(0.U(20.W), ds_bus.inst(21, 10)) 
    val csrnum  = ds_bus.inst(23, 10)

    /*
    high -> low
    0      0      0    0
    mem  regfile  csr  0sign / 1unsign
    0 0   4byte
    1 0   2byte
    0 1   1byte
    */

    val Decode = ListLookup(ds_bus.inst, List(func.null, op1.null, op2.null, "b000000".U(6.W)), Array (     
        inst.add_w       -> List(func.add,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.sub_w       -> List(func.sub,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.slt         -> List(func.slt,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.sltu        -> List(func.sltu,     op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.slti        -> List(func.slt,      op1.rj,     op2.si12,       "b010000".U(6.W)),
        inst.sltui       -> List(func.sltu,     op1.rj,     op2.si12,       "b010000".U(6.W)),
        inst.nor         -> List(func.nor,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.and         -> List(func.and,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.or          -> List(func.or,       op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.xor         -> List(func.xor,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.andi        -> List(func.and,      op1.rj,     op2.si12u,      "b010000".U(6.W)),
        inst.ori         -> List(func.or,       op1.rj,     op2.si12u,      "b010000".U(6.W)),
        inst.xori        -> List(func.xor,      op1.rj,     op2.si12u,      "b010000".U(6.W)),
        inst.slli_w      -> List(func.sll,      op1.rj,     op2.ui5,        "b010000".U(6.W)),
        inst.srli_w      -> List(func.srl,      op1.rj,     op2.ui5,        "b010000".U(6.W)),
        inst.sra_w       -> List(func.sra,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.sll_w       -> List(func.sll,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.srl_w       -> List(func.srl,      op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.srai_w      -> List(func.sra,      op1.rj,     op2.ui5,        "b010000".U(6.W)),
        inst.addi_w      -> List(func.add,      op1.rj,     op2.si12,       "b010000".U(6.W)),
        inst.ld_w        -> List(func.load,     op1.rj,     op2.si12,       "b010000".U(6.W)),
        inst.st_w        -> List(func.store,    op1.rj,     op2.si12,       "b100000".U(6.W)),
        inst.jirl        -> List(func.jirl,     op1.rj,     op2.of16,       "b010000".U(6.W)),
        inst.b           -> List(func.b,        op1.pc,     op2.of26,       "b000000".U(6.W)),
        inst.bl          -> List(func.bl,       op1.pc,     op2.of26,       "b010000".U(6.W)),
        inst.beq         -> List(func.beq,      op1.pc,     op2.of16,       "b000000".U(6.W)),
        inst.bne         -> List(func.bne,      op1.pc,     op2.of16,       "b000000".U(6.W)),
        inst.lu12i_w     -> List(func.lu12i,    op1.null,   op2.si20,       "b010000".U(6.W)),
        inst.pcaddu12i   -> List(func.add,      op1.pc,     op2.si20,       "b010000".U(6.W)),
        inst.mul_w       -> List(func.mull,     op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.mulh_w      -> List(func.mulh,     op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.mulh_wu     -> List(func.mulhu,    op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.div_w       -> List(func.div_sig,  op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.div_wu      -> List(func.div_uns,  op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.mod_w       -> List(func.mod_sig,  op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.mod_wu      -> List(func.mod_uns,  op1.rj,     op2.rk,         "b010000".U(6.W)),
        inst.blt         -> List(func.blt,      op1.pc,     op2.of16,       "b000000".U(6.W)),
        inst.bge         -> List(func.bge,      op1.pc,     op2.of16,       "b000000".U(6.W)),
        inst.bltu        -> List(func.bltu,     op1.pc,     op2.of16,       "b000000".U(6.W)),
        inst.bgeu        -> List(func.bgeu,     op1.pc,     op2.of16,       "b000000".U(6.W)),
        inst.ld_b        -> List(func.load,     op1.rj,     op2.si12,       "b010001".U(6.W)),
        inst.ld_h        -> List(func.load,     op1.rj,     op2.si12,       "b010010".U(6.W)),
        inst.st_b        -> List(func.store,    op1.rj,     op2.si12,       "b100001".U(6.W)),
        inst.st_h        -> List(func.store,    op1.rj,     op2.si12,       "b100010".U(6.W)),
        inst.ld_bu       -> List(func.load,     op1.rj,     op2.si12,       "b010101".U(6.W)),
        inst.ld_hu       -> List(func.load,     op1.rj,     op2.si12,       "b010110".U(6.W)),
        inst.csrrd       -> List(func.csrrd,    op1.null,   op2.null,       "b010000".U(6.W)),
        inst.csrwr       -> List(func.csrwr,    op1.null,   op2.null,       "b011000".U(6.W)),
        inst.csrxchg     -> List(func.csrxchg,  op1.null,   op2.null,       "b011000".U(6.W)),
        inst.syscall     -> List(func.syscall,  op1.null,   op2.null,       "b000000".U(6.W)),
        inst.ertn        -> List(func.ertn,     op1.null,   op2.null,       "b000000".U(6.W)),
        inst.break       -> List(func.break,    op1.null,   op2.null,       "b000000".U(6.W)),
        inst.rdcntid     -> List(func.add,      op1.null,   op2.rdcntid,    "b010000".U(6.W)),
        inst.rdcntvlw    -> List(func.add,      op1.null,   op2.counterl,   "b010000".U(6.W)),
        inst.rdcntvhw    -> List(func.add,      op1.null,   op2.counterh,   "b010000".U(6.W))
    ))
    val funct :: op1_tp :: op2_tp :: w_tp :: Nil = Decode

    val src1 = MuxCase(0.U(data_len.W), Seq(
        (op1_tp === op1.rj) -> rj_data    ,
        (op1_tp === op1.pc) -> ds_bus.pc
    ))
    val src2 = MuxCase(0.U(data_len.W), Seq(
        (op2_tp === op2.rk)     -> rk_data,
        (op2_tp === op2.ui5)    -> ui5    ,
        (op2_tp === op2.si12)   -> si12   ,
        (op2_tp === op2.si12u)  -> si12u  ,
        (op2_tp === op2.si20)   -> si20   ,
        (op2_tp === op2.of16)   -> of16   ,
        (op2_tp === op2.of26)   -> of26   ,
        (op2_tp === op2.csrnum) -> csrnum
        //(op2_tp === op2.rdcntid)
        //
        //
    ))
    val src3 = rd_data

    io.yuki         := io.rain
    io.ds_allowin   := ds_allowin

    io.to_es_valid  := ds_valid && ds_ready && !io.rain
    io.to_es.pc     := ds_bus.pc
    io.to_es.funct  := funct
    io.to_es.src1   := src1
    io.to_es.src2   := src2
    io.to_es.src3   := src3
    io.to_es.w_tp   := w_tp
    io.to_es.dest   := MuxCase(rd, Seq(
        (funct === func.bl)      -> 1.U(reg_addr.W),
        (op2_tp === op2.rdcntid) ->  rj(reg_addr.W)
    ))
}