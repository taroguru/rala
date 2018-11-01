package workshop.soso.jickjicke.db;

import android.content.Context;

import workshop.soso.jickjicke.ui.mediastore.playlist.PlayListFragment;
import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2015. 7. 10..
 */
//초기 생성시 playlist 시점에서 수행되어야 할 task 모음.
public class LoadAllPlayListAtInitTask extends LoadAllPlayListTask {
    public static final String TAG = "LoadAllPliayAtInitList";
    public static final String LOG_TAG = TAG;
    private PlayListFragment playlistFragment;

    public PlayListFragment getPlaylistFragment() {
        return playlistFragment;
    }

    public void setPlaylistFragment(PlayListFragment playlistFragment) {
        this.playlistFragment = playlistFragment;
    }


    public LoadAllPlayListAtInitTask(Context context, PlayListFragment drawer) {
        super(context);
        setPlaylistFragment(drawer);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        DLog.v("init time playlist load");
        DBHelper.loadAllPlayList(getmContext());
        return new Boolean(true);
    }
    @Override
    protected void onPostExecute(Boolean bSuccess) {
        DLog.i(TAG, "onPostExecute: " + bSuccess);
        getPlaylistFragment().onDataSetChanged();
    }

}
