package workshop.soso.jickjicke.ui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayerState;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.sound.MediaPlayerStateMachine;
import workshop.soso.jickjicke.sound.OnPlaySoundListener;
import workshop.soso.jickjicke.ui.PlayerSeekbar;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.GUIHelper;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;

import static workshop.soso.jickjicke.R.drawable.ic_play_circle_filled_black_48dp;

//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.ActionBarActivity;

public class PlayerFragment extends Fragment implements OnFloatingButtonStyleChange {//} implements OnPlaySoundListener {
    //constancs
    public final static String KEY_ABREPEATFORNEW = "ABREPEATFORNEW";
    public final static long FPS = 30;

    @Override
    public void changeButton(FloatingActionButton floatingButton) {
        try {
            GUIHelper.changeFloatingButtonToSearch( getContext(), floatingButton, CONSTANTS.PAGE_ALL_AUDIO_LIST);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    //enum


    //members
    private OnPlaySoundListener playSoundListener = null;
    private ABRepeat abRepeatForNew = null;
    private BroadcastReceiver broadcastReceiver = null;
    // app states
    private StateManager stateManager = null;


    //Constants
    public static final int REFRESH_SCREEN = 2;

    //UIs
    private PlayerSeekbar playingSeekBarFirst = null;
    private PlayerSeekbar playingSeekBarSecond = null;

    private TextView textCurrentTime;
    private TextView textTotalTime;

    private String LOG_TAG = "PlayFragment";

    private SwitchCompat loopSwitch = null;
    private SwitchCompat randomSwitch = null;
    private SwitchCompat repeatSwitch = null;

    private CardView buttonPlay = null;
    private ImageView playIcon = null;
    private CardView buttonRepeat = null;
    private CardView buttonPrevRepeat = null;
    private CardView buttonnextRepeat = null;
    private CardView buttonPrevious = null;
    private CardView buttonNext = null;
    private CardView buttonStop = null;
//    private CardView buttonAdd = null;

    private Timer refreshTimer;
    private TimerTask refreshScreenTask;
    private MessageHandler msgHandler;


    public void setOnPlaySoundListener(OnPlaySoundListener listener) {
        playSoundListener = listener;
    }

    private static final String BUG_TAG = "BUGBUG";

    /**
     * initialize members
     */

    private void initializeMember() {
        DLog.v(BUG_TAG, "This is initialize");

        if (stateManager == null)
            stateManager = (StateManager) getActivity().getApplicationContext();

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    try{
                        if (action.equals(ACTION.ChangeABRepeatList)) {
                            new Thread() {
                                @Override
                                public void run() {
                                    reloadSeekbarABRepeatList();
                                }
                            }.start();

                        } else if (action.equals(ACTION.PlayNewItem)) {
                            refreshScreenWhenNewItem();
                            //abrepeat state 정리
                            switch (getPlayerState()) {
                                case WAIT_B:
                                    moveToIdleState();
                                    break;
                                case AB_REPEAT:
                                    break;
                                case IDLE:
                                    break;
                            }
                        } else if (action.equals(ACTION.OnABRepeatMode)) {
                            boolean isABRepeatMode = intent.getBooleanExtra(EXTRA_VALUE.IsABRepeatMode, true);
                            if (isABRepeatMode) {
                                repeatSwitch.setChecked(true);
                            } else {
                                repeatSwitch.setChecked(false);
                            }
                        } else if (action.equals(ACTION.ChangePlayerState)) {
//                        MediaPlayerStateMachine.State state = (MediaPlayerStateMachine.State)intent.getSerializableExtra(ACTION.State);
//                        DLog.v("noti", state.toString());
                            refreshButtons();
                        } else if (action.equals(ACTION.RefreshScreen)) {
                            refreshScreen();
                            refreshButtons();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        //멤버 값 재점검.
        bindPlaySoundListener();

    }

    private void reloadSeekbarABRepeatList() {
        try {
            ArrayList abRepeatList = stateManager.getAbRepeatList().getItemlist();
            playingSeekBarFirst.setAbRepeatList(abRepeatList);
            playingSeekBarSecond.setAbRepeatList(abRepeatList);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeMember();
        //다시 시작하면 뭔가 재생중일 수 있으므로 현재 곡 기준으로 화면을 갱신합니다.
        loopSwitch.setChecked(stateManager.getLoop() == StateManager.LoopState.LOOP_ONLY_ONE);
        refreshScreenWhenNewItem();

        rescheduleTimerTask();
        IntentFilter intentFilter = new IntentFilter(ACTION.ChangeABRepeatList);
        intentFilter.addAction(ACTION.PlayNewItem);
        intentFilter.addAction(ACTION.OnABRepeatMode);
        intentFilter.addAction(ACTION.ChangePlayerState);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);
        //최초 프로그램 실행시 player를 켰을 때 화면 갱신을 위한 코드
        if (stateManager.hasPlayableItem()) {
            Utility.sendBroadcastPlayNewItem(getContext(), stateManager.getCurrentPlayListPosition(), stateManager.getCurrentPosition());
        }
//        refreshScreenWhenNewItem();
        //      reloadSeekbarABRepeatList();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        //ui thread
        refreshTimer.cancel();
        refreshTimer.purge();
        refreshTimer = null;
    }

    public static PlayerFragment newInstance() {

        Bundle args = new Bundle();

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeMember();
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_SCREEN:
                    refreshScreen();
                    return;
            }
            super.handleMessage(msg);
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
        refreshTimer.schedule(refreshScreenTask, (long) (1000 / FPS), (long) (1000 / FPS));
    }

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.player_fragment_main, container,
                false);

        View.OnClickListener buttonSoundControlListener = v -> controlSound(v);

        CompoundButton.OnCheckedChangeListener switchListener = (buttonView, isChecked) -> changedSwitch(buttonView, isChecked);

        loopSwitch = rootView.findViewById(R.id.loopsiwtch);
        loopSwitch.setOnCheckedChangeListener(switchListener);
        randomSwitch = rootView.findViewById(R.id.shuffleSiwtch);
        randomSwitch.setOnCheckedChangeListener(switchListener);
        repeatSwitch = rootView.findViewById(R.id.repeatsiwtch);
        repeatSwitch.setOnCheckedChangeListener(switchListener);

//        randomSwitch = (SwitchCompat) rootView.findViewById(R.id.randomsiwtch);
//        randomSwitch.setOnCheckedChangeListener(switchListener);


        buttonPlay = rootView.findViewById(R.id.ButtonPlay);
        buttonPlay.setOnClickListener(buttonSoundControlListener);
        playIcon = buttonPlay.findViewById(R.id.icon);
        playIcon.setTag(R.id.icon, ic_play_circle_filled_black_48dp);
        buttonRepeat = rootView.findViewById(R.id.ButtonRepeatMode);
        buttonRepeat.setOnClickListener(buttonSoundControlListener);

        buttonPrevRepeat = rootView.findViewById(R.id.ButtonPreviousRepeat);
        buttonPrevRepeat.setOnClickListener(buttonSoundControlListener);
        buttonnextRepeat = rootView.findViewById(R.id.ButtonNextRepeat);
        buttonnextRepeat.setOnClickListener(buttonSoundControlListener);

        buttonPrevious = rootView.findViewById(R.id.ButtonPrevious);
        buttonPrevious.setOnClickListener(buttonSoundControlListener);
        buttonNext = rootView.findViewById(R.id.ButtonNext);
        buttonNext.setOnClickListener(buttonSoundControlListener);

        buttonStop = rootView.findViewById(R.id.ButtonStop);
        buttonStop.setOnClickListener(buttonSoundControlListener);
//        buttonAdd = (CardView) rootView.findViewById(R.id.ButtonAdd);
//        buttonAdd.setOnClickListener(buttonSoundControlListner);


        textCurrentTime = rootView.findViewById(R.id.textCurrentMin);
        textTotalTime = rootView.findViewById(R.id.textTotalMin);


        SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int position = seekBar.getProgress();
                if (playSoundListener != null) {
                    PlayerSeekbar pSeekBar = (PlayerSeekbar) seekBar;
                    if (!pSeekBar.isSecondHalf()) {
                        playSoundListener.onSeekTo(position);
                    } else {
                        playSoundListener.onSeekTo(position + seekBar.getMax());  //전체 재생 길이의 절반을 max로 잡음.
                    }
                }
            }
        };

        playingSeekBarFirst = rootView.findViewById(R.id.playingSeekBarFirst);
        playingSeekBarFirst.setOnSeekBarChangeListener(seekBarListener);

        playingSeekBarSecond = rootView.findViewById(R.id.playingSeekBarSecond);
        playingSeekBarSecond.setSecondHalf(true);
        playingSeekBarSecond.setOnSeekBarChangeListener(seekBarListener);

        //화면 갱신 시작
        if (msgHandler == null) {
            msgHandler = new MessageHandler();
        }


        if (savedInstanceState == null) {
            moveToIdleState();
        } else {
            moveToSavedState(savedInstanceState);
        }

        //fragment생성 후에
        Utility.sendIntentLocalBroadcast(getContext(), ACTION.CREATEDPLAYERFREGMENT);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshButtons();

    }


    private void changedSwitch(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.loopsiwtch:
                if (isChecked) {
                    stateManager.setLoop(StateManager.LoopState.LOOP_ONLY_ONE);
                } else {
                    stateManager.setLoop(StateManager.LoopState.NO_LOOP);
                }
                playSoundListener.onChangeLoopMode(isChecked);

                break;
            case R.id.repeatsiwtch:
                if (isChecked) {
                    setPlayerState(PlayerState.AB_REPEAT);
                } else {
                    moveToIdleState();
                }
                break;
            case R.id.shuffleSiwtch:
                stateManager.setRandom(isChecked);
                break;
        }
    }

//    private void showGlobalContextActionBar() {
//        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setTitle(R.string.app_name);
//    }

    private void refreshScreen() {
        refreshProgress();
        refreshTimeLabel();
    }

    private void refreshButtons() {
        if (playSoundListener != null) {
            MediaPlayerStateMachine.State state = playSoundListener.onGetPlayerState();
            ImageView icon = (ImageView) buttonPlay.findViewById(R.id.icon);
            if (icon.getTag(R.id.icon) != null) {
                if (state == null || Utility.isPlayButtonState(state)) {
                    changeToPlayButton(buttonPlay);
                } else {
                    changeToPauseButton(buttonPlay);
                }
            }

        }

    }

    public boolean isABRepeatMode() {
        return PlayerState.AB_REPEAT == getPlayerState();
    }

    public PlayerState getPlayerState() {
        return stateManager.getPlayerState();
    }

    public void setPlayerState(PlayerState playerState) {
        stateManager.setPlayerState(playerState);
    }

    private void moveToWaitB() {
        /*
          INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        STOPPED,
        PAUSED,
        PLAYBACK_COMPLETED,
        ERROR,
        END
         */

        if (playSoundListener.onGetPlayerState() == MediaPlayerStateMachine.State.STARTED ||
                playSoundListener.onGetPlayerState() == MediaPlayerStateMachine.State.PAUSED) {
            //ABRepeat 시작 지점 저장
            abRepeatForNew.setStart(playSoundListener.onGetCurrentPosition());
            playingSeekBarFirst.setCurrentABRepeat(abRepeatForNew);
            playingSeekBarSecond.setCurrentABRepeat(abRepeatForNew);
            // 화면 : 텍스트 버튼 이름 바꾸기
            changeName(buttonRepeat, R.string.set_playback_end);
            setPlayerState(PlayerState.WAIT_B);
        } else {
            ShortTask.showSnack(getContext(), getString(R.string.setstartpointforplaystate));
        }
    }

    /**
     * @return 새로 추가된 abrepeat의 포지션
     */
    private int saveCurrentABRepeat(ABRepeat abRepeat) {
        // ABRepeat 추가
        int position = 0;
        try{

            position = stateManager.addABRepeatItem(abRepeat);
            playingSeekBarFirst.setCurrentABRepeat(null);
            playingSeekBarSecond.setCurrentABRepeat(null);
            ((MainActivity) getActivity()).updateABRepeatDrawer();
//        DLog.d(LOG_TAG, "saveCurrentABRepeat : "+stateManager.toString());
            // db에 insert
            long abrepeatid = DBHelper.insertABRepeatToDB(getActivity(), stateManager.getCurrentPlayItem().getAudio().getId(), abRepeat);
            if (abrepeatid == -1) {
                DLog.d(LOG_TAG, "fail to save abrepeat data to db.");
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return position;
    }

    private void moveToABRepeat() {
        abRepeatForNew.setEnd(playSoundListener.onGetCurrentPosition());// playingSeekBar.getProgress());
        int position = saveCurrentABRepeat(abRepeatForNew);
        //새로운 ABRepeat 재생 시작.
        playSoundListener.onPlayABRepeat(position);

        //logging.
        String currentA_msec = Utility.changeMSecToMSec(stateManager.getCurrentABRepeat().getStart());
        String currentB_msec = Utility.changeMSecToMSec(stateManager.getCurrentABRepeat().getEnd());
        DLog.i(LOG_TAG, String.format(
                "ABRepeatMode : ABRepeat[%s:%s]", currentA_msec,
                currentB_msec));

        // 화면 : 텍스트 버튼 이름 일반 상태로 돌리고 abrepeat 목록 갱신
        changeName(buttonRepeat, R.string.set_playback_start);
        ((MainActivity) getActivity()).updateABRepeatDrawer();

        //상태 변환
        setPlayerState(PlayerState.AB_REPEAT);
        repeatSwitch.setChecked(true);
    }

    // private int fileID; //파일 오픈 지점에서 db에 저장된 파일 아이디를 담음.
    private void changeABRepeatState(View view) {
        try {

            // ABrepeat mode 취소.
//        if (isABRepeatMode()) {
//            moveToIdleState();
//        } else {
            switch (getPlayerState()) {

                case IDLE: // 1. A기억하기
                {
                    moveToWaitB();
                }
                break;

                case WAIT_B: // 2. B기억하기
                {
                    moveToABRepeat();
                }
                break;

                case AB_REPEAT: // 3. 반복상태에서 새로운 반복 추가 불가.
                {
                    repeatSwitch.setChecked(false);
                    moveToWaitB();
//                    Snackbar.make(view, getString(R.string.cannotAddRepeatWhenRepeatMode), Snackbar.LENGTH_LONG).setAction(getString(R.string.off), new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            repeatSwitch.setChecked(false);
//                            moveToIdleState();
//                        }
//                    }).show();
                }
                break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
//        }
    }

    private void changeButtonImage(View view, int iconId) {
        try {
            ImageView image = (ImageView) view.findViewById(R.id.icon);
            image.setImageResource(iconId);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void moveToIdleState() {
        // 화면 : 텍스트 버튼 이름 일반 상태로 돌리기.
        abRepeatForNew = new ABRepeat(0, 0);
        setPlayerState(PlayerState.IDLE);

        if (buttonRepeat != null) changeName(buttonRepeat, R.string.set_playback_start);
    }


    private void moveToSavedState(Bundle savedState) {
        abRepeatForNew = (ABRepeat) savedState.getSerializable(KEY_ABREPEATFORNEW);

        //저장된 상태값에 따라 화면 갱신
        if (getPlayerState() == PlayerState.IDLE) {
            changeName(buttonRepeat, R.string.set_playback_start);
        } else if (getPlayerState() == PlayerState.AB_REPEAT) {
            changeName(buttonRepeat, R.string.set_playback_start);
            repeatSwitch.setChecked(true);
        } else //if(getPlayerState() == PlayerState.WAIT_B)
        {
            changeName(buttonRepeat, R.string.set_playback_end);
        }

    }

    //어디 갔다오면 이 이아기 끊겨있을수도 있겠구나...
    private void bindPlaySoundListener() {
        if (playSoundListener == null) {
            playSoundListener = (OnPlaySoundListener) ((MainActivity) getActivity());
        }
    }


    private void stopSound() {
        bindPlaySoundListener();
        playSoundListener.onStopPlay();
    }

    private void playSound(View playbutton) {
        // 1. play sound file
        try {
            if (stateManager.hasPlayableItem()) {
                //pause시 resume
                if (isPaused()) {
                    playSoundListener.onResumePausedMusic();
                } else {
                    //stop시 play
                    DLog.v(LOG_TAG, "play [" + String.valueOf(stateManager.getCurrentPosition()) + "] track.");
                    int playlistPosition = stateManager.getCurrentPlayListPosition();
                    int position = stateManager.getCurrentPosition();
                    //shared preference가 날아가는 케이스가 생기는걸까? 어쨋든 playableitem이 있는 경우이므로 강제로 첫번째 아이템을 재생하도록 변경하자.
                    if (position == StateManager.NO_PLAYABLE_TRACK) position = 0;
                    playSoundListener.onPlaySoundTrack(playlistPosition, position);
                    Utility.sendBroadcastPlayNewItem(getActivity(), playlistPosition, position);
                }
            } else {
                DLog.v(LOG_TAG, getString(R.string.has_no_playable_item));
                Snackbar.make(playbutton, getString(R.string.has_no_playable_item), Snackbar.LENGTH_LONG).setAction(getString(R.string.move_to_library), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent moveIntent = new Intent(ACTION.MainPageMove);
                        moveIntent.putExtra(EXTRA_VALUE.PageNumber, 3/*folder page*/);
                        Utility.sendIntentLocalBroadcast(getContext(), moveIntent);
                    }
                }).show();
            }
        } catch (NullPointerException e) {
            DLog.e(LOG_TAG, e.toString());
        }

    }

    private boolean isPaused() {
        return playSoundListener.onGetPlayerState() == MediaPlayerStateMachine.State.PAUSED;
    }

    public void refreshScreenWhenNewItem() {
        try {
            if (playSoundListener != null && textTotalTime != null) {
                PlayItem playItem = stateManager.getCurrentPlayItem();
                if (playItem != null) {
                    int duration = (int) playItem.getDuration();
                    playingSeekBarFirst.setMax(duration / 2);
                    playingSeekBarSecond.setMax(duration / 2);
                    refreshTitle(playItem);
                    String totalTimeStr = Utility.convertMsecToMin(duration);
                    textTotalTime.setText(totalTimeStr);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void refreshTitle(PlayItem playItem) {
        try {
            CharSequence title = playItem.getName();
            getActivity().setTitle(title);  //activity가 널일때 이 코드가 올 수 있는가?
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isStarted() {
        boolean result = false;
        try {
            result = playSoundListener.onGetPlayerState() == MediaPlayerStateMachine.State.STARTED;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void refreshTimeLabel() {
//        DLog.v("CurrentState = " + playSoundListener.onGetPlayerState());
        if (playSoundListener != null && textCurrentTime != null){// && isStarted()) {
            String currentTimeStr = Utility.convertMsecToMin(playSoundListener.onGetCurrentPosition());
            textCurrentTime.setText(currentTimeStr);
        }
    }

    public void refreshProgress() {
        if (playSoundListener != null){// && isStarted()) {
            int currentPosition = playSoundListener.onGetCurrentPosition();
            int max = playingSeekBarFirst.getMax();
            if (currentPosition <= max) {
                playingSeekBarFirst.setProgress(currentPosition);
                playingSeekBarSecond.setProgress(0);
            } else {
                playingSeekBarFirst.setProgress(max);
                playingSeekBarSecond.setProgress(currentPosition - max);
            }
        }
    }

    private void changeToPlayButton(View view) {
        int drawid = R.drawable.ic_play_circle_filled_black_48dp;
        int textid = R.string.play;
        changeIconNName(view, drawid, textid);
    }

    private void changeToPauseButton(View view) {
        int drawid = R.drawable.ic_pause_circle_filled_black_48dp;
        int textid = R.string.pause;
        changeIconNName(view, drawid, textid);
    }

    private void changeIconNName(View view, int drawid, int textid) {
        changeIcon(view, drawid);
        changeName(view, textid);
    }

    private void changeName(View view, int textid) {
        TextView textView = (TextView) view.findViewById(R.id.buttonName);
        textView.setText(getString(textid));
        textView.setTag(R.id.buttonName, textid);
    }

    private void changeIcon(View view, int drawid) {
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        iconView.setImageResource(drawid);
        iconView.setTag(R.id.icon, drawid);
    }


    /**
     * 실제 사운드 파일에 대한 재생을 컨트롤 하는 함수. 버튼을 누르면 이 함수를 탐.
     *
     * @param view
     * @return
     */
    public int controlSound(View view) {
        int id = view.getId();
        String buttonName="";
        switch (id) {
            case R.id.ButtonRepeatMode:
                changeABRepeatState(view);
                buttonName = "ButtonRepeatMode";
                break;

            case R.id.ButtonPreviousRepeat:
                if (playSoundListener != null) {
                    playSoundListener.onPlayPreviousABRepeat();
                }
                buttonName = "ButtonPreviousRepeat";
                break;

            case R.id.ButtonPlay:
                if (playSoundListener != null) {
                    if (playSoundListener.onIsNowPlaying()) {
                        //1. 동작
                        playSoundListener.onPausePlayingMusic();
                        //2. 화면 제어
                        changeToPlayButton(view);
                    } else {
                        //1. 동작
                        playSound(view);
                        //2. 화면제어.
                        changeToPauseButton(view);
                    }
                }
                buttonName = "ButtonPlay";
                break;
            case R.id.ButtonNextRepeat:
                if (playSoundListener != null) {
                    playSoundListener.onPlayNextABRepeat();
                }
                buttonName = "ButtonNextRepeat";
                break;

            // 다음 구간 반복 점프
            case R.id.ButtonPrevious:
                if (playSoundListener != null) {
                    playSoundListener.onPrevTrack();
                }
                buttonName = "ButtonPrevious";
                break;
            case R.id.ButtonStop:
                stopSound();
                buttonName = "ButtonStop";
                break;
            case R.id.ButtonNext:
                if (playSoundListener != null) {
                    playSoundListener.onNextTrack();
                }
                buttonName = "ButtonNext";
                break;
        }
        Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_PLAYER), buttonName );
        return 1;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_ABREPEATFORNEW, abRepeatForNew);
    }


}
