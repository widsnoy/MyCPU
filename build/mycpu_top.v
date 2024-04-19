module mycpu_top(
    input  wire        clk,
    input  wire        resetn,
    // inst sram interface
    output wire        inst_sram_en,
    output wire [ 3:0] inst_sram_we,
    output wire [31:0] inst_sram_addr,
    output wire [31:0] inst_sram_wdata,
    input  wire [31:0] inst_sram_rdata,
    // data sram interface
    output wire        data_sram_en,
    output wire [ 3:0] data_sram_we,
    output wire [31:0] data_sram_addr,
    output wire [31:0] data_sram_wdata,
    input  wire [31:0] data_sram_rdata,
    // trace debug interface
    output wire [31:0] debug_wb_pc,
    output wire [ 3:0] debug_wb_rf_we,
    output wire [ 4:0] debug_wb_rf_wnum,
    output wire [31:0] debug_wb_rf_wdata
);
widsnoy_cpu mycpu(
    .clock                  (clk),
    .reset                  (~resetn),
    .io_inst_en        (inst_sram_en),
    .io_inst_wen       (inst_sram_we),
    .io_inst_addr      (inst_sram_addr),
    .io_inst_wdata     (inst_sram_wdata),
    .io_inst_rdata     (inst_sram_rdata),
    .io_data_en        (data_sram_en),
    .io_data_wen       (data_sram_we),
    .io_data_addr      (data_sram_addr),
    .io_data_wdata     (data_sram_wdata),
    .io_data_rdata     (data_sram_rdata),
    .io_debug_wb_pc         (debug_wb_pc),
    .io_debug_wb_rf_wen     (debug_wb_rf_we),
    .io_debug_wb_rf_wnum    (debug_wb_rf_wnum),
    .io_debug_wb_rf_wdata   (debug_wb_rf_wdata)
);
endmodule