package workshop.soso.jickjicke.ui.util;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 6. 23..
 */


/**
 * 현재 플레이리스트를
 */
public class CurrentPlaylistReseter implements View.OnClickListener {

    private ArrayList<Audio> audioList = null;
    private PlayList playlist = null;
    private Context context = null;

    public CurrentPlaylistReseter(Context context, PlayList sourcePlaylist) {
        this.context = context;
//        audioList = new ArrayList<>();
        playlist = sourcePlaylist;
//        for (Object item : sourcePlaylist.getItemlist()
//             ) {
//            Audio audio = ((PlayItem)item).getAudio();
//            audioList.add(audio);
//        }
    }

    public CurrentPlaylistReseter(Context context, ArrayList<Audio> sourcePlaylist) {
        this.context = context;
//        audioList = new ArrayList<>();
//        audioList.addAll(sourcePlaylist);
        audioList = sourcePlaylist;
    }

    @Override
    public void onClick(View v) {
        DLog.v("PlaylistReseter is clicked" + v.toString());
        Utility.sendEventGoogleAnalytics(context, "PlaylistReseter", CONSTANTS.EVENT_BUTTON_CLICK);
        if (audioList != null)
            resetCurrentPlaylist(context, audioList);
        else if (playlist != null)
            resetCurrentPlaylist(context, playlist);
    }

    public static void resetCurrentPlaylist(Context context, PlayList playlist) {
        if(playlist.size() != 0)
        {
            String sentence = context.getString(R.string.playlist_is_playing);
            sentence = sentence.replace("#_#_#", playlist.getName() );
            ShortTask.showSnack(context, sentence);

            ArrayList audioList = new ArrayList<>();

            for (Object item : playlist.getItemlist()) {
                Audio audio = ((PlayItem) item).getAudio();
                audioList.add(audio);
            }

            resetCurrentPlaylist(context, audioList);
        } else {
            ShortTask.showSnack(context, context.getString(R.string.has_no_playable_item));
        }
    }

    public static void resetCurrentPlaylist(Context context, ArrayList<Audio> audioList) {
        if (audioList.size() != 0) {
            Utility.getCurrentPlayList(context).clear();

            DBHelper.clearCurrentPlaylist(context);
            DBHelper.insertPlayItemToCurrentPlaylist(context, audioList);

            Utility.sendIntentLocalBroadcast(context, ACTION.RefreshCurrentPlaylist);
        } else {
            ShortTask.showSnack(context, context.getString(R.string.has_no_playable_item));
        }
    }
}
