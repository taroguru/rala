package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.Serializable;

import workshop.soso.jickjicke.db.SoundContract;
import workshop.soso.jickjicke.util.DLog;

public class PlayList extends ItemList implements Serializable{
    public static final String TAG = "PlayList";

//    public String DATA = "_data";
//    public String DATE_ADDED = "date_added";
//    public static final String DATE_MODIFIED = "date_modified";

    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public PlayList() {
        super();
        setChecked(false);
    }

    public void setData(Cursor playListCursor)
    {
        setId(playListCursor);
        setName(playListCursor);
    }

    public void setId(Cursor playListCursor)
    {
        int playlistColIdx = playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
        if(playlistColIdx < 0)
        {
            playlistColIdx = playListCursor.getColumnIndex(SoundContract.DBPlaylistItem.COLUMN_PLAYLIST_ID);
        }
        if(playlistColIdx < 0)
        {
            DLog.d(TAG, "setId: this column has no id filed.");
        }
        else
        {
            setId( playListCursor.getLong(playlistColIdx) );
        }

    }

    public  void setName(Cursor playListCursor)
    {
        int playlistColIdx = playListCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
        if(playlistColIdx < 0)
        {
            DLog.d(TAG, "setName: this column has no name filed.");
        }
        else {
            setName(playListCursor.getString(playlistColIdx));
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean add(PlayItem item)
    {
        boolean result = false;
        if(itemlist != null)
        {
            result = itemlist.add(item);
        }

        return result;
    }

    public int size(){
        int size = 0;
        if(itemlist != null)
            size = itemlist.size();

        return size;
    }

    //
    public boolean hasPlayableTrack() {
        boolean result = false;

        if(itemlist.size() > 0)
        {
            result = true;
        }

        return true;
    }

}
