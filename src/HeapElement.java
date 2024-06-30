import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class HeapElement {

    abstract String getOutputRepresentation();

    static class BasicValue extends HeapElement{

        int value;

        public BasicValue(int value) {
            this.value = value;
        }

        @Override
        public String toString(){
            return String.format("(B, %d)", value);
        }

        @Override
        String getOutputRepresentation() {
            return ("" + value);
        }
    }

    // a vector stores heap addresses?
    static class Vector extends HeapElement{

        List<Integer> values;

        public Vector(){
            values = new ArrayList<>();
        }

        public int get(int address){
            return values.get(address);
        }

        public String toString(){

            String addressesAsString = values.stream().map(Object::toString).collect(Collectors.joining(", "));

            return String.format("(V, [%s])", addressesAsString);
        }

        @Override
        String getOutputRepresentation() {
            return toString();
        }
    }

    static class Function extends HeapElement{

        String jumpLabelFunctionStart;
        int appliedArgumentsVectorAddress;
        int globalVectorHeapAddress;

        public Function(String jumpLabelFunctionStart, int appliedArgumentsVectorAddress, int globalVectorHeapAddress){
            this.jumpLabelFunctionStart = jumpLabelFunctionStart;
            this.appliedArgumentsVectorAddress = appliedArgumentsVectorAddress;
            this.globalVectorHeapAddress = globalVectorHeapAddress;
        }

        @Override
        public String toString(){
            return String.format("(F, cp: %s, ap: %d, gp: %d)",
                    jumpLabelFunctionStart,
                    appliedArgumentsVectorAddress,
                    globalVectorHeapAddress);
        }

        @Override
        String getOutputRepresentation() {
            throw new RuntimeException("Function cannot be written to terminal");
        }
    }

    // closure = code + global vector
    // closures are 'snapshots' of functions
    static class Closure extends HeapElement {
        int globalVectorHeapAddress;
        String jumpLabelFunctionStart;

        public Closure(String jumpLabelFunctionStart, int globalVectorHeapAddress){
            this.jumpLabelFunctionStart = jumpLabelFunctionStart;
            this.globalVectorHeapAddress = globalVectorHeapAddress;
        }

        // creates a dummy (important for let rec)
        public Closure(){
            this("-1", -1);
        }

        public String toString(){
            return String.format("(C, cp: %s, gp: %d)", jumpLabelFunctionStart, globalVectorHeapAddress);
        }

        @Override
        String getOutputRepresentation() {
            throw new RuntimeException("Closure cannot be written to terminal");
        }
    }

    static class FList extends HeapElement {

        ListType listType;

        int heapAddressHead;
        int heapAddressListTail;

        Heap heap;

        public FList(){
            listType = ListType.NIL;
        }

        // needs reference to the heap for output representation
        public FList(int heapAddressHead, int heapAddressListTail, Heap heap){
            listType = ListType.CONS;
            this.heapAddressHead = heapAddressHead;
            this.heapAddressListTail = heapAddressListTail;
            this.heap = heap;
        }

        @Override
        public String toString(){
            if(listType == ListType.NIL){
                return "(L, Nil)";
            }
            else{
                return String.format("(L, Cons, h: %d, t: %d)", heapAddressHead, heapAddressListTail);
            }
        }

        @Override
        String getOutputRepresentation() {

            if(listType == ListType.NIL){
                return "[]";
            }
            else{
                HeapElement head = heap.get(heapAddressHead);
                HeapElement tail = heap.get(heapAddressListTail);

                return head.getOutputRepresentation() + "::" + tail.getOutputRepresentation();
            }
        }
    }
}
