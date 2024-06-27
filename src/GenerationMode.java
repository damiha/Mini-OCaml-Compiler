public enum GenerationMode {

    // B puts basic values (actual numbers on the stack)
    B,
    // V puts addresses (into the heap) on the stack (Call-by-Value)
    V,
    // C puts addresses to closures (that lie in the heap) on the stack (Call-by-Need)
    C
}
