package spotify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotify.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;

/**
 * Created by Adam on 29/02/2016.
 */
public class AsyncPaginationUtils {
    private static final Logger logger = LoggerFactory.getLogger(AsyncPaginationUtils.class);
    public static final int TIMEOUT_INITIAL_MILLIS = 2000;
    public static final int TIMEOUT_SUBSEQUENT_MILLIS = 1500;

    public static <T extends SpotifyResponse> List<T> paginateAsync(BiFunction<Integer, SpotifyDetails, Future<T>> func, SpotifyDetails details, int pageSize) {
        Optional<T> response = fetchInitialResponse(func, details);
        SpotifyResponse initialResponse = response.isPresent() ? response.get() : new EmptySpotifyResponse();
        int total = initialResponse.getTotal();
        int offset = initialResponse.getItems().size();

        List<FuncTuple<T>> funcList = new ArrayList<>();
        while (offset < total) {
            funcList.add(new FuncTuple<>(func, offset));
            offset += pageSize;
        }
        List<T> result = searchUntilSuccess(funcList, details);

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

    private static <T extends SpotifyResponse> Optional<T> fetchInitialResponse(BiFunction<Integer, SpotifyDetails, Future<T>> func, SpotifyDetails details) {
        try {
            return Optional.of(func.apply(0, details).get(TIMEOUT_INITIAL_MILLIS, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            logger.info(e.getMessage());
            return Optional.empty();
        }
    }

    public static <T extends SpotifyResponse> List<T> searchUntilSuccess(List<FuncTuple<T>> funcs, SpotifyDetails details) {
        List<FuncTuple<T>> candidates = new ArrayList<>(funcs);
        List<T> result = new ArrayList<>();
        List<FuncTuple<T>> failures;
        do {
            List<SearchResponseTuple<T>> futures = candidates.stream()
                    .map(f -> new SearchResponseTuple<>(f.getFunc().apply(f.getOffset(), details), f))
                    .collect(toList());

            List<BlockingResponse<T>> initial = futures.stream()
                    .map(f -> blockForResult(f))
                    .collect(toList());

            result.addAll(initial.stream()
                    .filter(o -> o.getResponse().isPresent())
                    .map(r -> r.getResponse().get())
                    .collect(toList()));

            failures = initial.stream()
                    .filter(o -> !o.getResponse().isPresent())
                    .map(BlockingResponse::getFunc)
                    .collect(toList());
            candidates = failures;
            if (result.size() > 0 || failures.size() > 0) {
                logger.info("Results: {} Failures: {}", result.size(), failures.size());
            }
        } while (failures.size() > 0);
        return result;
    }

    private static <T> BlockingResponse<T> blockForResult(SearchResponseTuple<T> responseTuple) {
        try {
            Optional<T> result = Optional.of(responseTuple.getFuture().get(TIMEOUT_SUBSEQUENT_MILLIS, TimeUnit.MILLISECONDS));
//                logger.info("Returning track {} with errors {}", result.get(),errors);

            return new BlockingResponse(result, responseTuple.getFunc());
        } catch (Exception e) {
            return new BlockingResponse(Optional.empty(), responseTuple.getFunc());
        }
    }
}
