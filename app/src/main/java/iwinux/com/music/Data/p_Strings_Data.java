package iwinux.com.music.Data;

import android.content.Context;

import iwinux.com.music.utils.StorageUtil;

public class p_Strings_Data {
    static public int PUBLIC_VERSION_APP = 1;
    Context context;
    static String domain = "zvooq.com";
    StorageUtil storageUtil;
    public p_Strings_Data(Context context){
        this.context =context;
        storageUtil = new StorageUtil( context );
        if(!storageUtil.loadText( "url_music" ).equals( "" ))
        domain = storageUtil.loadText( "url_music" );
    }


    static String p_artist_r = "artist_release";
    static String p_artist_ids = "artist_ids";
    static String p_artist_r_count = "artist_release_count";
    static String p_artists= "artists";
    static String p_artist_names = "artist_names";

    static String p_release_id ="release_id";
    static String p_release_title ="release_title"; //название альбома в котором был этот трек трека
    static String p_description = "description";
    static String p_id = "id";
    static String p_tracks = "tracks";
    static String p_credits = "credits"; // имя авторов трека
    static String p_duration = "duration";
    static String p_title = "title";
    static String p_image = "image";
    static String p_src = "src";
    static String p_track_ids = "track_ids";
    static String p_has_image = "has_image";
    static String p_result = "result";
    static String p_playlists = "playlists";
    static String p_stream = "stream";
    static String p_palette = "palette";
    static String SPACE ="%2C";
    static String SPACE_PARAM = "%20";
    static String p_releases ="releases";

    static String start = "https://"+domain;
    public String getServerUrl(){
        String url = storageUtil.loadText( "url_server" );
        if(!url.equals( "" ))
            return url;
        return server_URl;
    }
    ///////////////////////////////
    static String server_URl = "";
    /////////////////////////////////////////
    public String URL_GETALBUM = "https://"+domain+"/api/tiny/releases?ids={id}&include=track%2C";
    public String URL_GETPLAYLIST = "https://"+domain+"/api/tiny/playlists?ids={id}&include=track%2Crelease";
    public String URL_SUPERTAG = "https://"+domain+"/api/featured?name=supertags";
    public String URL_GET_TOP_100 = "https://"+domain+"/sapi/grid?name=zvuk_grid_top100";
    public String URL_GET_SOURCE_TRACK = "https://"+domain+"/api/tiny/track/stream?id={id}&quality=high";
    public String URL_GET_DATA_TRACK_ID = "https://"+domain+"/sapi/meta?tracks=";
    public String URL_GET_NEW_DATA = "https://"+domain+"/sapi/grid?name=zvuk_grid_new";
    public String URL_GET_DATA_ARTIST = "https://"+domain+"/api/tiny/artists?ids=";
    public String URL_GET_ALBUM_ARTIST = "https://"+domain+"/sapi/meta?artists={id_artist}&include=(artist%20(release%20false%20%3Afirst%20100))";
    public String URL_GET_POPULAR_TRACK_ARTIST = "https://"+domain+"/api/tiny/popular-tracks?artist={id_artist}&limit=20";
    public String URL_GET_ARTIST_RELATED = "https://"+domain+"/api/tiny/artist/related/?id={id_artist}&limit=20";
    public String URL_SEARCH_All_PARAM = "https://"+domain+"/sapi/search?query={search}&include=artist%20track%20release%20playlist&limit=24";
    static String VK_AUDIOS_LIST = "https://audiobot.me/audios";
    static String ActionAudioBot = "https://audiobot.me/action.php";
    static String SourceAudio = "https://audiobot.me/audio.php?play=";

    public String URL_GET_QUERY_SEARCH = "https://"+domain+"/api/tiny/search/autocomplete?query={search}&limit=5";
    public static String size_small = "90x90";
    public static String size_middle = "126x126";
    public static String size_big = "600x600";
    public static String size_superbig = "800x800";

}
