package iwinux.com.music.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.R;
import iwinux.com.music.utils.Adapters.AlbumAdapter;
import iwinux.com.music.utils.Adapters.ArtistAdapter;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.MainActivity.frameLayout;


public class search_fragment extends Fragment {

    View view;
    RecyclerView recyclerView,recyclerArtist,recyclerAlbum;
    Server server;
    View moreAlbums,moreArtist,view_album,view_artist;
    MaterialEditText editText;
    AudioAdapter adapter;
    TextView error_text;
    View prograss_bar;
    AlbumAdapter albumAdapter;
    ListView search_querys;
    NestedScrollView result;
    ArtistAdapter artistAdapter;
    Utils util;
    StorageUtil storageUtil;
    List<Artist> artists;
    List<Audio> audios;
    List<Album> albums;
    ArrayAdapter<String> query_adapter;
    List<String> querys = new ArrayList<>();
    LinearLayoutManager layoutManagerPopslar;

    public search_fragment () {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate( R.layout.search_fragment, container, false);
        util = new Utils();
        storageUtil = new StorageUtil( view.getContext() );
        server = new Server(view.getContext());
        content();
        return view;
    }

    private void getData(String query) {
        hideSoftKeyboard( editText );
        view_album.setVisibility( View.GONE );
        view_artist.setVisibility( View.GONE );
        hideQuerys( true );
        Log.i( TAG, "getData: "+query );
        initAudio( new ArrayList<>(  ) );
        prograss_bar.setVisibility( View.VISIBLE );
        server.getSearch( (audios, albums, artists,playlist) -> {
            prograss_bar.setVisibility( View.GONE );
            if((audios.size() == 0 )&& (albums.size() == 0 )&& (artists.size() == 0)) {
                error_text.setVisibility( View.VISIBLE );
                result.setVisibility( View.GONE );
                Log.i( TAG, "getData: SPace" );
            }
            else {
                this.audios = audios;
                this.albums = albums;
                this.artists = artists;
                initAlbums( albums );
                initAudio( audios );
                initArtists( artists );
            }
        },query );

    }
    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) this.view.getContext().getSystemService( Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    public void onStart() {
        super.onStart();
        if(editText != null){
            if(!editText.getText().toString().equals( "" ))
                getData( editText.getText().toString() );
        }
    }

    private void content() {
        prograss_bar = view.findViewById( R.id.progress_bar );
        query_adapter =  new  ArrayAdapter<>(
                view.getContext(), android.R.layout.simple_list_item_1, querys);
        search_querys = view.findViewById( R.id.list_view );
        error_text = view.findViewById( R.id.error_text );
        recyclerView = view.findViewById( R.id.tracks_search );
        recyclerAlbum = view.findViewById( R.id.recyclerAlbum );
        recyclerArtist = view.findViewById( R.id.recyclerArtist );
        view_album = view.findViewById( R.id.albums );
        view_artist = view.findViewById( R.id.artists );
        moreAlbums = view.findViewById( R.id.moreAlbums );
        moreArtist = view.findViewById( R.id.moreArtist );
        result = view.findViewById( R.id.result );
        editText = view.findViewById( R.id.editText );
        search_querys.setAdapter( query_adapter );

        PublishSubject<String> searchPublishSubject = PublishSubject.create();
        Observable<String> observable = searchPublishSubject.debounce(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String strings) {
                Log.e("observer","onNext"+strings);
                getSuggestedTopics( strings );
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        observable.subscribe(observer);
        editText.setOnClickListener( v -> {
            if(!editText.getText().toString().equals( "" ))
                getSuggestedTopics( editText.getText().toString() );
            initlist( new ArrayList<>(  ) );
            hideQuerys( false );
            error_text.setVisibility( View.GONE );
        });
        editText.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchPublishSubject.onNext(s.toString());
            }
        } );
        editText.setOnKeyListener( (v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_ENTER))
            {
                // сохраняем текст, введенный до нажатия Enter в переменную
//                Toast.makeText( v.getContext(), "text", Toast.LENGTH_SHORT ).show();
                getData( ((MaterialEditText) v).getText().toString() );
                return true;
            }
            return false;

        } );
        search_querys.setOnItemClickListener( (parent, view, position, id) -> {
            hideSoftKeyboard( editText );
            editText.setText( query_adapter.getItem( position )  );
            getData( query_adapter.getItem( position ) );
        } );
        if(getActivity().getIntent().getStringExtra( "find" ) != null && editText.getText().toString().equals( "" )){
            editText.setText( getActivity().getIntent().getStringExtra( "find" ) );
            getData( getActivity().getIntent().getStringExtra( "find" ) );

        }


    }
    private void initlist(List<String> data){
        query_adapter.clear();
        query_adapter.notifyDataSetChanged();
        query_adapter =  new  ArrayAdapter<>(
                view.getContext(), android.R.layout.simple_list_item_1, data);
        search_querys.setAdapter( query_adapter );
    }
    private void getSuggestedTopics(String s) {
        if(s.length() == 0){
            initlist( new ArrayList<>() );
        }else{
            try {
                server.getQuerySearch( data ->{
                    if(data.size() != 0){
                        initlist( data );
                    }else{
                        initlist( new ArrayList<>() );
                    }
                }, URLEncoder.encode(s, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                server.getQuerySearch( data ->{
                    if(data.size() != 0){
                        initlist( data );
                    }else{
                        initlist( new ArrayList<>() );
                    }
                }, s);
                e.printStackTrace();
            }
        }
    }

    private void hideQuerys(Boolean hide){

        if(hide){ result.setVisibility( View.VISIBLE );
           search_querys.setVisibility( View.GONE );
        }else {
            result.setVisibility( View.GONE );
            search_querys.setVisibility( View.VISIBLE );
        }
    }
    private void initAudio(List<Audio> data){
        Log.println(Log.ERROR,"CHECK", String.valueOf( data.size() ) );
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

    void loadFragment(Fragment fragment) {
        MainActivity.Frame = 1;
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }

    private void initArtists(List<Artist> data) {
        if(data.size() ==0) view_artist.setVisibility( View.GONE );
        else view_artist.setVisibility( View.VISIBLE );
        moreArtist.setOnClickListener( v ->   {loadFragment( list_fragment.newInstance( data, "" )); });
        recyclerArtist.setPadding( util.dpToPixel( 10,view.getContext() ),0 ,0,0);
        artistAdapter = new ArtistAdapter( view.getContext(),data,true);
        artistAdapter.setOnClickListen( (v,position,artist) -> {
            loadFragment( MoreInfo_fragment.newInstance( artist ) );
        } );
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerArtist.setLayoutManager(layoutManagerPopslar );
        recyclerArtist.setAdapter(  artistAdapter );
    }

    private void initAlbums(List<Album> data) {
        if(data.size() ==0) view_album.setVisibility( View.GONE );
        else view_album.setVisibility( View.VISIBLE );
        moreAlbums.setOnClickListener( v ->   {loadFragment( list_fragment.newInstance( data, 0 )); });
        recyclerAlbum.setPadding( util.dpToPixel( 10,view.getContext() ),0 ,0,0);
        albumAdapter = new AlbumAdapter( view.getContext(),data);
        layoutManagerPopslar = new LinearLayoutManager( view.getContext(),
                LinearLayoutManager.HORIZONTAL,false);
        albumAdapter.setOnClickListen( (v,position,album) -> {
            loadFragment( MoreInfo_fragment.newInstance( album ) );

        } );
        recyclerAlbum.setLayoutManager( layoutManagerPopslar );
        recyclerAlbum.setAdapter( albumAdapter );
    }


}
