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

    public String toString(){

        StringBuilder res = new StringBuilder();

        for(int i = 0; i < data.size(); i++){
            res.append(String.format("%d %s\n", i, data.get(i)));
        }
        return res.toString();
    }
}
