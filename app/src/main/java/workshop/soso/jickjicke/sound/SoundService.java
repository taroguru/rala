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
import android.graphics.Color;
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
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    private static final int NOTIFICATION_PLAYER = 572;
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

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MONITOR_STATE:
                    monitorState();
                    return;
            }
            super.handleMessage(msg);
        }
    }

    private void monitorState() {
        // 구간 반복 모드에서 다시 돌아가기.
        StateManager stateManager = getStateManager(this);
        PlayerState state = stateManager.getPlayerState();

        try {
            if (state == PlayerState.AB_REPEAT &&
                    stateManager.getAbRepeatList().getItemlist().size() != 0) {

                if (onGetPlayerState() == MediaPlayerStateMachine.State.STARTED && onIsNowPlaying()) {
                    if (state == PlayerState.AB_REPEAT) {
                        int currentposition = getCurrentPosition();
                        // repeat모드에서
                        ABRepeat currentABRepeat = stateManager.getCurrentABRepeat();

                        if (currentABRepeat != null) {
//                            DLog.v(LOG_TAG, String.format(
//                                    "ABRepeatMode[%d:%d], current[%d]", currentABRepeat.getStart(),
//                                    currentABRepeat.getEnd(), getCurrentPosition()));
                            int endPosition = currentABRepeat.getEnd();

                            if (currentposition >= endPosition) {
                                onSeekTo(currentABRepeat.getStart());
                            } else {    //아직 구간의 끝까지 도착하지 않음.
                                ;
                            }
                        }
                    } else {    //abrepeat 모드가 아님
                        ;
                    }
                }
            }
        } catch (IllegalStateException e) {
            DLog.v("MediaPlayer is not playing");
            e.printStackTrace();
        } catch (NullPointerException e){
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
    public void onStopPlay() {
        stop();
    }

    @Override
    public void onPlayABRepeat(int position) {
        playABRepeat(position);
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
    public void onResumePausedMusic() {
        resume();
    }

    @Override
    public int onGetDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public void onPlayPreviousABRepeat() {
        playPreviousABRepeat();
    }

    @Override
    public void onPlayNextABRepeat() {
        playNextABRepeat();
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
    public void onSeekTo(int position) {
        seekTo(position);
    }

    public class SoundBinder extends Binder {
        public SoundService getService() {
            return SoundService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initMembers();
        initNotification();
        initLocalBroadcastReceiver();
        initListener();
        rescheduleTimerTask();
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
                if (action.equals(ACTION.PlayNewItem)) {
                    notifyPlayItemChanged();
                } else if (action.equals(ACTION.PlayAudio)) {
                    int position = intent.getIntExtra(ACTION.Position, 0);
                    onPlaySoundTrack(position);
                }
                else if (action.equals(ACTION.ChangePlayerState)) {
                    MediaPlayerStateMachine.State state = (MediaPlayerStateMachine.State) intent.getSerializableExtra(ACTION.State);
                    DLog.v("noti", state.toString());

                    if (state == MediaPlayerStateMachine.State.END || state == MediaPlayerStateMachine.State.ERROR ||
                            state == MediaPlayerStateMachine.State.IDLE || state == MediaPlayerStateMachine.State.INITIALIZED ||
                            state == MediaPlayerStateMachine.State.PREPARING || state == MediaPlayerStateMachine.State.PREPARED) {
                        DLog.v("noti", "don't refresh noti");   //요기가 범인.
                    } else if (Utility.isPlayButtonState(state)) {
                        changeNotiPlayButtonImage(R.drawable.ic_play_noti_24dp);
                    } else {
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
                //                else if (action.equals(ACTION.PAUSE)) {
//                    onPausePlayingMusic();
//                }
//                else if(action.equals(ACTION.EXIT))
//                {
//                    //clear notification
//                    mNotificationManager.cancel(NOTIFICATION_PLAYER);
////                    mNotificationManager.cancelAll();
////                    onStopPlay();
//                    //service stop
////                    stopSelf();
//                }

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
//        intentFilter.addAction(ACTION.EXIT);
        //intentFilter.addAction(ACTION.PAUSE);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

    }


    @Override
    public void onDestroy() {
        mNotificationManager.cancel(NOTIFICATION_PLAYER);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

//        stopSelf();
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        DLog.v("onStartCommand : " + action);

        if (action.equals(ACTION.PLAY_PREV_PLAYITEM)) {
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
                    if (stateManager.hasPlayableItem()) {
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
            mNotificationManager.cancel(NOTIFICATION_PLAYER);
            onStopPlay();
            Utility.sendIntentLocalBroadcast(this, ACTION.EXIT);
        } else {
            DLog.v("unknown action : " + action);
        }

        return START_NOT_STICKY;
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
        }
    }

    private void notifyChangeNotification() {
//        Toast.makeText(this, "notification changed", Toast.LENGTH_LONG).show();
        mBuilder.setContent(notificationView);
        mNotificationManager.notify(NOTIFICATION_PLAYER, mBuilder.build());
    }

    RemoteViews notificationView;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;

    private void initNotification() {
        DLog.v("init Notification View and Event");
        //create notification compat

        DLog.v("init Notification View. set drawables to imagebutton");
        notificationView = new RemoteViews(getPackageName(), R.layout.notification_player_normal);
        changeNotiViewImage(R.id.buttonPrevPlayItem, R.drawable.ic_skip_previous_black_24dp, false);
        changeNotiViewImage(R.id.buttonPlay, R.drawable.ic_play_noti_24dp, false);
        changeNotiViewImage(R.id.buttonNextPlayItem, R.drawable.ic_skip_next_black_24dp, false);
//        changeNotiViewImage(R.id.buttonExit, R.drawable.ic_close_black_24dp, false);


        DLog.v("init PendingIntent");
        //button event mapping
        PendingIntent playPrevPlayItem = makePendingIntent(ACTION.PLAY_PREV_PLAYITEM);
//        PendingIntent playPrevRepeat = makePendingIntent(ACTION.PLAY_PREV_REPEAT);
        PendingIntent playPlay = makePendingIntent(ACTION.PLAY);
//        PendingIntent playNextRepeat = makePendingIntent(ACTION.PLAY_NEXT_REPEAT);
        PendingIntent playNextPlayItem = makePendingIntent(ACTION.PLAY_NEXT_PLAYITEM);
//        PendingIntent intentExit = makePendingIntent(ACTION.EXIT);


        notificationView.setOnClickPendingIntent(R.id.buttonPrevPlayItem, playPrevPlayItem);
//        notificationView.setOnClickPendingIntent(R.id.buttonPrevRepeat, playPrevRepeat);
        notificationView.setOnClickPendingIntent(R.id.buttonPlay, playPlay);
//        notificationView.setOnClickPendingIntent(R.id.buttonNextRepeat, playNextRepeat);
        notificationView.setOnClickPendingIntent(R.id.buttonNextPlayItem, playNextPlayItem);
//        notificationView.setOnClickPendingIntent(R.id.buttonExit, intentExit);

        // Creates an explicit intent for an Activity in your app
        PendingIntent pendingIntent = makePendingIntent();

        DLog.v("Build Notification");
        mBuilder =
                new NotificationCompat.Builder(this, CONSTANTS.NOTICHANNELID)
//                        .setSmallIcon(R.drawable.ic_repeat_white_24dp)
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
//            //todo. noti status bar의 아이콘이 vector image인 경우, kitkat 이하의 버전에서 에러 발생
//            //smallicon이 없는 경우, lollipop 이상의 버전에서 에러 발생. 버전 로드 삽입.
            mBuilder.setSmallIcon(R.drawable.ic_repeat_white_24dp);
        }
        // build and show notification
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //noti channel for oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CONSTANTS.NOTICHANNELID,getString(R.string.noti_player_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        mNotificationManager.notify(NOTIFICATION_PLAYER, mBuilder.build());

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

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
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
//                    mediaPlayer.release();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepareAsync();

                    if (stateManager.getLoop() == StateManager.LoopState.LOOP_ONLY_ONE) {
                        mediaPlayer.setLooping(true);
                    }
                    refreshPlayerState(playlistIndex, position);
//                    result = true;  //todo. success를 여기서 판단할 수 있는데, 밖에서는 함수가 다 돌아서 어쩔 수가 없네
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
        DLog.v("Play it!");
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

    public int stop() {
        int result = 1;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
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

    public void seekTo(int afterPosition) {
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
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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
        //재생할 트랙 계산.
        if (currentIndex == StateManager.NO_PLAYABLE_TRACK || nSoundFileCount <= 0) {
            ShortTask.showSnack(this, R.string.play_list_is_empty);
        } else if (currentIndex == 0) {
            //무조건 전체 반복모드로 가즈아!
//            if (isLoop == StateManager.LoopState.LOOP_PLAYLIST)    //마지막 트랙으로 보냄
//            {
            currentIndex = nSoundFileCount - 1;
//            } else {
//                ShortTask.showSnack(this, R.string.this_song_is_the_first_song);
//            }
        } else {
            currentIndex--;
        }

        showTrackInfo(currentIndex, nSoundFileCount, isLoop, originIndex);
        return currentIndex;
    }

    public boolean playNextTrack() {
        int currentIndex = getNextIndex();
        boolean result = false;
        StateManager stateManager = getStateManager(this);
        //다음곡 플레
        if (currentIndex == StateManager.NO_PLAYABLE_TRACK) {
            ShortTask.showSnack(this, R.string.this_song_is_the_last_song);
        } else {
            result = playTrack(stateManager.getCurrentPlayListPosition(), currentIndex);
        }

        return result;
    }

    private int getNextIndex() {
        int currentIndex;
        StateManager stateManager = getStateManager(this);
        currentIndex = stateManager.getCurrentPosition();
        int nSoundFileCount = stateManager.getCurrentPlayList().getItemlist().size();
        StateManager.LoopState isLoop = stateManager.getLoop();

        int originIndex = currentIndex;

        //재생할 트랙 계산.
//        if(isLoop == StateManager.LoopState.LOOP_ONLY_ONE)
//        {
//            //아무것도 안함. 현재 트랙 한번 더 재생
//        }
//        else
        if (currentIndex + 1 >= nSoundFileCount) {
//            if (isLoop == StateManager.LoopState.LOOP_PLAYLIST) {
            currentIndex = 0;   //첫곡으로.
//            } else {
//                currentIndex = StateManager.NO_PLAYABLE_TRACK;
//            }
        } else {
            currentIndex++;
        }

        showTrackInfo(currentIndex, nSoundFileCount, isLoop, originIndex);
        return currentIndex;
    }

    private void showTrackInfo(int currentIndex, int nSoundFileCount,
                               StateManager.LoopState isLoop, int originIndex) {
        DLog.v("SoundService", String.format("track[%d->%d]. TrackSize[%d]. isLoop[%d]",
                originIndex, currentIndex, nSoundFileCount, StateManager.LoopState.toInteger(isLoop)));
    }


    public void playPreviousABRepeat() {
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
        } catch (IllegalStateException e) {
            DLog.e("Cannot get current position in current state." + mediaPlayer.getState());
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public void playABRepeat(int position) {
        try{
            StateManager stateManager = getStateManager(this);
            stateManager.setCurrentABRepeatPosition(position);
            seekTo(stateManager.getCurrentABRepeat().getStart());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void playNextABRepeat() {
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
        } catch (IllegalStateException e) {
            DLog.e("Cannot get current position in current state." + mediaPlayer.getState());
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

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
        Log.v(LOG_TAG, "pause player");
        try {
            mediaPlayer.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        Log.v(LOG_TAG, "resume player");
        try {
            mediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return soundBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return false;
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


//api >= 23
//
//    public boolean setSpeed(float speed)
//    {
//        return mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
//    }

    /*

The MediaPlayer does not provide this feature but SoundPool has this functionality. The SoundPool class has a method called setRate (int streamID, float rate). If you are interested in the API have a look here.

This Snippet will work.

 float playbackSpeed=1.5f;
 SoundPool soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);

 soundId = soundPool.load(Environment.getExternalStorageDirectory()
                         + "/sample.3gp", 1);
 AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 final float volume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

 soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener()
 {
     @Override
     public void onLoadComplete(SoundPool arg0, int arg1, int arg2)
     {
         soundPool.play(soundId, volume, volume, 1, 0, playbackSpeed);
     }
 });

*
* */
}
