package iwinux.com.music.Objects;

public class Genre {
    private String name;
    private String urlImg;
    private String url;
    public Genre(String name, String urlImg, String url){
        this.name = name;
        this.urlImg = urlImg;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }
}
