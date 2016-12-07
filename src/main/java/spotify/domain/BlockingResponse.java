package spotify.domain;

import java.util.Optional;

/**
 * Created by castroneves on 28/11/2016.
 */
public class BlockingResponse<T> {
    private Optional<T> response;
    private FuncTuple<T> func;

    public BlockingResponse(Optional<T> response, FuncTuple<T> func) {
        this.response = response;
        this.func = func;
    }

    public Optional<T> getResponse() {
        return response;
    }

    public FuncTuple<T> getFunc() {
        return func;
    }
}
