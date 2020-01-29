package iwinux.com.music.Objects;

public class Ad {
    private String top_fragment;
    private String new_fragment;
    private String search_fragment;
    private String lenta;
    private String perehod;

    public Ad(String top_fragment, String new_fragment, String search_fragment, String lenta, String perehod) {
        this.top_fragment = top_fragment;
        this.new_fragment = new_fragment;
        this.search_fragment = search_fragment;
        this.lenta = lenta;
        this.perehod = perehod;
    }

    public String getTop_fragment() {
        return top_fragment;
    }

    public void setTop_fragment(String top_fragment) {
        this.top_fragment = top_fragment;
    }

    public String getNew_fragment() {
        return new_fragment;
    }

    public void setNew_fragment(String new_fragment) {
        this.new_fragment = new_fragment;
    }

    public String getSearch_fragment() {
        return search_fragment;
    }

    public void setSearch_fragment(String search_fragment) {
        this.search_fragment = search_fragment;
    }

    public String getLenta() {
        return lenta;
    }

    public void setLenta(String lenta) {
        this.lenta = lenta;
    }

    public String getPerehod() {
        return perehod;
    }

    public void setPerehod(String perehod) {
        this.perehod = perehod;
    }
}
