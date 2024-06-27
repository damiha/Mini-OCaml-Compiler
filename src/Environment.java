import java.util.HashMap;
import java.util.Map;

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

    public void insert(String varName, Visibility visibility, int address){
        env.put(varName, new Pair<Visibility, Integer>(visibility, address));
    }
}
