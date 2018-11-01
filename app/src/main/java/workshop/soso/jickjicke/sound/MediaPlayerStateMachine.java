package workshop.soso.jickjicke.sound;

import android.content.Context;
import android.content.Intent;

import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 4. 17..
 */
public class MediaPlayerStateMachine {

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public enum State{  IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        STOPPED,
        PAUSED,
        PLAYBACK_COMPLETED,
        ERROR,
        END
    }

    private State state;

    public State getState() {
        return state;
    }

    public State setState(State state) {
        this.state = state;

        Intent intent = new Intent();
        intent.setAction(ACTION.ChangePlayerState);
        intent.putExtra(ACTION.State, state);
        Utility.sendIntentLocalBroadcast(context, intent);

        return state;
    }

    public MediaPlayerStateMachine()
    {
        setState(State.END);
    }

    public State play()
    {
        return setState(State.INITIALIZED);
    }

    public State notifyReset()
    {
        return setState(State.IDLE);
    }

    public State notifyRelease()
    {
        return setState(State.END);
    }

    public State notifyError()
    {
        return setState(State.ERROR);
    }

    public State notifySetDataSource()
    {
        State nextState = getState();
        if(getState() == State.IDLE)
        {
            nextState = setState(State.INITIALIZED);
        }

        return nextState;
    }


    public State notifyPrepare()
    {
        State nextState = getState();
        if(getState() == State.INITIALIZED
                || getState() == State.STOPPED
                )
        {
            nextState = setState(State.PREPARED);
        }

        return nextState;
    }

    public State notifyOnPrepared()
    {
        State nextState = getState();
        if(getState() == State.PREPARING
                )
        {
            nextState = setState(State.PREPARED);
        }

        return nextState;
    }

    //deprecated. 항상 같은 상태로 이동하므로 호출할 필요 무.
    public State notifySeekTo()
    {
        State nextState = getState();

        if(getState() == State.PREPARED)
        {
            nextState = setState(State.PREPARED);
        }
        else if(getState() == State.STARTED)
        {
            nextState = setState(State.STARTED);
        }
        else if(getState() == State.PAUSED)
        {
            nextState = setState(State.PAUSED);
        }
        else if(getState() == State.PLAYBACK_COMPLETED)
        {
            nextState = setState(State.PLAYBACK_COMPLETED);
        }

        return nextState;
    }

    public State notifyStart()
    {
        DLog.v("action:notifystart()" + getState());
        State nextState = getState();

        if(getState() == State.PREPARED
                || getState() == State.STARTED
                || getState() == State.PAUSED)
        {
            nextState = setState(State.STARTED);
        }

        return nextState;
    }

    public State notifyStop()
    {
        State nextState = getState();

        if(getState() == State.STARTED
                || getState() == State.STOPPED
                || getState() == State.PAUSED
                || getState() == State.PREPARED
                || getState() == State.PLAYBACK_COMPLETED)
        {
            nextState = setState(State.STOPPED);
        }

        return nextState;
    }

    public State notifyPause()
    {
        State nextState = getState();

        if(getState() == State.STARTED
                || getState() == State.PAUSED)
        {
            nextState = setState(State.PAUSED);
        }

        return nextState;
    }

    public State notifyPreparedAsync()
    {
        State nextState = getState();

        if(getState() == State.STOPPED
                || getState() == State.INITIALIZED)
        {
            nextState = setState(State.PREPARING);
        }

        return nextState;
    }




}
