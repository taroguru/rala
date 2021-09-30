package workshop.soso.jickjicke.sound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.PlayerState;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;

import static androidx.core.app.NotificationCompat.CATEGORY_SERVICE;

public class SoundService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, OnPlaySoundListener {
    //const
    public static final int MONITOR_STATE = 4;

    private final String LOG_TAG = "SoundService";
    StateSoundPlayer mediaPlayer = null;
    //Context curContext;
    //StateManager stateManager;
    private final IBinder soundBind = new SoundBinder();
    private BroadcastReceiver broadcastReceiver;

    private Timer refreshTimer;
    private TimerTask refreshStateTask;
    private MessageHandler msgHandler;

    RemoteViews notificationView;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MONITOR_STATE:
                    playbackToRepeatStart();
                    return;
            }
            super.handleMessage(msg);
        }
    }

    //구간 반복 모드에서 시작점(A)로 다시 돌아가기.
    private void playbackToRepeatStart() {

        try {
            StateManager stateManager = getStateManager(this);
            PlayerState state = stateManager.getPlayerState();

            if (state == PlayerState.AB_REPEAT &&
                stateManager.getAbRepeatList().size() != 0 &&
                onGetPlayerState() == MediaPlayerStateMachine.State.STARTED &&
                onIsNowPlaying()) {

                ABRepeat currentABRepeat = stateManager.getCurrentABRepeat();

                if (currentABRepeat != null) {
                    int endPosition = currentABRepeat.getEnd();
                    int currentPosition = getCurrentPosition();

                    if (currentPosition >= endPosition) {
                        onSeekTo(currentABRepeat.getStart());
                    }
                }

            }
        } catch (IllegalStateException e) {
            DLog.v("MediaPlayer is not playing");
            e.printStackTrace();
        } catch (NullPointerException e){
            DLog.v("MediaPlayer has null");
            e.printStackTrace();
        }
    }

    private StateManager getStateManager(Context context) {
        return (StateManager) context.getApplicationContext();
    }

    @Override
    public boolean onPlaySoundTrack(int playOffset) {
        return playTrack(getStateManager(this).getCurrentPlayListPosition(), playOffset);
    }

    @Override
    public boolean onPlaySoundTrack(int playlistIndex, int playOffset) {
        return playTrack(playlistIndex, playOffset);
    }

    @Override
    public void onMoveToABRepeat(int position) {
        playABRepeat(position);
    }

    @Override
    public void onPausePlayingMusic() {
        pause();
    }

    @Override
    public boolean onNextTrack() {
        return playNextTrack();
    }

    @Override
    public boolean onPrevTrack() {
        return playPrevTrack();
    }

    @Override
    public boolean onStopPlay() {
        return stop();
    }

    @Override
    public boolean onPlayABRepeat(int position) {
        return playABRepeat(position);
    }

    @Override
    public int onGetCurrentPosition() {
        return getCurrentPosition();
    }

    @Override
    public boolean onIsNowPlaying() {
        boolean result = false;

        try {
            if (mediaPlayer != null) {
                result = mediaPlayer.isPlaying();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean onResumePausedMusic() {
        return resume();
    }

    @Override
    public int onGetDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public boolean onPlayPreviousABRepeat() {
        return playPreviousABRepeat();
    }

    @Override
    public boolean onPlayNextABRepeat() {
        return playNextABRepeat();
    }

    @Override
    public void onChangeLoopMode(boolean isLoop) {
        try {
            mediaPlayer.setLooping(isLoop);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MediaPlayerStateMachine.State onGetPlayerState() {
        return mediaPlayer.getStateMachine().getState();
    }

    @Override
    public boolean onSeekTo(int position) {
        return seekTo(position);
    }

    public class SoundBinder extends Binder {
        public SoundService getService() {
            return SoundService.this;
        }
    }


    @Override
    public void onCreate() {
        DLog.d(CONSTANTS.LOG_LIFECYCLE, "onCreate");
        super.onCreate();
        initMembers();
        initLocalBroadcastReceiver();
        initListener();
        rescheduleTimerTask();
    }

    private void startForegroundService() {
        DLog.d(CONSTANTS.LOG_LIFECYCLE, "startForegroundService");
        makeNotificationBuilder();
        startForeground(CONSTANTS.NOTIFICATIONID, mBuilder.build());
    }

    //전화가 왔을 때 재생중이면 일시정지
    PhoneStateListener phoneStateListener = null;
    private TelephonyManager telephonyManager = null;

    private void initListener() {
        //전화 올때, 걸때 소리 처리를 위한 telephonymanager 연결.

        if (Utility.hasPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            if (phoneStateListener == null) {
                phoneStateListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                            if (onIsNowPlaying() == true) {
                                onPausePlayingMusic();
                            }
                        }
                        super.onCallStateChanged(state, incomingNumber);
                    }

                };
            }

            if (telephonyManager == null) {
                telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            }
        }
    }

    private void rescheduleTimerTask() {
        refreshStateTask = new TimerTask() {
            @Override
            public void run() {
                msgHandler.sendEmptyMessage(MONITOR_STATE);
            }
        };
        refreshTimer = new Timer();
        refreshTimer.schedule(refreshStateTask, 30, 30);
    }

    private void initLocalBroadcastReceiver() {
        //새 아이템 재생시 notification 갱신을 위해서 여기서 잡아줌
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                DLog.v("SoundService get an Intent : " + action);
                if(action.equals(ACTION.StartSoundService)){
                    //
                }
                else if (action.equals(ACTION.PlayNewItem)) {
                    notifyPlayItemChanged();
                } else if (action.equals(ACTION.PlayAudio)) {
                    int position = intent.getIntExtra(ACTION.Position, 0);
                    onPlaySoundTrack(position);
                }
                else if (action.equals(ACTION.ChangePlayerState)) {
                    MediaPlayerStateMachine.State state = (MediaPlayerStateMachine.State) intent.getSerializableExtra(ACTION.State);
                    DLog.v(CONSTANTS.TAG_NOTIFICATION, state.toString());
                    if(state == MediaPlayerStateMachine.State.END || state == MediaPlayerStateMachine.State.IDLE ){
                        DLog.v(CONSTANTS.TAG_NOTIFICATION, "goodbye notification");  // 종료 시점에 notification을 보냅시다
                        notificationCancel();
                    } else if (state == MediaPlayerStateMachine.State.ERROR ||
                             state == MediaPlayerStateMachine.State.INITIALIZED ||
                            state == MediaPlayerStateMachine.State.PREPARING || state == MediaPlayerStateMachine.State.PREPARED) {
                        DLog.v(CONSTANTS.TAG_NOTIFICATION, "don't refresh noti");   //요기가 범인.
                    } else if (Utility.isPlayButtonState(state)) {
                        DLog.v(CONSTANTS.TAG_NOTIFICATION, "notification icon : PLAY");
                        mBuilder.setOngoing(false);
                        changeNotiPlayButtonImage(R.drawable.ic_play_noti_24dp);
                    } else {
                        DLog.v(CONSTANTS.TAG_NOTIFICATION, "notification icon : PAUSE");
                        mBuilder.setOngoing(true);
                        changeNotiPlayButtonImage(R.drawable.ic_pause_black_24dp);
                    }
                } else if (action.equals(ACTION.OnABRepeatMode)) {
                    StateManager stateManager = getStateManager(getBaseContext());
                    boolean isABRepeat = intent.getBooleanExtra(EXTRA_VALUE.IsABRepeatMode, true);
                    if (isABRepeat) {
                        stateManager.setPlayerState(PlayerState.AB_REPEAT);
                        int abrepeatposition = intent.getIntExtra(EXTRA_VALUE.ABRepeatPosition, 0);
                        onMoveToABRepeat(abrepeatposition);
                    } else {
                        stateManager.setPlayerState(PlayerState.IDLE);
                    }
                } else if (action.equals(ACTION.PLAY_PREV_PLAYITEM)) {
                    onPrevTrack();
                } else if (action.equals(ACTION.PLAY_PREV_REPEAT)) {
                    onPlayPreviousABRepeat();
                } else if (action.equals(ACTION.PLAY)) {
                    if (onIsNowPlaying()) {
                        onPausePlayingMusic();
                    } else {
                        if (onGetPlayerState() == MediaPlayerStateMachine.State.PAUSED) {
                            onResumePausedMusic();
                        } else {    //기타 상태. stop, idle ...
                            StateManager stateManager = getStateManager(context);
                            if (stateManager.hasPlayableItem()) {
                                int playlistPosition = stateManager.getCurrentPlayListPosition();
                                int position = stateManager.getCurrentPosition();
                                onPlaySoundTrack(playlistPosition, position);
                                Utility.sendBroadcastPlayNewItem(context, playlistPosition, position);
                            }
                        }
                    }
                } else if (action.equals(ACTION.PLAY_NEXT_REPEAT)) {
                    onPlayNextABRepeat();
                } else if (action.equals(ACTION.PLAY_NEXT_PLAYITEM)) {
                    onNextTrack();
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(ACTION.PlayNewItem);
        intentFilter.addAction(ACTION.ChangePlayerState);
        intentFilter.addAction(ACTION.PlayAudio);
        intentFilter.addAction(ACTION.OnABRepeatMode);
        intentFilter.addAction(ACTION.PLAY_PREV_PLAYITEM);
        intentFilter.addAction(ACTION.PLAY_PREV_REPEAT);
        intentFilter.addAction(ACTION.PLAY);
        intentFilter.addAction(ACTION.PLAY_NEXT_REPEAT);
        intentFilter.addAction(ACTION.PLAY_NEXT_PLAYITEM);
        intentFilter.addAction(ACTION.StartSoundService);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public void onDestroy() {
        DLog.d(CONSTANTS.LOG_LIFECYCLE, "onDestroy");
        stopSoundService();
        super.onDestroy();
    }
    private void notificationCancel()
    {
        try{
            mNotificationManager.cancel(CONSTANTS.NOTIFICATIONID);
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void stopSoundService()
    {
        try{
            onStopPlay();
            mediaPlayer.release();
            notificationCancel();
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
            stopForeground(true);
            stopSelf();
        }catch(IllegalStateException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            DLog.d("MediaPlayer is already null");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try{
            if(intent == null) {
                DLog.d(CONSTANTS.LOG_LIFECYCLE, "Null intent is received");
                return super.onStartCommand(intent, flags, startId);
            }

            String action = intent.getAction();
            DLog.d(CONSTANTS.LOG_LIFECYCLE, "onStartCommand() : " + action);

            if(action == null || action.isEmpty())
            {
                DLog.v("Empty Action.");
            }
            else if(action.equals(ACTION.StartSoundService)){
                DLog.v("Start Foreground Service");
                startForegroundService();
            }
            else if (action.equals(ACTION.PLAY_PREV_PLAYITEM)) {
                onPrevTrack();
            } else if (action.equals(ACTION.PLAY_PREV_REPEAT)) {
                DLog.v("play prev repeat. from notification");
                onPlayPreviousABRepeat();
            } else if (action.equals(ACTION.PLAY)) {
                if (onIsNowPlaying()) {
                    //1. 동작
                    onPausePlayingMusic();
                    //2. 화면 제어
                    changeNotiPlayButtonImage(R.drawable.ic_play_noti_24dp);
                } else {
                    if (onGetPlayerState() == MediaPlayerStateMachine.State.PAUSED) {
                        onResumePausedMusic();
                        changeNotiPlayButtonImage(R.drawable.ic_pause_black_24dp);
                    } else    //stop state
                    {
                        StateManager stateManager = getStateManager(this);
                        if (stateManager != null && stateManager.hasPlayableItem()) {
                            int playlistPosition = stateManager.getCurrentPlayListPosition();
                            int position = stateManager.getCurrentPosition();
                            onPlaySoundTrack(playlistPosition, position);
                            Utility.sendBroadcastPlayNewItem(this, playlistPosition, position);

                            changeNotiPlayButtonImage(R.drawable.ic_pause_black_24dp);
                        }
                    }
                }
            } else if (action.equals(ACTION.PLAY_NEXT_REPEAT)) {
                onPlayNextABRepeat();
            } else if (action.equals(ACTION.PLAY_NEXT_PLAYITEM)) {
                onNextTrack();
            } else if (action.equals(ACTION.EXIT)) {
                DLog.v(CONSTANTS.LOG_LIFECYCLE, "ACTION.EXIT");
                stopSoundService();
            } else {
                DLog.v("unknown action : " + action);
            }
        }catch(NullPointerException e){

            super.onStartCommand(intent, flags, startId);
        }
        return START_STICKY;
    }

    private void notifyPlayItemChanged() {
        DLog.v("noti", "notifyPlayItemChanged");
        StateManager stateManager = getStateManager(this);
        PlayItem playItem = stateManager.getCurrentPlayItem();
        if (playItem != null) {
            notificationView.setTextViewText(R.id.textTitle, playItem.getName());
            notificationView.setTextViewText(R.id.textArtist, playItem.getArtist());
            Bitmap notiImage = Utility.getAlbumart(this, (long) playItem.getAudio().getAlbumid());
            if (notiImage == null) {
                notiImage = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }
            mBuilder.setLargeIcon(notiImage);
            notifyChangeNotification();
        }
    }

    private void changeNotiPlayButtonImage(int imgResource) {
        changeNotiViewImage(R.id.buttonPlay, imgResource);
    }

    private void changeNotiViewImage(int viewId, int imgResource) {
        DLog.v("noti", "changeNotiViewImage");
        changeNotiViewImage(viewId, imgResource, true);
    }

    @SuppressLint("RestrictedApi")
    private void changeNotiViewImage(int viewId, int imgResource, boolean bSendNoti) {
        DLog.v("noti", "changeNotiViewImage int, int, boolean");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationView.setImageViewResource(viewId, imgResource);
            } else {
                //retricted api이므로 lint에 대한 annotation 추가
                Drawable d = AppCompatDrawableManager.get().getDrawable(this, imgResource);
                Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(),
                        d.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                d.setBounds(0, 0, c.getWidth(), c.getHeight());
                d.draw(c);
                notificationView.setImageViewBitmap(viewId, b);
            }

            if (bSendNoti) {
                notifyChangeNotification();
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void notifyChangeNotification() {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "Notification Notify");
        mBuilder.setContent(notificationView);
        mNotificationManager.notify(CONSTANTS.NOTIFICATIONID, mBuilder.build());
    }

    private void makeNotificationBuilder() {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "init Notification View and Event");
        notificationView = new RemoteViews(getPackageName(), R.layout.notification_player_normal);
        changeNotiViewImage(R.id.buttonPrevPlayItem, R.drawable.ic_skip_previous_black_24dp, false);
        changeNotiViewImage(R.id.buttonPlay, R.drawable.ic_play_noti_24dp, false);
        changeNotiViewImage(R.id.buttonNextPlayItem, R.drawable.ic_skip_next_black_24dp, false);

        DLog.v("init PendingIntent");
        PendingIntent playPrevPlayItem = makePendingIntent(ACTION.PLAY_PREV_PLAYITEM);
        PendingIntent playPlay = makePendingIntent(ACTION.PLAY);
        PendingIntent playNextPlayItem = makePendingIntent(ACTION.PLAY_NEXT_PLAYITEM);

        notificationView.setOnClickPendingIntent(R.id.buttonPrevPlayItem, playPrevPlayItem);
        notificationView.setOnClickPendingIntent(R.id.buttonPlay, playPlay);
        notificationView.setOnClickPendingIntent(R.id.buttonNextPlayItem, playNextPlayItem);

        // Creates an explicit intent for an Activity in your app
        PendingIntent pendingIntent = makePendingIntent();

        DLog.v("Build Notification");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notification channel for oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StateManager stateManager = getStateManager(this);

            if(!stateManager.isCreateNotificationChannel())
            {
                NotificationChannel notificationChannel = new NotificationChannel(CONSTANTS.NOTICHANNELID,getString(R.string.noti_player_name), NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setDescription(getString(R.string.channel_description));
                notificationChannel.enableLights(false);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);

                mNotificationManager.createNotificationChannel(notificationChannel);

                stateManager.setCreateNotificationChannel(true);
            }

        }

        mBuilder =
                new NotificationCompat.Builder(this, CONSTANTS.NOTICHANNELID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("content text")
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setOngoing(false)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setContentIntent(pendingIntent)
                        .setCategory(CATEGORY_SERVICE)
                        .setCustomContentView(notificationView)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            //todo. noti status bar의 아이콘이 vector image인 경우, kitkat 이하의 버전에서 에러 발생
            mBuilder.setSmallIcon(R.drawable.ic_repeat_white_24dp);
        }

    }

    private PendingIntent makePendingIntent(String action) {
        Intent pendingIntent = new Intent(this, SoundService.class);
        pendingIntent.setAction(action);
        return PendingIntent.getService(this, 0, pendingIntent, 0);
    }


    private PendingIntent makePendingIntentExit(String action) {
        Intent pendingIntent = new Intent(this, SoundService.class);
        pendingIntent.setAction(action);
        return PendingIntent.getService(this, (int)
                System.currentTimeMillis(), pendingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private PendingIntent makePendingIntent() {
        PendingIntent resultPendingIntent;
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return resultPendingIntent;
    }

    private void initMediaPlayer() {
        //1. create and set mediaplayer
        DLog.d(LOG_TAG, "initMediaPlayer");
        mediaPlayer = new StateSoundPlayer();
        mediaPlayer.setContext(this);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //2. set listener
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    private void initMembers() {
        //1. initmediaplayer
        initMediaPlayer();

        //2. ab repeat handler
        msgHandler = new MessageHandler();

    }

    public boolean setSpeed(float speed) {
        boolean result = false;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PlaybackParams param = new PlaybackParams();
                param.setSpeed(speed);
                mediaPlayer.setPlaybackParams(param);

                result = true;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }


        return result;
    }


    public boolean prepare(int playlistIndex, int position, String filePath) {
        boolean result = true;
        StateManager stateManager = getStateManager(this);
        new Thread() {

            @Override
            public void run() {


                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepareAsync();

                    if (stateManager.getLoop() == StateManager.LoopState.LOOP_ONLY_ONE) {
                        mediaPlayer.setLooping(true);
                    }
                    refreshPlayerState(playlistIndex, position);
                } catch (NullPointerException e) {
                    DLog.e("SoundService", "MediaPlayer is Null.");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    DLog.e("SoundService", "??");
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    initMediaPlayer();    //여기까지 오면 새로 만들어야된다
                    e.printStackTrace();
                } catch (IOException e) {
                    File failedFile = new File(filePath);
                    ShortTask.showSnack(getBaseContext(), String.format("%s - %s", failedFile.getName(), getString(R.string.file_is_nothing_or_broken)));
                    e.printStackTrace();
                }
            }
        }.start();


        return result;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        DLog.v("onPrepared. Play it!");
        try {
            new Thread() {
                @Override
                public void run() {
                    mediaPlayer.getStateMachine().notifyOnPrepared();
                    mediaPlayer.start();
                }
            }.start();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public boolean start(int playlistIndex, int position, String filePath) {
        boolean result = prepare(playlistIndex, position, filePath);

        return result;
    }

    public boolean stop() {
        boolean result = false;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                result = true;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean isNowPlaying() {
        boolean nowPlaying = false;

        try {
            if (mediaPlayer != null) {
                nowPlaying = mediaPlayer.isPlaying();
            }
        } catch (IllegalStateException e) {
            //mediaplayer의 상태를 알 수 없음
            e.printStackTrace();
        }

        return nowPlaying;
    }

    public boolean seekTo(int afterPosition) {
        boolean result = false;
        try {
            StateManager stateManager = getStateManager(this);
            if (stateManager.getPlayerState() == PlayerState.AB_REPEAT) {
                ABRepeat currentABRepeat = stateManager.getCurrentABRepeat();
                if (afterPosition < currentABRepeat.getStart() || afterPosition > currentABRepeat.getEnd()) {
                    stateManager.setPlayerState(PlayerState.IDLE);
                    Intent onABRepeatMode = new Intent(ACTION.OnABRepeatMode);
                    onABRepeatMode.putExtra(EXTRA_VALUE.IsABRepeatMode, false);
                    Utility.sendIntentLocalBroadcast(this, onABRepeatMode);
                }
            }
            mediaPlayer.seekTo(afterPosition);
            result = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return result;

    }

    public boolean playTrack(int playlistIndex, int position) {
        boolean result = false;
        StateManager stateManager = getStateManager(this);
        PlayList playlist = stateManager.getCurrentPlayList();

        try {
            if (Utility.hasItem(playlist, position)) {
                PlayItem playItem = (PlayItem) playlist.get(position);

                //해당 위치의 파일 재생
                if (playItem != null) {
                    //상태 갱신.
                    DLog.v(String.format("playtrack at [%d:%d], audioid is[%d]", playlistIndex, position, playItem.getAudioid()));
                    result = start(playlistIndex, position, playItem.getFile().getPath());
                } else {
                    ShortTask.showSnack(this, getString(R.string.has_no_playable_item));
                }
            } else {
                ShortTask.showSnack(this, R.string.has_no_playable_item);
                DLog.v(String.format("current playlist is null"));
            }
        } catch (NullPointerException e) {
            ShortTask.showSnack(this, R.string.has_no_playable_item);
            e.printStackTrace();
        }

        return result;
    }

    //track 이동에 따른 상태 갱신
    private void refreshPlayerState(int playlistIndex, int position) {
        Utility.sendBroadcastPlayNewItem(this, playlistIndex, position);
        refreshScreen();
    }

    /**
     * 화면 갱신을 위한 intent 전송
     */
    private void refreshScreen() {
        sendIntentLocalBroadcast(ACTION.RefreshScreen);
    }

    private void sendIntentLocalBroadcast(String intentName) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(intentName);
        broadcaster.sendBroadcast(intent);
    }

    private void sendIntentLocalBroadcast(Intent intent) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        broadcaster.sendBroadcast(intent);
    }

    public boolean playPrevTrack() {
        int currentIndex = getPrevIndex();
        StateManager stateManager = getStateManager(this);
        return playTrack(stateManager.getCurrentPlayListPosition(), currentIndex);
    }

    private int getPrevIndex() {
        int currentIndex;
        StateManager stateManager = getStateManager(this);
        currentIndex = stateManager.getCurrentPosition();
        int nSoundFileCount = stateManager.getCurrentPlayList().getItemlist().size();
        StateManager.LoopState isLoop = stateManager.getLoop();

        int originIndex = currentIndex;
        currentIndex = calculatePrevIndex(currentIndex, nSoundFileCount);

        showTrackInfo(currentIndex, nSoundFileCount, isLoop, originIndex);
        return currentIndex;
    }

    private int calculatePrevIndex(int currentIndex, int nSoundFileCount) {
        StateManager stateManager = getStateManager(this);
        boolean isRandom = stateManager.getRandom();

        if (currentIndex == StateManager.NO_PLAYABLE_TRACK || nSoundFileCount <= 0) {
            ShortTask.showSnack(this, R.string.play_list_is_empty);
        } else if(isRandom){
            currentIndex = calculateRandomIndex(currentIndex, nSoundFileCount);
        }
        else if (currentIndex == 0) {
            currentIndex = nSoundFileCount - 1;
        } else {
            currentIndex--;
        }
        return currentIndex;
    }

    public boolean playNextTrack() {
        int currentIndex = getNextIndex();
        boolean result = false;
        StateManager stateManager = getStateManager(this);

        if (currentIndex == StateManager.NO_PLAYABLE_TRACK) {
            ShortTask.showSnack(this, R.string.this_song_is_the_last_song);
        } else {
            result = playTrack(stateManager.getCurrentPlayListPosition(), currentIndex);
        }

        return result;
    }

    private int getNextIndex() {
        StateManager stateManager = getStateManager(this);
        int playlistLength = stateManager.getCurrentPlayList().getItemlist().size();
        int currentIndex = stateManager.getCurrentPosition();

        StateManager.LoopState isLoop = stateManager.getLoop();

        int originIndex = currentIndex;
        currentIndex = calculateNextIndex(currentIndex, playlistLength);

        showTrackInfo(currentIndex, playlistLength, isLoop, originIndex);
        return currentIndex;
    }

    private int calculateNextIndex(int currentIndex, int playlistLength) {
        StateManager stateManager = getStateManager(this);
        boolean isRandom = stateManager.getRandom();

        if(isRandom){
            currentIndex = calculateRandomIndex(currentIndex, playlistLength);
        }
        else if (currentIndex + 1 >= playlistLength) {
            currentIndex = 0;   //첫곡으로.
        } else {
            currentIndex++;
        }
        return currentIndex;
    }

    private int calculateRandomIndex(int currentIndex, int playlistLength){
        Random random = new Random();
        int candidate = 0;
        do{
            candidate = random.nextInt(playlistLength);
        } while(candidate == currentIndex);
        return candidate;
    }

    private void showTrackInfo(int currentIndex, int nSoundFileCount,
                               StateManager.LoopState isLoop, int originIndex) {
        DLog.v("SoundService", String.format("track[%d->%d]. TrackSize[%d]. isLoop[%d]",
                originIndex, currentIndex, nSoundFileCount, StateManager.LoopState.toInteger(isLoop)));
    }


    public boolean playPreviousABRepeat() {
        boolean result = false;
        try {
            StateManager stateManager = getStateManager(this);
            ArrayList<ABRepeat> abrepeatList = stateManager.getAbRepeatList().getItemlist();
            int size = abrepeatList.size();
            int currentPosition = mediaPlayer.getCurrentPosition();

            int currentIndex = -1;


            if (size > 0) {
                for (ABRepeat abRepeat : abrepeatList) {
                    if (abRepeat.getStart() + 500/*반복구간 재생 후 0.5초 사이면 이전 구간으로 점프*/ >= currentPosition) {
                        break;
                    }
                    ++currentIndex;
                }
                if (currentIndex != -1) {
                    playABRepeat(currentIndex);
                } else {
                    ShortTask.showSnack(this, R.string.no_abrepeat_next_currentposition);
                }
            } else {
                ShortTask.showSnack(this, R.string.abrepeatlist_is_empty);
            }
            result = true;
        } catch (IllegalStateException e) {
            DLog.e("Cannot get current position in current state." + mediaPlayer.getState());
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean playABRepeat(int position) {
        boolean result = false;
        try{
            StateManager stateManager = getStateManager(this);
            stateManager.setCurrentABRepeatPosition(position);
            seekTo(stateManager.getCurrentABRepeat().getStart());
            result = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean playNextABRepeat() {
        boolean result = false;

        try {
            StateManager stateManager = getStateManager(this);
            ArrayList<ABRepeat> abrepeatList = stateManager.getAbRepeatList().getItemlist();
            int size = abrepeatList.size();
            int currentPosition = mediaPlayer.getCurrentPosition();

            int currentIndex = 0;


            if (size > 0) {
                for (ABRepeat abRepeat : abrepeatList) {

                    if (abRepeat.getStart() >= currentPosition) {
                        break;
                    }
                    ++currentIndex;
                }
                if (currentIndex < abrepeatList.size()) {
                    playABRepeat(currentIndex);
                } else {
                    ShortTask.showSnack(this, R.string.no_abrepeat_infrontof_currentposition);
                }
            } else {
                ShortTask.showSnack(this, R.string.abrepeatlist_is_empty);
            }

            result = true;
        } catch (IllegalStateException e) {
            DLog.e("Cannot get current position in current state." + mediaPlayer.getState());
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 현재 플레이시간을 mSec만큼 이동한다.
     *
     * @param mSec 더하거나 뺄 시간
     */
    public void addTo(int mSec) {
        int nextPosition = mediaPlayer.getCurrentPosition() + mSec;
        if (nextPosition <= 0) {
            seekTo(0);
        } else if (nextPosition >= mediaPlayer.getDuration()) {
            playNextTrack();
        } else {
            seekTo(nextPosition);
        }

    }

    public void pause() {
        DLog.v(LOG_TAG, "pause player");
        try {
            mediaPlayer.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public boolean resume() {
        boolean result = false;
        DLog.v(LOG_TAG, "resume player");
        try {
            mediaPlayer.start();
            result = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onBind");
        return soundBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        DLog.v(CONSTANTS.LOG_LIFECYCLE, "onUnbind");

        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //재생이 끝나면 다음트랙 재생
        StateManager stateManager = getStateManager(this);
        DLog.d(LOG_TAG, "OnCompletion : " + stateManager.toString());

        StateManager.LoopState loop = stateManager.getLoop();
        if (loop == StateManager.LoopState.LOOP_ONLY_ONE) {
            onChangeLoopMode(true);
        } else {
            onChangeLoopMode(false);
            if (stateManager.hasPlayableItem()) {
                playNextTrack();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        DLog.e(String.format("Soundservice Error(%d, %d), %s", i, i1, mediaPlayer.toString()));
        return true;
    }

    public int getDuration() {
        int duration = 0;

        try {
            duration = mediaPlayer.getDuration();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return duration;
    }

    public int getCurrentPosition() {
        int position = 0;

        try {
            position = mediaPlayer.getCurrentPosition();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return position;
    }

}
