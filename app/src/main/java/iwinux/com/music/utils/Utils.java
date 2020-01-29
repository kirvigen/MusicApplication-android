package iwinux.com.music.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.ProductTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.PlayerActivity;
import iwinux.com.music.R;

import static com.android.volley.VolleyLog.TAG;

public class Utils {
    Context context;
    Audio a;
    public Utils(){

    }
    public float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }
    public List<Audio> addAfter(Audio a, int position, List<Audio> data){
        List<Audio> afterData = new ArrayList<>();
        Log.i( TAG, "addAfterSise: "+data.size() );
        for(int i=0;i<position+1;i++){
            afterData.add( data.get( i ) );
            Log.i( TAG, "addAfter: "+i );
        }
        afterData.add( a );
        for(int i=position+2; i< data.size()+1;i++){
            afterData.add( data.get( i-1 ) );
            Log.i( TAG, "addAfter: "+i );
        }
        return afterData;
    }

    public boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void Pay(ActivityCheckout mCheckout,Context context){
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override public void onReady(@NonNull BillingRequests requests) {
                requests.purchase( ProductTypes.IN_APP,context.getResources()
                        .getString( R.string.no_ad_id ) , null, mCheckout.getPurchaseFlow());
            }
        });
    }
    public void LoadGenre(Context context){
        Server server =new Server( context );
        StorageUtil storageUtil = new StorageUtil( context );
        server.getGenre( (data -> {
            storageUtil.saveGenres( data );
        }) );
    }
    public void DownloadFile(Audio audio,Context context,Boolean player){
        Server server = new Server( context );
        this.a = audio;
        this.context = context;
        StorageUtil storageUtil = new StorageUtil( context );
        audio.setPath( "d" );
        storageUtil.addDownloadAudio( audio );
        if(!audio.getId().contains( "vk:" ))
        server.getSourseTrack( audio.getId(), (url, id) -> {

            //set title for notification in status_bar
           Download( url,player );
        }, e -> {

        } );
        else{
            Download( audio.getId().replace( "vk:","" ),player  );
        }

    }
    public void Download(String url, Boolean player){
        if (ActivityCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE ) !=
                PackageManager.PERMISSION_GRANTED) {
            ContextWrapper new_co = (ContextWrapper) context;
            Toast.makeText( context,"Дайте разрешение и попробуйте снова",Toast.LENGTH_SHORT ).show();
            if(player)
            ActivityCompat.requestPermissions( (PlayerActivity) new_co.getBaseContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2 );
            else
                ActivityCompat.requestPermissions( (MainActivity) new_co.getBaseContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2 );
            return;

        } else {
            Thread download_thread = new Thread_Download( context,url,a );
            download_thread.start();
            Toast.makeText(context,
                    "Загрузка начата...",
                    Toast.LENGTH_SHORT ).show();

        }
    }

    static public boolean isOnline(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        return nInfo != null && nInfo.isConnected();
    }
    public void DeleteFile(String path){
          try {
              File f = new File( path );
              f.delete();
          }catch (Exception e) {

          }
    }
    public Boolean DeleteFile(String path,String s){
        try {
            File f = new File( path );
           return f.delete();
        }catch (Exception e) {
            return false;
        }
    }
    public String getimgUrl(String url,String size){
        if(url.contains( "{size}" ))
           return url.replace( "{size}",size );
        else return url;
    }
    public String initConvertTime(int seconds) {
        if(seconds>900 || seconds<0) return "00:00";
        int minuts = seconds/60;
        int sec = seconds - minuts*60;
        String min="";
        if(minuts<10)
            min = "0"+String.valueOf( minuts );
        else
            min = String.valueOf( minuts );
        String secun ="";
        if(sec<10)
            secun = "0"+String.valueOf( sec );
        else secun = String.valueOf( sec );

        return min+":"+secun;
    }
    public float pixelToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public int dpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    public String resizeString(String string){
        if(string.length() >18){
            string = string.substring( 0,15 )+"...";
            return string;
        }
        return string;
    }
    public String resizeString(String string,int lenth){
        if(string.length() >lenth){
            string = string.substring( 0,lenth-3 )+"...";
            return string;
        }
        return string;
    }
    public List<Audio> turn_over(List<Audio> list){
        List<Audio> newList = new ArrayList<>(  );
        for(int i =0; i<list.size();i++){
            newList.add(list.get( list.size()-1-i ));
    }
        return  newList;
    }
    public List<Album> turn_overAlbum(List<Album> list){
        List<Album> newList = new ArrayList<>(  );
        for(int i =0; i<list.size();i++){
            newList.add(list.get( list.size()-1-i ));
        }
        return  newList;
    }
    public List<Artist> turn_overArtist(List<Artist> list){
        List<Artist> newList = new ArrayList<>(  );
        for(int i =0; i<list.size();i++){
            newList.add(list.get( list.size()-1-i ));
        }
        return  newList;
    }
    public Drawable getDrawable(int[] colors,GradientDrawable.Orientation orientation){
        GradientDrawable gd = new GradientDrawable(
              orientation, colors);
        gd.setCornerRadius(0f);
        return gd;
    }

}
