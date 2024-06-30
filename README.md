
# Mini OCaml Compiler

![Alt text](img/mini_ocaml.webp)

**A Mini OCaml according to GPT4o**

In this project, I implemented
a small functional language (similar to OCaml but with
a bit of C-style syntax like ! instead of OCaml's 'not' mixed in).
The project includes lexer, parser, compiler and a virtual machine
with a custom instruction set.

An expression like:
```
let a = 19 in let b = a * a in a + b
```

gets compiled to:
```
LoadC 19
MakeBasic
PushLoc 0
GetBasic
PushLoc 1
GetBasic
Mul
MakeBasic
PushLoc 1
GetBasic
PushLoc 1
GetBasic
Add
MakeBasic
Slide 2
GetBasic
```

## How to try it out yourself?

```
String source = "YOUR CODE HERE";

Runner runner = new Runner();

System.out.println(runner.getOutput(source));
```

**NOTE:** To inspect the internal state of the virtual machine, you can set its attribute ```printDebug = true```

## What does the language support?

#### Let expressions

```
let a = 19 in let b = a * a in a + b
```

#### Function Definitions And Applications

```
let a = 17 in let f = fun b -> a + b in f 42
```

#### Functions as 'First Class Citizens'

- Partial application

```
let f = fun x y -> x + y in let f2 = f 10 in f2 100
```

- 'Continued application'

```
let f = fun x y -> (fun z -> x + y + z) in f 10 20 30
```

#### Recursion And Mutual Recursion

```
let rec f = fun x y -> 
if y <= 1 then x else f ( x * y ) ( y - 1 )
 
in f 1 3 // calculates 3!
```

```
let rec 
 even = (fun e -> if e == 0 then 1 else odd (e - 1)) and
 odd = (fun o -> if o == 0 then 0 else 1 - even(o - 1)) 
 
in even 77
```

#### Tuples

```
let (a, b, c) = (5, 3, 8) in a + b + c
```

```
let (a, b) = (5, 3) in let (c, d) = (8, 11) in a + b + c + d
```

```
let f = fun p -> (let (x, y) = p in x + y) in f (1, 3)
```

```
let (f1, f2) = (fun x -> x + 1, fun y -> y + 2) in (f1 2)  + (f2 3)
```

```
let f = (fun p -> #2 p) in f (1, 3, 5, 7) // outputs 5
```

```
let f = (fun x -> (1, 3)) in let (a, b) = f 0 in a + b
```

**NOTE:**
recursive unpacking like ```let (a, (b, c)) = (1, (2, 3)) in a + b + c```
is currently not supported

#### Lists (+ Small programs)
```
let (a, b) = (5, 3) in a::b::[]
```

```
let rec concat = (fun l1 l2 -> match l1 with 
  [] -> l2 |
  h::t -> h :: (concat t l2))
in concat (1::2::[]) (3::4::[])
```

```
let rec app = (fun x l -> match l with 
[] -> x::[] |
 h::t -> h::(app x t)) in
 
let rec rev = (fun l -> match l with 
[] -> [] | 
h :: t -> app h (rev t)) in

rev (1::2::3::[])
```

```
let rec insert = (fun x l -> match l with 
[] -> x::[] | 
h :: t -> (if x <= h then x::h::t else h::(insert x t))) in

let rec insort = (fun l -> match l with 
[] -> [] |
h :: t -> insert h (insort t)) in

insort (1::5::3::4::2::[])
```

```
let rec fib = fun n -> 
if n == 0 then 0 else 
if n == 1 then 1 else 
(fib (n-1) + fib (n-2))

in fib 11
```

**NOTE:**
More complicated matching like
```
match l with
[] -> [] |
x::[] -> x::[] |
x::y::[] -> x::[]
```
is currently not supported. We only
allow matching on lists. Unfortunately,
you cannot define your own data types and match
on their structure like: 
```
// t is a custom defined tree datatype

match t with
Leaf -> 0 |
(Node l x r) -> l
```

**CAUTION**:

the compiler doesn't optimize recursive functions, so
you run out of stack fairly quickly. There's no benefit in
writing tail-recursive functions.

