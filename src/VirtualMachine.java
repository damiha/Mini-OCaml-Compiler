public class VirtualMachine {

    int programCounter;
    int stackPointer;
    int framePointer;
    int globalPointer;

    int[] stack;
    Instr[] code;

    // for debugging purposes (so we always no which element should be
    // read as a value and which one should be read as an address
    StackType[] stackTypes;

    int maxStackSize = 1000;
    int maxCodeSize = 1000;

    Heap heap;

    boolean isRunning;

    boolean printDebug = true;

    boolean showHeapContent = true;

    Instr instructionRegister;

    public VirtualMachine(){
        stack = new int[maxStackSize];
        stackTypes = new StackType[maxStackSize];
        code = new Instr[maxCodeSize];

        heap = new Heap();
    }

    public void loadCode(Code code){
        // can't harm if halt instruction is at the end
        code.addInstruction(new Instr.Halt(), -1);

        for(int i = 0; i < code.instructions.size(); i++){
            this.code[i] = code.instructions.get(i);
        }

        instructionRegister = null;
        programCounter = 0;
        stackPointer = 0;
        globalPointer = heap.insert(new HeapElement.Vector());

        framePointer = -1;

        isRunning = true;
    }

    private int getValueInGlobalVector(int addressInGlobalVector){
        HeapElement.Vector globalVector = (HeapElement.Vector) heap.get(globalPointer);
        return globalVector.get(addressInGlobalVector);
    }

    // a functional program is just one big expression since nothing really has any state
    public int run(Code code){

        loadCode(code);

        while(isRunning){

            Instr instruction = this.code[programCounter++];
            instructionRegister = instruction;

            if(printDebug){
                System.out.println(this);
            }

            if(instruction instanceof Instr.Halt){
                isRunning = false;
            }
            else if(instruction instanceof Instr.LoadC){
                stack[++stackPointer] = ((Instr.LoadC) instruction).constant;
            }
            else if(instruction instanceof Instr.GetBasic){
                int heapAddress = stack[stackPointer];
                HeapElement atAddress = heap.get(heapAddress);

                if(atAddress instanceof HeapElement.BasicValue){
                    stack[stackPointer] = ((HeapElement.BasicValue) atAddress).value;
                }
                else{
                    throw new RuntimeException("Not a basic value.");
                }
            }
            else if(instruction instanceof Instr.MakeBasic){

                HeapElement basicValue = new HeapElement.BasicValue(stack[stackPointer]);
                int heapAddress = heap.insert(basicValue);
                stack[stackPointer] = heapAddress;
            }
            else if(instruction instanceof Instr.PushLoc){
                int relativeAddress = ((Instr.PushLoc) instruction).relativeAddress;

                stack[stackPointer + 1] = stack[stackPointer - relativeAddress];
                stackPointer++;
            }
            else if(instruction instanceof Instr.PushGlob){
                int addressInGlobalVector = ((Instr.PushGlob) instruction).addressInGlobalVector;
                stack[++stackPointer] = getValueInGlobalVector(addressInGlobalVector);
            }

        }

        return -1;
    }

    private String showRegisters(){

        String programCounterString = String.format("%d (%s)", programCounter, instructionRegister == null ? "null" : instructionRegister.toString());
        return String.format("PC: %s", programCounterString);
    }

    private String showStackContent(){
        return "";
    }

    private String showHeapContent(){

        if(!showHeapContent){
            return "???";
        }
        return heap.toString();
    }

    // important for debugging
    public String toString(){
        return String.format(
                """
                REGISTERS:
                %s
                STACK:
                %s
                HEAP:
                %s
                ---""", showRegisters(), showStackContent(), showHeapContent());
    }
}
