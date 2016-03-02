package spotify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotify.domain.EmptySpotifyResponse;
import spotify.domain.SpotifyDetails;
import spotify.domain.SpotifyResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 29/02/2016.
 */
public class AsyncPaginationUtils {
    private static final Logger logger = LoggerFactory.getLogger(AsyncPaginationUtils.class);

    public static <T extends SpotifyResponse> List<T> paginateAsync(BiFunction<Integer, SpotifyDetails, Future<T>> func, SpotifyDetails details, int pageSize) {
        T response = fetchInitialResponse(func, details);
        SpotifyResponse initialResponse = response == null ? new EmptySpotifyResponse() : response;
        int total = initialResponse.getTotal();
        int offset = initialResponse.getItems().size();

        List<Future<T>> responseList = new ArrayList<>();
        while(offset < total) {
            responseList.add(func.apply(offset, details));
            offset += pageSize;
        }
        List<T> result = responseList.stream().map(x -> {
            try {
                return x.get(1500, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.info(e.getMessage());
                return null;
            }
        }).collect(toList());
        return Stream.concat(result.stream(), Arrays.asList(response).stream()).filter(x -> x != null).collect(toList());
    }

    private static <T extends SpotifyResponse> T fetchInitialResponse(BiFunction<Integer, SpotifyDetails, Future<T>> func, SpotifyDetails details) {
        try {
            return func.apply(0, details).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.info(e.getMessage());
            return null;
        }
    }
}
