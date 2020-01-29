package iwinux.com.music.utils.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import iwinux.com.music.utils.SmartFragmentStatePagerAdapter;

public class BottomNavAdapter extends SmartFragmentStatePagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();

    public BottomNavAdapter(FragmentManager fragmentManager) {
        super( fragmentManager );
    }

    public void addFragments(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}