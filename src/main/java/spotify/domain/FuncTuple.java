package spotify.domain;

import java.util.concurrent.Future;
import java.util.function.BiFunction;

/**
 * Created by castroneves on 07/12/2016.
 */
public class FuncTuple<T, R, S> {
    private BiFunction<S, R,  Future<T>> func;
    private S offset;

    public FuncTuple(BiFunction<S, R, Future<T>> func, S offset) {
        this.func = func;
        this.offset = offset;
    }

    public BiFunction<S, R, Future<T>> getFunc() {
        return func;
    }

    public S getOffset() {
        return offset;
    }
}
