package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2017. 2. 9..
 */

public class Album extends ArrayItem {//implements MediaStore.Audio.AlbumColumns {

    private String ALBUM;
    private String ARTIST;
    private int NUMBER_OF_SONGS;
    private String ALBUM_KEY;
    private String ALBUM_ART;

    public String getALBUM() {
        return ALBUM;
    }

    public void setALBUM(String ALBUM) {
        this.ALBUM = ALBUM;
    }

    public String getARTIST() {
        return ARTIST;
    }

    public void setARTIST(String ARTIST) {
        this.ARTIST = ARTIST;
    }

    public int getNUMBER_OF_SONGS() {
        return NUMBER_OF_SONGS;
    }

    public void setNUMBER_OF_SONGS(int NUMBER_OF_SONGS) {
        this.NUMBER_OF_SONGS = NUMBER_OF_SONGS;
    }

    public String getALBUM_KEY() {
        return ALBUM_KEY;
    }

    public void setALBUM_KEY(String ALBUM_KEY) {
        this.ALBUM_KEY = ALBUM_KEY;
    }

    public String getALBUM_ART() {
        return ALBUM_ART;
    }

    public void setALBUM_ART(String ALBUM_ART) {
        this.ALBUM_ART = ALBUM_ART;
    }

    public Album() {

    }

    public void setData(Cursor cursor) {
        DLog.v("Set Album Data from cursor : "+cursor.toString());

        int idx_id = cursor.getColumnIndex(BaseColumns._ID);
        setId(cursor.getInt(idx_id));


        int idx_album = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
        setALBUM(cursor.getString(idx_album));


        int idx_artist = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
        setARTIST(cursor.getString(idx_artist));


        int idx_numofsongs = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);
        setNUMBER_OF_SONGS( cursor.getInt(idx_numofsongs) );

        int idx_album_key = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_KEY);
        setALBUM_KEY( cursor.getString(idx_album_key));


        int idx_album_art = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART);
        setALBUM_ART(cursor.getString(idx_album_art));

    }

    @Override
    public String toString() {
        return "Album{" +
                "ALBUM='" + ALBUM + '\'' +
                ", ARTIST='" + ARTIST + '\'' +
                ", NUMBER_OF_SONGS=" + NUMBER_OF_SONGS +
                ", ALBUM_KEY='" + ALBUM_KEY + '\'' +
                ", ALBUM_ART='" + ALBUM_ART + '\'' +
                '}';
    }
}
