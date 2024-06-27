import java.util.ArrayList;
import java.util.List;

public class Heap {

    List<HeapElement> data;

    public Heap(){
        data = new ArrayList<>();
    }

    public HeapElement get(int i){
        return data.get(i);
    }

    public int insert(HeapElement heapElement){
        int heapAddress = data.size();

        data.add(heapElement);

        return heapAddress;
    }
}
