package iwinux.com.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.R;
import iwinux.com.music.utils.AdManager;
import iwinux.com.music.utils.Adapters.AlbumAdapter;
import iwinux.com.music.utils.Adapters.ArtistAdapter;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.Utils;

import static iwinux.com.music.Data.p_Strings_Data.size_big;
import static iwinux.com.music.MainActivity.frameLayout1;
import static iwinux.com.music.MainActivity.player;

@SuppressLint("ValidFragment")
public class list_fragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    Server server;
    AudioAdapter adapter;
    AlbumAdapter albumAdapter;
    ArtistAdapter artistAdapter;
    Utils util;
    ImageView img;
    LinearLayout more;
    CircleImageView avatar;
    List<Audio> audios1;
    Album album;
    String name;
    TextView name_view;
    Artist artist;
    List<Audio> audio;
    String ids;
    NestedScrollView nestedScrollView;
    StorageUtil storageUtil;
    List<Album> albums;
    List<Artist> artists;
    ProgressBar progressBar;
    TextView big,small;
    LinearLayoutManager layoutManagerPopslar;
    private AdManager adManager;

    public list_fragment(List<Album> albums,int d){
        this.albums = albums;
    }
    public list_fragment(List<Artist> artists,String r){
        this.artists = artists;
    }
    public list_fragment(String name,List<Audio> audio){
        this.name = name;
        this.audio = audio;
    }
    public list_fragment(List<Audio> audio) {
        this.audio = audio;
    }

    public list_fragment(String ids_tracks) {
        this.ids = ids_tracks;
    }

    public static list_fragment newInstance(List<Audio> audio) {
        return new list_fragment(audio);
    }
    public static list_fragment newInstance(List<Artist> data,String p) {
        return new list_fragment(data,p);
    }
    public static list_fragment newInstance(String name,List<Audio> audio) {
        return new list_fragment(name,audio);
    }
    public static list_fragment newInstance(String ids_tracks) {
        return new list_fragment(ids_tracks);
    }

    public static list_fragment newInstance(List<Album> data, int i) {
        return new list_fragment(data, i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate( R.layout.list_fragment, container, false);
        util = new Utils();
        adManager = new AdManager( view.getContext() );
        server = new Server(view.getContext());
        storageUtil = new StorageUtil( view.getContext() );
        content();
        getData();
        return view;
    }

    private void getData() {
        progressBar.setVisibility( View.VISIBLE);
        if(album != null) {
            more.setVisibility( View.VISIBLE );
            Picasso.with( view.getContext() ).load( util.getimgUrl( album.getImgurl(), size_big ) )
                    .placeholder( getResources().getDrawable( R.drawable.placeholder ) ).into( img );
            big.setText( album.getTitle() );
            small.setText( album.getArtist() );
            server.getTracksIds( this::initAudio, album.getIds_track() );
        }else if(audio !=null){
            initAudio( audio );
        }else if(albums != null){
            initAlbums(albums);
        }else if(artists != null){
            initArtists(artists);
        }else if(ids != null){
            server.getTracksIds( this::initAudio,ids );
        }
        if(name != null){
            initAudio( storageUtil.loadAudio() );
            name_view.setVisibility( View.VISIBLE );
            name_view.setText( name );
            player.initRecycler( recyclerView );
        }
    }
    private void initAudio(List<Audio> data){
        progressBar.setVisibility( View.GONE );
        recyclerView.setItemViewCacheSize(10);
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
    private void content() {
        progressBar = view.findViewById( R.id.progress_bar );
        nestedScrollView = view.findViewById( R.id.nested );
        name_view = view.findViewById( R.id.name );
        recyclerView = view.findViewById( R.id.list );
        img = view.findViewById( R.id.img );
        big = view.findViewById( R.id.title );
        small = view.findViewById( R.id.artist );
        more = view.findViewById( R.id.moreInfo );
        avatar = view.findViewById( R.id.avatar );
    }
    void loadFragment(Fragment fragment) {
        adManager.setPerehod();
        MainActivity.Frame = 2;
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(frameLayout1.getId(), fragment);
        ft.commit();

    }

    private void initArtists(List<Artist> data) {
        progressBar.setVisibility( View.GONE );
        recyclerView.setPadding( util.dpToPixel( 10,view.getContext() ),0 ,0,0);
        artistAdapter = new ArtistAdapter( view.getContext(),data);
        artistAdapter.setOnClickListen( (v,position,artist) -> {
            loadFragment( MoreInfo_fragment.newInstance( artist ) );
        } );

        recyclerView.setLayoutManager( new GridLayoutManager(view.getContext(),3) );
        recyclerView.setAdapter(  artistAdapter );
    }

    private void initAlbums(List<Album> data) {
        progressBar.setVisibility( View.GONE );
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setPadding( util.dpToPixel( 10,view.getContext() ),0 ,0,0);
        albumAdapter = new AlbumAdapter( view.getContext(),data);
        albumAdapter.setOnClickListen( (v,position,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ) );

        } );
        recyclerView.setLayoutManager( new GridLayoutManager(view.getContext(),3) );
        recyclerView.setAdapter( albumAdapter );
    }
}