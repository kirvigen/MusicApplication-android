package iwinux.com.music.utils.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Objects.Artist;
import iwinux.com.music.Objects.Genre;
import iwinux.com.music.R;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.Data.p_Strings_Data.size_big;
import static iwinux.com.music.Data.p_Strings_Data.size_middle;

public class ArtistAdapter  extends RecyclerView.Adapter<ArtistAdapter.Holder> {
    Context context;
    List<Artist> artists;
    Utils utils;
    Boolean small = false;
    List<Genre> genres;
    Boolean G = false;
    Interfaces.OnClickArtistListen onClickListen;
    private Interfaces.Ganere ongenre;

    public ArtistAdapter(Context context, List<Artist> artists){
        this.context = context;
        this.artists = artists;
        utils = new Utils();
        Log.i( TAG, "AudioAdapter: Complite" );
    }
    public ArtistAdapter(Context context, List<Artist> artists,Boolean small){
        this.context = context;
        this.artists = artists;
        this.small = small;
        utils = new Utils();
        Log.i( TAG, "AudioAdapter: Complite" );
    }
    public ArtistAdapter(Context context, List<Genre> genres,String si){
        this.context = context;
        G = true;
        this.genres = genres;
        utils = new Utils();
        Log.i( TAG, "AudioAdapter: Complite" );
    }
    @Override
    public ArtistAdapter.Holder onCreateViewHolder(ViewGroup parent, int viwe) {
        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View itemview = inflater.inflate( R.layout.item_artist, parent, false );
        if(small)
        itemview = inflater.inflate( R.layout.small_artist_item, parent, false );
        return new ArtistAdapter.Holder( itemview );
    }

    @Override
    public void onBindViewHolder(ArtistAdapter.Holder holder, int position) {
        if(!G) {
            Artist artist = artists.get( position );
            String img;
            if (small) img = utils.getimgUrl( artist.getImg(), size_middle );
            else img = utils.getimgUrl( artist.getImg(), size_big );
            Glide.with( context ).load( img ).dontAnimate().error( context.getResources().getDrawable( R.drawable.ic_man ) ).into( holder.Img );
//        Picasso.with( context ).load( img ).memoryPolicy( MemoryPolicy.NO_CACHE )
//                .error( context.getResources().getDrawable( R.drawable.ic_man ) )
//                .into( holder.Img );
            holder.Title.setText( utils.resizeString( artist.getTitle() ) );
            holder.Artitst.setOnClickListener( v -> onClickListen.onClick( v, position, artist ) );
        }else{
            Genre genre = genres.get( position );
            Log.i( TAG, "onBindViewHolder: " + genre.getUrlImg());
            Glide.with( context ).load( genre.getUrlImg() ).dontAnimate().into( holder.Img );
            holder.Title.setText( genre.getName() );
            holder.Artitst.setOnClickListener( v->ongenre.ongenres( genre ) );
        }
    }

    @Override
    public int getItemCount() {
        if(!G)
        return artists.size();
        else{
            return genres.size();
        }
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

        TextView Title;
        CircleImageView Img;
        LinearLayout Artitst;
        public Holder(View itemView) {
            super( itemView );
            Img = itemView.findViewById( R.id.img );
            Artitst = itemView.findViewById( R.id.Artist );
            Title = itemView.findViewById( R.id.Title );
        }
    }
}
