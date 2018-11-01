package workshop.soso.jickjicke.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2015. 4. 17..
 */

public class SoundDBHelper extends SQLiteOpenHelper {
    public static final String TAG="SoundDBHelper";
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "sound.db";
    private Context mContext;
    public SoundDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public SoundDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    //최초 생성시 호
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SoundContract.DBSoundFile._CREATEQUERY);
        db.execSQL(SoundContract.DBABRepeat._CREATEQUERY);
        db.execSQL(SoundContract.DBPlaylist._CREATEQUERY);
        db.execSQL(SoundContract.DBPlaylistItem._CREATEQUERY);

        //신규  playlist 추가하기. 최초 설치에 대해서.
        insertDefaultPlaylist(db);
    }

    private long insertDefaultPlaylist(SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put(SoundContract.DBPlaylist.COLUMN_NAME, SoundContract.DEFAULT_PLAYLIST);
        long id = db.insert(SoundContract.DBPlaylist.TABLE_NAME, null, values);

        DLog.d(TAG, "insertDefaultPlaylist, dbversion = " + DATABASE_VERSION + ", DefaultPlaylist ID = "+String.valueOf(id));

        return id;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SoundContract.DBPlaylistItem._DELETEQUERY);
        db.execSQL(SoundContract.DBPlaylist._DELETEQUERY);
        db.execSQL(SoundContract.DBSoundFile._DELETEQUERY);
        db.execSQL(SoundContract.DBABRepeat._DELETEQUERY);

        onCreate(db);
    }
}
