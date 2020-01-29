package iwinux.com.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.ultraviewpager.transformer.UltraScaleTransformer;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.Genre;
import iwinux.com.music.R;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.Adapters.AlbumAdapter;
import iwinux.com.music.utils.Adapters.ArtistAdapter;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.Adapters.Ultraview_pagerAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.Utils;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static iwinux.com.music.Data.p_Strings_Data.size_big;
import static iwinux.com.music.MainActivity.frameLayout1;

@SuppressLint("ValidFragment")
public class MoreInfo_fragment extends Fragment {
    private Utils util;
    private Server server;
    Album album;
    Artist artist;
    AudioAdapter adapter;
    Genre genres;
    View button_refresh, error_dialog;
    ImageView img,back,like;
    View banners;
    RecyclerView recyclerBanner;
    TextView titleBar,moreAlbums,moreArtist;
    LinearLayout more,Artist_view;
    CircleImageView avatar;
    TextView big,small;
    int height;
    View album_view;

    ProgressBar progressBar;
    RelativeLayout appbar;
    NestedScrollView nestedScrollView;
    String artistid;
    String playlist;
    StorageUtil storage;
    View moreaudio;
    RecyclerView recyclerView, Albumrecycler,Artistrecycler;
    LinearLayoutManager layoutManagerPopslar;
    View view;
    double percent =0;
    ArtistAdapter artistAdapter;
    private AlbumAdapter albumAdapter;
    private AdManager adManager;
    private UltraViewPager ultraViewPager;

    public static MoreInfo_fragment newInstance(Album album) {
        return new MoreInfo_fragment(album);
    }
    public static MoreInfo_fragment newInstance(Artist artist) {
        return new MoreInfo_fragment(artist);
    }
    public static MoreInfo_fragment newInstance(Genre ganre) {
        return new MoreInfo_fragment(ganre);
    }
    public static MoreInfo_fragment newInstance(String artist) {
        return new MoreInfo_fragment(artist);
    }
    public MoreInfo_fragment(Genre genre) {
        this.genres = genre;
    }
    public MoreInfo_fragment(Artist artist) {
        this.artist = artist;
    }
    public MoreInfo_fragment(Album album) {
        this.album = album;
    }
    public MoreInfo_fragment(String artistid) {
        this.artistid = artistid;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate( R.layout.moreinfo_fragment, container, false);
        util = new Utils();
        adManager = new AdManager( view.getContext() );
        storage = new StorageUtil( view.getContext() );
        server = new Server(view.getContext());
        content();
        getData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(like != null){
            likeCheck();
        }
    }

    private void content() {
        recyclerBanner = view.findViewById( R.id.recyclerBanner );
        recyclerView = view.findViewById( R.id.list );
        img = view.findViewById( R.id.img );
        like = view.findViewById( R.id.like );
        big = view.findViewById( R.id.title );
        small = view.findViewById( R.id.artist );
        back = view.findViewById( R.id.back );
        more = view.findViewById( R.id.moreInfo );
        avatar = view.findViewById( R.id.avatar );
        appbar = view.findViewById( R.id.appbar );
        Artist_view = view.findViewById( R.id.artists );
        Artistrecycler = view.findViewById( R.id.recyclerArtist );
        moreArtist = view.findViewById( R.id.moreArtist );
        moreAlbums = view.findViewById( R.id.moreAlbums );
        Albumrecycler = view.findViewById( R.id.recyclerAlbum );
        titleBar = view.findViewById( R.id.titleBar );
        progressBar = view.findViewById( R.id.prograss_bar );
        album_view = view.findViewById( R.id.album );
        nestedScrollView = view.findViewById( R.id.nested );
        error_dialog = view.findViewById( R.id.error_dialog );
        button_refresh = view.findViewById( R.id.refresh );
        banners = view.findViewById( R.id.banners );
        moreaudio = view.findViewById( R.id.moreaudio );
        ultraViewPager = view.findViewById( R.id.ultraviewpager );

        nestedScrollView.setOnScrollChangeListener( (NestedScrollView.OnScrollChangeListener) (nestedScrollView, i, i1, i2, i3) -> {
            height = appbar.getHeight();
            percent = -1*((((float) nestedScrollView.getScrollY()) / ((float)
                    (nestedScrollView.getHeight() - view.getContext().getResources()
                            .getDisplayMetrics().heightPixels + appbar.getHeight()))));
//            Log.i( "AAS", "content: " + percent);
            if(percent>5){
                appbar.animate().alpha( 1 ).start();
            }else{
                appbar.animate().alpha( 0 ).start();
            }
//            appbar.setAlpha( (float) (percent)/100*1.5f);
//            int y = nestedScrollView.getScrollY()-more.getHeight()+appbar.getHeight();
//            if(nestedScrollView.getScrollY() < (more.getHeight()-height-height)) {
//                appbar.animate().alpha( 0 ).start();
//            }
//            else{
////                if(y - height > height && appbar.getAlpha() != 1)
////                    appbar.animate().alpha( 1 ).start();
////                else
//                appbar.setAlpha( (float) (y)/(height));
//            }
        } );
        likeCheck();
        back.setOnClickListener( v -> {
            getActivity().onBackPressed();
        } );
        like.setOnClickListener( v -> {
            if(storage.checkLikeAlbums( album )){
                storage.deletedLikesAlbums( album );
                like.setImageDrawable( view.getContext().getResources().getDrawable( R.drawable.like_outline ) );
            }else{
                storage.addlikesAlbum( album );
                like.setImageDrawable( view.getContext().getResources().getDrawable( R.drawable.like ) );
            }
        } );
    }
    private void getData() {
        progressBar.setVisibility( View.VISIBLE );
        nestedScrollView.setVisibility( View.VISIBLE );
        if(!Utils.isOnline( getActivity() )){
            nestedScrollView.setVisibility( View.GONE );
            error_dialog.setVisibility( View.VISIBLE );
            progressBar.setVisibility( View.GONE );
            button_refresh.setOnClickListener( v->{
                error_dialog.setVisibility( View.GONE );
                getData();
            } );
        }else {
            if (album != null) {
                Artist_view.setVisibility( View.GONE );
                album_view.setVisibility( View.GONE );
                more.setVisibility( View.VISIBLE );
                Glide.with( view.getContext() ).load( util.getimgUrl( album.getImgurl(), size_big ) ).placeholder( getResources().getDrawable( R.drawable.placeholder ) ).into( img );
                big.setText( album.getTitle() );
                titleBar.setText( util.resizeString( album.getTitle() ) );
                small.setText( album.getArtist() );
                small.setOnClickListener( v -> {
                    loadFragment( MoreInfo_fragment.newInstance( album.getIdArtist().split( "," )[0] ) );
                } );
                if(album.getAudios() != null)
                    initAudio(album.getAudios());
                else
                server.getAlbumId( (audios)->{
                    if(audios.size() != 0){
                        initAudio(audios);
                    }else{
                        new Handler().postDelayed(()->{
                            server.getPlaylistId(this::initAudio,album.getId());
                        },1000);

                    }
                },album.getId() );
            }else  if(genres !=null){
                album_view.setVisibility( View.GONE );
                like.setVisibility( View.GONE );
                Artist_view.setVisibility( View.GONE);
                img.setVisibility( View.GONE );
                more.setVisibility( View.VISIBLE );
                avatar.setVisibility( View.VISIBLE );
                titleBar.setText( util.resizeString( genres.getName() ) );
                Picasso.with( view.getContext() ).load( util.getimgUrl( genres.getUrlImg(), size_big ) ).into( avatar );
                big.setText( genres.getName() );
                small.setText( "" );
                server.getGenreInfo( genres.getUrl(), (audios, albums, artists, playlist) -> {
                    initPlaylist( playlist );
                    initAlbums( albums );
                    if(audios.size() > 25){
                        moreaudio.setVisibility( View.VISIBLE );
                        moreaudio.setOnClickListener( v->{
                            loadFragment( list_fragment.newInstance( audios ) );
                        } );
                        initAudio( audios.subList( 0,25 ) );
                    }else
                    initAudio( audios );
                } );
            } else if (artist != null) {
                like.setVisibility( View.GONE );
                Artist_view.setVisibility( View.GONE );
                img.setVisibility( View.GONE );
                more.setVisibility( View.VISIBLE );
                avatar.setVisibility( View.VISIBLE );
                titleBar.setText( util.resizeString( artist.getTitle() ) );
                Picasso.with( view.getContext() ).load( util.getimgUrl( artist.getImg(), size_big ) ).error( getResources().getDrawable( R.drawable.ic_man ) ).into( avatar );
                big.setText( artist.getTitle() );
                small.setText( "" );
                server.getArtistsAllData( new Interfaces.OnAllContent() {
                    @Override
                    public void onTracks(List<Audio> data) {
                        initAudio( data );
                    }

                    @Override
                    public void onAlbum(List<Album> data) {
                        initAlbums( data );
                    }

                    @Override
                    public void onArtist(List<Artist> data) {
                        initArtists( data );
                    }

                    @Override
                    public void onInfoArtist(Artist data) {

                    }
                }, artist.getId() );
            } else if (artistid != null) {
                like.setVisibility( View.GONE );
                Artist_view.setVisibility( View.GONE );
                img.setVisibility( View.GONE );
                album_view.setVisibility( View.GONE );
                server.getArtistsAllData( new Interfaces.OnAllContent() {
                    @Override
                    public void onTracks(List<Audio> data) {
                        initAudio( data );
                    }

                    @Override
                    public void onAlbum(List<Album> data) {
                        initAlbums( data );
                    }

                    @Override
                    public void onArtist(List<Artist> data) {
                        initArtists( data );
                    }

                    @Override
                    public void onInfoArtist(Artist data) {
                        artist = data;
                        more.setVisibility( View.VISIBLE );
                        avatar.setVisibility( View.VISIBLE );
                        titleBar.setText( util.resizeString( artist.getTitle() ) );
                        Picasso.with( view.getContext() ).load( util.getimgUrl( artist.getImg(), size_big ) ).error( getResources().getDrawable( R.drawable.ic_man ) ).into( avatar );
                        big.setText( artist.getTitle() );
                        small.setText( "" );
                    }
                }, artistid );
            }
        }
    }
    private void initPlaylist(List<Album> data) {
        banners.setVisibility( View.VISIBLE );
        Ultraview_pagerAdapter ultraview = new Ultraview_pagerAdapter(
                ((MainActivity) view.getContext()).getSupportFragmentManager(),data );
        ultraViewPager.setAdapter( ultraview );
        ultraViewPager.setMultiScreen(0.4f);
        ultraViewPager.setHGap( 0 );
        ultraViewPager.getViewPager().setPageMargin( -1*((int) util.dpToPixels(50,view.getContext())) );
        ultraViewPager.setOffscreenPageLimit(3);
        ultraViewPager.scrollNextPage();

        ultraViewPager.setPageTransformer(false, new UltraScaleTransformer() );
        ultraViewPager.setAutoScroll(5000);
//        playlist ="";
//        recyclerBanner.setItemViewCacheSize(6);
//        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),LinearLayout.HORIZONTAL,false );
//        Log.i( TAG, "initPlaylist: "+data.size() );
//        if(data.size() ==0)
//            recyclerBanner.setVisibility( View.GONE );
//
//        AlbumAdapter albumAdapter = new AlbumAdapter( view.getContext(),data,true );
//        albumAdapter.setOnClickListen( ( v, p,album) -> {
//            loadFragment( MoreInfo_fragment.newInstance( album ) );
//        } );
//        SnapHelper snapHelper = new LinearSnapHelper();
//
//        recyclerBanner.setLayoutManager( layoutManagerPopslar );
//        recyclerBanner.setAdapter( albumAdapter );
//        snapHelper.attachToRecyclerView( recyclerBanner );
//        Thread_banner_swipe thread_banner_swipe = new Thread_banner_swipe( layoutManagerPopslar,
//                recyclerBanner,5000,data.size());
//        thread_banner_swipe.start();



    }
    private void initAlbums(List<Album> data) {

        if(data.size() != 0)
        album_view.setVisibility( View.VISIBLE );
        else{
            album_view.setVisibility( View.GONE );
        }

        List<Album> finalData = data;
        moreAlbums.setOnClickListener( v -> {  loadFragment( list_fragment.newInstance( finalData, 0) );} );
        albumAdapter = new AlbumAdapter( view.getContext(),data);
        albumAdapter.setOnClickListen( (v,position,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ) );
        } );
        Albumrecycler.setLayoutManager( new LinearLayoutManager( view.getContext(), HORIZONTAL,false ) );
        Albumrecycler.setAdapter( albumAdapter );
    }
    private void initArtists(List<Artist> data) {
        if(data.size() ==0) Artist_view.setVisibility( View.GONE );
        else Artist_view.setVisibility( View.VISIBLE);

        data = util.turn_overArtist( data );
        List<Artist> finalData = data;
        moreArtist.setOnClickListener( v -> {  loadFragment( list_fragment.newInstance( finalData, "") );} );
        artistAdapter = new ArtistAdapter( view.getContext(),data);
        artistAdapter.setOnClickListen( (v,position,artist) -> {
            loadFragment( MoreInfo_fragment.newInstance( artist ) );
        } );
        Artistrecycler.setLayoutManager( new LinearLayoutManager( view.getContext(), HORIZONTAL,false ) );
        Artistrecycler.setAdapter( artistAdapter );
    }
    private void loadFragment(Fragment fragment) {
        adManager.setPerehod();
//        if(artist != null || album !=null || playlist != null ) {
            MainActivity.Frame = 2;
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.addToBackStack( null );
            ft.add( frameLayout1.getId(), fragment );
            ft.commit();
//        }else{
//            MainActivity.Frame = 1;
//            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//            ft.addToBackStack( null );
//            ft.add( frameLayout.getId(), fragment );
//            ft.commit();
//        }
    }
    private void likeCheck(){
         if(album != null)
             if(storage.checkLikeAlbums( album )){
                 like.setImageDrawable( view.getContext().getResources().getDrawable( R.drawable.like ) );
             }else{
                 like.setImageDrawable( view.getContext().getResources().getDrawable( R.drawable.like_outline ) );
             }
    }
    private void initAudio(List<Audio> data){
        progressBar.setVisibility( View.GONE );
        Log.println(Log.ERROR,"CHECK", String.valueOf( data.size() ));
        adapter = new AudioAdapter( view.getContext(),data) {
            @Override
            public void load() {

            }
        };
        adapter.setOnClickListen( (datal, position, v) -> {
            MainActivity.audioList = data;
            MainActivity.playAudio( position,v.getContext(),recyclerView );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setAdapter( adapter );

    }

}
