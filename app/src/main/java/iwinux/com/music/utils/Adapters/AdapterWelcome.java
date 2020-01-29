package iwinux.com.music.utils.Adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.fragments.welcome_fragment;

public class AdapterWelcome extends FragmentStatePagerAdapter {
    List<String> items = new ArrayList<>(  );
    int[] Res = new int[]{};

    public AdapterWelcome(FragmentManager fm, List<String> banners, int[] Res) {
        super(fm);
        this.Res = Res;
        this.items = banners;
    }

    @Override
    public Fragment getItem(int i) {

        Fragment fragment = new welcome_fragment();

        Bundle args = new Bundle();
        args.putString( "item_text",items.get( i ) );
        if(Res.length != i)
        args.putInt( "res",Res[i] );
        args.putInt("item_position", i);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Item ";
    }
}
