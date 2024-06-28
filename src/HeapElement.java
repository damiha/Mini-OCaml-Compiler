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
}
