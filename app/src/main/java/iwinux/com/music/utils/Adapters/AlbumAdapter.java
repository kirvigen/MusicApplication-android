package iwinux.com.music.utils.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import iwinux.com.music.Data.Interfaces;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.R;
import iwinux.com.music.utils.Utils;

import static com.android.volley.VolleyLog.TAG;
import static iwinux.com.music.Data.p_Strings_Data.size_big;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.Holder> {
    Context context;
    List<Album> albums;
    Utils util;
    Boolean playlist = false;
    Interfaces.OnClickAlmumListen onClickListen;
    public AlbumAdapter (Context context, List<Album> albumItems){
        this.context = context;
        this.albums = albumItems;
        util = new Utils();
        Log.i( TAG, "AlbumAdapter: Complite" );
    }
    public AlbumAdapter (Context context, List<Album> albumItems,Boolean playlist){
        this.context = context;
        this.playlist = playlist;
        this.albums = albumItems;
        util = new Utils();
        Log.i( TAG, "AlbumAdapter: Complite" );
    }
    @Override
    public AlbumAdapter.Holder onCreateViewHolder(ViewGroup parent, int viwe) {
        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View itemview;
        if(!playlist)
        itemview = inflater.inflate( R.layout.item_album, parent, false );
        else{
            itemview = inflater.inflate( R.layout.item_banner, parent, false );
        }
        return new AlbumAdapter.Holder( itemview );
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.Holder holder, int position) {
           Album item = albums.get( position );
           if(!playlist) {
               if (!item.getArtist().equals( "" )) {
                   holder.Artitst.setVisibility( View.VISIBLE );
                   holder.Artitst.setText( util.resizeString( item.getArtist() ) );
               }
               holder.Title.setText( util.resizeString( item.getTitle() ) );
               String img = util.getimgUrl( item.getImgurl(), size_big );
//           Picasso.with( context ).load( img ).placeholder( context.getResources().getDrawable( R.drawable.placeholder ) ).into( holder.Img );
               Glide.with( context ).load( img )
                       .error( context.getResources().getDrawable( R.drawable.placeholder ) ).transition( DrawableTransitionOptions.withCrossFade() ).into( holder.Img );
               holder.album.setOnClickListener( v -> onClickListen.onClick( v, position, item ) );
           }else{
               Glide.with( context ).load( item.getImgurl() )
                       .error( context.getResources().getDrawable( R.drawable.placeholder ) )
                       .transition( DrawableTransitionOptions.withCrossFade() ).into( holder.imgPlaylist );
               holder.textPlaylist.setText( item.getTitle() );
               holder.Player.setOnClickListener( v -> onClickListen.onClick( v, position, item ) );
           }

    }

    public List<Album> getData(){
        return albums;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    @Override
    public int getItemViewType(int position) {
        position=position+1;
        return position;
    }
    public void  setOnClickListen(Interfaces.OnClickAlmumListen onClickListen){
        this.onClickListen = onClickListen;
    }
    class Holder extends RecyclerView.ViewHolder {

        TextView Title;
        TextView Artitst;
        ImageView Img;
        ImageView imgPlaylist;
        TextView textPlaylist;
        LinearLayout album;
        RelativeLayout Player;
        public Holder(View itemView) {
            super( itemView );
            imgPlaylist = itemView.findViewById( R.id.img );
            textPlaylist = itemView.findViewById( R.id.text );
            Img = itemView.findViewById( R.id.preview );
            Artitst = itemView.findViewById( R.id.Artist );
            Title = itemView.findViewById( R.id.Title );
            album = itemView.findViewById( R.id.album );
            Player = itemView.findViewById( R.id.player );

        }
    }
}