package iwinux.com.music.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.R;
import iwinux.com.music.utils.Adapters.AlbumAdapter;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.Utils;

import static iwinux.com.music.MainActivity.frameLayout;
import static iwinux.com.music.MainActivity.mFirebaseAnalytics;

@SuppressLint("ValidFragment")
public class like_fragment extends Fragment {
    private Utils util;
    private Server server;

    public static AudioAdapter adapter;
    static TextView moreAlbums;
    View album_view;
    static TextView error_text;
    LinearLayout like_bg,download_bg;
    ImageView like,download,vk_export,settings;
    StorageUtil storage;
    static RecyclerView recyclerView;
    static RecyclerView recyclerAlbum;
    static LinearLayoutManager layoutManagerPopslar;
    View view;
    static public Boolean like_ = true;
    private static AlbumAdapter albumAdapter;
    private static List<Album> albums;

    public like_fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate( R.layout.like_fragment, container, false);
        util = new Utils();
        storage = new StorageUtil( view.getContext() );
        server = new Server(view.getContext());
        content();
        getData();
        return view;
    }
    private void content() {
        error_text = view.findViewById( R.id.error_text );
        recyclerView = view.findViewById( R.id.recycler );
        album_view = view.findViewById( R.id.album_view );
        recyclerAlbum = view.findViewById( R.id.recyclerAlbum );
        moreAlbums = view.findViewById( R.id.moreAlbums );
        like_bg = view.findViewById( R.id.like_bg );
        download = view.findViewById( R.id.download );
        like = view.findViewById( R.id.like );
        download_bg = view.findViewById( R.id.download_bg );
        vk_export = view.findViewById( R.id.vk );
        settings = view.findViewById( R.id.settings );
        vk_export.setOnClickListener( v -> {
            Bundle params = new Bundle();
            params.putString("open","vk_export");
            mFirebaseAnalytics.logEvent("click_user", params);
            loadFragment( new export_Vk_fragment() );
        } );
        settings.setOnClickListener( v->{
            loadFragment( new settings_fragment() );
        } );
        storage.filterAudio();

        download_bg.setOnClickListener( v-> initDownload() );
        like_bg.setOnClickListener( v-> initlike() );
    }
    Drawable getBg(boolean isActive){
        int Roud = 20;
        float[] outR = new float[] {Roud,Roud, Roud, Roud, Roud, Roud, Roud, Roud };
        RectF rectF;

        rectF = new RectF( 8, 8, 8, 8 );

        float[] inR = new float[] { 6, 6, 6, 6, 6, 6, 6,6};

        ShapeDrawable shape = new ShapeDrawable(
                new RoundRectShape(outR, rectF, inR));
        ShapeDrawable shapse = new ShapeDrawable(new RectShape());

        shape.setIntrinsicHeight(util.dpToPixel(100,getContext()));
        shape.setIntrinsicWidth(util.dpToPixel(40,getContext()));
        shape.getPaint().setColor(getResources().getColor(R.color.red_light));
        if(isActive){
            shapse.getPaint().setColor(getResources().getColor(R.color.red_light));
        }
        else{
            shapse.getPaint().setColor(getResources().getColor(R.color.white));
        }

        shapse.setShape((shape.getShape()));
       return shapse;
    }
    void initlike(){

        like_bg.setBackground(getResources().getDrawable(R.drawable.like_fragment_switch) );
        download_bg.setBackground( getResources().getDrawable(R.drawable.genre) );
        like.setColorFilter( Color.argb(255, 255, 255, 255));
        download.setColorFilter(getResources().getColor( R.color.red_light ));
        initAudio( storage.loadLikesAudio(),true );
    }
    void initDownload(){
        download_bg.setBackground( getResources().getDrawable(R.drawable.like_fragment_switch) );
        like_bg.setBackground( getResources().getDrawable(R.drawable.genre) );
        download.setColorFilter( Color.argb(255, 255, 255, 255));
        like.setColorFilter(getResources().getColor( R.color.red_light ));
        initAudio( storage.loadDonloadAudio(),false );
    }

    @Override
    public void onStart() {
        super.onStart();
        if(album_view != null){
            getData();
            if(like_)
                initlike();
            else
                initDownload();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(album_view != null){
            getData();
            if(like_)
                initlike();
            else
                initDownload();
        }
    }

    private void getData()
    {
        if(storage.loadLikesAlbums().size() != 0){
            initAlbums( util.turn_overAlbum( storage.loadLikesAlbums( )) );
        }else{
            album_view.setVisibility( View.GONE );
        }
    }
    void loadFragment(Fragment fragment) {
        MainActivity.Frame = 1;
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }
    static void loadFragment(Fragment fragment,Context context) {
        MainActivity.Frame = 1;
        FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }
    private void initAlbums(List<Album> data) {
        albums = data;
        moreAlbums.setOnClickListener( v ->  loadFragment( list_fragment.newInstance( data,0 ) ) );
        albumAdapter = new AlbumAdapter( view.getContext(),data);
        albumAdapter.setOnClickListen( ( v, p,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ) );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerAlbum.setLayoutManager(layoutManagerPopslar );
        recyclerAlbum.setAdapter( albumAdapter );
    }
    static public void initAlbums(List<Album> data,Context context) {
        albums = data;
        moreAlbums.setOnClickListener( v ->  loadFragment( list_fragment.newInstance( data,0 ),context ) );
        albumAdapter = new AlbumAdapter( context,data);
        albumAdapter.setOnClickListen( ( v, p,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ),context );
        } );
        layoutManagerPopslar = new LinearLayoutManager( context,
                LinearLayoutManager.HORIZONTAL,false);
        recyclerAlbum.setLayoutManager(layoutManagerPopslar );
        recyclerAlbum.setAdapter( albumAdapter );
    }
     void initAudio(List<Audio> data, boolean like){
        like_ = like;
        error_text.setVisibility( View.GONE );
        if(data.size() == 0){
            if(like)
                error_text.setText( getResources().getString( R.string.no_izbran ) );
            else
                error_text.setText( getResources().getString( R.string.no_download ) );
            error_text.setVisibility( View.VISIBLE );

        }
        Log.println(Log.ERROR,"CHECK", String.valueOf( data.size() ));
        adapter = new AudioAdapter( view.getContext(), util.turn_over( data ), like, true ) {
            @Override
            public void load() {

            }
        };
        adapter.setOnClickListen( (datal, position, v) -> {
            MainActivity.audioList = adapter.getAudios();
            MainActivity.playAudio( position,v.getContext(),recyclerView );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setAdapter( adapter );

    }
    public static void initAudio(List<Audio> data, boolean like, Context context){
        Utils util = new Utils();
        like_ = like;
        error_text.setVisibility( View.GONE );
        if(data.size() == 0){
            if(like)
                error_text.setText( context.getResources().getString( R.string.no_izbran ) );
            else
                error_text.setText( context.getResources().getString( R.string.no_download ) );
            error_text.setVisibility( View.VISIBLE );

        }
        Log.println(Log.ERROR,"CHECK", String.valueOf( data.size() ));
        adapter = new AudioAdapter( context, util.turn_over( data ), like, true ) {
            @Override
            public void load() {

            }
        };
        adapter.setOnClickListen( (datal, position, v) -> {
            MainActivity.audioList = adapter.getAudios();
            MainActivity.playAudio( position,v.getContext(),recyclerView );
        } );
        layoutManagerPopslar = new LinearLayoutManager( context,
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setAdapter( adapter );

    }

}
