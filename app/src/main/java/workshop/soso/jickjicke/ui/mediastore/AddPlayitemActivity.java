package workshop.soso.jickjicke.ui.mediastore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.intent.ACTION;

/**
 * Created by taroguru on 2017. 1. 26..
 */

public class AddPlayitemActivity extends AppCompatActivity{
    public static final int RESULT_CODE_CLOSE = -1;
    public static final int RESULT_CODE_ADD_ITEM = 1;
    private BroadcastReceiver broadcastReceiver;
    private FragmentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addplayitem);
        //create view
        createView();
        initAds();
        initLocalBroadcastReceiver();
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

    private void createToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) { //이건 뭐지 모르는 부분.
            VectorDrawableCompat indicator =
                    VectorDrawableCompat.create(getResources(), R.drawable.ic_arrow_back_black_24dp, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(),R.color.base_black,getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {  //홈메뉴
            //finisi activity
            finishActivity(RESULT_CODE_CLOSE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initAds()
    {
		MobileAds.initialize(this, getString(R.string.low_banner_ad_unit_id));

		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
    }
    /**
     * Activity 내부 createview 생성.
     */
    private void createView() {
        ViewPager viewPager = createViewPager();
        createToolBar();

    }



    private ViewPager createViewPager() {
        ViewPager viewPager;
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = createViewPagerAdapter();
        viewPager.setAdapter(adapter);
        createTabLayout(viewPager);

        return viewPager;
    }

    private FragmentAdapter createViewPagerAdapter()
    {
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        return adapter;
    }

    private void createTabLayout(ViewPager viewPager) {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).select();

    }
    public void finishActivity(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION.NotifyUpdateAddfileViewPager));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
