package iwinux.com.music.Objects;

import java.util.List;

public class Block {
    private String name;
    private String UrlMore;
    private List<Audio> audio;
    private List<AlbumItem> albums;
    public Block(String name, String urlMore, List<Audio> audio, List<AlbumItem> albums) {
        this.name = name;
        UrlMore = urlMore;
        this.albums = albums;
        this.audio = audio;

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlMore() {
        return UrlMore;
    }

    public void setUrlMore(String urlMore) {
        UrlMore = urlMore;
    }

    public List<Audio> getAudio() {
        return audio;
    }

    public void setAudio(List<Audio> audio) {
        this.audio = audio;
    }

    public List<AlbumItem> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumItem> albums) {
        this.albums = albums;
    }


    public static class AlbumItem{
       private String name;
       private String imgurl;
       private String author;
       private String moreurl;

        public AlbumItem(String name, String imgurl,String moreurl,String author) {
            this.name = name;
            this.imgurl = imgurl;
            this.moreurl = moreurl;
            this.author = author;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImgurl() {
            return imgurl;
        }

        public void setImgurl(String imgurl) {
            this.imgurl = imgurl;
        }

        public String getMoreurl() {
            return moreurl;
        }

        public void setMoreurl(String moreurl) {
            this.moreurl = moreurl;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }
}
