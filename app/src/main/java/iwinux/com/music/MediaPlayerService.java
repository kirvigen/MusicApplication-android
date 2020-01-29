package iwinux.com.music;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Data.Server;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.Data.p_Strings_Data.size_big;
import static iwinux.com.music.utils.Adapters.AudioAdapter.AD_TYPE;

/**
 * Created by Valdio Veliu on 16-07-11.
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {


    public static final String ACTION_PLAY = "muzlo.iwiwinux.com.myspotify.ACTION_PLAY";
    public static final String ACTION_PAUSE = "muzlo.iwiwinux.com.myspotify.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "muzlo.iwiwinux.com.myspotify.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "muzlo.iwiwinux.com.myspotify.ACTION_NEXT";
    public static final String ACTION_STOP = "muzlo.iwiwinux.com.myspotify.ACTION_STOP";
    public static final String ACTION_CLOSE= "muzlo.iwiwinux.com.myspotify.ACTION_Close";
    final String NOTIFI_ID = "muzlo.iwiwinux.com.myspotify";
    public MediaPlayer mediaPlayer;
    Interfaces.OnAudioListener onAudioListener = null;
    Interfaces.OnCall call = null;
    Interfaces.OnCall route = null;
    RecyclerView tracksrecycler;
    ImageView controll;
    Boolean stop = false;
    Bitmap album = null;
    Boolean repeat = false;
    Utils utils;
    Boolean headsActive = false;
    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    public MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    //Used to pause/resume MediaPlayer
    private int resumePosition;
    Boolean prepare = false;
    //AudioFocus
    private AudioManager audioManager;

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    //List of available Audio files
//    Boolean pause  = false;
    private List<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //an object on the currently playing audio
    public Boolean pause = false;
    View itemViews;
    private Server server;
    Interfaces.OnCall stoping;
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    RemoteControlClient mRemoteControlClient;

    /**
     * Service lifecycle methods
     */
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures
        server = new Server( this );
        utils = new Utils();
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();

        registerReceiver(
                becomingNoisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            //Load data from SharedPreferences

            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus

            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return START_REDELIVER_INTENT;
    }
    final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pause = true;
               transportControls.pause();
            }
        }
    };


    @Override
    public boolean onUnbind(Intent intent) {
        try {
            mediaPlayer.release();
        }catch (Exception e){

        }
        removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            removeNotification();
            stopMedia();
            mediaPlayer = null;
        }
        removeNotification();
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        try {
            unregisterReceiver( becomingNoisyReceiver );
            unregisterReceiver( playNewAudio );
        }catch (Exception e ) {
             register_playNewAudio();
        }

        //clear cached playlist
//        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }
    public void addQouin(Audio a){
        Log.i( TAG, "addQouin: "+utils.addAfter( a,audioIndex,audioList ).size() +" sds d sd sd "+ audioList.size() );
        StorageUtil storageUtil = new StorageUtil( this );
        List<Audio> afters = utils.addAfter( a,audioIndex,audioList );
        storageUtil.storeAudio( afters );
        audioList = afters;

    }
    public void initRecycler(RecyclerView recyclerView) {
        try {
            if (tracksrecycler != null) setAllNotPlay();
            AdManager adManager = new AdManager( this );
            UserManager userManager = new UserManager( this );
            tracksrecycler = recyclerView;
            Log.i( TAG, "initRecycler: " + new StorageUtil( getApplicationContext() ).loadAudioIndex() );
            try {
                Audio audio = ((AudioAdapter) tracksrecycler.getAdapter()).getAudios().get( audioIndex );
                Log.i( TAG, "initRecycler: "+audio.getTitle() );
                if(audio.getId().equals( audioList.get( audioIndex ).getId() ))
                if (tracksrecycler.findViewHolderForAdapterPosition( new StorageUtil( getApplicationContext() ).loadAudioIndex() ) != null) {
                    String[] strings = adManager.getAd().getLenta().split( "," );
                    if (!userManager.getsettings().getPay() && !userManager.getsettings().getPremium() && !((AudioAdapter) tracksrecycler.getAdapter()).like_list && ((AudioAdapter) tracksrecycler.getAdapter()).vk != 1) {
                        int first = 500, two = 500, three = 500;
                        if (adManager.getAd().getLenta().split( "," ).length == 1) {
                            first = (Integer.valueOf( adManager.getAd().getLenta() ));
                            two = 9000;
                            three = 9000;
                        } else {
                            try {
                                first = Integer.valueOf( strings[0] );
                                two = Integer.valueOf( strings[1] );
                                three = Integer.valueOf( strings[2] );
                            } catch (Exception e) {

                            }
                        }
                        int position = new StorageUtil( getApplicationContext() ).loadAudioIndex();
                        int k = 0;
                        if (position >= first - 1) k += 1;
                        if (position >= two - 1) k += 1;
                        if (position >= three - 2) k += 1;
                        itemViews = tracksrecycler.findViewHolderForAdapterPosition( position + k ).itemView;
                        Log.e( TAG, "initRecycler: " + position + k );
                        itemViews.findViewById( R.id.play ).setVisibility( View.VISIBLE );
                        controll = itemViews.findViewById( R.id.route );
                        controll.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
                    } else {
                        itemViews = tracksrecycler.findViewHolderForAdapterPosition( new StorageUtil( getApplicationContext() ).loadAudioIndex() ).itemView;
                        itemViews.findViewById( R.id.play ).setVisibility( View.VISIBLE );
                        controll = itemViews.findViewById( R.id.route );
                        controll.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
                    }
                } else {
//            Toast.makeText( getApplicationContext(),"NULL",Toast.LENGTH_LONG ).show();
                }
                else{
                    setAllNotPlay();
                }
            } catch (Exception e) {

            }
        }catch (Exception e){

        }

    }
    void setControll (PlaybackStatus status){
        try {
            if (status == PlaybackStatus.PLAYING) {
                if (mRemoteControlClient != null) mRemoteControlClient.setPlaybackState( RemoteControlClient.PLAYSTATE_PLAYING );
                controll.setImageDrawable( getResources().getDrawable( R.drawable.pause ) );
            } else {
                if (mRemoteControlClient != null)
                    mRemoteControlClient.setPlaybackState( RemoteControlClient.PLAYSTATE_PAUSED );
                controll.setImageDrawable( getResources().getDrawable( R.drawable.play ) );
            }
        }catch (Exception e){

        }

    }

    void setAllNotPlay(){
        AdManager adManager = new AdManager( this );
        UserManager userManager = new UserManager( this );
        if(!userManager.getsettings().getPay() && !userManager.getsettings().getPremium())
        for(int i=0;i<(tracksrecycler.getAdapter().getItemCount()+adManager.getAd().getLenta().split( "," ).length);i++){
         if(tracksrecycler.findViewHolderForAdapterPosition(i) != null && tracksrecycler.getAdapter().getItemViewType( i ) != AD_TYPE)
             tracksrecycler.findViewHolderForAdapterPosition( i ).itemView.findViewById( R.id.play ).setVisibility( View.GONE );

        }
        else{
            for(int i=0;i<tracksrecycler.getAdapter().getItemCount();i++){
                if(tracksrecycler.findViewHolderForAdapterPosition(i) != null && tracksrecycler.getAdapter().getItemViewType( i ) != AD_TYPE)
                    tracksrecycler.findViewHolderForAdapterPosition( i ).itemView.findViewById( R.id.play ).setVisibility( View.GONE );

            }
        }
    }

    /**
     * Service Binder
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }


    /**
     * MediaPlayer callback methods
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        Log.i( TAG, "onCompletion: CALL" );
        if(!pause && !stop && !repeat){
            StorageUtil storageUtil = new StorageUtil( this );
            storageUtil.saveHistoryAudio( activeAudio );
            transportControls.skipToNext();
        }
        else if(!pause && !stop && repeat){
            activeAudio = null;
            playAudio();
        }
//        else {
//            stopMedia();
//            removeNotification();
//            //stop the service
//            stopSelf();
//        }
////        else{
////            stopMedia();
////            removeNotification();
////            //stop the service
////            stopSelf();
////        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                if(extra == -2147483648){
                    Toast.makeText( this,"Ошибка воспроизведения",Toast.LENGTH_LONG ).show();
                    StorageUtil storageUtil = new StorageUtil( this );
                    Audio audio = storageUtil.getDownloadAudio( activeAudio );
                    if( audio !=null){
                      Utils util = new Utils();
                      util.DeleteFile( audio.getPath() );
                      storageUtil.deletedDownloadAudio( audio );
                    }
                }
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
            case -38:
                Log.i( TAG, "onError:sdfsfd " );
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        prepare = true;
        if(call != null)
            call.call();
        if(!pause)
        playMedia();
        else
            pauseMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying() && !pause && !stop){ mediaPlayer.start(); buildNotification( PlaybackStatus.PLAYING ); }
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()){ mediaPlayer.pause();  buildNotification( PlaybackStatus.PAUSED ); }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()){ mediaPlayer.pause();  buildNotification( PlaybackStatus.PAUSED ); }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    /**
     * AudioFocus
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService( Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        try {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus( this );
        }catch (Exception e){
            onDestroy();
            return Boolean.parseBoolean( null );
        }
    }


    /**
     * MediaPlayer actions
     */
    private void initMediaPlayer() {
        prepare = false;
        pause = false;
        stop = false;
        album = null;
        mediaPlayer = new MediaPlayer();
        if (tracksrecycler != null) initRecycler( tracksrecycler );
        StorageUtil storageUtil = new StorageUtil( this );
        if (storageUtil.getDownloadAudio( activeAudio ) != null) {
            if (!storageUtil.getDownloadAudio( activeAudio ).getPath().equals( "d" )) {
                mediaPlayer.setOnCompletionListener( this );
                mediaPlayer.setOnErrorListener( this );
                mediaPlayer.setOnPreparedListener( this );
                mediaPlayer.setOnBufferingUpdateListener( this );
                mediaPlayer.setOnSeekCompleteListener( this );
                mediaPlayer.setOnInfoListener( this );
                //Reset so that the MediaPlayer is not pointing to another data source
                try {
                    mediaPlayer.reset();
                } catch (Exception e) {
                }


                mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
                try {
                    // Set the data source to the mediaFile location
                    activeAudio = storageUtil.getDownloadAudio( activeAudio );
                    mediaPlayer.setDataSource( this, Uri.fromFile( new File( activeAudio.getPath() ) ) );
                } catch (Exception e) {
                    utils.DeleteFile( activeAudio.getPath() );
                    storageUtil.deletedDownloadAudio( activeAudio );
                    initInternet();
                    e.printStackTrace();

                }
                try {
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    onDestroy();
                }
            }
         else {
            initInternet();
        }
       }else initInternet();


        if(onAudioListener !=null)
            onAudioListener.getAudio( audioList.get( audioIndex ),audioIndex );

    }
    private void initInternet(){
        if(!activeAudio.getId().contains( "vk:" ))
        server.getSourseTrack( audioList.get( audioIndex ).getId(), (text,id) -> {
            //new MediaPlayer instance
            if(this.activeAudio.getId().equals( id )) {
                InitMP( text );
            }
        }, e -> {
            Random random = new Random();

            if(this.activeAudio.getId().equals(e ))
                new Handler(  ).postDelayed( () -> {
                    initMediaPlayer();
                },random.nextInt(4)*1500 );

        } );
        else {
            InitMP( activeAudio.getId().replace( "vk:","" ) );
        }
    }
    void InitMP(String text){

            //Set up MediaPlayer event listeners
            mediaPlayer.setOnCompletionListener( this );
            mediaPlayer.setOnErrorListener( this );
            mediaPlayer.setOnPreparedListener( this );
            mediaPlayer.setOnBufferingUpdateListener( this );
            mediaPlayer.setOnSeekCompleteListener( this );
            mediaPlayer.setOnInfoListener( this );
            //Reset so that the MediaPlayer is not pointing to another data source
            try {
                mediaPlayer.reset();
            } catch (Exception e) {
            }


            mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
            try {
                // Set the data source to the mediaFile location
                ///////////////////////////////////////////////////
                  Map<String, String> headers = new HashMap<String, String>();
                if(activeAudio.getId().contains( "vk:" )){
                    headers.put( "Accept","*/*" );
                    headers.put( "Accept-Encoding","identity;q=1, *;q=0" );
                    headers.put( "Accept-Language","ru,en;q=0.9,la;q=0.8" );
                    headers.put( "chrome-proxy","frfr" );
                    headers.put( "Connection","keep-alive" );
                    headers.put( "Host","audiobot.me" );
                    headers.put( "Referer","https://audiobot.me/audios254691354");
                    headers.put( "User-Agent","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Mobile Safari/537.36" );


                    mediaPlayer.setDataSource( this, Uri.parse( text ),headers);
                }
                else{

                    headers.put( "Accept","*/*" );
                    headers.put( "Accept-Encoding","identity;q=1, *;q=0" );
                    headers.put( "Accept-Language","ru,en;q=0.9,la;q=0.8" );
                    headers.put( "chrome-proxy","frfr" );
                    headers.put( "Connection","keep-alive" );
                    headers.put( "Host","cdn52.zvooq.com" );
                    headers.put( "Range","bytes=0-" );
                    headers.put( "Referer",text );
                    headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Mobile Safari/537.36");
// Use java reflection call the hide API:
                    mediaPlayer.setDataSource( this,Uri.parse( text ),headers );
                }
                ///////////////////////////////////////////////////

            } catch (Exception e) {
                e.printStackTrace();

            }
            try {
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                onDestroy();
            }

    }
    public void playMedia() {

        if (!mediaPlayer.isPlaying() && !pause && !stop) {
            pause = false;
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        stop= true;
        if (mediaPlayer == null) return;
        else {try{mediaPlayer.stop();}catch (Exception e){mediaPlayer = null;}}
//        if (mediaPlayer.isPlaying()) {
//
//        }
    }

    public void pauseMedia() {
        try{
        if (mediaPlayer.isPlaying()) {
            pause = true;
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
        }catch (Exception e){
            playAudio();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    public void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist

            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        //reset mediaPlayer
        try {
            mediaPlayer.reset();
        }catch (Exception e){mediaPlayer = null;}
        initMediaPlayer();
    }

    public void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        try {
            mediaPlayer.reset();
        }catch (Exception e){mediaPlayer = null;}
        initMediaPlayer();

    }


    /**
     * ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs
     */




    /**
     * Handle PhoneState changes
     */
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService( Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;

                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
//        telephonyManager.listen(phoneStateListener,
//                PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * MediaSession and Notification actions
     */
    @SuppressLint("WrongConstant")
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService( Context.MEDIA_SESSION_SERVICE);
        }
        // Create a new MediaSession

        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);

        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags( MediaSession.FLAG_HANDLES_MEDIA_BUTTONS| MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS );

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                if(prepare) {
                    if(route != null)
                    route.call();
                    resumeMedia();
                    buildNotification( PlaybackStatus.PLAYING );
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                if(route != null)
                    route.call();
                pauseMedia();
                buildNotification( PlaybackStatus.PAUSED );

            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                stop = true;
                removeNotification();
                //Stop the service
                stopSelf();
                onDestroy();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.placeholder); //replace with medias albumArt
        // Update the current metadata
        try {
            Glide.with(this)
                    .asBitmap()
                    .load(utils.getimgUrl( activeAudio.getImgUrl(), size_big ))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                          album = resource;
                            mediaSession.setMetadata(new MediaMetadataCompat.Builder()

                                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "")
                                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                                    .build());
                           setUpRemoteControlClient();
                           updateRemoteControlClientMetadata( resource );

                        }
                    });
        }catch (Exception e){
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()

                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                    .build());
        }

    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setUpRemoteControlClient() {
        AudioManager audioManager = (AudioManager)this.getSystemService(AUDIO_SERVICE);
            if (mRemoteControlClient == null) {
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);

                // create and register the remote control client
                mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(mRemoteControlClient);
            }

            mRemoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP);

    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void updateRemoteControlClientMetadata(Bitmap img) {

        if (mRemoteControlClient != null) {
            RemoteControlClient.MetadataEditor editor = mRemoteControlClient.editMetadata(true);
            editor.putString( MediaMetadataRetriever.METADATA_KEY_ALBUM, activeAudio.getAlbumname());
            editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, activeAudio.getArtist());
            editor.putString(MediaMetadataRetriever.METADATA_KEY_GENRE, "");
            editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, activeAudio.getTitle());
            editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, activeAudio.getDuration());
            editor.putBitmap( RemoteController.MetadataEditor.BITMAP_KEY_ARTWORK, img);
            editor.apply();
        }

    }
    private void buildNotification(PlaybackStatus playbackStatus) {
        if(controll!=null)
         setControll( playbackStatus );
        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        Boolean ongoing = true;
        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            ongoing = false;
            play_pauseAction = playbackAction(0);
        }

        final Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.album_img); //replace with your own image
        Intent PlayerIntent = new Intent(this, PlayerActivity.class);
        PlayerIntent.putExtra( "id",audioIndex );
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,PlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Boolean finalOngoing = ongoing;
        final int finalNotificationAction = notificationAction;
        final PendingIntent finalPlay_pauseAction = play_pauseAction;
        Uri link_sound = Uri.parse("android.resource://"+this.getPackageName()+"/"+R.raw.notify);
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage( AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFI_ID,"Notification",
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription( getResources().getString( R.string.app_name ) );
            notificationChannel.enableLights( true );
            notificationChannel.setSound( link_sound, attributes);
            assert notificationManager != null;
            notificationManager.createNotificationChannel( notificationChannel );
        }

        try {

            Glide.with(this)
                    .asBitmap()
                    .load(utils.getimgUrl( activeAudio.getImgUrl(), size_big ))
                    .error( R.drawable.placeholder )
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            album = resource;
                            NotificationCompat.Builder notificationBuilder = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                setUpRemoteControlClient();
                                updateRemoteControlClientMetadata( resource );

                                notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),NOTIFI_ID)
                                        // Hide the timestamp
                                        .setShowWhen( false )
                                        .setOngoing( finalOngoing )
                                        // Set the Notification style
                                        .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                                // Attach our MediaSession token
                                                .setMediaSession( MediaSessionCompat.Token.fromToken( mRemoteControlClient.getMediaSession().getSessionToken() ) )
                                                // Show our playback controls in the compat view
                                                .setShowActionsInCompactView(0, 1, 2))
                                        // Set the Notification color
                                        .setColor(getResources().getColor(R.color.colorAccent))
                                        // Set the large and small icons
                                        .setLargeIcon(resource)
                                        .setUsesChronometer(false)
                                        .setSmallIcon(R.drawable.ic_logo_item)
                                        // Set Notification content information
                                        .setContentText(activeAudio.getArtist())
                                        .setVibrate( null )
                                        .setContentTitle(activeAudio.getTitle())
                                        // Add playback actions
                                        .setContentIntent( contentIntent )
                                        .setSound(link_sound)
                                        .setSubText( activeAudio.getTitle() )
                                        .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                                        .addAction( finalNotificationAction, "pause", finalPlay_pauseAction )
                                        .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                                        .addAction( R.drawable.ic_close,"close", playbackAction(4));
                            }else{
                                notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),NOTIFI_ID)
                                        // Hide the timestamp
                                        .setShowWhen( false )
                                        .setOngoing( finalOngoing )
                                        // Set the Notification style
                                        .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                                // Attach our MediaSession token
                                                .setMediaSession( mediaSession.getSessionToken() )
                                                // Show our playback controls in the compat view
                                                .setShowActionsInCompactView(0, 1, 2))
                                        // Set the Notification color
                                        .setColor(getResources().getColor(R.color.colorAccent))
                                        // Set the large and small icons
                                        .setLargeIcon(resource)
                                        .setUsesChronometer(false)
                                        .setSmallIcon(R.drawable.ic_logo_item)
                                        // Set Notification content information
                                        .setContentText(activeAudio.getArtist())
                                        .setContentTitle(activeAudio.getTitle())
                                        // Add playback actions
                                        .setVibrate( null )
                                        
                                        .setContentIntent( contentIntent )
                                        .setSound(link_sound)
                                        .setSubText( activeAudio.getTitle() )
                                        .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                                        .addAction( finalNotificationAction, "pause", finalPlay_pauseAction )
                                        .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                                        .addAction( R.drawable.ic_close,"close", playbackAction(4));
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                                notificationBuilder.setVisibility(  Notification.VISIBILITY_PUBLIC);
                            startForeground(NOTIFICATION_ID, notificationBuilder.build() );
                        }
                    });
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),NOTIFI_ID)
                    // Hide the timestamp
                    .setShowWhen( false )
                    .setOngoing( finalOngoing )
                    // Set the Notification style
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                            // Attach our MediaSession token
                            .setMediaSession(mediaSession.getSessionToken())
                            // Show our playback controls in the compat view
                            .setShowActionsInCompactView(0, 1, 2))
                    // Set the Notification color
                    .setColor( Color.parseColor( activeAudio.getPallete().split( "," )[0] ) )
                    // Set the large and small icons
                    .setUsesChronometer(false)
                    .setSmallIcon(R.drawable.ic_logo_item)
                    // Set Notification content information
                    .setContentText(activeAudio.getArtist())
                    .setContentTitle(activeAudio.getTitle())
                    // Add playback actions
                    .setContentIntent( contentIntent )
                    .setSound(link_sound)
                    .setSubText( activeAudio.getTitle() )
                    .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                    .addAction( finalNotificationAction, "pause", finalPlay_pauseAction )
                    .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                    .addAction( R.drawable.ic_close,"close", playbackAction(4));
            if(album != null) notificationBuilder.setLargeIcon( album );
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                notificationBuilder.setVisibility(  Notification.VISIBILITY_PUBLIC);


            startForeground(NOTIFICATION_ID, notificationBuilder.build() );
        }catch (Exception e){
        // Create a new Notification

        }
    }


    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 4:
                // Previous track
                playbackAction.setAction(ACTION_CLOSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
        if(tracksrecycler != null)
        setAllNotPlay();
        activeAudio = new Audio( "","","","","" );
        stopForeground (true);
        try {
            mediaPlayer.reset();
        }catch (Exception e){

        }
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            pause = false;
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            pause = true;
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            prepare = false;
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_CLOSE)) {
            try {
                if(stoping !=null)
                    stoping.call();
                transportControls.stop();
            }catch (Exception e){
                onDestroy();
            }

        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            prepare = false;
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            if(stoping !=null)
                stoping.call();
            transportControls.stop();
        }
    }


    /**
     * Play new Audio
     */
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            playAudio();

        }
    };
    public void playAudio(){
        audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
        audioList = new StorageUtil( getApplicationContext() ).loadAudio();
        if(activeAudio != null ){
            if(audioList.get( audioIndex ).getId().equals( activeAudio.getId() ) && !pause ) {
                pause = true;
                transportControls.pause();
                return;
            }else if(audioList.get( audioIndex ).getId().equals( activeAudio.getId() ) && pause){

                transportControls.play();
                pause=false;
                return;
            }
        }
        if (audioIndex != -1 && audioIndex < audioList.size()) {
            //index is in a valid range
            activeAudio = audioList.get( audioIndex );
        } else {
            stopSelf();
        }
        //A PLAY_NEW_AUDIO action received
        //reset mediaPlayer to play the new Audio
        stopMedia();
        if (mediaPlayer != null) mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification( PlaybackStatus.PLAYING );


    }
    public int getSeek(){
        try {
            return mediaPlayer.getCurrentPosition() / 1000;
        }catch (Exception e){
            return 0;
        }
    }
    public int timelife(){
        if(prepare)
            return mediaPlayer.getDuration() / 1000;
       else return 0;
    }
    public void setSeek(int pos){
        mediaPlayer.seekTo( pos*1000 );
    }

    public void setAudioList(Interfaces.OnAudioListener onAudioListener){
        this.onAudioListener = onAudioListener;
    }
    public void setPrepare(Interfaces.OnCall call){
        this.call = call;
    }
    public Audio getAudioTrack(){
        return activeAudio;
    }
    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);

    }
    public void setAudioListener(Interfaces.OnCall au){
        this.route = au;
    }
    public void setStopinglistener(Interfaces.OnCall top){
        stoping = top;
    }
  public Boolean getPlaying(){
        if(prepare){
           return mediaPlayer.isPlaying();
        }else return prepare;

  }

}
