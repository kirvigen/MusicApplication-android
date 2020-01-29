package iwinux.com.music.Objects;


import java.io.Serializable;

/**
 * Created by Valdio Veliu on 16-07-18.
 */
public class Audio implements Serializable {

    private String id;
    private String title;
    private String artist;

    private String imgUrl;
    private String albumid;
    private String StorageUri;
    private int duration;
    private String pallete;
    private String albumname;
    private String artistid;
    private String path;
    public Audio(){

    }
    public Audio(String id, String title, String artist, String imgUrl, String albumid) {
        this.id = id;
        this.title = title;

        this.artist = artist;
        this.imgUrl = imgUrl;
        this.albumid = albumid;
    }
    public Audio(String id, String title, String artist, String imgUrl, String albumid, int duration, String pallete, String albumname, String artistid) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.artist = artist;
        this.imgUrl = imgUrl;
        this.albumid = albumid;
        this.pallete = pallete;
        this.albumname = albumname;
        this.artistid = artistid;
    }
    public Audio(String id, String title, String artist, String imgUrl, String albumid, int duration, String pallete, String albumname, String artistid,String path) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.path = path;
        this.artist = artist;
        this.imgUrl = imgUrl;
        this.albumid = albumid;
        this.pallete = pallete;
        this.albumname = albumname;
        this.artistid = artistid;
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


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumid() {
        return albumid;
    }

    public void setAlbumid(String albumid) {
        this.albumid = albumid;
    }

    public String getStorageUri() {
        return StorageUri;
    }

    public void setStorageUri(String storageUri) {
        StorageUri = storageUri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAlbumname() {
        return albumname;
    }

    public void setAlbumname(String albumname) {
        this.albumname = albumname;
    }

    public String getArtistid() {
        return artistid;
    }

    public void setArtistid(String artistid) {
        this.artistid = artistid;
    }

    public String getPallete() {
        return pallete;
    }

    public void setPallete(String pallete) {
        this.pallete = pallete;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
