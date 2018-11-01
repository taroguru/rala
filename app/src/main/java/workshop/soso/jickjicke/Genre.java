package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2017. 2. 9..
 */

public class Genre extends ArrayItem {//implements MediaStore.Audio.AlbumColumns {

    private String name;    //genre

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Genre() {

    }

    public void setData(Cursor cursor) {
        DLog.v("Set Genre Data from cursor : "+cursor.toString());

        int idx_id = cursor.getColumnIndex(BaseColumns._ID);
        setId(cursor.getInt(idx_id));

        int idx_genre = cursor.getColumnIndex(MediaStore.Audio.GenresColumns.NAME);
        setName(cursor.getString(idx_genre));

    }

    @Override
    public String toString() {
        return "Genre{" +
                "name='" + name + '\'' +
                '}';
    }
}
