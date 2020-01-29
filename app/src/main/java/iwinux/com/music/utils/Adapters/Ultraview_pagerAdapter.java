package iwinux.com.music.utils.Adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import iwinux.com.music.Objects.Album;
import iwinux.com.music.fragments.banner_fragment;

public class Ultraview_pagerAdapter extends FragmentStatePagerAdapter {

    List<Album> playlists;

    public Ultraview_pagerAdapter(FragmentManager fm, List<Album> plalists) {
        super(fm);
        this.playlists= plalists;
    }

    @Override
    public banner_fragment getItem(int i) {
        return new banner_fragment(playlists.get( i ));
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Item " + (position + 1);
    }
}