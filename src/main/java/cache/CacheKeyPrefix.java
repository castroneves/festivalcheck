package cache;

/**
 * Created by Adam on 12/09/2015.
 */
public enum CacheKeyPrefix {
    LISTENED("lastfm_"),
    RECCOMENDED("recco_"),
    RUMOUR("rumour_"),
    SCHEDULE("schedule_"),
    SPOTIFYACCESSTOKEN("");

    private final String prefix;

    CacheKeyPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
