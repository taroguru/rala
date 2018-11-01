package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.io.Serializable;

import workshop.soso.jickjicke.util.DLog;

//playlist member
public class PlayItem extends Audio implements Serializable{

    private Audio audio;
    private int playlistId;
    private int playorder;

    public long getAudioid() {
        return audio.getId();
    }

    public void setAudioid(int audioid) {
        audio.setId(audioid);
    }

    public int getPlayorder() {
        return playorder;
    }

    public void setPlayorder(int playorder) {
        this.playorder = playorder;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public PlayItem() {
        super();
        initialize();
    }

    public PlayItem(Audio audio) {
        this.audio = audio;
        initialize();
    }

    private void initialize() {
        playorder = 0;
    }

    public void setFile(File file)
    {
        audio = new Audio();
        audio.setFile(file);
    }

    public File getFile() {
        return audio.getFile();
    }

    public int getPlayCount() {
        return audio.getPlayCount();
    }

    public void setPlayCount(int playCount) {
        audio.setPlayCount( playCount );
    }

    public Audio getAudio() {
        return audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public long getAudioId(){return audio.getId();}

    public String getArtist()
    {
        try{
            if(audio != null)
            {
                return audio.getArtist();
            }
            else
            {
                return "null";
            }
        }
        catch(NullPointerException e)
        {
            DLog.e(e.toString());
            return "null";
        }
    }

    public String getName() {
        try{
            if(audio != null)
            {
                return audio.getTitle();
            }
            else
            {
                return "null";
            }
        }
        catch(NullPointerException e)
        {
            DLog.e(e.toString());
            return "null";
        }

    }
    public void setName(String name) {  getAudio().setTitle(name); }

    public void setData(Cursor cursor)
    {
        setAudioData(cursor);
        setPlayItemData(cursor);
    }

    public void setAudioData(Cursor cursor)
    {
        Audio audio = new Audio();
//        audio.setAudioFileInfo(cursor);    //자체 파일은 나중에 설정하자
        audio.setData(cursor);
        setAudio(audio);
    }

    public void setPlayItemData(Cursor cursor)
    {
        setId(cursor);

        int orderIdx = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER);
        setPlayorder( cursor.getInt(orderIdx) );

        int playlistIdx = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAYLIST_ID);
        setPlaylistId( cursor.getInt(playlistIdx) );

    }

    public void setId(Cursor cursor)
    {
        int playitemIdx = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID);
        setId( cursor.getLong(playitemIdx) );
    }

    public long getDuration()
    {
        return audio.getDuration();
    }

    @Override
    public String toString() {
        return "PlayItem{" +
                "audio=" + audio +
                ", playlistId=" + playlistId +
                ", playorder=" + playorder +
                '}';
    }
}
