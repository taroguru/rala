package workshop.soso.jickjicke.db;

import android.content.Context;
import android.os.AsyncTask;

import workshop.soso.jickjicke.sound.OnPlaySoundListener;
import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2015. 7. 10..
 */
public class LoadAllPlayListTask extends AsyncTask<String, Void, Boolean> {
    public static final String TAG = "LoadAllPliayList";
    //playlist 패칭

    private Context mContext;

    private OnPlaySoundListener playSoundListener;
    private int playlistPosition;
    private int position;

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public LoadAllPlayListTask(Context context) {
        mContext = context;
        playSoundListener = null;
        playlistPosition = -1;
        position = -1;

    }

    public void setOnPlaySoundListener(OnPlaySoundListener listener)
    {
        playSoundListener = listener;
    }

    private boolean isSoundListenerBound(){
        return playSoundListener != null;
    }
    //adapter
    @Override
    protected Boolean doInBackground(String... params) {
        DBHelper.loadAllPlayList(getmContext());
        return new Boolean(true);
    }

    @Override
    protected void onPostExecute(Boolean bSuccess) {
        DLog.i(TAG, "onPostExecute: " + bSuccess);
//        Utility.sendIntentLocalBroadcast(getmContext(), ACTION.RefreshCurrentPlaylist);
//        if(isSoundListenerBound())
//        {
//            playSoundListener.onPlaySoundTrack(playlistPosition, position);
//        }
    }

    public void setPlayListPosition(int playListPosition) {
        this.playlistPosition = playListPosition;
    }

    public void setPlayPosition(int playPosition) {
        this.position = playPosition;
    }
}

