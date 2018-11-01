package workshop.soso.jickjicke.db;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.ui.currentlist.CurrentPlayItemArrayAdapter;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017-04-21.
 */

public class LoadCurrentPlayListTask extends AsyncTask<String, Void, Boolean>{
    private Context context;
    List<MultiItemAdapter.Row<?>> data = new ArrayList<>();
    protected AbstractMediaArrayAdapter adapter;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public AbstractMediaArrayAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(AbstractMediaArrayAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Boolean result = true;

        DBHelper.loadCurrentPlayList(context);
        StateManager stateManager = Utility.getStateManager(context);
        PlayList playlist = stateManager.getCurrentPlayList();
        for(int index = 0; index < playlist.size(); ++index)
        {
            PlayItem item = (PlayItem)playlist.get(index);
            MultiItemAdapter.Row row = MultiItemAdapter.Row.create(index, CurrentPlayItemArrayAdapter.AUDIOTYPE, item);

            data.add(row);
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        //send intent to
        adapter.setmRows(data);
        adapter.notifyDataSetChanged();
    }

}
