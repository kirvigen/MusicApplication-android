package iwinux.com.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import iwinux.com.music.Objects.Audio;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

import static iwinux.com.music.Data.p_Strings_Data.size_superbig;


public class PlayerActivity extends AppCompatActivity {
    List<Audio> audioList;
    ImageView preview,route,previews,next,like,download;
    TextView end,start,title,artist;
    SeekBar seekBar;
    Window window;
    ImageView repeat,playlist;
    Utils util;
    UserManager userManager;
    int Thumb_size = 13;
    StorageUtil storage;
    RelativeLayout backgraund;
    Audio audio;
    int activeaudio;
    MediaPlayerService player = MainActivity.player;
    Boolean serviceBound = false;
    String TAG = "PlayerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_player );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        userManager = new UserManager( this );

//            bindService( new Intent( this,MediaPlayerService.class ),serviceConnection, Context.BIND_ABOVE_CLIENT);
            content();

    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void content() {
        storage = new StorageUtil( this );
        audioList = storage.loadAudio();
        util = new Utils();
        window = getWindow();
        playlist = findViewById( R.id.playlist );
        download = findViewById( R.id.download );
        preview = findViewById( R.id.preview );
        title = findViewById( R.id.title );
        artist = findViewById( R.id.Artist );
        seekBar = findViewById( R.id.seek_bar );
        end = findViewById( R.id.end );
        start = findViewById( R.id.start );
        route = findViewById( R.id.route );
        next = findViewById( R.id.next );
        previews = findViewById( R.id.previews );
        repeat = findViewById( R.id.repeat );
        backgraund = findViewById( R.id.background );
        download = findViewById( R.id.download );
        activeaudio = getIntent().getIntExtra( "id",0 );
        like = findViewById( R.id.like );
        try {
            audio = MainActivity.player.getAudioTrack();
        }catch (Exception e ){
            audio = audioList.get( storage.loadAudioIndex() );
        }
        setData( audio );
        playlist.setOnClickListener( v->{
            Intent intent = new Intent( v.getContext(), MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra( "playlist", "1"  );
            startActivity( intent );
        } );
        repeat.setOnClickListener( v->{
            if(!player.repeat){
                player.repeat = true;
                repeat.setColorFilter(getResources().getColor( R.color.red_light ));
            }else{
                player.repeat = false;
                repeat.setColorFilter(Color.BLACK);
            }
        } );
        title.setSelected(true);
        artist.setOnClickListener( v -> {
            Intent intent = new Intent( v.getContext(), MainActivity.class );
            Log.i( TAG, "content: " + audio.getArtistid() );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            if(!audio.getId().contains( "vk:" )) {
                intent.putExtra( "artist", audio.getArtistid().split( "," )[0] );
            }else{
                if(audio.getArtist().split( "," ).length ==0)
                intent.putExtra( "find", audio.getArtist() );
                else
                    intent.putExtra( "find", audio.getArtist().split( "," )[0] );
            }
            startActivity( intent );
        } );
        like.setOnClickListener( (v)->{
            if(storage.checkLikeAudio( audio )){

                storage.deletedLikesAudio( audio );
                like.setImageDrawable( getResources().getDrawable( R.drawable.like_outline  ) );
                Log.i( TAG, "content: TRUE" );
            }else{
                storage.addlikesAudio( audio );
                like.setImageDrawable( getResources().getDrawable( R.drawable.like ) );

                Log.i( TAG, "content: False" );
            }

        } );
        initDownload();
        route.setOnClickListener( v -> {
            if(!player.pause){
                player.pause = true;
                player.transportControls.pause();
                route.setImageDrawable( getResources().getDrawable( R.drawable.play ) );
            }else{
                player.pause = false;
                player.transportControls.play();
                route.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
            }
        } );
        try {
            player.setStopinglistener( () -> {
                route.setImageDrawable( getResources().getDrawable( R.drawable.play ) );
            } );
        }catch (Exception e){

        }

        next.setOnClickListener( v -> player.transportControls.skipToNext() );
        previews.setOnClickListener( v ->  player.transportControls.skipToPrevious());
        PlayerActivity.this.runOnUiThread( new Runnable() {
            @Override
            public void run() {
                try {
                    if(MainActivity.player != null) {
                        seekBar.setProgress( player.getSeek() );
                        start.setText( util.initConvertTime( player.getSeek() ) );
                    }
                }catch (Exception e){
                    finish();
                }
          new Handler(  ).postDelayed( this,1000 )  ;
        }} );
        player.setAudioList( (data, index) -> setData( data ));
        player.setAudioListener( ()->{
           try {
           if(player.getPlaying()) {
               route.setImageDrawable( getResources().getDrawable( R.drawable.play ) );

           }else{
               route.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
           }
              }catch (Exception e){}
        }  );
        seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {
                  start.setText( util.initConvertTime( seekBar.getProgress() ) );
           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {
               if(player.timelife() >0)
              player.setSeek( seekBar.getProgress() );
           }
       } );
    }
    void initCazual(Drawable drawable,Audio data){
        if(userManager.getsettings().getAdaptivePlayer() && !audio.getId().contains( "vk:" )) {
            backgraund.setBackground( getDrawable( ((BitmapDrawable) drawable).getBitmap() ) );
            ShapeDrawable circle = new ShapeDrawable( new OvalShape() );
            circle.setIntrinsicHeight (util.dpToPixel( Thumb_size,this ));
            circle.setIntrinsicWidth (util.dpToPixel( Thumb_size,this ));
            circle.getPaint ().setColor (Color.parseColor( data.getPallete().split( "," )[1] ));
            seekBar.setThumb( circle );
            seekBar.getProgressDrawable()
                    .setColorFilter(Color.parseColor( data.getPallete().split( "," )[1] ),
                            PorterDuff.Mode.MULTIPLY);
            seekBar.getProgressDrawable().
                    setColorFilter(Color.parseColor( data.getPallete().split( "," )[1] ),
                            PorterDuff.Mode.SRC_ATOP);
        }
        else{
            backgraund.setBackgroundColor( getResources().getColor( R.color.white ) );
            ShapeDrawable circle = new ShapeDrawable( new OvalShape() );
            circle.setIntrinsicHeight (util.dpToPixel( Thumb_size,this ));
            circle.setIntrinsicWidth (util.dpToPixel( Thumb_size,this ));
            circle.getPaint ().setColor (getResources().getColor( R.color.red_light ));
            seekBar.setThumb( circle );
            seekBar.getProgressDrawable()
                    .setColorFilter(getResources().getColor( R.color.red_light ),
                            PorterDuff.Mode.MULTIPLY);
            seekBar.getProgressDrawable().
                    setColorFilter(getResources().getColor( R.color.red_light ),
                            PorterDuff.Mode.SRC_ATOP);
        }

    }
    Drawable getDrawable(Bitmap bitmap){
        int defadt = 0x000000;
        Palette palette = Palette.from(bitmap).generate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.clearFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags( WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            Log.i( "Player", "getDrawable: "+String.valueOf( palette.getDarkVibrantColor( defadt ) ) );
        if(palette.getDarkVibrantColor( defadt )*-1>16790320 || palette.getDarkVibrantColor( defadt ) ==0)
            window.setStatusBarColor(Color.parseColor( audio.getPallete().split( "," )[0] ));
           else window.setStatusBarColor(Color.parseColor( audio.getPallete().split( "," )[0] ));

        }
        int[] colors = {Color.parseColor( audio.getPallete().split( "," )[0] ), Color.parseColor( "#ffffff" )};
        GradientDrawable gd = new GradientDrawable( GradientDrawable.Orientation.TOP_BOTTOM, colors );
        gd.setCornerRadius( 0f );
        return gd;
    }
    void initDownload(){
        if(storage.getDownloadAudio( audio ) != null){

                download.setImageDrawable( getResources().getDrawable( R.drawable.ic_round_delete ) );
                download.setOnClickListener( v -> {
                    download.setImageDrawable( getResources().getDrawable( R.drawable.download) );

                        if (ActivityCompat.checkSelfPermission( v.getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions( (PlayerActivity)  v.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2 );
                            return;
                        } else {
                            if(!storage.getDownloadAudio( audio ).equals( "d" )) {
                                File f = new File( storage.getDownloadAudio( audio ).getPath() );
                                f.delete();
                            }
                        }


                    storage.deletedDownloadAudio( audio );
                    initDownload();
                });

            }
        else {
            download.setImageDrawable( getResources().getDrawable( R.drawable.download ) );
            download.setOnClickListener( v -> {
                if (ActivityCompat.checkSelfPermission( v.getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions( (PlayerActivity)  v.getContext(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2 );
                    return;
                } else {
                    util.DownloadFile( audio, v.getContext() ,true);
                    download.setImageDrawable( getResources().getDrawable( R.drawable.ic_round_delete ) );
                    initDownload();
                }

            } );
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {

            if(!player.pause) {
                route.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
            }else{
                route.setImageDrawable( getResources().getDrawable( R.drawable.play ) );
            }
            if(download !=null){
                initDownload();
            }
            setPrepare();
        }catch (Exception e){}
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    void setData(Audio data){
        audio = data;
        initDownload();

        if(!player.repeat){
            repeat.setColorFilter(Color.BLACK);
        }else{
            repeat.setColorFilter(getResources().getColor( R.color.red_light ));
        }
        route.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
        title.setText( data.getTitle() );
        if(storage.checkLikeAudio( audio ))
            like.setImageDrawable( getResources().getDrawable( R.drawable.like ) );
        else like.setImageDrawable( getResources().getDrawable( R.drawable.like_outline ) );
        artist.setText( data.getArtist() );
        Picasso.with( this ).load( util.getimgUrl( audio.getImgUrl(),size_superbig ) ).
                placeholder( getResources().getDrawable( R.drawable.placeholder ) ).
                into( preview, new Callback() {
            @Override
            public void onSuccess() {
                initCazual(preview.getDrawable(),audio);
            }

            @Override
            public void onError() {

            }
        } );
        setPrepare();
    }
    void setPrepare(){
        end.setText( util.initConvertTime( audio.getDuration() ));
        if(storage.checkLikeAudio( audio ))
            like.setImageDrawable( getResources().getDrawable( R.drawable.like ) );
        else like.setImageDrawable( getResources().getDrawable( R.drawable.like_outline ) );
        if(player != null)
        if(player.timelife() > 0){
            seekBar.setMax( audio.getDuration() );
            end.setText( util.initConvertTime(player.timelife()));
        }
        if(player!= null)
        player.setPrepare( () -> {
            route.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
            seekBar.setMax( audio.getDuration() );
            end.setText( util.initConvertTime(player.timelife() ));
        } );


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
    }

}
