import java.util.*;

public class Code {

    List<Instr> instructions;
    Map<Integer, Integer> jumpTable;

    // for debugging purposes
    List<Integer> stackDistances;

    public Code(){
        instructions = new ArrayList<>();
        stackDistances = new ArrayList<>();
        jumpTable = new HashMap<>();
    }

    public void addInstruction(Instr instruction, int stackDistance){
        instructions.add(instruction);
        stackDistances.add(stackDistance);
    }

    private void mergeJumpTables(Code other){
        // TODO
    }

    public void addCode(Code other){

        mergeJumpTables(other);

        instructions.addAll(other.instructions);
        stackDistances.addAll(other.stackDistances);
    }

    public String toString(){
        List<String> instructionStrings = new ArrayList<>();

        for(int i = 0; i < instructions.size(); i++){
            instructionStrings.add(String.format("%d %s", stackDistances.get(i), instructions.get(i)));
        }

        return String.join("\n", instructionStrings);
    }

}
