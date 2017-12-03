package spotify.domain;

import java.util.concurrent.Future;

/**
 * Created by castroneves on 28/11/2016.
 */
public class SearchResponseTuple<T, R, S> {
    private Future<T> future;
    private FuncTuple<T, R, S> func;

    public SearchResponseTuple(Future<T> future, FuncTuple<T, R, S> func) {
        this.future = future;
        this.func = func;
    }

    public Future<T> getFuture() {
        return future;
    }

    public FuncTuple<T, R, S> getFunc() {
        return func;
    }
}
