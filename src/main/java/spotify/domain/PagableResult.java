package spotify.domain;

/**
 * Created by castroneves on 02/10/2017.
 */
public interface PagableResult {
    Integer getTotal();
    Integer getResults();
}
