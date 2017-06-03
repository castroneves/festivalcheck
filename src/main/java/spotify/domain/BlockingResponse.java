package spotify.domain;

import java.util.Optional;

/**
 * Created by castroneves on 28/11/2016.
 */
public class BlockingResponse<T,S> {
    private Optional<S> response;
    private int status;
    private FuncTuple<T> func;

    public BlockingResponse(Optional<S> response,int status, FuncTuple<T> func) {
        this.response = response;
        this.status = status;
        this.func = func;
    }

    public Optional<S> getResponse() {
        return response;
    }

    public FuncTuple<T> getFunc() {
        return func;
    }

    public int getStatus() {
        return status;
    }
}
