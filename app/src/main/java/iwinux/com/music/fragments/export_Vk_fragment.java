package iwinux.com.music.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.Data.Server;
import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Audio;
import iwinux.com.music.R;
import iwinux.com.music.utils.Adapters.AudioAdapter;
import iwinux.com.music.utils.StorageUtil;
import iwinux.com.music.utils.Utils;

public class export_Vk_fragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    Server server;
    EditText editText;
    TextView loading_text;
    ImageView load_button;
    StorageUtil storageUtil;
    Utils util;
    ProgressBar progressBar;
    static public String LINK_SAVE = "LINK_VK";
    private AudioAdapter adapter;
    private LinearLayoutManager layoutManagerPopslar;
    private List<Audio> audios1;

    public export_Vk_fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate( R.layout.activity_export_vk, container, false);
        util = new Utils();
        server = new Server(view.getContext());
        content();
        return view;
    }
    private void content() {

        server  = new Server(view.getContext() );
        storageUtil = new StorageUtil( view.getContext() );
        editText = view.findViewById( R.id.editText );
        loading_text = view.findViewById( R.id.load_text );
        progressBar = view.findViewById( R.id.prograss_bar );
        load_button = view.findViewById( R.id.import_vk );
        recyclerView = view.findViewById( R.id.recycler );


        if(!storageUtil.loadText( LINK_SAVE ).equals( "" )){
            editText.setText( storageUtil.loadText( LINK_SAVE ) );
//            StartSearch();
        }
        load_button.setOnClickListener( v->{
            if(!editText.getText().toString().equals( "" )){
                StartSearch();
            }
        } );
    }
    void StartSearch(){
        initAudio( new ArrayList<>(  ) );
        editText.setEnabled( false );
        load_button.setVisibility( View.GONE );
        progressBar.setVisibility( View.VISIBLE );
        loading_text.setVisibility( View.VISIBLE );
        loading_text.setText( getResources().getString( R.string.loading_text ) );
        server.GetAudioVk( this::initAudio,editText.getText().toString(), e -> {
            load_button.setVisibility( View.VISIBLE );
            editText.setEnabled( true );
            progressBar.setVisibility( View.GONE );
            loading_text.setText( e );

        } );
    }
    private void initAudio(List<Audio> data){
        editText.setEnabled( true );
        if(data.size() != 0)
            storageUtil.saveText( LINK_SAVE,editText.getText().toString() );
        load_button.setVisibility( View.VISIBLE );
        progressBar.setVisibility( View.GONE );
        loading_text.setText( getResources().getString( R.string.download_now ) );
        recyclerView.setItemViewCacheSize(20);
        Log.println(Log.ERROR,"CHECK", String.valueOf( data.size() ));
        audios1 = data;

        adapter = new AudioAdapter( view.getContext(), data, 1 ) {
            @Override
            public void load() {

            }
        };
        adapter.setOnClickListen( (datal, position, v) -> {
            MainActivity.audioList = data;
            MainActivity.playAudio( position,v.getContext(),recyclerView );

        } );
        layoutManagerPopslar = new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManagerPopslar );
        recyclerView.setAdapter( adapter );

    }
}
