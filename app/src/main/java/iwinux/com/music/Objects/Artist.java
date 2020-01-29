package iwinux.com.music.Objects;

import java.io.Serializable;

public class Artist implements Serializable {
    private String id;
    private String description;
    private String img;
    private String title;
    private String palette;

    public Artist(String id, String description, String img, String title, String palette) {
        this.id = id;
        this.description = description;
        this.img = img;
        this.title = title;
        this.palette = palette;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPalette() {
        return palette;
    }

    public void setPalette(String palette) {
        this.palette = palette;
    }
}
