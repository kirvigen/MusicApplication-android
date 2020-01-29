package iwinux.com.music.Objects;

import java.io.Serializable;
import java.util.List;

public class Album implements Serializable {
    private String ids_track;
    private List<Audio> audios;
    private String id;
    private String title;
    private String artist;
    private String idArtist;
    private String imgurl;
    private String palette;

    public Album(String ids_track, String id, String title, String artist, String idArtist, String imgurl, String palette) {
        this.ids_track = ids_track;
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.idArtist = idArtist;
        this.imgurl = imgurl;
        this.palette = palette;
    }

    public String getIds_track() {
        return ids_track;
    }

    public void setIds_track(String ids_track) {
        this.ids_track = ids_track;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getIdArtist() {
        return idArtist;
    }

    public void setIdArtist(String idArtist) {
        this.idArtist = idArtist;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getPalette() {
        return palette;
    }

    public void setPalette(String palette) {
        this.palette = palette;
    }

    public List<Audio> getAudios() {
        return audios;
    }

    public void setAudios(List<Audio> audios) {
        this.audios = audios;
    }
}
