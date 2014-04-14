# ex1.s
# expected output: 10000

        .data
X:      .word 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
Y:      .word 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
i:      .word 0
sum:    .word 0

        .text
        .globl main


main:   
        la $t0, i       # address of i -> $t0
        lw $t0, 0($t0)  # store i in t0
        la $t1, sum     # address of sum -> $t1
        lw $t1, 0($t1)  # store sum in t1

for_head:
        bge $t0, 100, enddo     # exit loop when i >= 100 
        la $t2, X               # store address of X in t2
        move $t3, $t0           # t3 now holds offset
        add $t3, $t3, $t3       # double offset
        add $t3, $t3, $t3       # double offset again (now 4x)
        add $t2, $t2, $t3       # combine base and offset, store in $t2
        lw $t4, 0($t2)          # get the value from X[i] and store in $t4

        la $t2, Y               # store address of Y in t2
        move $t3, $t0           # t3 now holds offset
        add $t3, $t3, $t3       # double offset
        add $t3, $t3, $t3       # double offset again (now 4x)
        add $t2, $t2, $t3       # combine base and offset, store in $t2
        lw $t5, 0($t2)          # get the value from Y[i] and store in $t5

        mul $t6, $t4, $t5       # multiply X[i] * Y[i] and store in $t6
        add $t1, $t1, $t6       # sum + X[i] * Y[i]. store back in $t1
        addi $t0, $t0, 1        # increment i
        b for_head              # branch unconditionally to for_head

enddo:  li $v0, 1       # load syscall 1 (print integer)
        move $a0, $t1   # store sum in argument before call
        syscall         # call print_int
        jr $ra          # return to caller
