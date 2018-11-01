package workshop.soso.jickjicke.db;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import workshop.soso.jickjicke.ABRepeatList;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2015. 7. 10..
 */
public class LoadFileABRepeatTask extends AsyncTask<String, Void, Boolean> {
    public static final String TAG = "LoadFileABRepeatTask";
    //playlist 패칭

    private Context mContext;
    private StateManager stateManager;

    public LoadFileABRepeatTask(Context context, StateManager stateManager) {
        mContext = context;
        this.stateManager = stateManager;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        ABRepeatList abRepeatList = DBHelper.loadFileABRepeatList(mContext, stateManager.getCurrentPlayItem());
        stateManager.setAbRepeatList(abRepeatList);

        return new Boolean(true);
    }

    @Override
    protected void onPostExecute(Boolean bSuccess) {
        DLog.i(TAG, "onPostExecute: " + bSuccess);
        //abrepeatlist 갱신을 위한.
        Intent intent = new Intent(ACTION.ChangeABRepeatList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
