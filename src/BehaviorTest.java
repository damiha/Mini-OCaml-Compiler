
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorTest {

    @Test
    public void testNestedLet(){

        String source = "let a = 19 in let b = a * a in a + b";

        Runner runner = new Runner(source);

        int result = runner.run();

        // 19 + (19 * 19) = 380
        assertEquals(result, 380);
    }
}
