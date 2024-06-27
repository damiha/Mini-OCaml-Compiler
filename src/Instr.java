public class Instr {

    static class LoadC extends Instr{
        int constant;

        public LoadC(int constant){
            this.constant = constant;
        }

        @Override
        public String toString() {
            return String.format("LoadC %d", constant);
        }
    }

    static class Halt extends Instr {

        @Override
        public String toString(){
            return "Halt";
        }
    }

    static class GetBasic extends Instr{

        @Override
        public String toString(){
            return "GetBasic";
        }
    }

    static class PushLoc extends Instr {
        int relativeAddress;

        public PushLoc(int relativeAddress){
            this.relativeAddress = relativeAddress;
        }

        @Override
        public String toString(){
            return String.format("PushLoc %d", relativeAddress);
        }
    }

    static class PushGlob extends Instr {
        int addressInGlobalVector;

        public PushGlob(int addressInGlobalVector){
            this.addressInGlobalVector = addressInGlobalVector;
        }

        @Override
        public String toString(){
            return String.format("PushGlob %d", addressInGlobalVector);
        }
    }

    static class MakeVec extends Instr{

        int g;

        public MakeVec(int g){
            this.g = g;
        }

        @Override
        public String toString(){
            return String.format("MakeVec %d", g);
        }
    }

    // creates a functional value
    static class MakeFunVal extends Instr{

        String jumpLabel;

        public MakeFunVal(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        @Override
        public String toString(){
            return String.format("MakeFunVal %s", jumpLabel);
        }
    }

    static class Jump extends Instr {
        String jumpLabel;

        public Jump(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        @Override
        public String toString(){
            return String.format("Jump %s", jumpLabel);
        }
    }

    static class TestArg extends Instr{

        int k;

        public TestArg(int k){
            this.k = k;
        }

        @Override
        public String toString(){
            return String.format("TestArg %d", k);
        }
    }

    static class Return extends Instr {

        int k;

        public Return(int k){
            this.k = k;
        }

        @Override
        public String toString(){
            return String.format("Return %d", k);
        }
    }

    static class Add extends Instr {

        @Override
        public String toString(){
            return "Add";
        }
    }

    static class Mul extends Instr {

        @Override
        public String toString(){
            return "Mul";
        }
    }

    static class Slide extends Instr{
        int n;

        public Slide(int n){
            this.n = n;
        }

        @Override
        public String toString(){
            return String.format("Slide %d", n);
        }
    }

    static class MakeBasic extends Instr{

        @Override
        public String toString(){
            return "MakeBasic";
        }
    }
}
