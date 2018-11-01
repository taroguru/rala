package workshop.soso.jickjicke.ui.mediastore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment;
import workshop.soso.jickjicke.ui.mediastore.folder.MediastoreFragmentByFolder;
import workshop.soso.jickjicke.ui.mediastore.playlist.PlayListFragment;

/**
 * Created by taroguru on 2017. 1. 26..
 */

public class MediaStoreFragment extends Fragment {
    public static final int RESULT_CODE_CLOSE = -1;
    public static final int RESULT_CODE_ADD_ITEM = 1;
    private BroadcastReceiver broadcastReceiver;
    private FragmentAdapter adapter;
    private FragmentManager supportFragmentManager;

    public FragmentManager getSupportFragmentManager() {
        return supportFragmentManager;
    }

    public void setSupportFragmentManager(FragmentManager supportFragmentManager) {
        this.supportFragmentManager = supportFragmentManager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_medialist, container, false);
        //create view
        createViewPager(rootView);
        initLocalBroadcastReceiver();

        return rootView;
    }

    private void initLocalBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(ACTION.NotifyUpdateAddfileViewPager))
                {
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    private ViewPager createViewPager(View rootView) {
        ViewPager viewPager;
        viewPager = (ViewPager) rootView.findViewById(R.id.libraryPager);
        adapter = createViewPagerAdapter();
        viewPager.setAdapter(adapter);
        createTabLayout(rootView, viewPager);

        return viewPager;
    }

    private FragmentAdapter createViewPagerAdapter()
    {
        FragmentAdapter adapter = new FragmentAdapter(supportFragmentManager);
        //firstpage
        adapter.addFragment(MediastoreFragmentByFolder.newInstance(), getString(R.string.folder) );

        //전체
        adapter.addFragment(AudioListFragment.newInstance(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null),
                getString(R.string.AllFile));
        //playlistfragment
        adapter.addFragment(PlayListFragment.newInstance(), getString(R.string.playlist));
        return adapter;
    }

    private void createTabLayout(View rootView, ViewPager viewPager) {
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.libraryTab);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).select();

    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter(ACTION.NotifyUpdateAddfileViewPager));
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    public int getItemPosition(Object object)
    {
        return adapter.getItemPosition(object);
    }

}
