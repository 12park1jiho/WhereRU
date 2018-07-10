package jiho.whereru.org.ignitednewapplication.Pager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import jiho.whereru.org.ignitednewapplication.R;
import jiho.whereru.org.ignitednewapplication.Util.AlarmPagerAdapter;

public class Alarm_Fragment extends Fragment {
    AlarmPagerAdapter adapter;
    ViewPager pager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.alarm__fragment,container,false);
        TabLayout tabLayout = (TabLayout)layout.findViewById(R.id.tab_Alarm);
        tabLayout.addTab(tabLayout.newTab().setText("나의알람목록"));
        tabLayout.addTab(tabLayout.newTab().setText("친구알람목록"));


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        pager = (ViewPager)layout.findViewById(R.id.pager_Alarm);
        adapter = new AlarmPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        return layout;
    }
}
