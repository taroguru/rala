package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by taroguru on 2017. 2. 9..
 */

public class Artists extends ArrayItem{


    private String ARTIST;
    private String ARTIST_KEY;
    private int NUMBER_OF_ALBUMS;
    private int NUMBER_OF_TRACKS;

    public String getARTIST() {
        return ARTIST;
    }

    public void setARTIST(String ARTIST) {
        this.ARTIST = ARTIST;
    }

    public String getARTIST_KEY() {
        return ARTIST_KEY;
    }

    public void setARTIST_KEY(String ARTIST_KEY) {
        this.ARTIST_KEY = ARTIST_KEY;
    }

    public int getNUMBER_OF_ALBUMS() {
        return NUMBER_OF_ALBUMS;
    }

    public void setNUMBER_OF_ALBUMS(int NUMBER_OF_ALBUMS) {
        this.NUMBER_OF_ALBUMS = NUMBER_OF_ALBUMS;
    }

    public int getNUMBER_OF_TRACKS() {
        return NUMBER_OF_TRACKS;
    }

    public void setNUMBER_OF_TRACKS(int NUMBER_OF_TRACKS) {
        this.NUMBER_OF_TRACKS = NUMBER_OF_TRACKS;
    }

    public Artists() {

    }

    public void setData(Cursor cursor) {
        int idx_artist = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
        setARTIST(cursor.getString(idx_artist));

        int idx_artist_key = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
        setARTIST_KEY(cursor.getString(idx_artist_key));

        int idx_number_of_album = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
        setNUMBER_OF_ALBUMS(cursor.getInt(idx_number_of_album));

        int idx_number_of_tracks = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
        setNUMBER_OF_TRACKS(cursor.getInt(idx_number_of_tracks));
    }

    @Override
    public String toString() {
        return "Artists{" +
                "ARTIST='" + ARTIST + '\'' +
                ", ARTIST_KEY='" + ARTIST_KEY + '\'' +
                ", NUMBER_OF_ALBUMS=" + NUMBER_OF_ALBUMS +
                ", NUMBER_OF_TRACKS=" + NUMBER_OF_TRACKS +
                '}';
    }
}
