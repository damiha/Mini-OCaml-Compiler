import java.util.Map;
import java.util.Vector;

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

    boolean printDebug = false;

    boolean showHeapContent = true;

    Instr instructionRegister;

    Map<String, Integer> jumpTable;

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

        jumpTable = code.jumpTable;

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
                stackTypes[stackPointer] = StackType.V;
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
                stackTypes[stackPointer] = StackType.V;
            }
            else if(instruction instanceof Instr.MakeBasic){

                HeapElement basicValue = new HeapElement.BasicValue(stack[stackPointer]);
                int heapAddress = heap.insert(basicValue);
                stack[stackPointer] = heapAddress;

                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.MakeVec){

                HeapElement.Vector vector = new HeapElement.Vector();
                int addressToVector = heap.insert(vector);

                // this stack value is at 0
                // we have g, so we should hop g - 1 below?
                int g = ((Instr.MakeVec) instruction).g;

                // in the slides, this gets transformed to -g + 1
                stackPointer = stackPointer - (g - 1);

                for(int i = 0; i < g; i++){
                    vector.values.add(stack[stackPointer + i]);
                }

                stack[stackPointer] = addressToVector;
                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.Alloc){
                int n = ((Instr.Alloc) instruction).n;

                for(int i = 1; i <= n; i++){
                    // adds dummy closures to the heap so they can be filled in later
                    stack[stackPointer + i] = heap.insert(new HeapElement.Closure());
                    stackTypes[stackPointer + i] = StackType.H;
                }

                stackPointer = stackPointer + n;
            }
            else if(instruction instanceof Instr.Rewrite){

                // the heap object whose address is on top of the stack
                // should be inserted n cells down below
                int n = ((Instr.Rewrite) instruction).n;

                int heapAddress = stack[stackPointer];
                HeapElement newHeapElement = heap.get(heapAddress);

                int heapAddressWhereChange = stack[stackPointer - n];

                heap.data.set(heapAddressWhereChange, newHeapElement);

                // consume heap address (which was used to rewrite)
                stackPointer -= 1;
            }
            else if(instruction instanceof Instr.LessThanOrEqual){
                executeBinOp(BinaryOperator.LEQ);
            }
            else if(instruction instanceof Instr.Equal){
                executeBinOp(BinaryOperator.EQUAL);
            }
            else if(instruction instanceof Instr.MakeFunVal){
                // top most value is the global vector
                int globalVectorAddress = stack[stackPointer];
                String jumpLabelFunctionStart = ((Instr.MakeFunVal) instruction).jumpLabel;

                int appliedArgumentsVectorAddress = heap.insert(new HeapElement.Vector());

                HeapElement.Function funVal = new HeapElement.Function(jumpLabelFunctionStart, appliedArgumentsVectorAddress, globalVectorAddress);

                int funValHeapAddress = heap.insert(funVal);

                stack[stackPointer] = funValHeapAddress;
                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.PushLoc){
                int relativeAddress = ((Instr.PushLoc) instruction).relativeAddress;

                stack[stackPointer + 1] = stack[stackPointer - relativeAddress];
                stackPointer++;
                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.PushGlob){
                int addressInGlobalVector = ((Instr.PushGlob) instruction).addressInGlobalVector;
                stack[++stackPointer] = getValueInGlobalVector(addressInGlobalVector);

                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.Jump){

                String jumpLabel = ((Instr.Jump) instruction).jumpLabel;
                programCounter = jumpTable.get(jumpLabel);
            }
            else if(instruction instanceof Instr.JumpZ){
                String jumpLabel = ((Instr.JumpZ) instruction).jumpLabel;

                if(stack[stackPointer] == 0){
                    programCounter = jumpTable.get(jumpLabel);
                }

                // in any case, the conditional value is consumed
                stackPointer--;
            }
            else if(instruction instanceof Instr.Mark){
                // the mark instruction establishes the new stackframe

                // new frame pointer points to PC that we should jump to after return
                // below the old frame pointer
                // below the old global vector
                stack[stackPointer + 1] = globalPointer;
                stackTypes[stackPointer + 1] = StackType.GP;

                stack[stackPointer + 2] = framePointer;
                stackTypes[stackPointer + 2] = StackType.FP;

                String jumpLabelAfterReturn = ((Instr.Mark) instruction).jumpLabel;
                int jumpAddressAfterReturn = jumpTable.get(jumpLabelAfterReturn);

                stack[stackPointer + 3] = jumpAddressAfterReturn;
                stackTypes[stackPointer + 3] = StackType.PC;

                framePointer = stackPointer + 3;
                stackPointer += 3;
            }
            else if(instruction instanceof Instr.Apply){
                Apply();
            }
            else if(instruction instanceof Instr.TestArg){
                // TestArg looks for under suppl
                // if we have too few values, we have to wrap up the function again
                // we can't apply it and return a functional value
                int argumentsApplied = stackPointer - framePointer;

                if(argumentsApplied < ((Instr.TestArg) instruction).k){
                    // under supply
                    MakeVec0();
                    Wrap();
                    PopEnv();
                }
                // otherwise (right amount or over supply) the function just does nothing
                // over supply is handled in return
            }
            else if(instruction instanceof Instr.Slide){
                Slide(((Instr.Slide) instruction).n);
            }
            else if(instruction instanceof Instr.Return){

                // if we reach this point
                // the arguments + 1 for the return value are on the stack
                int argumentsApplied = stackPointer - framePointer - 1;

                // SP - FP - 1 == k
                // SP - FP == (k + 1) (on slide 157)
                if(argumentsApplied == ((Instr.Return) instruction).k){

                    // right number of arguments
                    PopEnv();
                }
                else{
                    // we oversupplied
                    // more arguments than the function takes
                    // the function must have returned a function value on top of the stack (i.e. its heap address)

                    // eliminate used arguments
                    // not argumentsApplied because we don't want to delete everything
                    // just delete the max that the function can take
                    Slide(((Instr.Return) instruction).k);

                    // apply the function again
                    Apply();
                }
            }
            else if(instruction instanceof Instr.Mul){
                executeBinOp(BinaryOperator.MUL);
            }
            else if(instruction instanceof Instr.Nil){
                HeapElement nilElement = new HeapElement.FList();
                stack[++stackPointer] = heap.insert(nilElement);
                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.TList){

                // address to list to match is on top of the stack
                int heapAddress = stack[stackPointer];

                HeapElement element = heap.get(heapAddress);

                if(!(element instanceof HeapElement.FList)){
                    throw new RuntimeException("match expression expects a list");
                }

                HeapElement.FList list = (HeapElement.FList) element;

                if(list.listType == ListType.NIL){
                    // we just consume the address to the Nil list and return
                    stackPointer--;
                }
                else{
                    // Cons case
                    int headAddress = list.heapAddressHead;
                    int tailAddress = list.heapAddressListTail;

                    // we have l, t in the stack
                    // l should be where stack pointer currently is
                    stack[stackPointer] = tailAddress;
                    stack[stackPointer + 1] = headAddress;

                    stackPointer++;

                    // TList also encompasses a jump statement
                    programCounter = jumpTable.get(((Instr.TList) instruction).jumpLabel);
                }
            }
            else if(instruction instanceof Instr.Cons){


                // tail is on top of the stack (look at compiler)
                int heapAddressListTail = stack[stackPointer];
                int heapAddressHead = stack[stackPointer - 1];

                // head is on bottom
                HeapElement consElement = new HeapElement.FList(heapAddressHead, heapAddressListTail, heap);

                stack[--stackPointer] = heap.insert(consElement);
                stackTypes[stackPointer] = StackType.H;
            }
            else if(instruction instanceof Instr.Add){
                executeBinOp(BinaryOperator.PLUS);
            }
            else if(instruction instanceof Instr.GetVec){
                // make vec makes vector and returns heap addres
                // get vec pushes on the values on the stack and consumes the heap address
                int heapAddress = stack[stackPointer];

                HeapElement element = heap.get(heapAddress);

                if(!(element instanceof HeapElement.Vector)){
                    throw new RuntimeException("Not a vector.");
                }

                HeapElement.Vector vec = (HeapElement.Vector) element;

                stackPointer--;

                // overwrite the heap address with the first value
                for(int i = 0; i < ((Instr.GetVec) instruction).k; i++){
                    stack[++stackPointer] = vec.get(i);
                }
            }
            else if(instruction instanceof Instr.Get){
                // the address of the vector lies on the stack
                HeapElement heapElement = heap.get(stack[stackPointer]);

                if(!(heapElement instanceof HeapElement.Vector)){
                    throw new RuntimeException("Get must index into a vector");
                }

                HeapElement.Vector vec = (HeapElement.Vector) heapElement;

                int j = ((Instr.Get) instruction).j;

                if(j < 0 || j >= vec.values.size()){
                    throw new RuntimeException("Index out of bounds.");
                }

                stack[stackPointer] = vec.get(j);
            }
            else if(instruction instanceof Instr.Sub){
                executeBinOp(BinaryOperator.MINUS);
            }
            else if(instruction instanceof Instr.And){
                executeBinOp(BinaryOperator.AND);
            }
            else if(instruction instanceof Instr.Or){
                executeBinOp(BinaryOperator.OR);
            }
            else if(instruction instanceof Instr.UnEqual){
                executeBinOp(BinaryOperator.UNEQUAL);
            }
            else if(instruction instanceof Instr.Div){
                executeBinOp(BinaryOperator.DIV);
            }
            else if(instruction instanceof Instr.Mod){
                executeBinOp(BinaryOperator.MOD);
            }
            else if(instruction instanceof Instr.GreaterOrEqual){
                executeBinOp(BinaryOperator.GEQ);
            }
            else if(instruction instanceof Instr.Less){
                executeBinOp(BinaryOperator.LESS);
            }
            else if(instruction instanceof Instr.Greater){
                executeBinOp(BinaryOperator.GREATER);
            }
            else if(instruction instanceof Instr.Negate){
                stack[stackPointer] = toInt(!toBool(stack[stackPointer]));
            }
            else if(instruction instanceof Instr.FlipSign){
                stack[stackPointer] = -stack[stackPointer];
            }
            else{
                throw new RuntimeException(String.format("Instruction '%s' not supported.", instruction));
            }
        }

        return stack[stackPointer];
    }

    private void Apply(){
        int addressToFunctionInHeap = stack[stackPointer];

        HeapElement retrievedElement = heap.get(addressToFunctionInHeap);

        if(!(retrievedElement instanceof HeapElement.Function)){
            throw new RuntimeException(String.format("Heap element at '%d' is not a function.", addressToFunctionInHeap));
        }

        HeapElement.Function function = (HeapElement.Function) retrievedElement;

        // continue after the function
        programCounter = jumpTable.get(function.jumpLabelFunctionStart);

        // set a new global vector (global from the perspective of the function is different)
        globalPointer = function.globalVectorHeapAddress;

        // push all arguments that have already been applied onto the stack
        HeapElement appliedArguments = heap.get(function.appliedArgumentsVectorAddress);

        if(!(appliedArguments instanceof HeapElement.Vector)){
            throw new RuntimeException(String.format("Heap element at '%d' is not a vector.", function.appliedArgumentsVectorAddress));
        }

        HeapElement.Vector appliedArgsVector = (HeapElement.Vector) appliedArguments;

        for(int i = 0; i < appliedArgsVector.values.size(); i++){
            stack[stackPointer + i] = appliedArgsVector.get(i);
        }

        stackPointer += (appliedArgsVector.values.size() - 1);
    }

    // micro instructions for TestArg

    // wraps up all applied arguments into a vector
    // the vector is stored in the heap
    // the address is returned on the stack
    private void MakeVec0(){
        int argumentsApplied = stackPointer - framePointer;

        HeapElement.Vector vector = new HeapElement.Vector();
        int vectorHeapAddress = heap.insert(vector);

        // this is the same as stackPointer - (stackPointer - framePointer - 1)
        // = framePointer + 1 (on slide 150)
        stackPointer = stackPointer - (argumentsApplied - 1);

        // collect in vector
        for(int i = 0; i < argumentsApplied; i++){
            vector.values.add(stack[stackPointer + i]);
        }

        // all g arguments are consumed and a new heap address is put on the stack,
        // so we are at framePointer + 1

        // return address to vector on the stack
        stack[stackPointer] = vectorHeapAddress;
    }

    private void Wrap(){

        // test arg is currently executed
        // that means the program counter is already one ahead
        // with -1, we get back to test arg
        int newProgramCounter = programCounter - 1;

        String newJumpLabel = searchJumpLabel(newProgramCounter);

        // thanks to MakeVec0
        int appliedArgumentsVectorAddress = stack[stackPointer];

        HeapElement.Function wrappedFunction = new HeapElement.Function(
                newJumpLabel,
                appliedArgumentsVectorAddress,
                globalPointer
                );

        int wrappedFunctionAddress = heap.insert(wrappedFunction);

        stack[stackPointer] = wrappedFunctionAddress;
    }

    // Release the stack frame
    private void PopEnv(){

        // load old global pointer
        // old global pointer is bottom most of the three organizational cells
        globalPointer = stack[framePointer - 2];

        // top of the stack is return value
        // the return value overwrites bottom most organizational cell
        stack[framePointer - 2] = stack[stackPointer];

        // top most is where to continue after application
        programCounter = stack[framePointer];

        // top of the stack should point to return value
        stackPointer = framePointer - 2;

        // load old frame pointer
        framePointer = stack[framePointer - 1];
    }

    private void Slide(int k){

        // keep topmost value but delete k values below it
        stack[stackPointer - k] = stack[stackPointer];
        stackPointer = stackPointer - k;
    }

    // TODO: make this more efficient by creating inverse mapping at compile time
    private String searchJumpLabel(int jumpAddress){

        for(String jumpLabel : jumpTable.keySet()){
            int address = jumpTable.get(jumpLabel);

            if(address == jumpAddress){
                return jumpLabel;
            }
        }

        throw new RuntimeException(String.format("Jump label at address '%d' not found", jumpAddress));
    }

    private void executeBinOp(BinaryOperator binaryOperator){
        stackPointer--;
        int leftOperand = stack[stackPointer];
        int rightOperand = stack[stackPointer + 1];

        int res = switch(binaryOperator){
            case PLUS -> leftOperand + rightOperand;
            case MUL -> leftOperand * rightOperand;
            case MINUS -> leftOperand - rightOperand;
            case DIV -> leftOperand / rightOperand;
            case MOD -> leftOperand % rightOperand;
            case LEQ -> toInt(leftOperand <= rightOperand);
            case GEQ -> toInt(leftOperand >= rightOperand);
            case EQUAL -> toInt(leftOperand == rightOperand);
            case UNEQUAL -> toInt(leftOperand != rightOperand);
            case LESS -> toInt(leftOperand < rightOperand);
            case GREATER -> toInt(leftOperand > rightOperand);
            case AND -> toInt(toBool (leftOperand) && toBool(rightOperand));
            case OR -> toInt(toBool(leftOperand) || toBool(rightOperand));
            default -> throw new RuntimeException("Operator " + binaryOperator + " not implemented.");
        };

        stack[stackPointer] = res;
    }

    private boolean toBool(int n){
        return n != 0;
    }

    private int toInt(boolean val){
        return val ? 1 : 0;
    }

    private String showRegisters(){

        String programCounterString = String.format("%d (%s)", programCounter, instructionRegister == null ? "null" : instructionRegister.toString());
        return String.format("PC: %s, FP: %d", programCounterString, framePointer);
    }

    private String showStackContent(){

        StringBuilder res = new StringBuilder();

        int nLastElements = 10;

        for(int i = 0; i < nLastElements; i++){
            if(stackPointer - i < 0){
                break;
            }

            int stackLocation = stackPointer - i;
            int content = stack[stackLocation];
            StackType type = stackTypes[stackLocation];

            res.append(String.format("%d | %d (%s)\n", stackLocation, content, type));
        }
        return res.toString();
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
