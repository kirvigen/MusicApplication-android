package iwinux.com.music.utils;

import android.content.Context;
import android.content.SharedPreferences;

import iwinux.com.music.Objects.SettingsUser;

public class UserManager {
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;

    private String PREF_NAME = "SETTINGS";
    private String SAVE_AdaptivePlayer = "ADAPTIVE_PLAYER";
    private String SAVE_nightTheme = "NIGHTTHEME";
    private String PREMIUM = "PREMIUM";
    private String PROMO = "PROMO";
    private String FULL_SCREEN = "FULL_SCREEN";
    private String ID = "ID";
    private String First_OPEN = "FIRST_OPEN";
    private String Pay = "PAY";
    private String TIME = "TIME";

    public UserManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public void createSettings(SettingsUser settings){
        editor.putBoolean( SAVE_AdaptivePlayer,settings.getAdaptivePlayer() );
        editor.putBoolean( PREMIUM,settings.getPremium() );
        editor.putBoolean( SAVE_nightTheme,settings.getNightTheme() );
        editor.putString( PROMO,settings.getPromocode() );
        editor.putString( ID,settings.getId() );
        editor.putLong( TIME,settings.getTimeActive() );
        editor.putBoolean( FULL_SCREEN,settings.getFullScreen() );
        editor.apply();
    }
    public SettingsUser  getsettings(){
         SettingsUser settingsUser = new SettingsUser(  sharedPreferences.getBoolean( SAVE_AdaptivePlayer,true ),
                 sharedPreferences.getBoolean( SAVE_nightTheme,false ),
                 sharedPreferences.getBoolean( PREMIUM,false ),
                 sharedPreferences.getBoolean( Pay,false )
                 , sharedPreferences.getString( PROMO,"" ),
                 sharedPreferences.getString( ID ,""),
                 sharedPreferences.getLong( TIME,0 ),
                 sharedPreferences.getBoolean( FULL_SCREEN,false ) );
        return  settingsUser;
    }
    public void saveNightTheme(Boolean check){
        editor.putBoolean( SAVE_nightTheme,check );
        editor.apply();
    }
    public void saveFullScreen(Boolean check){
        editor.putBoolean( FULL_SCREEN,check );
        editor.apply();
    }
    public void setAdaptivePlayer(Boolean check){
        editor.putBoolean( SAVE_AdaptivePlayer,check );
        editor.apply();
    }
    public void setPREMIUM(Boolean check){
        editor.putBoolean( PREMIUM ,check );
        editor.apply();
    }
    public Boolean getFirstOpen(){
        return sharedPreferences.getBoolean( First_OPEN,false );
    }
    public void setFirstOpen(Boolean check){
        editor.putBoolean( First_OPEN ,check );
        editor.apply();
    }
}
