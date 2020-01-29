package iwinux.com.music.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.R;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

public class settings_fragment extends Fragment {
    View view;
    TextView promo;
    RelativeLayout  clear_download;
    CardView no_ad;
    UserManager userManager;
    Dialog alert;
    Server server;
    Utils utils;
    StorageUtil storageUtil;
    LinearLayout ad;
    SettingsUser settingsUser;
    Switch nightTheme, adaptivePlayer,full_screen;
    public settings_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.settings_fragment, container, false );
        server = new Server( view.getContext() );
        storageUtil = new StorageUtil( view.getContext() );
        utils = new Utils();
        content();
        return view;
    }

    private void content() {
        ad = view.findViewById( R.id.ad );
        promo = view.findViewById( R.id.promo );
        full_screen = view.findViewById( R.id.fullscreen );
        clear_download  = view.findViewById( R.id.clear );
        no_ad = view.findViewById( R.id.ad_block );
//        nightTheme = view.findViewById( R.id.nightTheme );
        adaptivePlayer = view.findViewById( R.id.adaptivePlayer );
        userManager = new UserManager( view.getContext() );
        settingsUser = userManager.getsettings();
        if(userManager.getsettings().getPremium()||userManager.getsettings().getPay() ){
            ad.setVisibility( View.GONE );
        }
        full_screen.setChecked( settingsUser.getFullScreen() );
//        nightTheme.setChecked( settingsUser.getNightTheme() );
        adaptivePlayer.setChecked( settingsUser.getAdaptivePlayer() );
//        nightTheme.setOnCheckedChangeListener( (buttonView, isChecked) -> {
//            userManager.saveNightTheme( isChecked );
//        } );
        full_screen.setOnCheckedChangeListener( (v,check)->{
            userManager.saveFullScreen( check );

            Intent intent = new Intent( v.getContext(), MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );

        } );
        clear_download.setOnClickListener( v ->{
            List<Audio> audios = storageUtil.loadDonloadAudio();
            for(int i =0;i<audios.size();i++){
                Audio audio =audios.get( i );
                if(utils.DeleteFile(audio.getPath(),"" )){
                    storageUtil.deletedDownloadAudio(audio);
                    continue;
                }
                if(i+1==audios.size()){
                    Toast.makeText( v.getContext(),"Очищенно",Toast.LENGTH_LONG ).show();
                }
            }
         } );
        adaptivePlayer.setOnCheckedChangeListener( (v,check)->{
            userManager.setAdaptivePlayer( check );
        } );
        promo.setOnClickListener( (v)->{
          initPromo();
        } );
    }

    private void initPromo() {
        alert = new Dialog( view.getContext() );
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

            ,(e)->{
                   editText.setEnabled( true );
                   progressBar.setVisibility( View.GONE );
                   button.setVisibility( View.VISIBLE );
                   Toast.makeText( v.getContext(),e,Toast.LENGTH_LONG ).show();
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
        if(userManager != null)
        if(userManager.getsettings().getPremium()){
            ad.setVisibility( View.GONE );
        }
    }
}
