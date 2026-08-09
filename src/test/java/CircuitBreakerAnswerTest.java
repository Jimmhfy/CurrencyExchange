import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CircuitBreakerAnswerTest {
    static CircuitBreakerAnswer<Integer, Integer> circuitBreakerAnswer;
    static Function<Integer, Integer> testFunction;
    @BeforeAll
    public static void init() {
        testFunction = i -> i*2;
        circuitBreakerAnswer = new CircuitBreakerAnswer<>(testFunction, 3);
    }

    @Test
    public void test_SUCCESS(){
        var output = circuitBreakerAnswer.execute(2);
        assertEquals(4, output);
    }
}
