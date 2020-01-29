package iwinux.com.music.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Genre;
import iwinux.com.music.R;
import iwinux.com.music.utils.Adapters.ArtistAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.MainActivity.frameLayout;

public class genre_fragment extends Fragment {

    Server server;
    Utils util;
    StorageUtil storageUtil;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManagerPopslar;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate( R.layout.list_fragment, container, false);
        util = new Utils();
        storageUtil = new StorageUtil( view.getContext() );
        server = new Server(view.getContext());
        content();
        return view;
    }

    private void content() {
        recyclerView = view.findViewById( R.id.list );
        initGenres( storageUtil.getGenres() );
    }
    private void initGenres(List<Genre> data){
        ArtistAdapter genreAdapter = new ArtistAdapter( view.getContext(),data,"");
        Log.e( TAG, "initGenres: "+data.size()  );
        if(data.size() == 0)
            server.getGenre( (data1)->{
                storageUtil.saveGenres( data1 );
                initGenres( data1 );
            } );
        genreAdapter.setOnGenre( genre -> {
            loadFragment( MoreInfo_fragment.newInstance( genre ) );
        } );
        GridLayoutManager gridLayoutManager = new GridLayoutManager( view.getContext(),2 );

        recyclerView.setLayoutManager( gridLayoutManager );
        recyclerView.setAdapter( genreAdapter );
    }
    void loadFragment(Fragment fragment) {
        MainActivity.Frame = 1;
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(frameLayout.getId(), fragment);
        ft.commit();
    }
}
