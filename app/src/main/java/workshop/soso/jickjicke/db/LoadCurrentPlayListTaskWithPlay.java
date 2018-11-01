package workshop.soso.jickjicke.db;

import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017-04-21.
 */

public class LoadCurrentPlayListTaskWithPlay extends LoadCurrentPlayListTask{
    private int playIndex = 0;
    private boolean isSetIndex = false;

    public void setPlayIndex(int index){
        isSetIndex = true;
        playIndex = index;
    }
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        DLog.v("Load Currentplaylist Complete. send localbroadcast " + ACTION.LoadCurrentPlaylistComplete);

        if(isSetIndex)
        {
            Utility.sendBroadcastPlayAudio(getContext(), playIndex);
        }
        else
        {
            Utility.sendBroadcastPlayLastItem(getContext());
        }

        super.onPostExecute(aBoolean);


    }

}
