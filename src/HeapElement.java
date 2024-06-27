import java.util.ArrayList;
import java.util.List;

public class HeapElement {

    static class BasicValue extends HeapElement{

        int value;

        public BasicValue(int value) {
            this.value = value;
        }
    }

    static class Vector extends HeapElement{

        List<Integer> values;

        public Vector(){
            values = new ArrayList<>();
        }

        public int get(int address){
            return values.get(address);
        }
    }
}
