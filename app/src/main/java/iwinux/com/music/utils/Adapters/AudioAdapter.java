package iwinux.com.music.utils.Adapters;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.R;
import iwinux.com.music.fragments.MoreInfo_fragment;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static com.google.android.gms.ads.AdSize.SMART_BANNER;
import static iwinux.com.music.Data.p_Strings_Data.size_small;
import static iwinux.com.music.MainActivity.frameLayout1;
import static iwinux.com.music.MainActivity.mFirebaseAnalytics;
import static iwinux.com.music.MainActivity.player;
import static iwinux.com.music.fragments.like_fragment.initAudio;
import static iwinux.com.music.fragments.like_fragment.like_;


public abstract class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.Holder> {

    public static final int AD_TYPE = 55501;
    Context context;
    List<Audio> audio;
    List<Audio> audioPlay;
    int first=500,two=500,three = 500;
    int index;
    int cur = 0;
    Utils utils;
    Boolean PREMIUM = false;
    AdManager adManager;
   public int vk =0;
    public Boolean like;
    public Boolean like_list = false;
    StorageUtil storage;
    Interfaces.OnAudioClick onClickListen;
    private Dialog alert;

    public AudioAdapter(Context context, List<Audio> audioList){
        this.context = context;
        adManager = new AdManager( context );
        audio = audioList;
        utils = new Utils();
        UserManager userManager = new UserManager( context );
        PREMIUM = userManager.getsettings().getPremium();
//        PREMIUM = true;
        if(userManager.getsettings().getPay())
            PREMIUM = true;
        String[] strings = adManager.getAd().getLenta().split( "," );
        if(!PREMIUM)
        if( adManager.getAd().getLenta().split( "," ).length ==1) {
            first = (Integer.valueOf( adManager.getAd().getLenta() ));
            two = 9000;
            three = 9000;
        }
        else {
            try {
                first = Integer.valueOf( strings[0] );
                two = Integer.valueOf( strings[1] );
                three = Integer.valueOf( strings[2] );
            }catch (Exception e){

            }
        }
        storage = new StorageUtil( context );
        Log.i( TAG, "AudioAdapter: Complite" );
        audioPlay = storage.loadAudio();
        index = storage.loadAudioIndex();
    }
    public AudioAdapter(Context context, List<Audio> audioList,Boolean like,Boolean like_list){
        this.context = context;
        audio = audioList;
        this.like = like;
        utils = new Utils();
        this.like_list = like_list;
        PREMIUM = true;
        storage = new StorageUtil( context );
        Log.i( TAG, "AudioAdapter: Complite" );
        audioPlay = storage.loadAudio();
        index = storage.loadAudioIndex();
    }
    public AudioAdapter(Context context, List<Audio> audioList,int vk){
        this.context = context;
        audio = audioList;
        this.vk = vk;
        utils = new Utils();
        PREMIUM = true;
        storage = new StorageUtil( context );
        Log.i( TAG, "AudioAdapter: Complite" );
        audioPlay = storage.loadAudio();
        index = storage.loadAudioIndex();
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viwe) {
        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View v = null;

        if (viwe == AD_TYPE)
        {
            v = new AdView(context);
            ((AdView)v).setAdSize( SMART_BANNER );
            ((AdView)v).setAdUnitId( context.getResources().getString( R.string.AD_id ) );
            float density = context.getResources().getDisplayMetrics().density;
            int height = Math.round( SMART_BANNER.getHeight() * density);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,height);
            v.setLayoutParams(params);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice( "23C722AFDD17C32267DC2FC8B58AB0EB" ).build();
            ((AdView)v).loadAd(adRequest);
        }
        else {
            v = inflater.inflate( R.layout.item_audio, parent, false );


        }
        return new Holder( v );
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        if(holder.getItemViewType() != AD_TYPE) {
            int k=0;
            if(!PREMIUM){
                if(position>first)
                    k+=1;
                if(position>two)
                    k+=1;
                if(position>three)
                    k+=1;
            }

            int p = position-k;
            Audio a = audio.get( p );
            Audio s = storage.getDownloadAudio( a );
            if (s != null) holder.download.setVisibility( View.VISIBLE );
            String img = utils.getimgUrl( a.getImgUrl(), size_small );
            Glide.with( context ).load( img )

                    .error( context.getResources().getDrawable( R.drawable.placeholder ) )
                    .into( holder.Img );
//            holder.Img.post( () ->
//                    Glide.with( context ).load( img )
//                            .apply(new RequestOptions().override(holder.Img.getWidth(), holder.Img.getHeight()))
//                            .error( context.getResources().getDrawable( R.drawable.placeholder ) )
//                            .into( holder.Img )
//                            );

            holder.Title.setText( utils.resizeString( a.getTitle(), 25 ) );
            holder.Artitst.setText( utils.resizeString( a.getArtist(), 50 ) );
            holder.duration.setText( utils.initConvertTime( a.getDuration() ) );
            int finalP1 = p;
            holder.click.setOnLongClickListener( v -> {
                Log.e( TAG, "onBindViewHolder: "+position  );
                InitDiaolg( a, v, finalP1, holder );
                return false;
            } );
            int finalP = p;
            holder.click.setOnClickListener( v -> {
                Bundle params = new Bundle();
                params.putString("play",a.getTitle());
                mFirebaseAnalytics.logEvent("click_user", params);
                onClickListen.getAudios( audio, finalP, v );

            } );
            holder.more.setOnClickListener( v -> InitDiaolg( a, v, position, holder ) );
            try {
                if (a.getId().equals( audioPlay.get( index ).getId() )) {
                    if (MainActivity.player != null) {
                        holder.playing.setVisibility( View.VISIBLE );
                        if (!MainActivity.player.pause) {
                            holder.route.setImageDrawable( context.getResources().getDrawable( R.drawable.pause ) );
                        } else holder.route.setImageDrawable( context.getResources().getDrawable( R.drawable.play ) );
                    }
                }
            } catch (Exception e) {

            }
        }
        if ((position >= getItemCount() - (20)) && cur < getItemCount()) {
            //для остановки бессконечной загрузки
            Log.i( TAG, "onBindViewHolder: " + "FFFFFFFFFFFFFFFFFFFFFLOAD" );
            cur = getItemCount();
            load();
        }
    }
    public abstract void load();
    private void InitDiaolg(Audio a,View view,int position,Holder holder) {
        alert = new Dialog( view.getContext() );
        alert.setContentView( R.layout.alert_layout );
        alert.setCancelable( true );
        alert.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
        ImageView like = alert.findViewById( R.id.like );
        ImageView download = alert.findViewById( R.id.download );
        TextView Title = alert.findViewById( R.id.Title );
        TextView Artitst = alert.findViewById( R.id.Artist );
        TextView in_artist = alert.findViewById( R.id.in_artist );
        ImageView Img = alert.findViewById( R.id.preview );
        LinearLayout addPlaylist = alert.findViewById( R.id.addQouin );


        if(player != null)
            if(!player.getAudioTrack().getId().equals( a.getId() )) {
                addPlaylist.setVisibility( View.VISIBLE );
                addPlaylist.setOnClickListener( v -> {
                     player.addQouin( a );
                     alert.dismiss();
                } );
            }


        Title.setText( a.getTitle() );
        if(a.getId().contains( "vk:" ))
            in_artist.setVisibility( View.GONE );
        Artitst.setText( utils.resizeString( a.getArtist(), 50 ) );
        String img = utils.getimgUrl( a.getImgUrl(), size_small );
        Picasso.with( context ).load( img ).memoryPolicy( MemoryPolicy.NO_CACHE ).into( Img );
        RelativeLayout dissmis = alert.findViewById( R.id.cancel );
        if(storage.getLikeAudio( a ) != null){
            like.setImageDrawable( context.getResources().getDrawable( R.drawable.like ) );
            like.setOnClickListener( v -> {
                if(this.like != null)
                if(this.like) {
                    audio.remove( position );
                    notifyItemRemoved( position );
                }
                storage.deletedLikesAudio( a );
                notifyDataSetChanged();
                alert.dismiss();

            } );
        }else{
            like.setImageDrawable( context.getResources().getDrawable( R.drawable.like_outline ) );
            like.setOnClickListener( v -> {
                storage.addlikesAudio( a );
                alert.dismiss();
                 if(vk == 1){
                     if(like_)
                     initAudio(storage.loadLikesAudio(),true,context);
                 }

            } );

        }
        if (storage.getDownloadAudio( a ) != null){

             download.setImageDrawable( context.getResources().getDrawable( R.drawable.ic_round_delete ) );
             download.setOnClickListener( v -> {

                 if(!storage.getDownloadAudio( a ).getPath().equals( "d" )) {
                    File f = new File( storage.getDownloadAudio( a ).getPath() );
                    f.delete();
                 }
                 holder.download.setVisibility( View.GONE );
                 if(this.like != null)
                 if(!this.like) {
                     audio.remove( position );
                     notifyItemRemoved( position );
                 }

                 storage.deletedDownloadAudio( a );
                 notifyDataSetChanged();
                 alert.dismiss();

               } );

         }else
        download.setOnClickListener( v -> {
            if (ActivityCompat.checkSelfPermission( context, Manifest.permission.WRITE_EXTERNAL_STORAGE ) !=
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText( context, "Дайте разрешение и попробуйте снова", Toast.LENGTH_SHORT ).show();
                Dexter.withActivity((MainActivity)context)
                        .withPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        .withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.isAnyPermissionPermanentlyDenied())
                        Toast.makeText( context, "Можете скачивать треки", Toast.LENGTH_SHORT ).show();
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {token.continuePermissionRequest();}
                } ).onSameThread().check();

            }else{
                Bundle params = new Bundle();
                params.putString("download",a.getTitle());
                mFirebaseAnalytics.logEvent("click_user", params);
                utils.DownloadFile( a, v.getContext(), false );
            }
            alert.dismiss();
        } );
        in_artist.setOnClickListener( v->{
          loadFragment( MoreInfo_fragment.newInstance( a.getArtistid().split( "," )[0] ) );
          alert.dismiss();
        } );
        dissmis.setOnClickListener( v -> {
                 alert.dismiss();
        } );
        alert.show();

    }
    public List<Audio> getAudios(){
        return audio;
    }
    void loadFragment(Fragment fragment) {
        MainActivity.Frame = 2;
        FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout1.getId(), fragment);
        ft.commit();
    }
    @Override
    public int getItemCount() {
        if(!PREMIUM) {
            int k=0;
            if(audio.size()>first)
                k+=1;
            if(audio.size()>two)
                k+=1;
            if(audio.size()>three)
                k+=1;
            return audio.size() + k;
        }
        else
            return audio.size();
    }

    @Override
    public int getItemViewType(int position) {
     if(!PREMIUM)
     if(first == position || three == position||two==position)

         return AD_TYPE;
     return position;

    }
    public void  setOnClickListen(Interfaces.OnAudioClick onClickListen){
        this.onClickListen = onClickListen;
    }

    class Holder extends RecyclerView.ViewHolder {
        RelativeLayout playing;
        TextView Title;
        TextView Artitst;
        TextView duration;

        ImageView Img,route,more,download;

        LinearLayout click;
        public Holder(View itemView) {
            super( itemView );
            download = itemView.findViewById( R.id.download );
            more = itemView.findViewById( R.id.more );
            playing = itemView.findViewById( R.id.play );
            duration = itemView.findViewById( R.id.duration );
            Img = itemView.findViewById( R.id.preview );
            Artitst = itemView.findViewById( R.id.Artist );
            Title = itemView.findViewById( R.id.Title );
            click = itemView.findViewById( R.id.click );
            route = itemView.findViewById( R.id.route );
        }
    }
}
