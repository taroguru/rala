package workshop.soso.jickjicke.sound;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by taroguru on 2017. 4. 17..
 */

public class StateSoundPlayer extends MediaPlayer {
    //야매로 동기화.
    private MediaPlayerStateMachine stateMachine;
    public MediaPlayerStateMachine.State getState(){
        if(stateMachine == null)        return MediaPlayerStateMachine.State.ERROR;
        return                          stateMachine.getState();
    }
    private Context context;

    public MediaPlayerStateMachine getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(MediaPlayerStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public StateSoundPlayer() {
        super();
        stateMachine = new MediaPlayerStateMachine();
    }

    @Override
    public void reset() throws IllegalStateException{
        stateMachine.notifyReset();
        super.reset();

    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        stateMachine.notifySetDataSource();
        super.setDataSource(path);

    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        stateMachine.notifyPrepare();
        super.prepare();

    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        stateMachine.notifyPreparedAsync();
        super.prepareAsync();

    }

    @Override
    public void start() throws IllegalStateException, SecurityException {
        stateMachine.notifyStart();
        super.start();

    }

    @Override
    public void stop() throws IllegalStateException {
        stateMachine.notifyStop();
        super.stop();

    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        super.seekTo(msec);
        //stateMachine.notifySeekTo();  //항상 같은 스테이트에 머물기 때문에 이동시킬 필요가 없음. state이동을 시키면 알람창에 계속 noti를 주므로 잠금.
    }

    @Override
    public void pause() throws IllegalStateException {
        stateMachine.notifyPause();
        super.pause();

    }

    @Override
    public void release() throws IllegalStateException{
        stateMachine.notifyRelease();
        super.release();

    }

    public void setContext(SoundService context) {
        this.context = context;
        stateMachine.setContext(context);
    }

    @Override
    public int getCurrentPosition() {
        int position = 0;
        if(getState() == null)   return position;

        try
        {
            if(getState() == MediaPlayerStateMachine.State.STARTED || getState() == MediaPlayerStateMachine.State.PAUSED)
                position = super.getCurrentPosition();
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
        }
        return position;

    }

    @Override
    public int getDuration() {
        int duration = 0;

        try
        {
            duration = super.getDuration();
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
        }

        return duration;
    }
}
