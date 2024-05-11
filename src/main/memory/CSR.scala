package mycpu

import chisel3._
import chisel3.util._
import common._
import common.Consts._

//CRMD、PRMD、ESTAT、ERA、EENTRY、SAVE0~3

class CSR extends Module {
    val io = IO(new Bundle {
        val csr = Flipped(new CSR_IO())
        val flush_pc = Output(Bool())
        val next_pc  = Output(UInt(LENX.W))
    })
    val regfile = RegInit(VecInit(
        8.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W), 0.U(LENX.W)
    ))
    def get_vaddr(addr: UInt): UInt = {
        MuxCase(CSR_BADVADDR.U(CSR_SIZE_LOG.W), Seq(
            (addr === CRMD_x)  -> CRMD,
            (addr === PRMD_x)  -> PRMD,
            (addr === ESTAT_x) -> ESTAT,
            (addr === ERA_x)   -> ERA,
            (addr === EENTRY_x)-> EENTRY,
            (addr === SAVE0_x) -> SAVE0,
            (addr === SAVE1_x) -> SAVE1,
            (addr === SAVE2_x) -> SAVE2,
            (addr === SAVE3_x) -> SAVE3
        ))
    }
    val r_vaddr = get_vaddr(io.csr.raddr)
    val w_vaddr = get_vaddr(io.csr.waddr)
    io.csr.rdata := Mux(r_vaddr === CSR_BADVADDR.U, 0.U(32.W), regfile(r_vaddr))
    io.flush_pc         := io.csr.excp =/= 0.U
    io.next_pc          := Mux(io.csr.excp === 1.U, regfile(EENTRY), regfile(ERA))
    when (io.csr.excp === 1.U) {
        regfile(CRMD) := Cat(regfile(CRMD)(31, 3), 0.U(3.W))
        regfile(PRMD) := Cat(regfile(PRMD)(31, 3), regfile(CRMD)(2, 0))
        regfile(ERA)  := io.csr.pc
        regfile(ESTAT):= Cat(regfile(ESTAT)(31), io.csr.Esubcode, io.csr.Ecode, regfile(ESTAT)(15, 0))
    }.elsewhen (io.csr.excp === 2.U) {
        when (regfile(ESTAT)(21, 16) === "h3F".U) {
            regfile(CRMD) := Cat(regfile(CRMD)(31, 6), "b101".U(3.W), regfile(PRMD)(2, 0))
        }.otherwise {
            regfile(CRMD) := Cat(regfile(CRMD)(31, 3), regfile(PRMD)(2, 0))
        }
    }.otherwise {
        val __T = (~io.csr.mask & regfile(w_vaddr)) | (io.csr.mask & io.csr.wdata)
        val Temp = Mux(io.csr.usemask, __T, io.csr.wdata)
        val CRMD_PG = Cat(0.U(23.W), "b0101".U(4.W), io.csr.wdata(4, 0))
        when (io.csr.wen) {
            when (w_vaddr === CRMD && io.csr.wdata(4) === 1.U) {
                regfile(CRMD) := CRMD_PG
            }.otherwise {
                regfile(w_vaddr) := Mux(w_vaddr === CSR_BADVADDR.U, 0.U(32.W), Temp)
            }
        }
    }
}