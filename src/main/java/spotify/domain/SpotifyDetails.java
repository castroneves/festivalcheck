package spotify.domain;

/**
 * Created by Adam on 13/10/2015.
 */
public class SpotifyDetails {

    private String accessCode;
    private SpotifyPlaylist playlist;
    private String userId;



    public SpotifyDetails(String accessCode) {
        this.accessCode = accessCode;
    }


    public SpotifyDetails(String accessCode, String userId) {
        this.accessCode = accessCode;
        this.userId = userId;
    }

    public SpotifyDetails(String accessCode, SpotifyPlaylist playlist, String userId) {
        this.accessCode = accessCode;
        this.playlist = playlist;
        this.userId = userId;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public SpotifyPlaylist getPlaylist() {
        return playlist;
    }

    public String getUserId() {
        return userId;
    }
}
