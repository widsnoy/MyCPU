## 4.16
目前实现了一个单周期 CPU, 支持 CPU 实战 Loongarch32 版实验 6 的 20 条指令，并且通过了 trace 测试。  
遇到了一个奇怪的问题  
如果我在 EXE 阶段计算某个值，就会出现计算结果错误的问题  
```scala
(exe_fun === BR_BEQ)   -> (op1_data + Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W)))
```
但是如果我在 IF 阶段就把这个值算出来，EXE 阶段直接用就没有问题了。
```scala
val of16_sex = Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W)) // IF Stage
(exe_fun === BR_BEQ)   -> (op1_data + op2_data)                // EX Stage
```