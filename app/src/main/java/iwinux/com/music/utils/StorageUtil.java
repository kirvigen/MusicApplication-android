package iwinux.com.music.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.Genre;

/**
 * Created by Valdio Veliu on 16-07-30.
 */
public class StorageUtil {

    private final String STORAGE = "muzlo.iwiwinux.com.myspotify.STORAGE";
    private final String Likes = "muzlo.iwiwinux.com.myspotify.Likes";
    private final String DOWNLOAD = "muzlo.iwiwinux.com.myspotify.DOWNLOAD";
    private final String likesAudio = "likesAudio";
    private final String likesAlbum = "likesAlbum";
    String Genres = "GENRE";
    public String OpenApp = "OpenApp";
    private SharedPreferences preferences;
    private Context context;

    MySqlDatabase database;

    public StorageUtil(Context context) {
        this.context = context;
        database = new MySqlDatabase( context, null,1 );
    }
    public void saveText(String name, String text) {
        SharedPreferences prefs = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(name, text);
        ed.commit();
    }
    public void saveHistoryAudio(Audio audio){
         List<Audio> history = database.loadAudiosHistory( 10 );
         if(history.size() != 0){
             if(!history.get( 0 ).getId().equals( audio.getId() )){
                 database.addHistoryAudio(audio);
             }
         }else{
             database.addHistoryAudio(audio);
         }
    }
    public int getOpen(){
        String open = loadText( OpenApp );
        if(open.equals( "" ))
            return 0;
        else
        return Integer.valueOf( open );
    }
    public void openApp(){
        String openCount = loadText( OpenApp );
        int opens = 0;
        if(!openCount.equals( "" ))
            opens = Integer.valueOf(openCount);
            saveText( OpenApp ,String.valueOf( opens+1 ) );
    }
    public  String loadText(String name) {
        SharedPreferences prefs = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        String savedText = prefs.getString(name,"");
        return savedText;
    }
    public void saveGenres(List<Genre> data){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        editor.putString(Genres, json);
        editor.apply();
    }
    public List<Genre> getGenres(){
        preferences = context.getSharedPreferences( STORAGE , Context.MODE_PRIVATE );
        Gson gson = new Gson();
        String json = preferences.getString( Genres, null );
        Type type = new TypeToken<ArrayList<Genre>>() {
        }.getType();
        ArrayList<Genre> check = gson.fromJson( json, type );
        if (check != null){
            if (check.size() != 0) {
                return check;
            } else return new ArrayList<>();
        }else
            return new ArrayList<>();
    }
    public void likesAudio(List<Audio> arrayList) {
        preferences = context.getSharedPreferences(Likes, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(likesAudio, json);
        editor.apply();
    }
    public void likesAlbums(List<Album> arrayList) {
        preferences = context.getSharedPreferences(Likes, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(likesAlbum, json);
        editor.apply();
    }

    public List<Audio> loadDonloadAudio() {
        return database.loadAudios( MySqlDatabase.TYPE_DOWNLOAD,MySqlDatabase.TABLE_AUDIOS_USER );
    }
    public void deletedDownloadAudio(Audio audio) {
        database.deleteAudio( audio, MySqlDatabase.TABLE_AUDIOS_USER,MySqlDatabase.TYPE_DOWNLOAD );
    }
    public Audio getDownloadAudio(Audio audio){
         return database.getAudio( audio,MySqlDatabase.TABLE_AUDIOS_USER,MySqlDatabase.TYPE_DOWNLOAD );
    }
    public void addDownloadAudio(Audio audio){
        database.addAudio( audio,MySqlDatabase.TYPE_DOWNLOAD,MySqlDatabase.TABLE_AUDIOS_USER);
    }
    public ArrayList<Album> loadLikesAlbums() {
        preferences = context.getSharedPreferences(Likes, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(likesAlbum, null);
        Type type = new TypeToken<ArrayList<Album>>() {
        }.getType();
        if(gson.fromJson(json, type) ==null)
            return new ArrayList<>(  );
        else
        return gson.fromJson(json, type);
    }
    public void deletedLikesAlbums(Album album) {

        ArrayList<Album> likesaudio = loadLikesAlbums();
        assert likesaudio != null;
        for(int i=0;i<likesaudio.size();i++){
            Album a = likesaudio.get( i );
            if(a.getId().equals( album.getId() )){
                likesaudio.remove( i );
            }
        }
        likesAlbums(likesaudio);
    }
    public void addlikesAlbum(Album album){
        ArrayList<Album> albums = loadLikesAlbums();

        if(albums ==null) albums = new ArrayList<>(  );
        albums.add( album );
        likesAlbums( albums );
    }

    public Boolean checkLikeAlbums(Album album){
        ArrayList<Album> albums = loadLikesAlbums();
        if(albums != null) {
            for (Album a : albums) {
                if (a.getId().equals( album.getId() )) {
                    return true;
                }
            }
        }
        return false;
    }
    public List<Audio> loadLikesAudio() {
        List<Audio> audios = database.loadAudios( MySqlDatabase.TYPE_LIKE,MySqlDatabase.TABLE_AUDIOS_USER );
        return audios;
    }
    public void filterAudio(){
    }
    public void filterAlbum(){
        ArrayList<Album> likesalbums = loadLikesAlbums();
        for(int i=0;i<likesalbums.size();i++){
            Album a = likesalbums.get( i );
            if(a.getId()==null){
                likesalbums.remove( i );
            }
        }
        likesAlbums( likesalbums );
    }
    public void deletedLikesAudio(Audio audio) {
       database.deleteAudio( audio, MySqlDatabase.TABLE_AUDIOS_USER,MySqlDatabase.TYPE_LIKE );
    }
    public void addlikesAudio(Audio audio){
        database.addAudio( audio,MySqlDatabase.TYPE_LIKE,MySqlDatabase.TABLE_AUDIOS_USER );
    }
    public Boolean checkLikeAudio(Audio audio){
         Audio a = database.getAudio( audio,MySqlDatabase.TABLE_AUDIOS_USER,MySqlDatabase.TYPE_LIKE );
        return (a!=null);
    }
    public Audio getLikeAudio(Audio audio){
        return database.getAudio( audio,MySqlDatabase.TABLE_AUDIOS_USER,MySqlDatabase.TYPE_LIKE );
    }
    public void storeAudio(List<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
