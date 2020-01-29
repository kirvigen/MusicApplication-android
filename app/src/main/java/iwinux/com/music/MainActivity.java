package iwinux.com.music;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Data.Server;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.fragments.MoreInfo_fragment;
import iwinux.com.music.fragments.genre_fragment;
import iwinux.com.music.fragments.like_fragment;
import iwinux.com.music.fragments.list_fragment;
import iwinux.com.music.fragments.new_fragment;
import iwinux.com.music.fragments.search_fragment;
import iwinux.com.music.fragments.top_fragment;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.Adapters.BottomNavAdapter;
import iwinux.com.music.utils.MyFirebaseMessagingService;
import iwinux.com.music.utils.MySqlDatabase;
import iwinux.com.music.utils.NoSwipePager;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

import static iwinux.com.music.ExportVkActivity.serviceBoundVk;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final String Broadcast_PLAY_NEW_AUDIO = "muzlo.iwiwinux.com.myspotify.PlayNewAudio";

    static public MediaPlayerService player;
    static boolean serviceBound = false;
    public static List<Audio> audioList = new ArrayList<>(  );
    BottomNavigationViewEx navigationView;
    static public ImageView playlist;
    FragmentTransaction ft;
    static Bundle save;
    StorageUtil storage;
    RecyclerView recyclerView;
    static public AudioAdapter audioAdapter;
    static NoSwipePager viewPager;
    public static int Frame = 0;
    static public FrameLayout frameLayout,frameLayout1;
    AudioAdapter adapter;
    LinearLayoutManager layoutManagerPopslar;
    int imageIndex = 0;
    static public ServiceConnection serviceConnection;
    static Utils util;
    Server server;
    UserManager userManager;
    ImageView next;
    MySqlDatabase sqlDatabase;
    static ImageView controll;
    static RelativeLayout player_bottom;
    static TextView title_bottom;
    BottomNavAdapter adapterBottom;
    static  public FragmentManager fragmentManager;
    Audio test;
    String TAG = "MAINACTIVITY";
    FirebaseAnalytics firebaseAnalytics;
    public static ActivityCheckout mCheckout;
    Inventory mInventory;
    static public FirebaseAnalytics mFirebaseAnalytics;

    private class PurchaseListener extends EmptyRequestListener<Purchase> {
        @Override
        public void onSuccess(Purchase purchase) {
            // here you can process the loaded purchase
                SettingsUser settingsUser = userManager.getsettings();
                settingsUser.setPremium( true );
                settingsUser.setPay( true );
                userManager.createSettings( settingsUser );
                Intent intent = new Intent( getApplicationContext(), MainActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
        }

        @Override
        public void onError(int response, Exception e) {
            // handle errors here
            Log.e( TAG, "onError: "+response+e.getMessage()  );
        }
    }
    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            // your code here
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate( savedInstanceState );
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        server = new Server( this );
        server.ServerGetAD();
        fragmentManager = getSupportFragmentManager();
        mCheckout = Checkout.forActivity( this, MyApplication.get().getBilling());
        userManager = new UserManager( this );
        util = new Utils();
        if(!util.isMyServiceRunning( MyFirebaseMessagingService.class,this )){
            Intent intent = new Intent( this,MyFirebaseMessagingService.class );
            startService( intent );
        }

        storage = new StorageUtil( this );
        storage.openApp();
        if(!userManager.getFirstOpen()){
            userManager.setFirstOpen( true );
        }
        if(storage.getOpen()>10){
            util.LoadGenre( this );
            storage.saveText( storage.OpenApp,"0" );
        }

        mCheckout.start();
        mCheckout.createPurchaseFlow(new PurchaseListener());
        mInventory = mCheckout.makeInventory();
        mInventory.load(
                Inventory.Request.create().loadAllPurchases()
                        .loadSkus( ProductTypes.IN_APP,getResources().getString( R.string.no_ad_id )),
                new InventoryCallback());
        if(userManager.getsettings().getFullScreen())
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView( R.layout.activity_main );
        firebaseAnalytics = FirebaseAnalytics.getInstance( this );
        content();
        }catch (Exception e){
            Intent intent  = new Intent( this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
            finish();
        }

        Bundle params = new Bundle();
        params.putString("open","true");
        mFirebaseAnalytics.logEvent("app_open", params);
        mFirebaseAnalytics.setUserProperty( "isPremium", String.valueOf( userManager.getsettings().getPremium() ) );
        mFirebaseAnalytics.setUserProperty( "isPay", String.valueOf( userManager.getsettings().getPay() ) );
        mFirebaseAnalytics.setUserProperty( "isFullScreen", String.valueOf( userManager.getsettings().getFullScreen() ) );
        mFirebaseAnalytics.setUserProperty( "isAdaptivePlayer", String.valueOf( userManager.getsettings().getAdaptivePlayer() ) );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onBackPressed() {
        if(Frame == 2) {
            frameLayout1.removeAllViews();
            Frame = 1;
        }
        else if(Frame == 1) {
            frameLayout.removeAllViews();
            Frame = 0;
        }
        if(viewPager.getCurrentItem() == 3)
        if(like_fragment.adapter !=null){
            if(like_fragment.like_)
                like_fragment.initAudio( storage.loadLikesAudio(),true,this );
            else like_fragment.initAudio( storage.loadDonloadAudio(),false,this );
            like_fragment.initAlbums( util.turn_overAlbum( storage.loadLikesAlbums( )),this );
        }

    }
    void checkFrame(){
        if(Frame != 0) {
            frameLayout.removeAllViews();
            frameLayout1.removeAllViews();
            Frame = 0;
        }
    }
    private void content() {
        recyclerView = findViewById( R.id.recycler );
        navigationView = findViewById( R.id.navigation );
        viewPager = findViewById( R.id.fl_content );
        frameLayout = findViewById( R.id.frame );
        player_bottom = findViewById( R.id.bottom_player );
        title_bottom = findViewById( R.id.title );
        next = findViewById( R.id.next );
        controll = findViewById( R.id.controll );
        playlist = findViewById( R.id.playlist );
        sqlDatabase = new MySqlDatabase( this,null,1 );

        title_bottom.setSelected(true);
        storage.filterAudio();


        adapterBottom = new BottomNavAdapter( getSupportFragmentManager() );
        adapterBottom.addFragments( new top_fragment() );
        adapterBottom.addFragments( new new_fragment() );
        adapterBottom.addFragments( new search_fragment() );
        adapterBottom.addFragments( new like_fragment() );
        adapterBottom.addFragments( new genre_fragment() );
        viewPager.setAdapter( adapterBottom );
        frameLayout1 = findViewById( R.id.frame1 );
        viewPager.setPagingEnabled( false );
        navigationView.setOnNavigationItemSelectedListener( menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_top:
                    Bundle paramss = new Bundle();
                    paramss.putString("open","top");
                    mFirebaseAnalytics.logEvent("app_open", paramss);
                    checkFrame();
                    viewPager.setCurrentItem( 0 ,false);
                    return true;
                case R.id.navigation_news:
                    checkFrame();
                    Bundle params = new Bundle();
                    params.putString("open","news");
                    mFirebaseAnalytics.logEvent("user_open", params);
                    viewPager.setCurrentItem( 1,false );
                    return true;
                case R.id.navigation_sg:
                    Bundle parass = new Bundle();
                    parass.putString("open","search");
                    mFirebaseAnalytics.logEvent("user_open", parass);
                    checkFrame();
                    viewPager.setCurrentItem( 2,false );
                    return true;
                case R.id.navigation_like:
                    Bundle par = new Bundle();
                    par.putString("open","like");
                    mFirebaseAnalytics.logEvent("user_open", par);
                    checkFrame();
                    if(like_fragment.adapter !=null){
                        if(like_fragment.like_)
                        like_fragment.initAudio( storage.loadLikesAudio(),true,this );
                        else like_fragment.initAudio( storage.loadDonloadAudio(),false,this );
                        like_fragment.initAlbums( util.turn_overAlbum( storage.loadLikesAlbums( )),this );
                    }
                    viewPager.setCurrentItem( 3,false );
                    return true;
                case R.id.navigation_profile:
                    Bundle pars = new Bundle();
                    pars.putString("open","genre");
                    mFirebaseAnalytics.logEvent("user_open", pars);
                    checkFrame();
                    viewPager.setCurrentItem( 4,false );
                    return true;
            }
            return false;
        } );
        navigationView.setTextSize( util.dpToPixel( 3.5f,this ) );
        navigationView.enableAnimation( false );
        navigationView.enableShiftingMode(false);
        navigationView.enableItemShiftingMode(false);
        controll.setOnClickListener( v -> {
            if(!player.stop) {
                if (player.pause) {
                    player.pause = false;
                    controll.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
                    player.transportControls.play();
                } else {
                    player.pause = true;
                    controll.setImageDrawable( getResources().getDrawable( R.drawable.play ) );
                    player.transportControls.pause();
                }
            }else{
                player.stop = false;
                player.pause = false;
                player.playAudio();
            }
        } );
        next.setOnClickListener( v -> player.transportControls.skipToNext() );

        title_bottom.setOnClickListener( v -> {
            Intent intent = new Intent( v.getContext(), PlayerActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra( "id", new StorageUtil(this).loadAudioIndex() );
            v.getContext().startActivity( intent );
        } );
        if(storage.loadLikesAudio().size() >0 || storage.loadLikesAlbums().size() >0){
            navigationView.setCurrentItem( 3 );
        }
        Log.d( TAG, "content: "+sqlDatabase.loadAudiosHistory( 5 ).toString() );
        if(getIntent().getStringExtra( "artist" ) != null){
            loadFragment( MoreInfo_fragment.newInstance( getIntent().getStringExtra( "artist" ) ) );
        }else if(getIntent().getStringExtra( "find" ) != null){
            navigationView.setCurrentItem( 2 );
        }else if(getIntent().getStringExtra( "playlist" ) != null){

            loadFragment( list_fragment.newInstance(getResources().getString( R.string.play_now ),null) );
        }
    }

    void loadFragment(Fragment fragment) {
        Frame = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }
    static void loadFragment(Fragment fragment,Context context) {
        Frame = 1;
        FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
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
    static void initBottonPlayer(Context context,Audio audio){
        player_bottom.setVisibility( View.VISIBLE );
        viewPager.setPadding( 0,0, 0,util.dpToPixel( 50,context ));
        frameLayout1.setPadding( 0,0,0, util.dpToPixel( 50,context ));
        frameLayout.setPadding( 0,0,0, util.dpToPixel( 50,context ));
        title_bottom.setText( audio.getTitle()  + " • " + audio.getArtist());
        playlist.setOnClickListener( v->{
            loadFragment( list_fragment.newInstance( context.getResources().getString( R.string.play_now ),
                    null ), context);
        } );
        if(player.pause|| player.stop){
            controll.setImageDrawable( context.getResources().getDrawable( R.drawable.play ) );
        }else{
            controll.setImageDrawable( context.getResources().getDrawable( R.drawable.pause) );

        }
        player.setAudioList( (data, index) -> {
            initBottonPlayer(context,data);
        } );
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(player != null){
            if(player_bottom !=null){
                initListenerControll( this );
                initStoping(this);
                Audio audio = new StorageUtil(this).loadAudio().get(
                        new StorageUtil( this ).loadAudioIndex()
                );
                initBottonPlayer(this,audio);
            }
        }else if(player_bottom !=null){
            checkFrame();
            player_bottom.setVisibility( View.GONE );
            setNullPadding();
        }
    }

    //Binding this Client to the AudioPlayer Service
    static void setNullPadding(){
        viewPager.setPadding( 0,0, 0,0);
        frameLayout1.setPadding( 0,0,0, 0);
        frameLayout.setPadding( 0,0,0, 0);
    }
    static void initListenerControll(Context context){
        player.setAudioListener( ()->{
            try {
                if(player.getPlaying()) {
                    controll.setImageDrawable( context.getResources().getDrawable( R.drawable.play ) );
                }else{
                    controll.setImageDrawable( context.getResources().getDrawable( R.drawable.pause ) );
                }
            }catch (Exception e){}
        } );
    }
    static void initPlay(Context context){
        Audio audio = new StorageUtil(context).loadAudio().get(
                new StorageUtil( context ).loadAudioIndex()
        );
        initBottonPlayer(context,audio);
    }
    public static void playAudio(int audioIndex, Context context,RecyclerView recyclerView) {
        //Check is service is active
        if (!serviceBound) {
            Log.i( "play Audio", "playAudio: not" );
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(context);
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
            Intent playerIntent = new Intent(context, MediaPlayerService.class);
            playerIntent.setAction( Intent.ACTION_USER_FOREGROUND);
            context.startService(playerIntent);

           serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
                    player = binder.getService();
                    serviceBound = true;
                    player.initRecycler( recyclerView );
                    initPlay( context );
                    player.setAudioList( (data, index) -> {
                        initBottonPlayer(context,data);
                    } );
                    initListenerControll(context);
                    initStoping(context);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    player_bottom.setVisibility( View.GONE );
                    setNullPadding();
                    serviceBound = false;
                }
            };
            context.bindService(playerIntent, serviceConnection, BIND_IMPORTANT);

        } else {
            Log.i( "play Audio", "playAudio: yes" );
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(context);
            storage.storeAudioIndex(audioIndex);
            storage.storeAudio(audioList);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO

             player.initRecycler( recyclerView );
             player.playAudio();

        }
    }
    public static void playAudio(int audioIndex, Context context, RecyclerView recyclerView, Interfaces.OnCall calll, Interfaces.OnAudioListener call) {
        //Check is service is active

        if (!serviceBound && !serviceBoundVk && ExportVkActivity.playervk == null) {
            Log.i( "play Audio", "playAudio: not" );
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(context);
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);
            Intent playerIntent = new Intent(context, MediaPlayerService.class);
            playerIntent.setAction( Intent.ACTION_USER_FOREGROUND);
            context.startService(playerIntent);

            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
                    player = binder.getService();
                    serviceBound = true;
                    player.initRecycler( recyclerView );
                    initPlay( context );
                    calll.call();
                    player.setAudioList( (data, index) -> {
                        if(call !=null)
                        call.getAudio( data,index );
                        initBottonPlayer(context,data);
                    } );
                    initListenerControll(context);
                    initStoping(context);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    player_bottom.setVisibility( View.GONE );
                    setNullPadding();
                    serviceBound = false;
                }
            };
            try {
                context.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }catch (Exception e){

            }


        } else {
            Log.i( "play Audio", "playAudio: yes" );
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(context);
            storage.storeAudioIndex(audioIndex);
            storage.storeAudio(audioList);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO

            player.initRecycler( recyclerView );
            player.playAudio();

        }
    }
    private static void initStoping(Context context) {
        player.setStopinglistener( ()->{
            controll.setImageDrawable( context.getResources().getDrawable( R.drawable.play ) );
        } );
    }

    @Override
    protected void onDestroy() {


        if (serviceBound) {
            try {
                unbindService( serviceConnection );
            }catch (Exception e){

            }
            //service is active
            player.stopSelf();

        }
        super.onDestroy();
    }
}
