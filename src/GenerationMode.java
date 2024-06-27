public enum GenerationMode {

    // B puts basic values (actual numbers on the stack)
    B,
    // V puts addresses (into the heap) on the stack (Call-by-Value)
    V,

    // we don't implement the closure stuff
}
