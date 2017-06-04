package cache;

/**
 * Created by Adam on 12/09/2015.
 */
public enum CacheKeyPrefix {
    LISTENED("lastfm_"),
    RECCOMENDEDOWN("reccoown_"),
    RECCOMENDEDALL("reccoall_"),
    RUMOUR("rumour_"),
    SCHEDULE("schedule_"),
    CLASHFINDER("clashfinder_"),
    SPOTIFYACCESSTOKEN(""),
    ARTISTMAPOWN("artistmapown_"),
    ARTISTMAPEXTERNAL("artistmapext_"),
    ARTISTMAPRECOWN("artistmaprecown_"),
    ARTISTMAPRECEXTERNAL("artistmaprecext_"),
    SPOTIFYARTISTSOWN("spotifyartistsown_"),
    SPOTIFYARTISTSALL("spotifyartistsall_");

    private final String prefix;

    CacheKeyPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
