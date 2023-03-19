# Proteus Virtual Machine

This is the official proteus virtual machine.

## Examples
```

alloc 0x02
load 0x00
push 0x01
iadd
store 0x01

```

This example allocates eight bytes of memory, loads the value at address 0x00, pushes the value 0x01 onto the stack, adds the two values, and stores the result at address 0x01.

## Instruction Set

### Memory

| Instruction | Operands  | Description                                             | Example      |
|-------------|-----------|---------------------------------------------------------|--------------|
| `load`      | `address` | Loads a value from memory and pushes it onto the stack. | `load 0x00`  |
| `store`     | `address` | Pops a value off the stack and stores it in memory.     | `store 0x00` |
| `push`      | `value`   | Pushes a value onto the stack.                          | `push 0x00`  |
| `pop`       |           | Pops a value off the stack.                             | `pop`        |
### Arithmetic

| Instruction | Operands | Description                                                              | Example |
|-------------|----------|--------------------------------------------------------------------------|---------|
| `iadd`      |          | Pops two values off the stack, adds them, and pushes the result.         | `iadd`  |
| `isub`      |          | Pops two values off the stack, subtracts them, and pushes the result.    | `isub`  |
| `imul`      |          | Pops two values off the stack, multiplies them, and pushes the result.   | `imul`  |
| `idiv`      |          | Pops two values off the stack, divides them, and pushes the result.      | `idiv`  |
| `imod`      |          | Pops two values off the stack, divides them, and pushes the remainder.   | `imod`  |
| `radd`      |          | Pops two pointers off the stack, adds them, and pushes the result.       | `radd`  |
| `rsub`      |          | Pops two pointers off the stack, subtracts them, and pushes the result.  | `rsub`  |
| `rmul`      |          | Pops two pointers off the stack, multiplies them, and pushes the result. | `rmul`  |
| `rdiv`      |          | Pops two pointers off the stack, divides them, and pushes the result.    | `rdiv`  |
| `rmod`      |          | Pops two pointers off the stack, divides them, and pushes the remainder. | `rmod`  |

### Logic

| Instruction | Operands | Description                                                                     | Example |
|-------------|----------|---------------------------------------------------------------------------------|---------|
| `iand`      |          | Pops two values off the stack, performs a bitwise AND, and pushes the result.   | `iand`  |
| `ior`       |          | Pops two values off the stack, performs a bitwise OR, and pushes the result.    | `ior`   |
| `ixor`      |          | Pops two values off the stack, performs a bitwise XOR, and pushes the result.   | `ixor`  |
| `inot`      |          | Pops a value off the stack, performs a bitwise NOT, and pushes the result.      | `inot`  |
| `rand`      |          | Pops two pointers off the stack, performs a bitwise AND, and pushes the result. | `rand`  |
| `ror`       |          | Pops two pointers off the stack, performs a bitwise OR, and pushes the result.  | `ror`   |
| `rxor`      |          | Pops two pointers off the stack, performs a bitwise XOR, and pushes the result. | `rxor`  |
| `rnot`      |          | Pops a pointer off the stack, performs a bitwise NOT, and pushes the result.    | `rnot`  |

### Comparison

| Instruction | Operands | Description                                                            | Example |
|-------------|----------|------------------------------------------------------------------------|---------|
| `ieq`       |          | Pops two values off the stack, compares them, and pushes the result.   | `ieq`   |
| `ine`       |          | Pops two values off the stack, compares them, and pushes the result.   | `ine`   |
| `ilt`       |          | Pops two values off the stack, compares them, and pushes the result.   | `ilt`   |
| `ile`       |          | Pops two values off the stack, compares them, and pushes the result.   | `ile`   |
| `igt`       |          | Pops two values off the stack, compares them, and pushes the result.   | `igt`   |
| `ige`       |          | Pops two values off the stack, compares them, and pushes the result.   | `ige`   |
| `req`       |          | Pops two pointers off the stack, compares them, and pushes the result. | `req`   |
| `rne`       |          | Pops two pointers off the stack, compares them, and pushes the result. | `rne`   |
| `rlt`       |          | Pops two pointers off the stack, compares them, and pushes the result. | `rlt`   |
| `rle`       |          | Pops two pointers off the stack, compares them, and pushes the result. | `rle`   |
| `rgt`       |          | Pops two pointers off the stack, compares them, and pushes the result. | `rgt`   |
| `rge`       |          | Pops two pointers off the stack, compares them, and pushes the result. | `rge`   |

### Control Flow

| Instruction | Operands  | Description                                                               | Example     |
|-------------|-----------|---------------------------------------------------------------------------|-------------|
| `jmp`       | `address` | Jumps to an address.                                                      | `jmp 0x00`  |
| `jz`        | `address` | Pops a value off the stack, jumps to an address if the value is zero.     | `jz 0x00`   |
| `jnz`       | `address` | Pops a value off the stack, jumps to an address if the value is not zero. | `jnz 0x00`  |
| `call`      | `address` | Calls a function.                                                         | `call 0x00` |
| `ret`       |           | Returns from a function.                                                  | `ret`       |

### System

| Instruction | Operands | Description                | Example |
|-------------|----------|----------------------------|---------|
| `halt`      |          | Halts the virtual machine. | `halt`  |













