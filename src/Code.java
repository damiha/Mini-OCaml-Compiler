import java.util.*;

public class Code {

    List<Instr> instructions;
    Map<String, Integer> jumpTable;

    // for debugging purposes
    List<Integer> stackDistances;

    int jumpLabelsIssued = 0;

    public Code(){
        instructions = new ArrayList<>();
        stackDistances = new ArrayList<>();
        jumpTable = new HashMap<>();
    }

    public void addInstruction(Instr instruction, String jumpLabel, int stackDistance){

        if(jumpLabel != null) {
            jumpTable.put(jumpLabel, instructions.size());
        }

        instructions.add(instruction);
        stackDistances.add(stackDistance);
    }

    public void addInstruction(Instr instruction, int stackDistance){
        addInstruction(instruction, null, stackDistance);
    }

    public String getNewJumpLabel(){
        return String.format("_%d", jumpLabelsIssued++);
    }

    private void mergeJumpTables(Code other){
        // TODO
    }

    public void addCode(Code other, String jumpLabel){

        if(jumpLabel != null) {
            jumpTable.put(jumpLabel, instructions.size());
        }

        mergeJumpTables(other);

        instructions.addAll(other.instructions);
        stackDistances.addAll(other.stackDistances);
    }

    public void setJumpLabelAtEnd(String jumpLabel){
        jumpTable.put(jumpLabel, instructions.size());
    }

    public void addCode(Code other){
        addCode(other, null);
    }

    private String[] getJumpLabelPrefix(){
        String[] jumpLabelPrefix = new String[instructions.size()];

        for(String jumpLabel : jumpTable.keySet()){

            int lineNumber = jumpTable.get(jumpLabel);

            String currentPrefix = jumpLabelPrefix[lineNumber];
            currentPrefix = currentPrefix != null ? currentPrefix : "";

            currentPrefix += String.format("%s: ", jumpLabel);

            jumpLabelPrefix[lineNumber] = currentPrefix;
        }

        return jumpLabelPrefix;
    }

    public String toString(){
        List<String> instructionStrings = new ArrayList<>();

        String[] jumpLabelPrefix = getJumpLabelPrefix();

        for(int i = 0; i < instructions.size(); i++){

            String prefix = jumpLabelPrefix[i];
            prefix = prefix != null ? prefix : "";

            instructionStrings.add(String.format("%d %s%s", stackDistances.get(i), prefix, instructions.get(i)));
        }

        return String.join("\n", instructionStrings);
    }

}
