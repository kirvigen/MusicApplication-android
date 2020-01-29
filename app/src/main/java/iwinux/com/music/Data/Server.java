package iwinux.com.music.Data;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import iwinux.com.music.Objects.Ad;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.Genre;
import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.Data.p_Strings_Data.ActionAudioBot;
import static iwinux.com.music.Data.p_Strings_Data.PUBLIC_VERSION_APP;
import static iwinux.com.music.Data.p_Strings_Data.SPACE;
import static iwinux.com.music.Data.p_Strings_Data.SPACE_PARAM;
import static iwinux.com.music.Data.p_Strings_Data.SourceAudio;
import static iwinux.com.music.Data.p_Strings_Data.VK_AUDIOS_LIST;
import static iwinux.com.music.Data.p_Strings_Data.domain;
import static iwinux.com.music.Data.p_Strings_Data.p_artist_ids;
import static iwinux.com.music.Data.p_Strings_Data.p_artists;
import static iwinux.com.music.Data.p_Strings_Data.p_credits;
import static iwinux.com.music.Data.p_Strings_Data.p_description;
import static iwinux.com.music.Data.p_Strings_Data.p_duration;
import static iwinux.com.music.Data.p_Strings_Data.p_id;
import static iwinux.com.music.Data.p_Strings_Data.p_image;
import static iwinux.com.music.Data.p_Strings_Data.p_palette;
import static iwinux.com.music.Data.p_Strings_Data.p_playlists;
import static iwinux.com.music.Data.p_Strings_Data.p_release_id;
import static iwinux.com.music.Data.p_Strings_Data.p_release_title;
import static iwinux.com.music.Data.p_Strings_Data.p_releases;
import static iwinux.com.music.Data.p_Strings_Data.p_result;
import static iwinux.com.music.Data.p_Strings_Data.p_src;
import static iwinux.com.music.Data.p_Strings_Data.p_stream;
import static iwinux.com.music.Data.p_Strings_Data.p_title;
import static iwinux.com.music.Data.p_Strings_Data.p_track_ids;
import static iwinux.com.music.Data.p_Strings_Data.p_tracks;
import static iwinux.com.music.Data.p_Strings_Data.start;


public class Server {
    private Context context;
    private p_Strings_Data strings_data;
    private StorageUtil storageUtil;
    private Interfaces.OnError onerror = null;
    public Server(Context context){
        this.context = context;
        storageUtil = new StorageUtil( context );
        strings_data = new p_Strings_Data( context );
    }
    public Server(Context context, Interfaces.OnError onerror){
        this.context = context;
        this.onerror = onerror;
        storageUtil = new StorageUtil( context );
        strings_data = new p_Strings_Data( context );
    }
    public void getGenre(Interfaces.OnGenreListner onGenreListner){

        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_SUPERTAG , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONArray data = res.getJSONArray( p_result );
                onGenreListner.onGenre( getGenres(data) );

            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Query", e.getMessage() );
            }
        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    public void getGenreInfo(String url,  Interfaces.OnAllContentListener onAllContentListener){
        StringRequest request = new StringRequest( Request.Method.GET, url.replace(" ","_") , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getTop: "+response);
                JSONObject tracks = data.getJSONObject( p_tracks );
                JSONObject albums = data.getJSONObject( p_releases );
                JSONObject playlist = data.getJSONObject( p_playlists );
              onAllContentListener.OnContent( getTracks( tracks ),getAlbums( albums ),new ArrayList<>(  ),
                      getPlaylist( playlist,0 ));


            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    private List<Genre> getGenres(JSONArray data) {
       List<Genre> genres = new ArrayList<>(  );
        try {
            for (int i =0;i<data.length();i++) {
                JSONObject genre = data.getJSONObject( i );
                String title = genre.getString( "title" );
                JSONObject image =  genre.getJSONObject( p_image );
                String img = image.getString( p_src );
                Log.i( TAG, "getGenres: " + img);
                JSONObject action = genre.getJSONObject( "action" );
                String url = start+action.getString( "url" );
                genres.add( new Genre( title,img,url ) );
            }
            return genres;
        }catch (Exception e){
            Log.e( TAG, "getGenres: "+e.getMessage()  );
            return new ArrayList<>(  );
        }

    }

    public void getArtistsAllData(Interfaces.OnAllContent onAllContent,String id_artist){
        getPopularTrackArtist( onAllContent::onTracks,id_artist );
        getAlbumArtist( onAllContent::onAlbum, onAllContent::onInfoArtist, id_artist );
//        getArtistsRelated( onAllContent::onArtist,id_artist );
    }
    public void ServerRegistrToken(String token){
        StringRequest request = new StringRequest( Request.Method.POST, strings_data.getServerUrl()+"AllMusic/firebase.php" , response -> {
        }, error -> {
        } ){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>( );
                params.put( "token",token );
                params.put( "v", String.valueOf( PUBLIC_VERSION_APP ));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void ServerGetAD(){
        UserManager userManager = new UserManager( context );
        StringRequest request = new StringRequest( Request.Method.POST, strings_data.getServerUrl()+"AllMusic/info.php" , response -> {
            try {
                Log.i( TAG, "getServer: "+response );
                Log.i( TAG, "ServerGetAD: "+ userManager.getsettings().getId());
                JSONObject res = new JSONObject( response );
                JSONObject ads = res.getJSONObject( "ad" );
                String new_fragment = ads.getString( "new_fragment" );
                String top_fragment = ads.getString( "top_fragment" );
                String lents = ads.getString( "lenta" );
                String perehod = ads.getString( "perehod" );
                AdManager adManager = new AdManager( context );
                Ad ad = adManager.getAd();
                ad.setLenta( lents );
                ad.setNew_fragment( new_fragment );
                ad.setPerehod( perehod );
                ad.setTop_fragment( top_fragment );
                adManager.setAd( ad );
                String domain = ads.getString( "domain" );
                if(!domain.equals( "0" )){
                    storageUtil.saveText( "url_music",domain );
                }
                if(!userManager.getsettings().getId().equals( "" ) && !userManager.getsettings().getPay()){
                    int time = res.getJSONObject( "user" ).getInt( "promocode_end" );
                    if(time<=0){
                        SettingsUser settingsUser = userManager.getsettings();
                        settingsUser.setPremium( false );
                        userManager.createSettings( settingsUser );
                    }else{
                        SettingsUser settingsUser = userManager.getsettings();
                        settingsUser.setTimeActive( time );
                        settingsUser.setPremium( true );
                        userManager.createSettings( settingsUser );
                    }
                }

            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Search", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } ){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>( );
                if(!userManager.getsettings().getId().equals( "" ))
                    params.put( "user_id", userManager.getsettings().getId());
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void sendPromocode(String promocode,Interfaces.OnPromocode call,Interfaces.OnError onerror){
        this.onerror = onerror;
        StringRequest request = new StringRequest( Request.Method.POST, strings_data.getServerUrl()+"AllMusic/info.php" , response -> {
            try {
                Log.i( TAG, "sendPromocode: "+response );
                JSONObject res = new JSONObject( response );
                JSONObject promo = res.getJSONObject( "promo" );
                if(promo.getString( "success" ).equals( "1" )){
                    call.onSucces( promo.getString( "user_id" ),promo.getLong( "promocode_end" ) );
                    Toast.makeText( context,promo.getString( "message" ),Toast.LENGTH_LONG ).show();
                }else{
                    onerror.onError( promo.getString( "message" ) );
                }
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Search", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } ){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>( );
                UserManager userManager = new UserManager( context );
                if(!userManager.getsettings().getId().equals( "" ))
                    params.put( "user_id", userManager.getsettings().getId());
                params.put( "promocode",promocode );
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    public void getSearch(Interfaces.OnAllContentListener onAllContent,String query){
        String querys = "";
        final int[] getBadRequest = {0};
        querys = strings_data.URL_SEARCH_All_PARAM.replace( "{search}",query ).replace( " ",SPACE_PARAM );
        StringRequest request = new StringRequest( Request.Method.GET, querys , response -> {
            try {
                Log.i( TAG, "getSearch: "+response );
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                JSONObject search = data.getJSONObject( "search" );
                JSONObject tracks = search.getJSONObject( p_tracks );
                JSONObject albums = search.getJSONObject( p_releases );
                JSONObject artists = search.getJSONObject( p_artists );
                if (tracks.getInt( "total" ) > 0)
                    tracks = data.getJSONObject( p_tracks );
                if (albums.getInt( "total" ) > 0)
                    albums = data.getJSONObject( p_releases );
                if (artists.getInt( "total" ) > 0)
                    artists = data.getJSONObject( p_artists );
                onAllContent.OnContent( getTracks( tracks ),getAlbums( albums ),getArtist( artists ),null );

            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Search", e.getMessage() );
            }


        }, error -> {
            error.printStackTrace();
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    public void getQuerySearch(Interfaces.OnQueryListener queryListener,String search){
        String querys = strings_data.URL_GET_QUERY_SEARCH.replace( "{search}",search );
        StringRequest request = new StringRequest( Request.Method.GET, querys, response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONArray data = res.getJSONArray( p_result );
                List<String> queris = new ArrayList<>();
                for(int i =0;i<data.length();i++){
                    queris.add( data.getString( i ) );
                }
                Log.i( TAG, "getQuery: "+response);
                queryListener.onQoery(queris);
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Query", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                param.put("Host",domain);
                param.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko" +
                        ") Chrome/79.0.3945.117 YaBrowser/20.2.0.1043 Yowser/2.5 Safari/537.36");
                return param;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    public void getPopularTrackArtist(Interfaces.OnAudio onAudio,String id_artist){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_POPULAR_TRACK_ARTIST
                .replace( "{id_artist}",id_artist ), response -> {
            try {
                JSONObject res = new JSONObject( response );
                Log.i( TAG, "getPopularTrackArtist: "+response );
                JSONArray data = res.getJSONArray( p_result );
                Log.i( TAG, "getPopularTracks: "+data);
                String ids ="";
                for(int i=0;i<data.length();i++){
                    if(ids.length() ==0)
                        ids+=data.getString( i );
                    else
                        ids = ids+","+data.getString( i );
                }
                String finalIds = ids;
                new Handler(  ).postDelayed( () -> getTracksIds( onAudio, finalIds ),1500 );

            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getAlbumArtist(Interfaces.OnAlbum onAlbum,String id_artist){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_ALBUM_ARTIST
                .replace( "{id_artist}",id_artist ), response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getAlbums: "+response);
                JSONObject albums = data.getJSONObject( p_releases );
                onAlbum.getAlbum( getAlbums( albums ));
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getAlbumArtist(Interfaces.OnAlbum onAlbum,Interfaces.OnArtistInfo onArtist,String id_artist){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_ALBUM_ARTIST
                .replace( "{id_artist}",id_artist ), response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getAlbums: "+response);
                JSONObject artist = data.getJSONObject( p_artists ).getJSONObject( id_artist );
                JSONObject albums = data.getJSONObject( p_releases );
                onArtist.getArtist( getArtistInfo( artist ) );
                onAlbum.getAlbum( getAlbums( albums ));
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Albums", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getArtistsRelated(Interfaces.OnArtist onArtist,String id_artist){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_ARTIST_RELATED
                .replace( "{id_artist}",id_artist ), response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONArray data = res.getJSONArray( p_result );
                Log.i( TAG, "getArtists: "+response);
                String ids ="";
                for(int i=0;i<data.length();i++){
                    if(ids.length() ==0)
                        ids+=data.getString( i );
                    else
                        ids = ids+","+data.getString( i );
                }
                getArtistIds( onArtist,ids);
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Artist", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    private void getArtistIds(Interfaces.OnArtist onArtist, String ids) {
        ids = ids.replaceAll( ",",SPACE );
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_DATA_ARTIST+ids, response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getArtists: "+response);
                JSONObject artist = data.getJSONObject( p_artists );
                onArtist.getArtist( getArtist( artist ) );
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR_Artist_info", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    public void getTracksIds(Interfaces.OnAudio onaudio,String ids){
        ids = ids.replaceAll( ",",SPACE );
        String finalIds = ids;
        Log.i(TAG, "getTracksIds: "+strings_data.URL_GET_DATA_TRACK_ID+ids);
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_DATA_TRACK_ID+ids+"&include=(track%20(release%20label)%20)", response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getTracks: "+response);
                JSONObject tracks = data.getJSONObject( p_tracks );
                onaudio.getAudio( getTracks( tracks ) );
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }


        }, error -> {
            if(error.networkResponse != null)
                if(error.networkResponse.statusCode == 502)
                    new Handler(  ).postDelayed( ()->{
                        getTracksIds( onaudio, finalIds );
                    },2000 );
                else{
                    if(onerror != null){
                        onerror.onError( error.getMessage() );
                    }
                }
            else if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getNew(Interfaces.OnNewContent onContent){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_NEW_DATA , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getTop: "+response);
                JSONObject tracks = data.getJSONObject( p_tracks );
                JSONObject albums = data.getJSONObject( p_releases );
                JSONObject playlist = data.getJSONObject( p_playlists );
                JSONObject ads = playlist.getJSONObject(  playlist.keys().next() );
                JSONArray aids = ads.getJSONArray( p_track_ids );
                String ids ="";
                for(int i=0;i<aids.length();i++){
                    if(ids.length() ==0)
                        ids+=aids.getString( i );
                    else
                        ids = ids+","+aids.getString( i );
                }
                onContent.OnContent( getTracks( tracks ),getAlbums(albums), ids );


            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getAlbumId(Interfaces.OnAudio onAudio,String id){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GETALBUM.replace("{id}",id) , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getAlbumTracks: "+response);
                JSONObject tracks = data.getJSONObject( p_tracks );
                onAudio.getAudio(getTracks(tracks));
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }
        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getPlaylistId(Interfaces.OnAudio onAudio,String id){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GETPLAYLIST.replace("{id}",id) , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getAlbumTracks: "+response);
                JSONObject tracks = data.getJSONObject( p_tracks );
                onAudio.getAudio(getTracks(tracks));
            }catch (JSONException e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }
        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }
    public void getTop(Interfaces.OnAllContentListener onContent){
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_TOP_100 , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject data = res.getJSONObject( p_result );
                Log.i( TAG, "getTop: "+response);
                JSONObject tracks = data.getJSONObject( p_tracks );
                JSONObject albums = data.getJSONObject( p_releases );
                JSONObject artists = data.getJSONObject( p_artists );
                JSONObject playlist = data.getJSONObject( p_playlists );
                onContent.OnContent( getTracks( tracks ),getAlbums(albums),getArtist(artists),getPlaylist(playlist,1) );


            }catch (JSONException e){
                    Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }


        }, error -> {
            if(onerror != null){
                onerror.onError( error.getMessage() );
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );

    }

    List<Album> getPlaylist(JSONObject playlist, int i) {
        List<Album> albums = new ArrayList<>(  );
        try {

            for (Iterator<String> it = playlist.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject album = playlist.getJSONObject( key );
                String title = album.getString( p_title );
                if (!title.equals( "Топ-100" )) {
                    String id = album.getString( p_id );
                    JSONObject image = album.getJSONObject( p_image );
                    String img = start + image.getString( p_src );
                    String pallete = image.getString( p_palette );

                    JSONArray ids = album.getJSONArray( p_track_ids );
                    String track_ids = "";
                    if (ids.length() == 1) track_ids = ids.getString( 0 );
                    else for (int j = 0; j < ids.length(); j++) {
                       if (track_ids.length() == 0) track_ids = ids.getString( j );
                       else track_ids = track_ids + "," + ids.getString( j );
                }
                albums.add( new Album( track_ids, id, title, "", "", img, pallete ) );
            }
            }
            return  albums;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.println( Log.ERROR,"ERROR Album", e.getMessage() );
            return  new ArrayList<>(  );
        }
    }

    List<Artist> getArtist(JSONObject data) {
        try {
            List<Artist> artists = new ArrayList<>(  );
            for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject artist = data.getJSONObject( key );
                String id = artist.getString( p_id );
                String title = artist.getString( p_title );
                String description = artist.getString( p_description );
                JSONObject image = artist.getJSONObject( p_image );
                String palette = image.getString( p_palette );
                String img = image.getString( p_src );
                artists.add( new Artist( id,description,img,title, palette )  );
            }
            return artists;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.println( Log.ERROR,"ERROR Audios", e.getMessage() );
            return  new ArrayList<>(  );
        }
    }
    List<Album> getAlbums(JSONObject data) {
        List<Album> albums = new ArrayList<>(  );
        try {

         for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject album = data.getJSONObject( key );

             String id = album.getString( p_id );
             String title = album.getString( p_title );
             String artist = album.getString( p_credits );
             JSONObject image = album.getJSONObject( p_image );
             String pallete = image.getString( p_palette );
             String img = image.getString( p_src );
             JSONArray ids = album.getJSONArray( p_track_ids );
             String track_ids="";
             List<Audio> audios = new ArrayList<>();
             try {
                 JSONObject tracks = album.getJSONObject(p_tracks);
                 audios = getTracks(tracks);
             }catch (Exception e){

             }
             if(ids.length()==1)
                 track_ids= ids.getString( 0 );
             else
                 for (int j =0;j<ids.length();j++) {
                     if (track_ids.length() == 0)
                         track_ids = ids.getString( j );
                     else track_ids = track_ids + "," + ids.getString( j );
                 }
             JSONArray a = album.getJSONArray( p_artist_ids );
             String artist_id="";
             if(a.length()==1)
                 artist_id = a.getString( 0 );
             else
                 for (int j =0;j<a.length();j++) {
                     if (artist_id.length() == 0)
                         artist_id = a.getString(j);
                     else artist_id = artist_id + "," + a.getString(j);
                 }
             Album album1 = new Album( track_ids,id,title,artist,artist_id,img,pallete );
                 if(audios.size() != 0){
                     album1.setAudios(audios);
                 }
             albums.add(album1);
         }
        return  albums;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.println( Log.ERROR,"ERROR Album", e.getMessage() );
            return  new ArrayList<>(  );
        }

    }
    List<Audio> getTracks(JSONObject tracks){
        try {
        List<Audio> audios = new ArrayList<>(  );
            for (Iterator<String> it = tracks.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject track = tracks.getJSONObject( key );
                String id = track.getString( p_id );
                String title = track.getString( p_title );
                String artist = track.getString( p_credits );
                String album = track.getString( p_release_title );
                String albumid= track.getString( p_release_id );
                JSONObject image = track.getJSONObject( p_image );
                String pallete = image.getString( p_palette );
                String img = image.getString( p_src );

                int duration = track.getInt( p_duration );

                JSONArray a = track.getJSONArray( p_artist_ids );
                String artist_id="";
                if(a.length()==1)
                    artist_id = a.getString( 0 );
                else
                    for (int j =0;j<a.length();j++) {
                        if (artist_id.length() == 0)
                            artist_id = a.getString( j );
                        else artist_id = artist_id + "," + a.getString( j );
                    }
                audios.add( new Audio( id,title, artist, img, albumid, duration, pallete, album, artist_id) );
            }
        return audios;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.println( Log.ERROR,"ERROR Audios", e.getMessage() );
            return  new ArrayList<>(  );
        }
    }
    Artist getArtistInfo(JSONObject artist){
        try {
            String id = artist.getString( p_id );
            String title = artist.getString( p_title );
            String description = artist.getString( p_description );
            JSONObject image = artist.getJSONObject( p_image );
            String palette = image.getString( p_palette );
            String img = image.getString( p_src );
            return new Artist( id,description,img,title, palette );
        } catch (JSONException e) {
            e.printStackTrace();
            return new Artist( "","","","", "" );
        }

    }

    public void getSourseTrack(String id,Interfaces.OnEventListener eventListener,Interfaces.OnError onerror){
        this.onerror = onerror;
        StringRequest request = new StringRequest( Request.Method.GET, strings_data.URL_GET_SOURCE_TRACK.replace( "{id}",id ) , response -> {
            try {
                JSONObject res = new JSONObject( response );
                JSONObject result = res.getJSONObject( p_result );
                Log.i( TAG, "getSourseTrack: "+response );
                eventListener.onEvent( result.getString( p_stream ) , id);
            }catch (Exception e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }
        }, error -> {
            if(error.networkResponse != null) {
                if (error.networkResponse.statusCode == 403) Toast.makeText( context, "Трек не доступен", Toast.LENGTH_SHORT ).show();
                else if (onerror != null) {
                    onerror.onError( id );
                }
            }
            else{
                if (onerror != null) {
                    onerror.onError( id );
                }
            }
        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }


    public void GetAudioVk(Interfaces.OnAudio onAudio,String vk_url,Interfaces.OnError onerror){
        this.onerror = onerror;
        Log.i( TAG, "GetAudioVk: link" + vk_url);
        StringRequest request = new StringRequest( Request.Method.POST, ActionAudioBot , response -> {
            try {
                JSONObject res = new JSONObject( response );
                if(res.getBoolean( "result" )){
                    GetAudiosAudioBot( onAudio,res.getString( "user_id" ) );
                }else{
                    onerror.onError( "Ссылка неверна" );
                }
                Log.i( TAG, "GetAudioVk: "+response );

            }catch (Exception e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }
        }, error -> {

                if (onerror != null) {
                    onerror.onError( error.getMessage() );
                }

        } )  {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>( );
                params.put( "vk_url",vk_url );
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }

    public void GetAudiosAudioBot(Interfaces.OnAudio onAudio,String id){
        StringRequest request = new StringRequest( Request.Method.GET, VK_AUDIOS_LIST+id, response -> {
            try {
                Document html = Jsoup.parse( response );
                Elements audios = html.getElementsByClass( "audio" );
                if(audios.size() ==0){
                    onerror.onError( "Разрешите видеть список своих аудиозаписей всем пользователям.");
                }else
                onAudio.getAudio(  parseAudios( audios ) );
            }catch (Exception e){
                Log.println( Log.ERROR,"ERROR", e.getMessage() );
            }
        }, error -> {

            if (onerror != null) {
                onerror.onError( "Не удалось подключиться к серверу" );
            }

        } );
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add( request );
    }



    List<Audio> parseAudios(Elements audios){
        List<Audio> data = new ArrayList<>(  );
        for(int i=0;i<audios.size();i++){
            try {
                Element item = audios.get( i );
                String title = item.getElementsByClass( "title" ).get( 0 ).text();
                String artist = item.getElementsByClass( "artist" ).get( 0 ).text();
                String duration = item.getElementsByClass( "duration" ).get(0).attr( "data" );
                String play = item.getElementsByClass( "play" ).get( 0 ).attr( "data" );
                String url = "vk:"+SourceAudio+play;
                String img ="";
                Pattern pattern = Pattern.compile( "\\('(\\S*)'\\)" );
                Matcher matcher = pattern.matcher( item.getElementsByClass( "cover" ).attr( "style" ) );
                if (matcher.find())
                    img = matcher.group( 0 ).replace( "('", "" ).replace( "')", "" );
                if(img.equals( "../img/cover.png" )) img = img.replace( "..","https://audiobot.me" );

                Audio audio = new Audio( url,title,artist,img,"",Integer.valueOf( duration ),"","","" );
                data.add( audio );
            }catch (Exception e){

            }

        }
       Log.i( TAG, "parseAudios: ok" );
        return data;
    }

}
