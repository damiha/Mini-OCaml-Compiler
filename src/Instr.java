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
