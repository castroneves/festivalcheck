package exception;

import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by castroneves on 08/11/2016.
 */
public class FestivalNotFoundExceptionMapper implements ExceptionMapper<FestivalNotFoundException> {

    @Override
    public Response toResponse(FestivalNotFoundException e) {
        return Response.status(HttpStatus.NOT_FOUND_404).entity(e).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
