package iwinux.com.music.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.R;

public class MyFirebaseMessagingService  extends FirebaseMessagingService {
    String NOTIFI_ID = "PUSH";
    /*
    We need this service only if we want to receive notification payloads,
     and so onsend upstream messages
     */
    String TAG = "SEND_DATA_PUSH";
    public MyFirebaseMessagingService() {
        Log.e( TAG, "MyFirebaseMessagingService: ok"  );
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> map = remoteMessage.getData();
        Log.e( TAG, "onMessageReceived: "+map.get( "message" ));
        String message = map.get( "message" );
        try {
            if(!message.contains( "(data)" )) {
                String text = map.get( "text" );
                String Title = getResources().getString( R.string.app_name );
                Title = map.get( "title" );
                String priority = map.get( "priority" );
                int P = NotificationManager.IMPORTANCE_DEFAULT;
                try {
                    if (priority.equals( "high" )) {
                        P = NotificationManager.IMPORTANCE_HIGH;
                    } else if (priority.equals( "low" )) {
                        P = NotificationManager.IMPORTANCE_LOW;
                    }
                } catch (Exception e) {

                }
                try {
                    Intent intent;
                    if (message.contains( "(link)" )) {
                        String url = message.split( "," )[1];
                        intent = new Intent( Intent.ACTION_VIEW );
                        intent.setData( Uri.parse( url ) );
                    } else {
                        intent = new Intent( this, MainActivity.class );
                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                    }
                    NotificationCompat.Builder notificationBuilder;
                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                    bigTextStyle.bigText( text );
                    NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
                    PendingIntent pendingIntent = PendingIntent.getActivity( this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT );
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel( NOTIFI_ID, "Notification", P );
                        notificationChannel.setDescription( getResources().getString( R.string.app_name ) );
                        notificationChannel.enableLights( true );
                        assert notificationManager != null;
                        notificationManager.createNotificationChannel( notificationChannel );
                        notificationBuilder = new NotificationCompat.Builder( this ).setSmallIcon( R.drawable.ic_logo_item ).setContentTitle( Title ).setPriority( P ).setStyle( bigTextStyle ).setChannelId( NOTIFI_ID ).setWhen( System.currentTimeMillis() ).setAutoCancel( true ).setContentIntent( pendingIntent );
                    } else {
                        notificationBuilder = new NotificationCompat.Builder( this ).setSmallIcon( R.drawable.ic_logo_item ).setContentTitle( Title ).setStyle( bigTextStyle ).setWhen( System.currentTimeMillis() ).setAutoCancel( true ).setPriority( P ).setContentIntent( pendingIntent );
                    }


                    notificationManager.notify( 100 /* ID of notification */, notificationBuilder.build() );
                } catch (Exception e) {

                }
            }else{
                StorageUtil storageUtil = new StorageUtil( this );
                storageUtil.saveText( "url_server",message.split( "," )[1] );
            }
        }catch (Exception e){

        }


    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        Server server = new Server( this );
        server.ServerRegistrToken( token );
    }
    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }
}