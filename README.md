# Proteus-Lang


## Installation
## Operators
### Arithmetic operators
#### Plus
The plus operator is used to add two numbers together.
Examples:
```proteus
1 + 1
```
This will return 2.
```proteus
1 + 2 + 3
```
This will return 6.
#### Minus
The minus operator is used to subtract two numbers.
Examples:
```proteus
1 - 1
```
This will return 0.
```proteus
1 - 2 - 3
```
This will return -4.
#### Multiply
The multiply operator is used to multiply two numbers.
Examples:
```proteus
1 * 1
```
This will return 1.
```proteus
1 * 2 * 3
```
This will return 6.
#### Divide
The divide operator is used to divide two numbers.
Examples:
```proteus
1 / 1
```
This will return 1.
```proteus
100 / 10 / 2
```
This will return 5.
#### Power
The power operator is used to raise a number to a power.
Examples:
```proteus
2 ^^ 2
```
This will return 4.
```proteus
2 ^^ 3
```
This will return 8.
#### Chaining of arithmetic operators
Multiple different arithmetic operators can be chained together.
Examples:
```proteus
1 + 1 * 2 ^^ 2
```
This will return 5.
```proteus
1 + 1 * 2 ^^ 2 / 2
```
This will return 3.
### Advanced arithmetic operators
#### Left shift
The left shift operator is used to shift a number to the left.
Examples:
```proteus
1 << 1
```
This will return 2.
```proteus
1 << 2
```
This will return 4.
#### Right shift
The right shift operator is used to shift a number to the right.
Examples:
```proteus
1 >> 1
```
This will return 0.
```proteus
1 >> 2
```
This will return 0.
### Comparison operators
#### Equals
The equals operator is used to check if two values are equal.
Examples:
```proteus
1 == 1
```
This will return true.
```proteus
1 == 2
```
This will return false.
Note: The equals operator can only be used to compare two values of the same type.
#### Not equals
The not equals operator is used to check if two values are not equal.
Examples:
```proteus
1 != 1
```
This will return false.
```proteus
1 != 2
```
This will return true.
Note: The not equals operator can only be used to compare two values of the same type.
#### Greater than
The greater than operator is used to check if a value is greater than another value.
Examples:
```proteus
1 > 1
```
This will return false.
```proteus
1 > 2
```
This will return false.
```proteus
2 > 1
```
This will return true.
#### Less than
The less than operator is used to check if a value is less than another value.
Examples:
```proteus
1 < 1
```
This will return false.
```proteus
1 < 2
```
This will return true.
```proteus
2 < 1
```
This will return false.
#### Greater than or equal to
The greater than or equal to operator is used to check if a value is greater than or equal to another value.
Examples:
```proteus
1 >= 1
```
This will return true.
```proteus
1 >= 2
```
This will return false.
```proteus
2 >= 1
```
This will return true.
#### Less than or equal to
The less than or equal to operator is used to check if a value is less than or equal to another value.
Examples:
```proteus
1 <= 1
```
This will return true.
```proteus
1 <= 2
```
This will return true.
```proteus
2 <= 1
```
This will return false.
### Logical operators
#### And
The and operator is used to check if two values are true.
Examples:
```proteus
true and true
```
This will return true.
```proteus
true and false
```
This will return false.
```proteus
false and true
```
This will return false.
```proteus
false and false
```
This will return false.
#### Or
The or operator is used to check if one of two values is true.
Examples:
```proteus
true or true
```
This will return true.
```proteus
true or false
```
#### Not
The not operator is used to check if a value is false.
Examples:
```proteus
not true
```
This will return false.
```proteus
not false
```
#### Xor
The xor operator is used to check if one of two values is true, but not both.
Examples:
```proteus
true xor true
```
This will return false.
```proteus
true xor false
```
### Bitwise operators
#### Bitwise and
The bitwise and operator performs a bitwise and operation on two numbers.
Examples:
```proteus
1 & 1
```
This will return 1.
```proteus
1 & 2
```
This will return 0.
#### Bitwise or
The bitwise or operator performs a bitwise or operation on two numbers.
Examples:
```proteus
1 | 1
```
This will return 1.
```proteus
1 | 2
```
This will return 3.
#### Bitwise xor
The bitwise xor operator performs a bitwise xor operation on two numbers.
Examples:
```proteus
1 ^ 1
```
This will return 0.
```proteus
1 ^ 2
```
This will return 3.
### Type operators
#### Type of
The type of operator is used to get the type of a value.
Examples:
```proteus
typeof 1
```
This will return `Int`.
```proteus
typeof "Hello, world!"
```
This will return `String`.
#### Is
The is operator is used to check if a value is of a certain type.
Examples:
```proteus
1 is Int
```
This will return true.
```proteus
1 is String
```
This will return false.
## Variables
#### Assigning a value to a variable
The equals operator is used to assign a value to a variable.
Examples:
```proteus
a = 1
```
This will assign 1 to the variable a.
```proteus
a = 1 + 1
```
This will assign 2 to the variable a.
#### Reading a variable
A variable can be read by using its name.
Examples:
```proteus
a = 1
a
```
This will return 1.
```proteus
a = 1 + 1
a
```
This will return 2.
## Data types
### Int
A 32-bit signed integer.
Range: 2^32 -1 to -2^32
### Boolean
A 8-bit boolean value.
Range: true or false
## Parentheses
## Precedence
