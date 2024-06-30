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

    static class GetVec extends Instr{

        int k;

        public GetVec(int k){
            this.k = k;
        }

        @Override
        public String toString(){
            return String.format("GetVec %d", k);
        }
    }

    static class PushLoc extends Instr {
        int relativeAddress;

        Environment environment;
        int stackDistance;

        public PushLoc(int relativeAddress, Environment environment, int stackDistance){
            this.relativeAddress = relativeAddress;
            this.environment = environment;
            this.stackDistance = stackDistance;
        }

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

    static class Get extends Instr{
        int j;

        public Get(int j){
            this.j = j;
        }

        @Override
        public String toString(){
            return String.format("Get %d", j);
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

    static class Mark extends Instr{
        String jumpLabel;

        public Mark(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        @Override
        public String toString(){
            return String.format("Mark %s", jumpLabel);
        }
    }

    static class Alloc extends Instr{

        int n;

        public Alloc(int n){
            this.n = n;
        }

        public String toString(){
            return String.format("Alloc %d", n);
        }
    }

    static class Rewrite extends Instr{

        int n;

        public Rewrite(int n){
            this.n = n;
        }

        public String toString(){
            return String.format("Rewrite %d", n);
        }
    }

    static class Apply extends Instr{

        @Override
        public String toString(){
            return "Apply";
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

    static class JumpZ extends Instr{
        String jumpLabel;

        public JumpZ(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        @Override
        public String toString(){
            return String.format("JumpZ %s", jumpLabel);
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

    static class Sub extends Instr {

        @Override
        public String toString(){
            return "Sub";
        }
    }

    static class Mul extends Instr {

        @Override
        public String toString(){
            return "Mul";
        }
    }

    static class LessThanOrEqual extends Instr {
        @Override
        public String toString(){
            return "LessThanOrEqual";
        }
    }

    static class GreaterOrEqual extends Instr {
        @Override
        public String toString(){
            return "GreaterOrEqual";
        }
    }

    static class Equal extends Instr {
        @Override
        public String toString(){
            return "Equal";
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

    static class TList extends Instr{
        String jumpLabel;

        public TList(String jumpLabel){
            this.jumpLabel = jumpLabel;
        }

        @Override
        public String toString(){
            return String.format("TList %s", jumpLabel);
        }
    }

    static class Nil extends Instr{

        @Override
        public String toString(){
            return "Nil";
        }
    }

    static class Cons extends Instr{

        @Override
        public String toString(){
            return "Cons";
        }
    }

    static class MakeBasic extends Instr{

        @Override
        public String toString(){
            return "MakeBasic";
        }
    }
}
