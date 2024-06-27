public class VirtualMachine {

    int programCounter;
    int stackPointer;
    int framePointer;

    int[] stack;
    Instr[] code;

    int maxStackSize = 1000;
    int maxCodeSize = 1000;

    public VirtualMachine(){
        stack = new int[maxStackSize];
        code = new Instr[maxCodeSize];


    }
}
