package iwinux.com.music.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Collections;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.Objects.SettingsUser;
import iwinux.com.music.R;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.Adapters.AlbumAdapter;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.UserManager;
import iwinux.com.music.utils.Utils;

import static iwinux.com.music.MainActivity.frameLayout;

public class new_fragment extends Fragment {
    RecyclerView recyclerView,recyclerAlbums,recyclerArtist;
    AudioAdapter adapter;
    AlbumAdapter albumAdapter;

    Utils util;
    Context context;
    List<Album> albums;
    ShimmerLayout loadingShimmer;

    String ids_tracks;
    LinearLayoutManager layoutManagerPopslar;
    View view;
    AdManager adManager;
    CardView moreAudio,moreAlbums;
    private UserManager userManager;
    private StorageUtil storage;

    public new_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.new_fragment, container, false);
        context = view.getContext();
        adManager = new AdManager( view.getContext() );
        userManager = new UserManager( view.getContext() );
        util = new Utils();
        content();
        DataLoad();
        return view;
    }

    private void content() {
        loadingShimmer = view.findViewById( R.id.load );
        recyclerView = view.findViewById( R.id.recycler );
        recyclerAlbums = view.findViewById( R.id.recyclerAlbum );
        recyclerArtist = view.findViewById( R.id.recyclerArtist );
        moreAudio = view.findViewById( R.id.moreaudio );
        moreAlbums = view.findViewById( R.id.moreAlbums );
        loadingShimmer.startShimmerAnimation();
        storage = new StorageUtil( view.getContext() );
    }
    void initAd(){
        SettingsUser settingsUser = userManager.getsettings();
        AdManager adManager = new AdManager( view.getContext() );
        if(!settingsUser.getPremium() && adManager.getAd().getNew_fragment().equals( "1" ) ){
            AdView mAdView = view.findViewById( R.id.adview );
            mAdView.setVisibility( View.VISIBLE );
            AdRequest adRequest = new AdRequest.Builder().addTestDevice( "23C722AFDD17C32267DC2FC8B58AB0EB" ).build();
            mAdView.loadAd( adRequest );
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    private void DataLoad() {
        Server server = new Server(view.getContext());
        server.getNew( (audios,albums,ids) -> {

            initAudio( audios );
            initAlbums(albums);
            loadingShimmer.setVisibility( View.GONE );
            ids_tracks = ids;
            initAd();
        } );
    }
    private void initAlbums(List<Album> data) {
        albums = data;
        moreAlbums.setOnClickListener( v -> loadFragment( list_fragment.newInstance( data,0 ) ));
        albumAdapter = new AlbumAdapter( view.getContext(),data.subList( 0,6 ) );
        albumAdapter.setOnClickListen( ( v, p,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ) );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerAlbums.setPadding( util.dpToPixel( 10,view.getContext() ),0,0 ,0);
        recyclerAlbums.setLayoutManager( new GridLayoutManager( view.getContext(),3 ) );
        recyclerAlbums.setAdapter( albumAdapter );
    }
    private void initAudio(List<Audio> data){
        moreAudio.setOnClickListener( v -> loadFragment( list_fragment.newInstance( ids_tracks ) ) );
        if(storage.getOpen()%3==0)
        Collections.shuffle(data);
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
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setAdapter( adapter );

    }
    void loadFragment(Fragment fragment) {
        MainActivity.Frame = 1;
        adManager.setPerehod();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack( "main" );
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }
}
