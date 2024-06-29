import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HeapElement {

    static class BasicValue extends HeapElement{

        int value;

        public BasicValue(int value) {
            this.value = value;
        }

        @Override
        public String toString(){
            return String.format("(B, %d)", value);
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
    }
}
