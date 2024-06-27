import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Code {

    List<Instr> instructions;
    Map<Integer, Integer> jumpTable;

    public Code(){
        instructions = new ArrayList<>();
        jumpTable = new HashMap<>();
    }

    public void addInstruction(Instr instruction){
        instructions.add(instruction);
    }

    private void mergeJumpTables(Code other){
        // TODO
    }

    public void addCode(Code other){

        mergeJumpTables(other);

        instructions.addAll(other.instructions);
    }

    public String toString(){
        List<String> instructionStrings = new ArrayList<>(instructions.stream().map(Object::toString).toList());

        return String.join("\n", instructionStrings);
    }
}
