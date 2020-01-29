package iwinux.com.music.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import iwinux.com.music.MainActivity;
import iwinux.com.music.Objects.Album;
import iwinux.com.music.R;

import static iwinux.com.music.MainActivity.frameLayout1;

@SuppressLint("ValidFragment")
public class banner_fragment extends Fragment {
    Album albums;
    View view;

    public banner_fragment(Album albums) {
         this.albums = albums;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(
                R.layout.item_banner, container, false);

        content();
        return view;
    }

    private void content() {
        TextView title = view.findViewById( R.id.text );
        ImageView imageView = view.findViewById( R.id.img );
        Glide.with( view.getContext() ).load( albums.getImgurl() )
                .placeholder( R.drawable.graybg )
                .error( getResources().getDrawable( R.drawable.graybg ) )
                .transition( DrawableTransitionOptions.withCrossFade() ).into( imageView );
        title.setText( albums.getTitle() );
        CardView player = view.findViewById( R.id.player );
        player.setOnClickListener( v-> {
            loadFragment( new MoreInfo_fragment( albums ) );
        });
    }
    private void loadFragment(Fragment fragment) {
        MainActivity.Frame = 2;
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ft.add(frameLayout1.getId(), fragment);
        ft.commit();
    }
}
