package spotify.domain;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Created by castroneves on 28/11/2016.
 */
public class BlockingResponse<S,R, T> {
    private Optional<S> response;
    private int status;
    private FuncTuple<Response, R, T> func;

    public BlockingResponse(Optional<S> response,int status, FuncTuple<Response, R, T> func) {
        this.response = response;
        this.status = status;
        this.func = func;
    }

    public Optional<S> getResponse() {
        return response;
    }

    public FuncTuple<Response, R, T> getFunc() {
        return func;
    }

    public int getStatus() {
        return status;
    }
}
