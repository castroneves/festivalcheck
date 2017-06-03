package spotify;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotify.domain.*;

import javax.swing.text.html.Option;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 29/02/2016.
 */
public class AsyncPaginationUtils {
    private static final Logger logger = LoggerFactory.getLogger(AsyncPaginationUtils.class);
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final int TIMEOUT_INITIAL_MILLIS = 2000;
    public static final int TIMEOUT_SUBSEQUENT_MILLIS = 1500;

    private static final List<Integer> retryStatusCodes = Arrays.asList(429, -1);

    public static <T extends Response, S extends SpotifyResponse> List<S> paginateAsync(BiFunction<Integer, SpotifyDetails, Future<T>> func, Class<S> clazz, SpotifyDetails details, int pageSize) {
        Optional<S> response = fetchInitialResponse(func, clazz, details);
        SpotifyResponse initialResponse = response.isPresent() ? response.get() : new EmptySpotifyResponse();
        int total = initialResponse.getTotal();
        int offset = initialResponse.getItems().size();

        List<FuncTuple<T>> funcList = new ArrayList<>();
        while (offset < total) {
            funcList.add(new FuncTuple<>(func, offset));
            offset += pageSize;
        }
        List<S> result = searchUntilSuccess(funcList, clazz, details);

        return Stream.concat(result.stream(),
                asList(response).stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .collect(toList());
    }

    private static <T extends SpotifyResponse> Optional<T> blockForResult(Future<T> response) {
        try {
            return Optional.of(response.get(TIMEOUT_SUBSEQUENT_MILLIS, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            logger.info(e.getMessage());
            return Optional.empty();
        }
    }

    private static <T extends Response, S extends SpotifyResponse> Optional<S> fetchInitialResponse(BiFunction<Integer, SpotifyDetails, Future<T>> func, Class<S> clazz, SpotifyDetails details) {
        List<S> results = searchUntilSuccess(Arrays.asList(new FuncTuple<>(func, 0)), clazz, details);
        return results.stream().findFirst();
    }

    public static <T extends Response, S extends SpotifyResponse> List<S> searchUntilSuccess(List<FuncTuple<T>> funcs, Class<S> clazz, SpotifyDetails details) {
        List<FuncTuple<T>> candidates = new ArrayList<>(funcs);
        List<S> result = new ArrayList<>();
        List<FuncTuple<T>> failures;
        do {
            List<SearchResponseTuple<T>> futures = candidates.stream()
                    .map(f -> new SearchResponseTuple<>(f.getFunc().apply(f.getOffset(), details), f))
                    .collect(toList());

            List<BlockingResponse<T, S>> initial = futures.stream()
                    .map(f -> blockForResult(f, clazz))
                    .collect(toList());

            result.addAll(initial.stream()
                    .filter(o -> o.getResponse().isPresent())
                    .map(r -> r.getResponse().get())
                    .collect(toList()));

            failures = initial.stream()
                    .filter(o -> !o.getResponse().isPresent())
                    .filter(r -> retryStatusCodes.contains(r.getStatus()))
                    .map(BlockingResponse::getFunc)
                    .collect(toList());
            candidates = failures;
            if (result.size() > 1 || failures.size() > 0) {
                logger.info("Results: {} Failures: {}", result.size(), failures.size());
            }
        } while (failures.size() > 0);
        return result;
    }

    private static <T extends Response, S extends SpotifyResponse> BlockingResponse<T, S> blockForResult(SearchResponseTuple<T> responseTuple, Class<S> clazz) {
        int statusCode = -1;
        try {
            T response = responseTuple.getFuture().get(TIMEOUT_SUBSEQUENT_MILLIS, TimeUnit.MILLISECONDS);
            statusCode = response.getStatus();
            Optional<S> result = Optional.of(response.readEntity(clazz));

            return new BlockingResponse(result, statusCode, responseTuple.getFunc());
        } catch (Exception e) {
            return new BlockingResponse(Optional.empty(), statusCode, responseTuple.getFunc());
        }
    }
}
