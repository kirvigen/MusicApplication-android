package iwinux.com.music.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.ultraviewpager.transformer.UltraScaleTransformer;

import java.util.Collections;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;
import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.R;
import iwinux.com.music.testing.CardFragmentPagerAdapter;
import iwinux.com.music.testing.CardPagerAdapter;
import iwinux.com.music.testing.ShadowTransformer;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.Adapters.AdapterCardGenres;
import iwinux.com.music.utils.Adapters.AlbumAdapter;
import iwinux.com.music.utils.Adapters.ArtistAdapter;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.Adapters.Ultraview_pagerAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.MainActivity.frameLayout;

public class top_fragment extends Fragment {
    RecyclerView recyclerView,recyclerAlbums,recyclerArtist,recyclerBanner;
    AudioAdapter adapter;
    AlbumAdapter albumAdapter;
    ArtistAdapter artistAdapter;
    Utils util;
    Context context;
    TextView moreArtist, moreAlbums;
    List<Album> albums;

    int height;
    List<Artist> artists;
    NestedScrollView nestedScrollView;
    LinearLayoutManager layoutManagerPopslar;
    View view,ad_block;
    Server server;
    CardView moreAudio;
    RecyclerView genres;
    RelativeLayout check;
    ShimmerLayout loading;
    UserManager userManager;
    LinearLayout error_dialog,content;
    AppBarLayout appBarLayout;
    CardView refresh;
    View share;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private CardFragmentPagerAdapter mFragmentCardAdapter;
    private ShadowTransformer mFragmentCardShadowTransformer;

    private boolean mShowingFragments = false;
    UltraViewPager ultraViewPager;
    private Dialog alert;
    private AdManager adManager;
    private String share_text ="Приложение для прослушивания и скачивания музыки БЕСПЛАТНО https://play.google.com/store/apps/details?id=iwinux.com.music";
    private StorageUtil storage;

    public top_fragment() {
    }

    public static top_fragment newInstance() {
        return new top_fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.top_fragment, container, false);
        context = view.getContext();
        adManager = new AdManager( view.getContext() );
        util = new Utils();
        userManager = new UserManager( view.getContext() );
        content();
        DataLoad();
        return view;
    }

    private void content() {
        recyclerBanner = view.findViewById( R.id.recyclerBanner );
        recyclerView = view.findViewById( R.id.recycler );
        recyclerAlbums = view.findViewById( R.id.recyclerAlbum );
        recyclerArtist = view.findViewById( R.id.recyclerArtist );
        moreArtist = view.findViewById( R.id.moreArtist );
        loading = view.findViewById( R.id.loading );
        moreAlbums = view.findViewById( R.id.moreAlbums );
        share = view.findViewById( R.id.share );
        moreAudio = view.findViewById( R.id.moreaudio );
        nestedScrollView = view.findViewById( R.id.nested );
        appBarLayout = view.findViewById( R.id.appbar );
        check = view.findViewById( R.id.check );
        ad_block = view.findViewById( R.id.ad_block );
        error_dialog = view.findViewById( R.id.error_dialog );
        refresh = view.findViewById( R.id.refresh );
        genres = view.findViewById( R.id.genres );
        content = view.findViewById( R.id.content );
        server = new Server( view.getContext(), e -> {
             error_dialog.setVisibility( View.VISIBLE );
             refresh.setOnClickListener( v -> {
                 DataLoad();
                 error_dialog.setVisibility( View.GONE );
             });
        } );
        storage = new StorageUtil( view.getContext() );
        ultraViewPager = view.findViewById( R.id.ultraviewpager );
        if(userManager.getsettings().getPremium() || userManager.getsettings().getPay())
            ad_block.setVisibility( View.GONE );
        ad_block.setOnClickListener( v->{
            ShowAd();
        } );
        share.setOnClickListener( v->{
            Intent shas= new Intent(Intent.ACTION_SEND);
            shas.setType("text/plain");
            shas.putExtra(android.content.Intent.EXTRA_TEXT,share_text);
            startActivityForResult(Intent.createChooser(shas, "Поделиться"),0);
        } );

//        nestedScrollView.setOnScrollChangeListener( (NestedScrollView.OnScrollChangeListener) (nestedScrollView, i, i1, i2, i3) -> {
//            height = appBarLayout.getHeight();
//            if(nestedScrollView.getScrollY() < 3){
//                appBarLayout.animate().alpha( 0 ).start();
//            }else{
//                appBarLayout.setAlpha( (float) (nestedScrollView.getScrollY()-height)/height );
//            }
//
//        } );
    }

    private void ShowAd() {
        Dialog dialog = new Dialog( view.getContext() );
        dialog.setContentView( R.layout.no_ad );
        dialog.setCancelable( true );
        dialog.getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
        View button_pay = dialog.findViewById( R.id.pay );
        button_pay.setOnClickListener( v->{
            //pay
            util.Pay( MainActivity.mCheckout,view.getContext() );
        } );
        View promocode = dialog.findViewById( R.id.promo );
        promocode.setOnClickListener( v->{
            dialog.dismiss();
            initPromo();
        } );
        dialog.show();

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
    private void DataLoad() {
        server.getTop( (audios,albums,artists,playlist) -> {
            Log.i( TAG, "DataLoad: "+audios.get( 0 ).getTitle());
            initPlaylist( playlist );
            initAudio( audios );
            initAlbums(albums);
            initArtist( artists );
            initAd();

            loading.setVisibility( View.GONE );
        } );
    }
    void loadFragment(Fragment fragment) {
        adManager.setPerehod();
        MainActivity.Frame = 1;
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }
    void initAd(){
        AdManager adManager = new AdManager( view.getContext() );
        if(!userManager.getsettings().getPremium() && adManager.getAd().getTop_fragment().equals( "1" )  ){
            AdView mAdView = view.findViewById( R.id.adview );
            mAdView.setVisibility( View.VISIBLE );
            AdRequest adRequest = new AdRequest.Builder().addTestDevice( "23C722AFDD17C32267DC2FC8B58AB0EB" ).build();
            mAdView.loadAd( adRequest );
        }
    }
    private void initGenres(){
        genres.setVisibility( View.VISIBLE );
        AdapterCardGenres adapterCardGenres = new AdapterCardGenres( view.getContext(),storage.getGenres(),null );
        adapterCardGenres.setOnGenre( genre -> {
            loadFragment( MoreInfo_fragment.newInstance( genre ) );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        genres.setAdapter( adapterCardGenres );
        genres.setHasFixedSize(true);
        genres.setLayoutManager(layoutManagerPopslar );
        genres.setItemViewCacheSize(10);

    }
    private void initPlaylist(List<Album> data) {
        Ultraview_pagerAdapter ultraview = new Ultraview_pagerAdapter(
                ((MainActivity) view.getContext()).getSupportFragmentManager(),data );
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        ultraViewPager.setAdapter( ultraview );
        ultraViewPager.setMultiScreen(0.4f);
        ultraViewPager.setHGap( 0 );
        ultraViewPager.getViewPager().setPageMargin( -1*((int) util.dpToPixels(50,view.getContext())) );
        ultraViewPager.setOffscreenPageLimit( 4 );
        ultraViewPager.scrollNextPage();
        ultraViewPager.setPageTransformer(false, new UltraScaleTransformer() );
        ultraViewPager.setAutoScroll(10000);

//        ViewPager mViewPager = view.findViewById(R.id.ultraviewpager);
//        mViewPager.setAdapter(ultraview);
//        mViewPager.setPageMargin(  );
//        mViewPager.setOffscreenPageLimit(3);
//        recyclerBanner.setItemViewCacheSize(6);
//        Log.i( TAG, "initPlaylist: "+data.size() );
//        if(data.size() ==0)
//            recyclerBanner.setVisibility( View.GONE );
//
//        albumAdapter = new AlbumAdapter( view.getContext(),data,true );
//        albumAdapter.setOnClickListen( ( v, p,album) -> {
//            loadFragment( MoreInfo_fragment.newInstance( album ) );
//        } );
//        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
//                LinearLayoutManager.HORIZONTAL,false);
//        SnapHelper snapHelper = new LinearSnapHelper();
//        recyclerBanner.setLayoutManager(layoutManagerPopslar );
//        recyclerBanner.setAdapter( albumAdapter );
//        snapHelper.attachToRecyclerView( recyclerBanner );
//        Thread_banner_swipe thread_banner_swipe = new Thread_banner_swipe( layoutManagerPopslar,
//                recyclerBanner,5000,data.size());
//        thread_banner_swipe.start();
    }
    private void initAlbums(List<Album> data) {

        recyclerAlbums.setItemViewCacheSize(6);
        albums = data;
        moreAlbums.setOnClickListener( v ->  loadFragment( list_fragment.newInstance( data,0 ) ) );
        albumAdapter = new AlbumAdapter( view.getContext(),data.subList( 0,5 ) );
        albumAdapter.setOnClickListen( ( v, p,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ) );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerAlbums.setLayoutManager(layoutManagerPopslar );
        recyclerAlbums.setAdapter( albumAdapter );
    }

    private void initAudio(List<Audio> data){
        if(storage.getGenres().size() > 0){
            initGenres();
        }
        moreAudio.setOnClickListener( v -> loadFragment( list_fragment.newInstance( data ) ) );
        if(storage.getOpen()%3==0)
        Collections.shuffle(data);
        if(data.size()>=11)
        adapter = new AudioAdapter( view.getContext(), data.subList( 0, 10 ) ) {
            @Override
            public void load() {

            }
        };
        else
            adapter = new AudioAdapter( view.getContext(), data ) {
                @Override
                public void load() {
                }
            };
        adapter.setOnClickListen( (data1, position, v) -> {
            MainActivity.audioList = data;
            MainActivity.playAudio( position,v.getContext(),recyclerView );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter( adapter );
        if(!userManager.getsettings().getPremium() && !userManager.getsettings().getPay()){
            ad_block.setVisibility( View.VISIBLE );
        }

    }

    private void initArtist(List<Artist> data) {
        artists = data;
        moreArtist.setOnClickListener( v ->  loadFragment( list_fragment.newInstance( data,"" ) ));
        artistAdapter = new ArtistAdapter( view.getContext(),data.subList( 0,4 ) );
        artistAdapter.setOnClickListen( (  v,position,artist) -> {
            if(artist.getId().equals( "more" ))
                loadFragment( list_fragment.newInstance( data,"" ) );
                else
            loadFragment( MoreInfo_fragment.newInstance( artist ) );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerArtist.setLayoutManager(layoutManagerPopslar );
        recyclerArtist.setAdapter( artistAdapter );
    }


}
