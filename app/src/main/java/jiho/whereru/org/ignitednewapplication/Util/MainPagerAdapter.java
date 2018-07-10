package jiho.whereru.org.ignitednewapplication.Util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import jiho.whereru.org.ignitednewapplication.Pager.Alarm_Fragment;
import jiho.whereru.org.ignitednewapplication.Pager.Map_Fragment;
import jiho.whereru.org.ignitednewapplication.Pager.SOS_Fragment;

public class MainPagerAdapter  extends FragmentStatePagerAdapter {
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Alarm_Fragment();
            case 1:
                return new SOS_Fragment();
            case 2:
                return new Map_Fragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 3;
    }
}
