package iwinux.com.music.Objects;

public class SettingsUser {
    private Boolean adaptivePlayer;
    private Boolean nightTheme;
    private Boolean premium;
    private Boolean pay;
    private String promocode;
    private String id;
    private long timeActive;
    private Boolean fullScreen;
    public SettingsUser(Boolean adaptivePlayer, Boolean nightTheme, Boolean premium, Boolean pay, String promocode, String id, long timeActive, Boolean fullScreen) {
        this.adaptivePlayer = adaptivePlayer;
        this.nightTheme = nightTheme;
        this.premium = premium;
        this.pay = pay;
        this.promocode = promocode;
        this.id = id;
        this.timeActive = timeActive;
        this.fullScreen = fullScreen;
    }

    public Boolean getAdaptivePlayer() {
        return adaptivePlayer;
    }

    public void setAdaptivePlayer(Boolean adaptivePlayer) {
        this.adaptivePlayer = adaptivePlayer;
    }

    public Boolean getNightTheme() {
        return nightTheme;
    }

    public void setNightTheme(Boolean nightTheme) {
        this.nightTheme = nightTheme;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public String getPromocode() {
        return promocode;
    }

    public void setPromocode(String promocode) {
        this.promocode = promocode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeActive() {
        return timeActive;
    }

    public void setTimeActive(long timeActive) {
        this.timeActive = timeActive;
    }

    public Boolean getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(Boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public Boolean getPay() {
        return pay;
    }

    public void setPay(Boolean pay) {
        this.pay = pay;
    }
}
