package jiho.whereru.org.ignitednewapplication.Util;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import jiho.whereru.org.ignitednewapplication.Pager.AlarmTab.MyTab_Fragment;
import jiho.whereru.org.ignitednewapplication.Pager.AlarmTab.YourTab_Fragment;

public class AlarmPagerAdapter extends FragmentStatePagerAdapter {
    public AlarmPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new MyTab_Fragment();
            case 1:
                return new YourTab_Fragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 2;
    }
}