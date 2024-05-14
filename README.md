## 4 月 16 日
目前实现了一个单周期 CPU, 支持 CPU 实战 Loongarch32 精简版实验 6 的 20 条指令，并且通过了 trace 测试。  
遇到了一个奇怪的问题  
如果我在 EXE 阶段计算某个值，就会出现计算结果错误的问题  
```scala
// IF 阶段得到 op2_sel
val op2_data = MuxCase(0.U(32.W), Seq(
    (op2_sel === OP2_OF16) -> i16
))
(exe_fun === BR_BEQ)   -> (op1_data + Cat(Fill(14, op2_data(15)), op2_data, 0.U(2.W)))
```
但是如果我在 IF 阶段就把这个值算出来，EXE 阶段直接用就没有问题了。
```scala
val of16_sex = Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W)) // IF Stage
(exe_fun === BR_BEQ)   -> (op1_data + op2_data)                // EX Stage
```

在学长的帮助下解决了这个问题。原因是 val op2_data = MuxCase 那里会自动推断无符号扩展成 32 位的数，我把它当成 16 位 Cat 就会出问题。


## 4 月 19 日
完成了不考虑相关冲突的流水线，并通过了测试。  
感觉和单周期的区别就是加上了缓存和一些控制信号。

## 4 月 22 日
做了实验 8 之后，被 lwt 拉去搞 soc_simulator，搞了一晚上 trace 都进不去... 暂时先不管模拟器了

## 4 月 23 日
感觉前递解决冲突比阻塞好写多了，速度还快些。当然我是不管源寄存器是否有用都等着拿写数据，如果 ld.w 比较多可能会慢一些。但是特判又会增加代码复杂度，目前就先不管了。目前测试下来比生成 golden_trace 的那个 cpu 慢一些，比阻塞的快接近一倍。优化等代码基本完成再说吧，现在也不知道是不是关键路径。

## 4 月 28 日
1. 感谢学长指出我代码的问题，优化了 ALU, 提前计算答案避免综合出多个 ALU。  
2. 将一字节变量的位运算改为 Bool 的逻辑运算，增强代码可读性。   
3. 参考 pua_mips 用 ip 核实现乘除法完成了 lab10。

## 4 月 30 日
昨天对着 confreg.v 改了下模拟器 confreg，终于可以用了。  
发现之前的代码判断 to_fs_valid 为 1 的时机是有问题的，应该是 reset 置 0 的下一拍，这时候 inst_sram 才能读出数据，将 to_fs_valid 设为 reg 就可以解决。  
完成了 lab11, 比较坑的一点是 ld.b, st.b 之类地址可以是非对齐的，最开始没理解调试了很久。

## 5 月 11 日
哎，感觉得趁还记得抽空注释一下  
记录一下完成 exp12 遇到的问题：  
1. 处理例外结束后，IF 实际上是第二拍才 valid 为 1, 也就是说需要用寄存器将 CSR 的 flush_pc 保存一拍  
2. A 是一个两位无符号数，B 是一位无符号数。A & B 期望补符号位，实际上补零，没有注意位宽不同调试了很久  

## 5 月 14 日
为CPU增加取指地址错（ADEF）、地址非对齐（ALE）、断点（BRK）和指令不存在（INE）异常的支持。  
为CPU增加中断的支持，包括2个软件中断、8个硬件中断和定时器中断。  
为CPU增加控制状态寄存器ECFG、BADV、TID、TCFG、TVAL、TICLR。  

emmm，CSR 超 lwt 的。感觉比我的数组模拟不知道高到哪里去了。  
书上都不提要加 stable_counter 相关指令的，调了半天在 test.s 里面发现一个 RDTIMEL.W 才知道。导致 INE 了，产生了很奇怪的问题...    

另外现在没有 ADEM 支持，不知道该怎么判断。  
debug 的时候得到一个经验就是模拟器不支持 x 和 z 状态，如果取值一个不存在的地址，得到 x 模拟器会当作 0 处理。而 vivado 得到的 x 会导致译码时候出现死循环。