import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Environment {

    Map<String, Pair<Visibility, Integer>> env;

    public Environment(){
        env = new HashMap<>();
    }

    public Environment deepCopy(){
        Environment deepCopy = new Environment();


        for(String varName : env.keySet()){
            deepCopy.env.put(varName, env.get(varName));
        }

        return deepCopy;
    }

    public Pair<Visibility, Integer> get(String varName){
        if(env.containsKey(varName)){
            return env.get(varName);
        }
        throw new RuntimeException(String.format("Variable '%s' not found.", varName));
    }

    // how many inserted
    public int insert(Expr expr, Visibility visibility, int startAddress, Index index){

        if(canBeTarget(expr)) {

            List<String> allNames = collectAllVarNames(expr);

            for(int i = 0; i < allNames.size(); i++){
                int address = startAddress + (index == Index.INCREASING ? i : -i);
                env.put(allNames.get(i), new Pair<>(visibility, address));
            }

            return allNames.size();
        }
        else{
            throw new RuntimeException("Expr cannot be target.");
        }
    }

    public int insert(List<Expr> expressions, Visibility visibility, int startAddress, Index index){

        int currentAddress = startAddress;
        int allK = 0;
        for(Expr expr : expressions){
            int k = insert(expr, visibility, currentAddress, index);
            currentAddress = currentAddress + (index == Index.INCREASING ? k : -k);
            allK += k;
        }
        return allK;
    }

    public int insert(Set<Expr> expressions, Visibility visibility, int startAddress, Index index){
        return insert(new ArrayList<>(expressions), visibility, startAddress, index);
    }

    public int insertParallelDefs(List<Pair<Expr, Expr>> parallelDefs, Visibility visibility, int startAddress, Index index){
        List<Expr> leftHandSides = new ArrayList<>();

        for(Pair<Expr, Expr> p : parallelDefs){
            leftHandSides.add(p.first());
        }

        return insert(leftHandSides, visibility, startAddress, index);
    }

    public List<String> collectAllVarNames(Expr expr){

        List<String> allNames = new ArrayList<>();
        collectAllNamesHelper(expr, allNames);
        return allNames;
    }

    public void collectAllNamesHelper(Expr expr, List<String> allNames){
        if(expr instanceof Expr.Variable){
            allNames.add(((Expr.Variable) expr).varName);
        }
        else{
            // can only be a tuple, otherwise, couldn't have been an assignment target
            for(Expr element : ((Expr.Tuple)expr).expressions){
                collectAllNamesHelper(element, allNames);
            }
        }
    }

    private boolean canBeTarget(Expr expr){

        if(expr instanceof Expr.Variable){
            return true;
        }
        else if(expr instanceof Expr.Tuple){
            // every single one needs to be a target...
            for(Expr element : ((Expr.Tuple) expr).expressions){
                if(!canBeTarget(element)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
