public enum HeapTag {

    // basic value (an int literal)
    B,
    // a closure (for lazy evaluation), global vector + program counter
    C,

    // a function, program counter + arguments applied (already) + global vector (not part of the function parameters)
    F,

    // a global Vector
    V
}
