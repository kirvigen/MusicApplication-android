package iwinux.com.music.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.R;
import iwinux.com.music.VideoViewFull;
import iwinux.com.music.WelcomActivity;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

public class welcome_fragment extends Fragment {

    public static final String ARG_TEXT = "item_text";
    public static final String ARG_POSITION = "item_position";
    VideoViewFull videoViewFull;
    View rootView;
    public static final String ARG_COUNT = "item_count";
    int[] Text = new int[]{R.string.text_1,R.string.text_2,R.string.text_3, R.string.text_4};
    int[] Head = new int[]{R.string.head_1,R.string.head_2,R.string.head_3,R.string.head_4};
    private Utils util;
    private Dialog alert;
    ProgressBar progressBar;
    UserManager userManager;
    private Server server;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        rootView = inflater.inflate(
                R.layout.item_welcome, container, false);
        util = new Utils();
        server = new Server( rootView.getContext() );
        progressBar = rootView.findViewById( R.id.progress_bar );
        userManager = new UserManager( rootView.getContext() );
        Bundle args = getArguments();
        LottieAnimationView animationView = rootView.findViewById( R.id.animation );
        TextView text =  rootView.findViewById( R.id.text );
        TextView holder =  rootView.findViewById( R.id.holderText );
        if(args.getInt( ARG_POSITION ) == Text.length){
            LinearLayout end = rootView.findViewById( R.id.end );
            videoViewFull = rootView.findViewById( R.id.video_full );
            DisplayMetrics displayMetrics = rootView.getContext().getResources().getDisplayMetrics();
            Uri path = Uri.parse("android.resource://iwinux.com.music/" + R.raw.video);
            videoViewFull.setVisibility( View.VISIBLE );
            videoViewFull.setVideoURI(path);
            videoViewFull.start();
            videoViewFull.setOnPreparedListener( mp -> {
                mp.setVolume( 0,0 );
                mp.setLooping( true );
            } );
            videoViewFull.getHolder().setFixedSize( displayMetrics.widthPixels,displayMetrics.heightPixels );
            videoViewFull.setDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels);
            Button lkb = rootView.findViewById( R.id.lk );
            final Button promo =rootView.findViewById( R.id.premium );
            TextView skip = rootView.findViewById( R.id.skip );
            animationView.setVisibility( View.GONE );
            text.setVisibility( View.GONE );
            holder.setVisibility( View.GONE );
            lkb.setOnClickListener( v -> {
                util.Pay( WelcomActivity.mCheckout,rootView.getContext() );
            } );
            promo.setOnClickListener( v -> {
                 initPromo();
            } );

            skip.setOnClickListener( v -> {
                skip.setEnabled( false );
                UserManager userManager = new UserManager( rootView.getContext() );
                Utils utils = new Utils();
                if(!userManager.getFirstOpen()){
                    userManager.setFirstOpen( true );
                    utils.LoadGenre( rootView.getContext() );
                }
                Intent intent = new Intent( v.getContext(), MainActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
                skip.setVisibility( View.GONE );
                progressBar.setVisibility( View.VISIBLE );
                ((Activity) v.getContext()).finish();
            } );
            end.setVisibility( View.VISIBLE );
        }
        else {

            if(args.getInt( ARG_POSITION ) == 1){
                animationView.setImageDrawable( getResources().getDrawable( R.drawable.infinity ) );
            }else if(args.getInt( ARG_POSITION ) == 2){
                ImageView imageView  = rootView.findViewById( R.id.download );
                imageView.setImageDrawable( getResources().getDrawable( R.drawable.white_download ) );
                imageView.setVisibility( View.VISIBLE );
            }
            else {
                animationView.setAnimation( args.getInt( "res" ) );
                animationView.playAnimation();
            }
            text.setText(
                    getResources().getString( Text[args.getInt( ARG_POSITION )] ) );
            holder.setText(
                    getResources().getString( Head[args.getInt( ARG_POSITION )] ) );
        }
        


        return rootView;
    }
    private void initPromo() {
        alert = new Dialog( rootView.getContext() );
        alert.setContentView( R.layout.alert_promo );
        alert.setCancelable(true);
        alert.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
        RelativeLayout button = alert.findViewById( R.id.ok );
        EditText editText = alert.findViewById( R.id.edit_promo );
        ProgressBar progressBar = alert.findViewById( R.id.progressBar );
        button.setOnClickListener( (v)->{
            String text = editText.getText().toString();
            progressBar.setVisibility( View.VISIBLE );
            button.setVisibility( View.GONE );
            editText.setEnabled( false );
            if(!text.equals( "" )){
                server.sendPromocode( text, new Interfaces.OnPromocode() {
                            @Override
                            public void onSucces(String id, long time) {
                                userManager.setPREMIUM( true );
                                SettingsUser after =userManager.getsettings();
                                after.setId( id );
                                after.setPromocode( text );
                                after.setTimeActive( time );
                                userManager.createSettings( after );
                                Intent intent = new Intent( v.getContext(), MainActivity.class );
                                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( intent );
                            }

                            @Override
                            public void onError() {

                            }
                        }
                        , (e) -> {
                            editText.setEnabled( true );
                            progressBar.setVisibility( View.GONE );
                            button.setVisibility( View.VISIBLE );
                            Toast.makeText( v.getContext(), e, Toast.LENGTH_LONG ).show();
                        } );
            }else{
                editText.setEnabled( true );
                progressBar.setVisibility( View.GONE );
                button.setVisibility( View.VISIBLE );
                Toast.makeText( v.getContext(), "Поле пустое", Toast.LENGTH_LONG ).show();
            }
        } );
        alert.show();
    }
    @Override
    public void onResume() {
        super.onResume();
        if(videoViewFull != null){
            DisplayMetrics displayMetrics = rootView.getContext().getResources().getDisplayMetrics();
            Uri path = Uri.parse("android.resource://iwinux.com.music/" + R.raw.video);
            videoViewFull.setVisibility( View.VISIBLE );
            videoViewFull.setVideoURI(path);
            videoViewFull.start();
            videoViewFull.setOnPreparedListener( mp -> {
                mp.setVolume( 0,0 );
                mp.setLooping( true );
            } );
            videoViewFull.getHolder().setFixedSize( displayMetrics.widthPixels,displayMetrics.heightPixels );
            videoViewFull.setDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }
}
