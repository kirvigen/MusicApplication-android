package iwinux.com.music.Data;

import android.view.View;

import java.util.List;

import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.Block;
import iwinux.com.music.Objects.Genre;

public class Interfaces {
    public interface OnGenreListner{
        void onGenre(List<Genre> data);
    }
    public interface OnPromocode{
        void onSucces(String id, long time);
        void onError();
    }
    public interface Ganere{
        void ongenres(Genre genre);
    }
    public interface OnAudioClick {
        void getAudios(List<Audio> data, int position, View v);
    }
    public interface OnDataUser{
        void onuser(String id,long time);
    }
    public interface OnBlock{
        void Blocks(List<Block> data);
    }
    public interface OnQueryListener{
        void onQoery(List<String> data);
    }
    public interface OnAudioListener {
        void getAudio(Audio data,int index);
    }
    public interface OnAudio {
        void getAudio(List<Audio> data);
    }
    public interface OnAlbum {
        void getAlbum(List<Album> data);
    }
    public interface OnArtist {
        void getArtist(List<Artist> data);
    }
    public interface OnArtistInfo {
        void getArtist(Artist data);
    }
    public interface OnError{
        void onError(String e);
    }
    public interface OnSwipeView{
        void onSwipe(View v,int position);
    }
    public interface OnClickListen{
        void onClick(View v, int position);
    }
    public interface OnClickAlmumListen{
        void onClick(View v, int position,Album album);
    }
    public interface OnAllContent{
        void onTracks(List<Audio> data);
        void onAlbum(List<Album> data);
        void onArtist(List<Artist> data);
        void onInfoArtist(Artist data);
    }

    public interface OnNewContent{
        void OnContent(List<Audio> audios, List<Album> albums, String ids);
    }

    public interface OnClickArtistListen{
        void onClick(View v, int position, Artist artist);
    }
    public interface OnEventListener{
        void onEvent(String text,String id);
    }
    public interface OnCall{
        void call();
    }
    public interface OnAllContentListener{
        void OnContent(List<Audio> audios, List<Album> albums, List<Artist> artists,List<Album> playlist);
    }
}
