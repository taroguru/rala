package workshop.soso.jickjicke;

import android.os.Bundle;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import workshop.soso.jickjicke.db.LoadFileABRepeatTask;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.sound.MediaPlayerStateMachine;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

//@AcraCore(buildConfigClass = BuildConfig.class)
//@AcraMailSender(mailTo = "ateliersososo@gmail.com")
public class StateManager extends Application implements Serializable {
    public static final String TAG = StateManager.class.toString();

    //constants
    public static final int NO_PLAYLIST = -1;
    public static final int NO_PLAYABLE_TRACK = -1;
    public static final String PREF_LAUNCH_FIRST_TIME = "PREF_LAUNCH_FIRST_TIME";
    public static final String PREF_CURRENT_PLAY_LIST_POSITION = "PREF_CURRENT_PLAY_LIST_POSITION";
    public static final String PREF_CURRENT_POSITION = "PREF_CURRENT_POSITION";
    public static final String PREF_LOOP = "PREF_LOOP";
    public static final String PREF_SPEED = "PREF_SPEED";
    public static final String PREF_RANDOM = "PREF_RANDOM";
    public static final String PREF_CREATE_NOTIFICATION_CHANNEL = "PREF_NOTIFICATION_CHANNEL";

    public static boolean DEBUG;   //현재  debug모드인지, release모드인지 확인.
    public static final int CurrentPlaylistPosition = 0;


    private static FirebaseAnalytics firebaseAnalytics;

    public void sendEventGoogleAnalytics(String title, String message) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, message);
            firebaseAnalytics.logEvent(title, bundle);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private PlayerState playerState;
    private MediaPlayerStateMachine playerStateMachine;

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState state) {
        playerState = state;
    }

//    public MediaPlayerStateMachine getPlayerStateMachine() {
//        return playerStateMachine;
//    }

//    public void setPlayerStateMachine(MediaPlayerStateMachine playerStateMachine) {
//        this.playerStateMachine = playerStateMachine;
//    }

//    public MediaPlayerStateMachine.State getPlayerState() {
//        if(playerStateMachine != null)
//        {
//            return playerStateMachine.getState();
//        }
//        else
//        {
//            return MediaPlayerStateMachine.State.IDLE;
//        }
//    }

    //utility
    private BroadcastReceiver broadcastReceiver;

    public enum LoopState {
        NO_LOOP, LOOP_ONLY_ONE, LOOP_PLAYLIST;

        public static LoopState toEnum(int x) {
            switch (x) {
                case 0:
                    return NO_LOOP;
                case 1:
                    return LOOP_ONLY_ONE;
                case 2:
                    return LOOP_PLAYLIST;
            }
            return null;
        }

        public static int toInteger(LoopState x) {
            switch (x) {
                case NO_LOOP:
                    return 0;
                case LOOP_ONLY_ONE:
                    return 1;
                case LOOP_PLAYLIST:
                    return 2;
            }
            return -1;
        }
    }

    ;

    private ABRepeatList abRepeatList;              //현재 파일의 abRepeatList 전체
//    private ABRepeat currentABRepeat;           //현재 abrepeat 구간

    private File currentPath;            //마지막으로 이용자가 접근한 path
    private int currentPlayListIndex;

    private int currentABRepeatPosition;

    private ArrayList<PlayList> entirePlayList;
    private PlayList currentPlayList;    //지금 플레이하는 플레이리스트. 항상 가지고 있는 바로 당신.

//
//    public boolean isPause() {
//
//        MediaPlayerStateMachine.State state = getPlayerState();
//
//        return (state == MediaPlayerStateMachine.State.PAUSED);
//    }

    public ArrayList<PlayList> getEntirePlayList() {
        return entirePlayList;
    }


    public void setEntirePlayList(ArrayList<PlayList> entirePlayList) {
        this.entirePlayList = entirePlayList;
    }

    public static int getNoPlayableTrack() {
        return NO_PLAYABLE_TRACK;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMembers();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void initMembers() {

        abRepeatList = new ABRepeatList();
        abRepeatList.setName(getString(R.string.MenuTitle_RepeatList));

        currentPath = null;
//        fileList = new ArrayList<PlayItem>();

        File externalStoragePath = Environment.getExternalStorageDirectory();
        setCurrentPath(externalStoragePath);

        entirePlayList = new ArrayList<PlayList>();
        currentPlayList = new PlayList();

        initLocalBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION.PlayNewItem));

        DEBUG = isDebuggable(this);
    }

    private boolean isDebuggable(StateManager context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
        }

        return debuggable;
    }

    @Override
    public void onTerminate() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onTerminate();
    }

    private void initLocalBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ACTION.PlayNewItem)) {
                    //새곡 플레이시 state 정리
                    int playlistindex = intent.getIntExtra(ACTION.PlaylistPosition, 0);
                    int position = intent.getIntExtra(ACTION.Position, 0);
                    //같은 곡을 재생할 경우 갱신 필요 없음.
                    //todo. 최초 실행 감지를 위해 일단 넣어둠 나중에는 할 필요가 없긴 한데...
                    //todo. mediaplyer statediagram을 구현하자.
//                    if(position != getCurrentPosition() || playlistindex != getCurrentPlayListPosition())
//                    {
                    //abrepeat 관련 state 정리
                    abRepeatList.getItemlist().clear();
                    setCurrentABRepeatPosition(0);
                    //새로운 상태 설정
                    setPlayingItem(playlistindex, position);
                    loadABRepeatListFromDB();
//
//                    }
                }
            }
        };
    }

    /**
     * @param playlistindex 플레이할 플레이리스트
     * @param position      플레이 아이템 위치
     * @return 해당 아이템이 플레이 가능한지 반환.
     */
    private boolean setPlayingItem(int playlistindex, int position) {
        DLog.v(TAG, String.format("setPlayingItem[%d:%d]", playlistindex, position));
        boolean isPlayableItem = false;
        PlayList prevPlayList = getCurrentPlayList();
        PlayItem prevPlayItem = getCurrentPlayItem();

        //새로운 인덱스 설정.
        setCurrentPlayListPosition(playlistindex);
        setCurrentPosition(position);
        //현재 재생 중인 item check 변경
        if (prevPlayItem != null) {
            prevPlayItem.setChecked(false);
        }
        if (prevPlayList != null) {
            prevPlayList.setChecked(false);
        }

        if (getCurrentPlayItem() != null) {
            isPlayableItem = true;
            getCurrentPlayList().setChecked(true);
            getCurrentPlayItem().setChecked(true);
        }


        return isPlayableItem;
    }

    private void loadABRepeatListFromDB() {
        LoadFileABRepeatTask task = new LoadFileABRepeatTask(this, this);
        task.execute();
    }

    public LoopState getLoop() {
        return LoopState.toEnum(getIntSharedPreference(PREF_LOOP, LoopState.toInteger(LoopState.NO_LOOP)));
    }

    public void setLoop(LoopState isLoop) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_LOOP, LoopState.toInteger(isLoop));
        editor.commit();
    }


    public boolean getRandom() {
        return getBooleanSharedPreference(PREF_RANDOM, false) ;
    }

    public void setRandom(boolean isShuffle) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_RANDOM, isShuffle);
        editor.commit();
    }

    public boolean isCreateNotificationChannel(){
        //get sharedpreference
        SharedPreferences sp =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sp.getBoolean(PREF_CREATE_NOTIFICATION_CHANNEL,false);
    }

    public void setCreateNotificationChannel(boolean isSet){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_CREATE_NOTIFICATION_CHANNEL, isSet);
        editor.commit();
    }

    public float getSpeed() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sp.getFloat(PREF_SPEED, 1.0f);    //최초 실행시 값이 없을 때 play할 아이템이 없다고 하자.
    }

    public void setSpeed(float speed) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(PREF_SPEED, speed);
        editor.commit();

    }

    public ABRepeatList getAbRepeatList() {
        return abRepeatList;
    }

    public void setAbRepeatList(ABRepeatList abRepeatList) {
        this.abRepeatList = abRepeatList;
    }

    public ABRepeat getCurrentABRepeat() {
        try {
            ArrayList<ABRepeat> abRepeatList = getAbRepeatList().getItemlist();
            if (abRepeatList != null && abRepeatList.size() >= currentABRepeatPosition) {
                return abRepeatList.get(currentABRepeatPosition);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }


    public ABRepeat getCurrentABRepeat(int position) {
        ABRepeat currentABRepeat = null;

        ArrayList<ABRepeat> abRepeatList = getAbRepeatList().getItemlist();
        if (abRepeatList != null && abRepeatList.size() >= 1) {
            for (int i = 0; i < abRepeatList.size(); ++i) {
                ABRepeat abRepeat = abRepeatList.get(i);
                if (abRepeat.getStart() <= position && position <= abRepeat.getEnd() + 100)    //0.1초의 마진을 줍시다.
                {
                    currentABRepeat = abRepeat;
                    currentABRepeatPosition = i;
                }
            }
        }
        return currentABRepeat;
    }

    public long getCurrentPlayListID() {
        return getCurrentPlayList().getId();
    }

    public PlayList getCurrentPlayList() {
        return currentPlayList;
    }

    public void setCurrentPlaylist(PlayList playlist) {
        currentPlayList = playlist;
    }

    public void setCurrentPlayListPosition(int position) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_CURRENT_PLAY_LIST_POSITION, position);
        editor.commit();
    }

    public int getCurrentPlayListPosition() {
        return CurrentPlaylistPosition;
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//        return sp.getInt(PREF_CURRENT_PLAY_LIST_POSITION, NO_PLAYLIST);   //todo. 0가 아니고 default 트랙이 추가되어야 할 듯.
    }

    public boolean hasPlayableItem() {
        boolean hasItem = true;

        if ((getCurrentPlayList() == null || getCurrentPlayList().size() < 1)) {
            hasItem = false;
        }

        return hasItem;
    }


    private boolean getBooleanSharedPreference(String key, boolean defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sp.getBoolean(key, defaultValue);    //최초 실행시 값이 없을 때 play할 아이템이 없다고 하자.
    }

    private int getIntSharedPreference(String key, int defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sp.getInt(key, defaultValue);    //최초 실행시 값이 없을 때 play할 아이템이 없다고 하자.
    }

    private int getIntSharedPreference(String key) {
        return getIntSharedPreference(key, 0);
    }

    public int getCurrentPosition() {
        return getIntSharedPreference(PREF_CURRENT_POSITION, NO_PLAYABLE_TRACK);
    }

    public void setCurrentPosition(int position) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_CURRENT_POSITION, position);
        editor.commit();
    }

    public File getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(File externalStoragePath) {
        this.currentPath = externalStoragePath;
    }

    public int addABRepeatItem(ABRepeat abRepeat) {
        int position = abRepeatList.addItem(abRepeat);
        setCurrentABRepeatPosition(position);

        return position;
    }

    public int setCurrentABRepeatPosition(int position) {
        return currentABRepeatPosition = position;
    }

    public int getCurrentABRepeatPosition() {
        return currentABRepeatPosition;
    }
//
//    public void setCurrentA(int currentA) {
//        currentABRepeat.setStart(currentA);
//    }
//
//    public void setCurrentB(int currentB) {
//        currentABRepeat.setEnd(currentB);
//    }

    /**
     * 선택된 playitem을 반환한다.
     *
     * @return 현재 선택된 playitem. 없을 경우 null
     */
    public PlayItem getCurrentPlayItem() {
        PlayItem playitem = null;
        int currentPlaylistPosition = getCurrentPlayListPosition();
        int currentPosition = getCurrentPosition();
        if (currentPosition < 0 || currentPlaylistPosition < 0) {
            DLog.v(TAG, "playitem is not selected.");
        } else {
            PlayList playlist = getCurrentPlayList();
            if (playlist != null) {
                DLog.v(TAG, String.format("getCurrentPlayItem: %s(%d)[%d/%d]", playlist.getName(), currentPlaylistPosition, currentPosition, playlist.getItemlist().size() - 1));
                playitem = getPlayItem(currentPlaylistPosition, currentPosition);
            }
        }
        return playitem;
    }

    public boolean hasPlaylist(String playlistname) {
        boolean result = false;

        ArrayList<PlayList> playlists = getEntirePlayList();

        for (PlayList playlist : playlists) {
            if (playlist.getName().equals(playlistname)) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        //모든 멤버를 출력하자.
        String buffer;
        try{
            buffer = String.format(TAG + " ISLOOP : " + String.valueOf(LoopState.toInteger(getLoop()))
                + ", ABRepeatList : " + abRepeatList.toString()
                + ", currentPath : " + currentPath.toString()
                + ", currentPlayListIndex : " + String.valueOf(getCurrentPlayListPosition())
                + ", currentPosition : " + String.valueOf(getCurrentPosition())
                + ", currentABRepeatPosition : " + String.valueOf(currentABRepeatPosition)
                + ", fileid : " + String.valueOf((getCurrentPlayItem() != null) ? getCurrentPlayItem().getAudio().getId() : "null")
                + ", filename : " + String.valueOf((getCurrentPlayItem() != null) ? getCurrentPlayItem().getAudio().getName() : "null")
                + ", entireplaylist size : " + String.valueOf((entirePlayList != null) ? entirePlayList.size() : "null")
        );
        }catch(NullPointerException e){
            e.printStackTrace();
            buffer = "null";
        }

        return buffer;
    }

    public boolean hasOnePlaylist() {
        int playlistSize = getEntirePlayList().size();
        return playlistSize == 1;
    }

    public PlayItem getPlayItem(int playistPosition, int position) {
        PlayItem playItem = null;
        PlayList playlist = null;

        if (playistPosition == CurrentPlaylistPosition) {
            playlist = currentPlayList;
        } else if (entirePlayList.size() > playistPosition) {
            playlist = entirePlayList.get(playistPosition);
        }

        if (playlist != null) {
            if (playlist.size() > position) {
                playItem = (PlayItem) playlist.getItemlist().get(position);
            }
        }

        return playItem;
    }

    public void launchAppFirstTime() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (sp.getBoolean(StateManager.PREF_LAUNCH_FIRST_TIME, true)) {
            setCurrentPlayListPosition(0);

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(PREF_LAUNCH_FIRST_TIME, false);
            editor.commit();

//            if (getCurrentPlayListPosition() == StateManager.NO_PLAYLIST)
//            {
//                if(entirePlayList != null && entirePlayList.size()>0)
//                {
//                    PlayList playlist = getEntirePlayList().get(0);    //@ref sounddbprovider에서 최초 생성시 default_playlist를 추가해준다. 그러므로 하나는 있어야 됨 ㅡ_ㅜ
//                    if(playlist != null)
//                    {
//                        setCurrentPlayListPosition(0);
//
//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putBoolean(PREF_LAUNCH_FIRST_TIME, false);
//                        editor.commit();
//                    }
//                }
//            }
        }
    }

    public void alignmentIndexWhenRemovePlaylist(int removePosition) {
        int playlistPosition = getCurrentPlayListPosition();
        int position = getCurrentPosition();
        if (entirePlayList.size() < 1) {
            playlistPosition = NO_PLAYLIST;
            position = NO_PLAYABLE_TRACK;
            Utility.sendBroadcastPlayNewItem(this, playlistPosition, position);
        } else if (playlistPosition == removePosition) {
            //todo. 정책. 재생중인 플레이리스트를 삭제하면 처음 플레이리스트를 현재 플레이리스트로 선택
            playlistPosition = 0;
            if (getCurrentPlayList().size() > 0) {
                position = 0;
            } else {
                position = NO_PLAYABLE_TRACK;
            }
            Utility.sendBroadcastPlayNewItem(this, playlistPosition, position);
        } else {
            //do nothing
        }


    }

    public boolean addPlaylist(PlayList playlist) {
        boolean bSuccessAdd = false;
        if (entirePlayList != null) {
            bSuccessAdd = true;
            entirePlayList.add(playlist);
            if (getCurrentPlayListPosition() == NO_PLAYLIST) {
                setCurrentPlayListPosition(0);
            }
        }

        return bSuccessAdd;
    }

    public PlayList getPlayList(int playlistPosition) {
        PlayList playlist = null;
        if (entirePlayList != null && playlistPosition > -1 && entirePlayList.size() > playlistPosition) {
            playlist = entirePlayList.get(playlistPosition);
        }

        return playlist;
    }

    public PlayList getPlayList(long playlistId) {
        PlayList playlist = null;
        if (entirePlayList != null && entirePlayList.size() > 0) {
            for (PlayList pl : entirePlayList) {
                if (pl.getId() == playlistId) {
                    playlist = pl;
                    break;
                }
            }
        }

        return playlist;
    }


    public void alignmentIndexWhenRemovePlayItem(int playlistPosition, int position) {
        int currentPlaylistPosition = getCurrentPlayListPosition();
        int currentPosition = getCurrentPosition();

        //현재 재생중인 플레이리스트를 지우려고 한다. 유저님, 이러지마로라.
        if (playlistPosition == currentPlaylistPosition &&
                position == currentPosition) {
            //todo. 정책. 재생중인 플레이리스트를 삭제하면 처음 플레이리스트를 현재 플레이리스트로 선택
            if (getCurrentPlayList().size() == position + 1) //마지막이면 하나 전 재생
            {
                //이전 곡 재생
                --position;
            }
            Utility.sendBroadcastPlayNewItem(this, playlistPosition, position);
        } else {
            //do nothing
        }
    }

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//
//        // The following line triggers the initialization of ACRA
//        ACRA.init(this);
//    }
}
