package iwinux.com.music.utils.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Objects.Genre;
import iwinux.com.music.R;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;

public class AdapterCardGenres extends RecyclerView.Adapter<AdapterCardGenres.Holder> {
    Context context;
    Utils utils;
    List<Genre> genres;

    Interfaces.OnClickArtistListen onClickListen;
    private Interfaces.Ganere ongenre;


    public AdapterCardGenres(Context context, List<Genre> genres, Interfaces.Ganere s){
        this.context = context;
        this.genres = genres;
        ongenre = s;
        utils = new Utils();
        Log.i( TAG, "AdapterCardGenres: Complite" );
    }
    @Override
    public AdapterCardGenres.Holder onCreateViewHolder(ViewGroup parent, int viwe) {
        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View itemview = inflater.inflate( R.layout.item_genre_top, parent, false );
        return new AdapterCardGenres.Holder( itemview );
    }

    @Override
    public void onBindViewHolder(AdapterCardGenres.Holder holder, int position) {
            Genre genre = genres.get( position );
            Log.i( TAG, "onBindViewHolder: " + genre.getUrlImg());
//            Glide.with( context ).load( genre.getUrlImg() ).dontAnimate().into( holder.Img );
            holder.button.setText( genre.getName() );
            holder.button.setOnClickListener( v->ongenre.ongenres( genre ) );

    }

    @Override
    public int getItemCount() {
            return genres.size();
    }

    @Override
    public int getItemViewType(int position) {
        position=position+1;
        return position;
    }
    public void setOnGenre(Interfaces.Ganere onGenre){
        this.ongenre = onGenre;
    }
    public void  setOnClickListen(Interfaces.OnClickArtistListen onClickListen){
        this.onClickListen = onClickListen;
    }
    class Holder extends RecyclerView.ViewHolder {
        Button button;
        public Holder(View itemView) {
            super( itemView );
            button = itemView.findViewById( R.id.genre );

        }
    }
}
