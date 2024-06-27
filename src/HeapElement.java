public class HeapElement {

    HeapTag heapTag;

    public HeapElement(HeapTag tag){
        this.heapTag = tag;
    }

    static class BasicValue extends HeapElement{

        int value;

        public BasicValue(HeapTag tag, int value) {
            super(tag);
            this.value = value;
        }
    }
}
