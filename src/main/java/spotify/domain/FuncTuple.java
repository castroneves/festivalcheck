package spotify.domain;

import java.util.concurrent.Future;
import java.util.function.BiFunction;

/**
 * Created by castroneves on 07/12/2016.
 */
public class FuncTuple<T> {
    private BiFunction<Integer, SpotifyDetails, Future<T>> func;
    private int offset;

    public FuncTuple(BiFunction<Integer, SpotifyDetails, Future<T>> func, int offset) {
        this.func = func;
        this.offset = offset;
    }

    public BiFunction<Integer, SpotifyDetails, Future<T>> getFunc() {
        return func;
    }

    public int getOffset() {
        return offset;
    }
}
