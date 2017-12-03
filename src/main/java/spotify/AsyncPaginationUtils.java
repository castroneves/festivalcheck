package spotify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotify.domain.BlockingResponse;
import spotify.domain.FuncTuple;
import spotify.domain.PagableResult;
import spotify.domain.SearchResponseTuple;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int TIMEOUT_SUBSEQUENT_MILLIS = 1500;

    private static final int NO_RESPONSE = -1;
    private static final List<Integer> retryStatusCodes = Arrays.asList(HTTP_TOO_MANY_REQUESTS, NO_RESPONSE);

    public static <S extends PagableResult, R> List<S> paginateAsync(BiFunction<Integer, R, Future<Response>> func, Class<S> clazz, R context, int pageSize) {
        Optional<S> response = fetchInitialResponse(func, clazz, context);
        if (!response.isPresent()) {
            return new ArrayList<>();
        }
        S initialResponse = response.get();
        int total = initialResponse.getTotal();
        int offset = initialResponse.getResults();

        List<FuncTuple<Response, R, Integer>> funcList = new ArrayList<>();
        while (offset < total) {
            funcList.add(new FuncTuple<>(func, offset));
            offset += pageSize;
        }
        List<S> result = searchUntilSuccess(funcList, clazz, context);

        return Stream.concat(result.stream(),
                Stream.of(initialResponse))
                .collect(toList());
    }

    private static <S, R> Optional<S> fetchInitialResponse(BiFunction<Integer, R, Future<Response>> func, Class<S> clazz, R context) {
        List<S> results = searchUntilSuccess(Arrays.asList(new FuncTuple<>(func, 0)), clazz, context);
        return results.stream().findFirst();
    }

    public static <S, R, T> List<S> searchUntilSuccess(List<FuncTuple<Response, R, T>> funcs, Class<S> clazz, R context) {
        List<FuncTuple<Response, R, T>> candidates = new ArrayList<>(funcs);
        List<S> result = new ArrayList<>();
        List<FuncTuple<Response, R, T>> failures;
        do {
            List<SearchResponseTuple<Response, R, T>> futures = candidates.stream()
                    .map(f -> new SearchResponseTuple<>(f.getFunc().apply(f.getOffset(), context), f))
                    .collect(toList());

            List<BlockingResponse<S, R, T>> initial = futures.stream()
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

    private static <S, R, T> BlockingResponse<S, R, T> blockForResult(SearchResponseTuple<Response, R, T> responseTuple, Class<S> clazz) {
        int statusCode = NO_RESPONSE;
        try {
            Response response = responseTuple.getFuture().get(TIMEOUT_SUBSEQUENT_MILLIS, TimeUnit.MILLISECONDS);
            statusCode = response.getStatus();
            Optional<S> result = Optional.of(response.readEntity(clazz));

            return new BlockingResponse<>(result, statusCode, responseTuple.getFunc());
        } catch (Exception e) {
            return new BlockingResponse<>(Optional.empty(), statusCode, responseTuple.getFunc());
        }
    }
}
