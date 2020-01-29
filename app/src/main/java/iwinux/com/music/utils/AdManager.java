package iwinux.com.music.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import iwinux.com.music.Objects.Ad;
import iwinux.com.music.R;

public class AdManager  {
    UserManager userManager;
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    private String PREF_NAME = "AD";
    private String TOP_FRAGMENT = "TOP_FRAGMENT";
    private String NEW_FRAGMENT = "NEW_FRAGMENT";
    private String SEARCH_FRAGMENT = "SEARCH_FRAGMENT";
    private String LENTA = "LENTA";
    private String Count_Perehod = "COUNT_PEREHOD";
    private String PEREHOD = "PEREHOD";
    public AdManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userManager = new UserManager( context );
    }
    public void setPerehod(){
        int perehod = Integer.valueOf(getAd().getPerehod());
        int count = getCount()+1;
        saveCount( count );
        if(count >= perehod && !userManager.getsettings().getPremium() && !userManager.getsettings().getPay() ){
            InterstitialAd mInterstitialAd;
            mInterstitialAd = new InterstitialAd( context );
            mInterstitialAd.setAdUnitId(context.getResources().getString( R.string.Ad_Perehod));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mInterstitialAd.show();
                    mInterstitialAd.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed() {
                            saveCount( 0 );
                        }
                    });
                }

                @Override
                public void onAdClosed() {
                    // Load the next interstitial.
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }

            });


        }

    }
    public void saveCount(int count){
        editor.putInt( Count_Perehod,count );
        editor.apply();
    }
    public int getCount(){
        return sharedPreferences.getInt( Count_Perehod,0 );
    }
    public void setAd(Ad ad){
        editor.putString( TOP_FRAGMENT,ad.getTop_fragment() );
        editor.putString( NEW_FRAGMENT,ad.getNew_fragment() );
        editor.putString( SEARCH_FRAGMENT,ad.getSearch_fragment() );
        editor.putString( LENTA,ad.getLenta() );
        editor.putString( PEREHOD,ad.getPerehod() );
        editor.apply();
    }
    public Ad getAd(){
        Ad ad =new Ad( sharedPreferences.getString( TOP_FRAGMENT,"1" ),
                sharedPreferences.getString( NEW_FRAGMENT,"1" ),
                sharedPreferences.getString( SEARCH_FRAGMENT,"1" ),
                sharedPreferences.getString( LENTA,"4,10,14"),
                sharedPreferences.getString( PEREHOD,"10" ));
        return ad;
    }
}
