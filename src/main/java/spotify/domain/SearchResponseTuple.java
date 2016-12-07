package spotify.domain;

import java.util.concurrent.Future;

/**
 * Created by castroneves on 28/11/2016.
 */
public class SearchResponseTuple<T> {
    private Future<T> future;
    private FuncTuple<T> func;

    public SearchResponseTuple(Future<T> future, FuncTuple<T> func) {
        this.future = future;
        this.func = func;
    }

    public Future<T> getFuture() {
        return future;
    }

    public FuncTuple<T> getFunc() {
        return func;
    }
}
