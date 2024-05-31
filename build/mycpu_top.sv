module mycpu_top(
    input  wire        resetn,
    input  wire        clk,

    output wire        inst_sram_req    ,
    output wire        inst_sram_wr     ,
    output wire [1 :0] inst_sram_size   ,
    output wire [3 :0] inst_sram_wstrb  ,
    output wire [31:0] inst_sram_addr   ,
    output wire [31:0] inst_sram_wdata  ,
    input  wire        inst_sram_addr_ok,
    input  wire        inst_sram_data_ok,
    input  wire [31:0] inst_sram_rdata  ,

    output wire        data_sram_req    ,
    output wire        data_sram_wr     ,
    output wire [1 :0] data_sram_size   ,
    output wire [3 :0] data_sram_wstrb  ,
    output wire [31:0] data_sram_addr   ,
    output wire [31:0] data_sram_wdata  ,
    input  wire        data_sram_addr_ok,
    input  wire        data_sram_data_ok,
    input  wire [31:0] data_sram_rdata  ,

    output wire [31:0] debug_wb_pc      ,
    output wire [ 3:0] debug_wb_rf_we  ,
    output wire [ 4:0] debug_wb_rf_wnum ,
    output wire [31:0] debug_wb_rf_wdata

);
widsnoy_cpu mycpu(
    .clock                      (clk),
    .reset                      (~resetn),
    .io_inst_ram_pf_req         (inst_sram_req),
    .io_inst_ram_pf_wr          (inst_sram_wr) ,
    .io_inst_ram_pf_size        (inst_sram_size),
    .io_inst_ram_pf_addr        (inst_sram_addr),
    .io_inst_ram_pf_addr_ok     (inst_sram_addr_ok),
    .io_inst_ram_fs_data_ok     (inst_sram_data_ok),
    .io_inst_ram_fs_rdata       (inst_sram_rdata),
    .io_data_ram_es_req         (data_sram_req),
    .io_data_ram_es_wr          (data_sram_wr),
    .io_data_ram_es_size        (data_sram_size),
    .io_data_ram_es_addr        (data_sram_addr),
    .io_data_ram_es_wstrb       (data_sram_wstrb),
    .io_data_ram_es_wdata       (data_sram_wdata),
    .io_data_ram_es_addr_ok     (data_sram_addr_ok),
    .io_data_ram_ms_data_ok     (data_sram_data_ok),
    .io_data_ram_ms_rdata       (data_sram_rdata),
    .io_debug_wb_pc             (debug_wb_pc),
    .io_debug_wb_rf_wen         (debug_wb_rf_we),
    .io_debug_wb_rf_wnum        (debug_wb_rf_wnum),
    .io_debug_wb_rf_wdata       (debug_wb_rf_wdata)
);
endmodule