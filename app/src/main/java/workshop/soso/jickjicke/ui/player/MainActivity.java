package workshop.soso.jickjicke.ui.player;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.sound.MediaPlayerStateMachine;
import workshop.soso.jickjicke.sound.OnPlaySoundListener;
import workshop.soso.jickjicke.sound.SoundService;
import workshop.soso.jickjicke.ui.MarqueeToolbar;
import workshop.soso.jickjicke.ui.currentlist.CurrentPlayItemFragment;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.mediastore.MediaStoreFragment;
import workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment;
import workshop.soso.jickjicke.ui.mediastore.folder.MediastoreFragmentByFolder;
import workshop.soso.jickjicke.ui.mediastore.playlist.PlayListFragment;
import workshop.soso.jickjicke.ui.player.drawer.ABRepeatFragment;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.GUIHelper;
import workshop.soso.jickjicke.util.Utility;


public class MainActivity extends AppCompatActivity implements OnPlaySoundListener, SearchView.OnQueryTextListener {

    private static final String KEY_SERVICE_BIND = "KEY_SERVICE_BIND";

    private Adapter adapter;
    private BroadcastReceiver broadcastReceiver;


    private CoordinatorLayout coordinatorLayout;

    private TabLayout tabs;
    private int onStartCount;

    //viewpager
    private ViewPager viewPager;
    //fragments
    private ABRepeatFragment mABRepeatFragment;
    private PlayerFragment playerFragment;
    private CurrentPlayItemFragment mCurrentPlaylistFragment;
    private MediastoreFragmentByFolder folderFragment;
    private AudioListFragment allAudioFragment;
    private PlayListFragment playlistFragment;

    //for bottom player
    private ImageView imgAlbum;
    private TextView txtTime;
    private TextView txtCurrentTime;
    private CardView playButton;

    //accessories : floating button, searchview...
    private FloatingActionButton floatAddButton;
    private BottomAppBar bottomToolBar;
    private CharSequence mTitle;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private RelativeLayout bottomPlayerView;
    protected SearchView searchView;

    //member
    private StateManager stateManager = null;

    //service
    private SoundService soundService = null;
    private Intent playIntent = null;
    private boolean soundServiceBound = false; //?

    public FloatingActionButton getFloatingButton(){
        return floatAddButton;
    }
    private ServiceConnection soundConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SoundService.SoundBinder binder = (SoundService.SoundBinder) service;
            soundService = binder.getService();
            Utility.sendIntentLocalBroadcast(getApplicationContext(), ACTION.RefreshScreen);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            soundService = null;
        }
    };

    private void moveTab(int page) {
        TabLayout.Tab tab = tabs.getTabAt(page);
        if (tab != null) {
            tab.select();  //기본적으로 라이브러리 탭 선player탭을 선택
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onSaveInstanceState()");
        outState.putBoolean(KEY_SERVICE_BIND, isSoundServiceBound());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
        soundServiceBound = savedInstanceState.getBoolean(KEY_SERVICE_BIND);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onCreate()");
        super.onCreate(savedInstanceState);

        initEnterAnimation(savedInstanceState);
        //create view
        createView();
        //member init
        initMember(savedInstanceState);
        initBroadCastReceiver();

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initSoundService(Bundle savedInstanceState) {
        DLog.d("initSoundService");
        if(isSoundServiceBound()){

        } else
        {
            Intent startServiceIntent = new Intent(getApplicationContext(), SoundService.class);
            startServiceIntent.setAction(ACTION.StartSoundService);

            DLog.d("startSoundService");
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startServiceIntent);
            }
            else {
                startService(startServiceIntent);
            }

            bindSoundService();

        }
    }

    private boolean bindSoundService() {
        DLog.d("bindSoundService");
        playIntent = new Intent(MainActivity.this, SoundService.class);

        if( getApplicationContext().bindService(playIntent, soundConnection, Context.BIND_AUTO_CREATE))
            soundServiceBound = true;

        return soundServiceBound;
    }

    private void initScreenState() {
        moveTab(CONSTANTS.PAGE_PLAYER);
    }

    private void createView() {
        setContentView(R.layout.activity_main);
        coordinatorLayout = findViewById(R.id.main_content);
        //메인 스크린의 툴바 설치.
        MarqueeToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomToolBar = findViewById(R.id.bottomPlayerAppBar);

        //viewpager 메인 설정
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(7);

        //탭 생성.
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //네비게이션 드로우어(좌측 메뉴) 생성
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //하단 player 생성
        createViewBottomPlayer();

        mTitle = getTitle();    //어찌하리까? 타이틀을.

        // This method will trigger on item Click of navigation menu
        navigationView.setNavigationItemSelectedListener(menuItem ->
        {

            try {
                DLog.d("DEBUG!", stateManager.toString());

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.menu_go_to_developer:
                        Utility.showDeveloperPage(getApplicationContext());
                        break;
                    case R.id.menu_go_to_google_play:
                        Utility.showMarket(getApplicationContext());
                        break;

                }
                // Closing drawer on item click
                mDrawerLayout.closeDrawers();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return true;
        });

    }

    private void createViewBottomPlayer() {
        bottomPlayerView = findViewById(R.id.bottomPlayerRelativeLayout);
        LayoutInflater.from(this).inflate(R.layout.bottom_player, bottomPlayerView, true);

        playButton = bottomPlayerView.findViewById(R.id.PlayButtonClickArea);
        playButton.setOnClickListener(v -> {
            Utility.sendIntentLocalBroadcast(getApplicationContext(), ACTION.PLAY);
            Utility.sendEventGoogleAnalytics(getBaseContext(), "BottomPlayer", "PlayButton" );
        });

        //txtAlbumArtist = bottomPlayerView.findViewById(R.id.txtAlbum);

        imgAlbum = bottomPlayerView.findViewById(R.id.imgAlbum);
        txtCurrentTime = bottomPlayerView.findViewById(R.id.txtCurrentTime);
        txtTime = bottomPlayerView.findViewById(R.id.txtTime);

    }

    private void initEnterAnimation(Bundle savedInstanceState) {
        onStartCount = 1;
        if (savedInstanceState == null) // 1st time
        {
            this.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        } else // already created so reverse animation
        {
            onStartCount = 2;
        }
    }

    private void showAdvertisementDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout mainView = (RelativeLayout) inflater.inflate(R.layout.dialog_advertisement_body, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.exit_app)).setView(mainView).
                setPositiveButton(R.string.exit, (dialog, which) -> {
                    //종료
                    Utility.sendIntentLocalBroadcast(getParent(), ACTION.EXIT);
                }).setNegativeButton(R.string.go_to_google_play, (dialog, which) -> {
                    //구글 마켓으로
                    Utility.showMarket(getBaseContext());
                    //dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void setupViewPager(ViewPager viewPager) {
        //first page
        mABRepeatFragment = new ABRepeatFragment();
        //second page
        playerFragment = PlayerFragment.newInstance();
        //third page
        mCurrentPlaylistFragment = CurrentPlayItemFragment.newInstance();
        //forth page
        folderFragment = (MediastoreFragmentByFolder) MediastoreFragmentByFolder.newInstance();
        //firth page
        allAudioFragment = AudioListFragment.newInstance(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        //sixth page
        playlistFragment = PlayListFragment.newInstance();

        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(mABRepeatFragment, getString(R.string.TITLE_REPEAT));
        adapter.addFragment(playerFragment, getString(R.string.TITLE_PLAYER));
        adapter.addFragment(mCurrentPlaylistFragment, getString(R.string.TITLE_PLAYLIST));
        adapter.addFragment(folderFragment, getString(R.string.folder));
        adapter.addFragment(allAudioFragment, getString(R.string.AllFile));
        adapter.addFragment(playlistFragment, getString(R.string.playlist));

        // Adding Floating Action Button to bottom right of main view
        floatAddButton = findViewById(R.id.fab);
        adapter.setFloatingButton(floatAddButton);

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                MainActivity.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void onPageSelected(int position) {

        try {
            //floating button
            if(adapter != null)  adapter.changeFloatingButton(position);

            //bottom bar
            if(bottomToolBar != null) {
                if (position == CONSTANTS.PAGE_PLAYER) {
                    bottomToolBar.setVisibility(View.GONE);
                } else {
                    bottomToolBar.setVisibility(View.VISIBLE);
                }
            }

            //searchview
            if(searchView != null){
                changeSearchView(position);
            }

            Tracker tracker = stateManager.getDefaultTracker();
            tracker.setScreenName(CONSTANTS.screenName(position));
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void changeSearchView(int position){
        if ( position == CONSTANTS.PAGE_ABREPEATLIST || position == CONSTANTS.PAGE_PLAYER){
            searchView.setVisibility(View.GONE);
        }else{
            //다른 목록으로 넘어가면 검색 취소
            searchView.setIconified(true);
            searchView.setIconified(true);
            searchView.setVisibility(View.VISIBLE);
        }
    }

    public void searchViewClearFocus(boolean isIconified) {
        try {

            searchView.setIconified(true);
            searchView.setIconified(true);
            if (isIconified) {
                searchView.setIconified(true);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private FloatingActionButton floatingButton = null;

        public void setFloatingButton(FloatingActionButton button) {
            floatingButton = button;
        }

        public void changeFloatingButton(int position) {
            ((OnFloatingButtonStyleChange) getItem(position)).changeButton(floatingButton);
        }

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        //child fragment에 pager가 있을 경우 이벤트를 던지자.
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public MediaPlayerStateMachine.State onGetPlayerState() {
        MediaPlayerStateMachine.State state = null;
        if (isUsableSoundService()) {
            state = soundService.onGetPlayerState();
        }
        return state;
    }

    @Override
    public int onGetDuration() {
        int duration = -1;
        if (isUsableSoundService()) {
            duration = soundService.getDuration();
        }
        return duration;
    }

    @Override
    protected void onResume() {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onResume()");
        super.onResume();
        try{
            stateManager.launchAppFirstTime();
            refreshScreen(stateManager.getCurrentPlayItem());    //현재 화면으로 돌아온 경우 화면 갱신함
            rescheduleTimerTask();
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    private void rescheduleTimerTask() {
        refreshScreenTask = new TimerTask() {
            @Override
            public void run() {
                if (msgHandler != null)
                    msgHandler.sendEmptyMessage(REFRESH_SCREEN);
            }
        };

        refreshTimer = new Timer();
        refreshTimer.schedule(refreshScreenTask, (long) (1000), (long) (1000)); //1초에 한번씩 갱신

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            //ui thread
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private class SoundCommand{

        CommandBody commandBody;

        SoundCommand(CommandBody runCommand){
            commandBody = runCommand;
        }

        public boolean execute() {
            boolean result = false;
            SoundService service = getSoundService();
            if (service != null) {
                result = commandBody.run(service);
            }
            return result;
        }

    }

    @FunctionalInterface
    public interface CommandBody{
        boolean run(SoundService service);
    }

    @FunctionalInterface
    public interface CommandBodyInteger{
        int run(SoundService service);
    }

    @Override
    public boolean onNextTrack() {
        return new SoundCommand((service)->{ return service.onNextTrack(); }).execute();
    }

    @Override
    public boolean onStopPlay() {
        return new SoundCommand((service)->{ return service.onStopPlay(); }).execute();
    }

    @Override
    public boolean onSeekTo(int position) {
        return new SoundCommand((service)->{ return service.onSeekTo(position); }).execute();
    }

    @Override
    public boolean onPlayABRepeat(int position) {
        return new SoundCommand((service)->{ return service.onPlayABRepeat(position); }).execute();
    }

    @Override
    public boolean onResumePausedMusic() {
        return new SoundCommand((service)->{ return service.onResumePausedMusic(); }).execute();
    }

    @Override
    public boolean onPrevTrack() {
        return new SoundCommand((service)->{ return service.onPrevTrack(); }).execute();
    }

    @Override
    public boolean onPlayPreviousABRepeat() {
        return new SoundCommand((service)->{ return service.onPlayPreviousABRepeat(); }).execute();
    }

    @Override
    public boolean onPlayNextABRepeat() {
        return new SoundCommand((service)->{ return service.onPlayNextABRepeat(); }).execute();
    }

    @Override
    public void onChangeLoopMode(boolean isLoop) {
        if (isUsableSoundService()) {
            soundService.onChangeLoopMode(isLoop);
        }
    }

    @Override
    public int onGetCurrentPosition() {
        int position = -1;
//        new SoundCommand((service)->{
//            position = service.getCurrentPosition();
//            return true;
//        }).execute();

        if(isUsableSoundService()){
            position = soundService.onGetCurrentPosition();
        }
        return position;
    }

    @Override
    public boolean onIsNowPlaying() {
        boolean result = false;
        if (isSoundServiceBound()) {
            result = soundService.isNowPlaying();
        }

        return result;
    }

    private boolean isSoundServiceBound() {
        return soundServiceBound;
    }

    private boolean isUsableSoundService(){
        return soundService != null;
    }

    private SoundService getSoundService() {
        if(!isUsableSoundService()) bindSoundService();
        return soundService;
    }

    private void initBroadCastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String action = intent.getAction();
                    DLog.v("MainActivity get an Intent : " + action);
                    if (action.equals(ACTION.RefreshScreen)) {
                        PlayItem playItem = stateManager.getCurrentPlayItem();
                        refreshScreen(playItem);
                    } else if (action.equals(ACTION.EXIT)) {
                        soundService.onStopPlay();
                        unbindSoundService();
                        stopService(new Intent(MainActivity.this, SoundService.class));
                        finishAffinity();
                    } else if (action.equals(ACTION.PlayNewItem)) {
                        PlayItem playItem = stateManager.getCurrentPlayItem();
                        refreshScreen(playItem);
                    } else if (action.equals(ACTION.MainPageMove)) {
                        int pageNumber = intent.getIntExtra(EXTRA_VALUE.PageNumber, CONSTANTS.PAGE_PLAYER);
                        moveTab(pageNumber);
                    } else if (action.equals(ACTION.ShowSnackBar)) {
                        String message = intent.getStringExtra(EXTRA_VALUE.Message);
                        if (!message.isEmpty()) {
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

                            //parameter로 둘까? toast하단을 띄워서 표시할 때 사용하자.
                            if(false) {
                                View snackbarView = snackbar.getView();
                                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();

                                int marginSide = 0;
                                int marginBottom = 0;

                                if (bottomToolBar.getVisibility() != View.GONE) {
                                    marginBottom = bottomToolBar.getHeight();
                                }

                                params.setMargins(
                                        params.leftMargin + marginSide,
                                        params.topMargin,
                                        params.rightMargin + marginSide,
                                        params.bottomMargin + marginBottom
                                );

                                snackbarView.setLayoutParams(params);
                            }
                            snackbar.show();

                        }
                    } else if (action.equals(ACTION.SearchList)) {

                        int pageNumber = intent.getIntExtra(EXTRA_VALUE.PageNumber, CONSTANTS.PAGE_PLAYER);
                        int currentPageNumber = tabs.getSelectedTabPosition();
                        if (pageNumber != currentPageNumber) {
                            moveTab(pageNumber);
                        }

                        if (searchView.isIconified()) {
                            searchViewFocus();
                        } else {
                            searchViewClearFocus(true);
                            //searchView.setIconified(true);
                        }

                    } else if (action.equals(ACTION.ChangePlayerState)) {
                        MediaPlayerStateMachine.State state = (MediaPlayerStateMachine.State) intent.getSerializableExtra(ACTION.State);
                        DLog.v("noti", state.toString());

                        if (state == MediaPlayerStateMachine.State.END || state == MediaPlayerStateMachine.State.ERROR ||
                                state == MediaPlayerStateMachine.State.IDLE || state == MediaPlayerStateMachine.State.INITIALIZED ||
                                state == MediaPlayerStateMachine.State.PREPARING || state == MediaPlayerStateMachine.State.PREPARED) {
                            DLog.v("noti", "don't refresh noti");   //요기가 범인.
                        } else if (Utility.isPlayButtonState(state)) {
                            changeIcon(playButton, R.drawable.ic_play_arrow_white_24dp);

                        } else {
                            changeIcon(playButton, R.drawable.ic_pause_white_24dp);
                        }
                    } else if(action.equals(ACTION.CREATEDPLAYERFREGMENT)){
                        initScreenState();
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    //Constants
    public static final int REFRESH_SCREEN = 2;

    //handler
    private Timer refreshTimer;
    private TimerTask refreshScreenTask;
    private MessageHandler msgHandler;

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_SCREEN:
                    refreshTimeLabel();
                    return;
            }
            super.handleMessage(msg);
        }
    }

    private void refreshScreen(PlayItem playItem) {
        if (playItem != null) {
            setTitle(playItem.getName());
            GUIHelper.setAlbumImage(getApplicationContext(), imgAlbum, playItem.getAudio().getAlbumid() );
            txtTime.setText(Utility.convertMsecToMin((int) playItem.getDuration()));
            refreshTimeLabel();
        }
    }

    public void refreshTimeLabel() {
        try {
            String txtTime = Utility.convertMsecToMin(onGetCurrentPosition());
            txtCurrentTime.setText(txtTime);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void changeIcon(View view, int drawid) {
        ImageView iconView = view.findViewById(R.id.icon);
        iconView.setImageResource(drawid);
        iconView.setTag(R.id.icon, drawid);
    }

    public void searchViewFocus() {
        try {
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void initMember(Bundle savedInstanceState) {
        initSoundService(savedInstanceState);
        if(stateManager == null)
        {
            stateManager = (StateManager) getApplicationContext();
        }
    }

    //최초 실행시점에서 soundservice가 널 일때만 생성.
    @Override
    protected void onStart() {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onStart()");
        super.onStart();

        //initMember(null);

        IntentFilter intentFilter = new IntentFilter(ACTION.RefreshScreen);
        intentFilter.addAction(ACTION.EXIT);
        intentFilter.addAction(ACTION.PlayNewItem);
        intentFilter.addAction(ACTION.MainPageMove);
        intentFilter.addAction(ACTION.SearchList);
        intentFilter.addAction(ACTION.ShowSnackBar);
        intentFilter.addAction(ACTION.PLAY);
        intentFilter.addAction(ACTION.ChangePlayerState);
        intentFilter.addAction(ACTION.CREATEDPLAYERFREGMENT);

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), intentFilter);

        if (onStartCount > 1) {
            this.overridePendingTransition(R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_left);

        } else if (onStartCount == 1) {
            onStartCount++;
        }

        if (msgHandler == null) {
            msgHandler = new MessageHandler();
        }


    }

    @Override
    protected void onStop() {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onStop()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onDestroy()");
        unbindSoundService();
        super.onDestroy();
    }

    private void unbindSoundService(){
        DLog.d(CONSTANTS.LOG_LIFECYCLE, "unbindSoundService");
        if (isSoundServiceBound()) {
            getApplicationContext().unbindService(soundConnection);
            soundServiceBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        try {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setIconifiedByDefault(true);
//            searchView.setIconified(true);
//            searchView.setVisibility(View.GONE);
            changeSearchView(tabs.getSelectedTabPosition());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        return onQueryTextSubmit(query);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Filter currentListFilter = null;

        currentListFilter = getCurrentPageFilter();
        if (currentListFilter != null) {
            currentListFilter.filter(query);
        }

        return true;
    }

    private Filter getCurrentPageFilter() {
        Filterable filterable = null;
        Filter filter = null;
        int currentPage = tabs.getSelectedTabPosition();

        switch (currentPage) {
            case CONSTANTS.PAGE_CURRENT_PLAYLIST:
                filterable = mCurrentPlaylistFragment.getAdapter();
                break;
            case CONSTANTS.PAGE_FOLDER_LIST:
                filterable = folderFragment.getAdapter();
                break;
            case CONSTANTS.PAGE_ALL_AUDIO_LIST:
                filterable = allAudioFragment.getAdapter();
                break;
            case CONSTANTS.PAGE_PLAYLIST:
                filterable = playlistFragment.getAdapter();
                break;
        }

        if (filterable != null) {
            ((AbstractMediaArrayAdapter)(filterable)).initLastPosition();
            filter = filterable.getFilter();
        }

        return filter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {  //홈메뉴
            mDrawerLayout.openDrawer(navigationView);
            return true;
        } else if (id == R.id.menu_exit) {
            //showExitDialog();
            showAdvertisementDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPlaySoundTrack(int playOffset) {
        return soundService.onPlaySoundTrack(playOffset);
    }

    @Override
    public boolean onPlaySoundTrack(int playlistIndex, int playOffset) {
        return soundService.onPlaySoundTrack(playlistIndex, playOffset);
    }

    @Override
    public void onMoveToABRepeat(int position) {
        soundService.onMoveToABRepeat(position);

    }

    @Override
    public void onPausePlayingMusic() {
        soundService.onPausePlayingMusic();
    }

    public void updateABRepeatDrawer() {
        mABRepeatFragment.onDataSetChanged();
    }

    @Override
    public void onBackPressed() {

        try {
            if (!searchView.isIconified()) {
                searchViewClearFocus(true);
            } else {
                showAdvertisementDialog();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog show = builder.setTitle(getString(R.string.exit_app))
                .setMessage(getString(R.string.really_want_to_close_app))
                .setPositiveButton(getString(R.string.exit), (dialog, which) -> Utility.sendIntentLocalBroadcast(getParent(), ACTION.EXIT))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

}
