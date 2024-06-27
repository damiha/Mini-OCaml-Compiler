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

    static class GetBasic extends Instr{

        @Override
        public String toString(){
            return "GetBasic";
        }
    }

    static class MakeBasic extends Instr{

        @Override
        public String toString(){
            return "MakeBasic";
        }
    }
}
