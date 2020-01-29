package iwinux.com.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.Data.Server;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;

public class ExportVkActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Server server;
    EditText editText;
    TextView loading_text;
    ImageView next;
    static public MediaPlayerService playervk = MainActivity.player ;
    static ImageView controll;
    static RelativeLayout player_bottom;
    static TextView title_bottom;
    ImageView load_button;
    StorageUtil storageUtil;
    ProgressBar progressBar;
    static public String LINK_SAVE = "LINK_VK";
    private AudioAdapter adapter;
    private LinearLayoutManager layoutManagerPopslar;
    static public boolean serviceBoundVk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView( R.layout.activity_export_vk );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        content();
    }

    private void content() {
        server  = new Server(this );
        storageUtil = new StorageUtil( this );
        editText = findViewById( R.id.editText );
        loading_text = findViewById( R.id.load_text );
        progressBar = findViewById( R.id.prograss_bar );
        load_button = findViewById( R.id.import_vk );
        recyclerView = findViewById( R.id.recycler );
        player_bottom = findViewById( R.id.bottom_player );
        title_bottom = findViewById( R.id.title );
        next = findViewById( R.id.next );
        controll = findViewById( R.id.controll );
        title_bottom.setSelected(true);


        controll.setOnClickListener( v -> {
            if(!playervk.stop) {
                if (playervk.pause) {
                    playervk.pause = false;
                    controll.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
                    playervk.transportControls.play();
                } else {
                    playervk.pause = true;
                    controll.setImageDrawable( getResources().getDrawable( R.drawable.play ) );
                    playervk.transportControls.pause();
                }
            }else{
                playervk.stop = false;
                playervk.pause = false;
                playervk.playAudio();
            }
        } );
        next.setOnClickListener( v -> playervk.transportControls.skipToNext() );

        title_bottom.setOnClickListener( v -> {
            Intent intent = new Intent( v.getContext(), PlayerActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra( "id", new StorageUtil(this).loadAudioIndex() );
            v.getContext().startActivity( intent );
        } );

        if(!storageUtil.loadText( LINK_SAVE ).equals( "" )){
            editText.setText( storageUtil.loadText( LINK_SAVE ) );
//            StartSearch();
        }
        load_button.setOnClickListener( v->{
            if(!editText.getText().toString().equals( "" )){
                StartSearch();
            }
        } );

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(playervk != null){
            if(player_bottom !=null){
                initListenerControll( this );
                initStoping();
                Audio audio = new StorageUtil(this).loadAudio().get(
                        new StorageUtil( this ).loadAudioIndex()
                );
                initBottonPlayer(this,audio);
            }
        }else if(player_bottom !=null){
            player_bottom.setVisibility( View.GONE );

        }
    }
    void initBottonPlayer(Context context,Audio audio){
        player_bottom.setVisibility( View.VISIBLE );
        title_bottom.setText( audio.getTitle()  + " • " + audio.getArtist());
        if(playervk.pause || playervk.stop){
            controll.setImageDrawable( context.getResources().getDrawable( R.drawable.play ) );
        }else{
            controll.setImageDrawable( context.getResources().getDrawable( R.drawable.pause) );

        }
        playervk.setAudioList( (data, index) -> {
            initBottonPlayer(context,data);
        } );
    }
    private void initStoping() {
        playervk.setStopinglistener( ()->{
            controll.setImageDrawable(getResources().getDrawable( R.drawable.play ) );
        } );
    }
    void initListenerControll(Context context){
        playervk.setAudioListener( ()->{
            try {
                if(playervk.getPlaying()) {
                    controll.setImageDrawable( context.getResources().getDrawable( R.drawable.play ) );

                }else{
                    controll.setImageDrawable( context.getResources().getDrawable( R.drawable.pause ) );
                }
            }catch (Exception e){}
        } );
    }
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            playervk = binder.getService();
            Audio audio = storageUtil.loadAudio().get(
                    storageUtil.loadAudioIndex() );
            initBottonPlayer( getApplicationContext(),audio );
            initListenerControll( getApplicationContext() );
            initStoping();
            serviceBoundVk = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBoundVk = false;
        }
    };
    void StartSearch(){
        initAudio( new ArrayList<>(  ) );
        editText.setEnabled( false );
        load_button.setVisibility( View.GONE );
        progressBar.setVisibility( View.VISIBLE );
        loading_text.setVisibility( View.VISIBLE );
        loading_text.setText( getResources().getString( R.string.loading_text ) );
        server.GetAudioVk( this::initAudio,editText.getText().toString(), e -> {
            load_button.setVisibility( View.VISIBLE );
            editText.setEnabled( true );
            progressBar.setVisibility( View.GONE );
            loading_text.setText( e );

        } );
    }

    @Override
    public void onBackPressed() {

        if(serviceBoundVk) {
            MainActivity.serviceBound = true;
            unbindService( serviceConnection );
            serviceBoundVk = false;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void initAudio(List<Audio> data){
        editText.setEnabled( true );
        if(data.size() != 0)
        storageUtil.saveText( LINK_SAVE,editText.getText().toString() );
        load_button.setVisibility( View.VISIBLE );
        progressBar.setVisibility( View.GONE );
        loading_text.setText( getResources().getString( R.string.download_now ) );
        recyclerView.setItemViewCacheSize(20);
        Log.println(Log.ERROR,"CHECK", String.valueOf( data.size() ));
        adapter = new AudioAdapter( this, data ) {
            @Override
            public void load() {

            }
        };
        adapter.setOnClickListen( (datal, position, v) -> {
            MainActivity.audioList = data;
            Log.e("Oclick", "initAudio: "+position );

            if(playervk ==null)
            MainActivity.playAudio( position,v.getContext(),recyclerView,()->{
                if(!serviceBoundVk ){
                    Intent playerIntent = new Intent(v.getContext(), MediaPlayerService.class);
                    playerIntent.setAction( Intent.ACTION_USER_FOREGROUND);
                    bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                }
            },(dataa,index)->{
                initBottonPlayer(getApplicationContext(),dataa);
            });
            else
              MainActivity.playAudio( position,v.getContext(),recyclerView );

        } );
        layoutManagerPopslar = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setAdapter( adapter );

    }
}
