package workshop.soso.jickjicke.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import workshop.soso.jickjicke.db.SoundContract.DBABRepeat;
import workshop.soso.jickjicke.db.SoundContract.DBPlaylistItem;
import workshop.soso.jickjicke.db.SoundContract.DBSoundFile;
import workshop.soso.jickjicke.util.DLog;

public class SoundDBProvider extends ContentProvider {
    private final String LOG_TAG = "DBProvider";
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int SOUNDFILE = 100;
    private static final int SOUNDFILE_ID = 101;
    private static final int SOUNDFILE_BY_KEY = 102;
    private static final int PLAYLIST = 300;
    private static final int PLAYLIST_WITHOUT_ITEM = 304;
    private static final int PLAYLIST_BY_NAME = 301;
    private static final int PLAYLISTITEMS_WITH_PLAYLISTNAME = 302;
    private static final int PLAYLIST_BY_ID = 303;
    private static final int PLAYLISTITEM = 400;
    private static final int PLAYLISTITEM_ID = 401;
    private static final int PLAYLISTITEM_ALL = 402;
    private static final int ABREPEAT = 500;
    private static final int ABREPEAT_ID = 501;
    private static final int ABREPEAT_WITH_SOUNDFILEID = 502;

    private static UriMatcher buildUriMatcher() {
//        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SoundContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, SoundContract.PATH_SOUNDFILE, SOUNDFILE);
        matcher.addURI(authority, SoundContract.PATH_SOUNDFILE + "/#", SOUNDFILE_ID);
        matcher.addURI(authority, SoundContract.PATH_SOUNDFILE + "/key/#", SOUNDFILE_BY_KEY);

        matcher.addURI(authority, SoundContract.PATH_PLAYLIST , PLAYLIST);                                      //전체 playlist
        matcher.addURI(authority, SoundContract.PATH_PLAYLIST +"/noitem", PLAYLIST_WITHOUT_ITEM);                 //전체 playlist. 아이템 없이 플레이리스트만.
        matcher.addURI(authority, SoundContract.PATH_PLAYLIST + "/id/#", PLAYLIST_BY_ID);                       //아이디에 해당하는 플레이리스트(아이템포함)
        matcher.addURI(authority, SoundContract.PATH_PLAYLIST + "/name/*", PLAYLIST_BY_NAME);                   //이름에 해당하는 플레이리스트(아이템포함)
        matcher.addURI(authority, SoundContract.PATH_PLAYLIST + "/items/*", PLAYLISTITEMS_WITH_PLAYLISTNAME);   //


        matcher.addURI(authority, SoundContract.PATH_PLAYLISTITEM, PLAYLISTITEM);
        matcher.addURI(authority, SoundContract.PATH_PLAYLISTITEM + "/id/#", PLAYLISTITEM_ID);
        matcher.addURI(authority, SoundContract.PATH_PLAYLISTITEM + "/all", PLAYLISTITEM_ALL);

        matcher.addURI(authority, SoundContract.PATH_ABREPEAT, ABREPEAT);
        matcher.addURI(authority, SoundContract.PATH_ABREPEAT+"/id/#", ABREPEAT_ID);
        matcher.addURI(authority, SoundContract.PATH_ABREPEAT+"/soundfile/#", ABREPEAT_WITH_SOUNDFILEID);

        return matcher;
    }

	private SoundDBHelper dbHelper;

    private static final SQLiteQueryBuilder PlayItemByListIdQueryBuilder;

    /*
     playlistitem  INNER JOIN playlist on playlistitem.playlistid = playlist.id
                    inner join soundfile on soundfile.id = playlistitem.soundfileid
    */
    static{
        PlayItemByListIdQueryBuilder = new SQLiteQueryBuilder();
        PlayItemByListIdQueryBuilder.setTables(
                SoundContract.DBPlaylistItem.TABLE_NAME + " INNER JOIN " +
                        SoundContract.DBPlaylist.TABLE_NAME +
                        " ON " + SoundContract.DBPlaylistItem.TABLE_NAME +
                        "." + SoundContract.DBPlaylistItem.COLUMN_PLAYLIST_ID +
                        " = " + SoundContract.DBPlaylist.TABLE_NAME +
                        "." + SoundContract.DBPlaylist._ID + " INNER JOIN " +
                        SoundContract.DBSoundFile.TABLE_NAME +
                        " ON " + SoundContract.DBSoundFile.TABLE_NAME +
                        "." + DBSoundFile._ID +
                        " = " + SoundContract.DBPlaylistItem.TABLE_NAME +
                        "." + DBPlaylistItem.COLUMN_SOUNDFILE_ID);
    }

//    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
//
//    static{
//        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
//        sWeatherByLocationSettingQueryBuilder.setTables(
//                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
//                        WeatherContract.LocationEntry.TABLE_NAME +
//                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
//                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
//                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
//                        "." + WeatherContract.LocationEntry._ID);
//    }


//    private class DatabaseHelper extends SQLiteOpenHelper {
//		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
//			super(context, name, factory, version);
//			// TODO Auto-generated constructor stub
//		}
//
//		//최초 생성시 호
//		@Override
//		public void onCreate(SQLiteDatabase db) {
//			db.execSQL(DBSoundFile._CREATEQUERY);
//			db.execSQL(DBABRepeat._CREATEQUERY);
//            db.execSQL(SoundContract.DBPlaylist._CREATEQUERY);
//            db.execSQL(SoundContract.DBPlaylistItem._CREATEQUERY);
//		}
//
//		@Override
//		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            db.execSQL(SoundContract.DBPlaylistItem._DELETEQUERY);
//            db.execSQL(SoundContract.DBPlaylist._DELETEQUERY);
//            db.execSQL(DBSoundFile._DELETEQUERY);
//			db.execSQL(DBABRepeat._DELETEQUERY);
//
//			onCreate(db);
//		}
//	}

	/**
	 * @param path
	 * @param key
	 * @return insert된 데이터의 id
	 */
	public long insertSoundFile(String path, String key){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DBSoundFile.COLUMN_PATH, path);
		values.put(DBSoundFile.COLUMN_KEY,  key);

		long newRowId = 0 ;
		newRowId = database.insert(DBSoundFile.TABLE_NAME,  null, values);
		
		return newRowId;
	}


    /**
     * @param fieldName
     * @param fieldValue
     * @return
     */
    @Deprecated
	public Cursor selectSoundFile(String fieldName, String[] fieldValue) {
        SQLiteDatabase database =  dbHelper.getReadableDatabase();
		String[] projection = {
				DBSoundFile._ID,
				DBSoundFile.COLUMN_PATH,
				DBSoundFile.COLUMN_KEY
		};
		
		String sortOrder = DBSoundFile._ID + " DESC";
		
		Cursor cursor = database.query(DBSoundFile.TABLE_NAME, projection, fieldName, fieldValue, null, null, sortOrder);
		
		return cursor;
	}
	
	public int deleteSoundFile(String id){
        SQLiteDatabase database =  dbHelper.getReadableDatabase();
		String selection = DBSoundFile._ID;
		String[] selectionArgs = {id};
		
		return database.delete(DBSoundFile.TABLE_NAME,  selection,  selectionArgs);
	}
	
	
	/**
	 * not yet implemented.
	 */
	public void updateSoundFile()
	{
		
	}
	
	public long insertABRepeat(int startMSec, int endMSec, int fileID){
        SQLiteDatabase database =  dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBABRepeat.COLUMN_START_MSEC, Integer.valueOf(startMSec));
		values.put(DBABRepeat.COLUMN_END_MSEC, Integer.valueOf(endMSec));
		values.put(DBABRepeat.COLUMN_SOUNDFILE_ID, Integer.valueOf(fileID));

		long newRowId = 0 ;
		newRowId = database.insert(DBABRepeat.TABLE_NAME,  null, values);
		
		return newRowId;
	}


    @Override
    public boolean onCreate() {
        dbHelper = new SoundDBHelper(getContext(), SoundDBHelper.DATABASE_NAME, null,   SoundDBHelper.DATABASE_VERSION);
        return false;
    }

/**
 *
 *
 private static final int PLAYLISTITEMS_WITH_PLAYLISTNAME = 302;
 private static final int ABREPEAT = 500;
 private static final int ABREPEAT_ID = 501;

 */
    /**
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database =  dbHelper.getReadableDatabase();
        int matchingResult = sUriMatcher.match(uri);
        DLog.v(LOG_TAG,"Uri Matching Result = " + String.valueOf(matchingResult));
        switch(matchingResult)
        {
            case PLAYLIST:
                return PlayItemByListIdQueryBuilder.query(database,
                        projection, selection, null, null, null, sortOrder);
            case PLAYLIST_BY_NAME:
            {
                String path = uri.getPath();
                String playlistName = path.substring(path.lastIndexOf('/') + 1 );
                DLog.v(LOG_TAG, "path = " + path + ", playlistname = " + playlistName);

                return PlayItemByListIdQueryBuilder.query(database,
                        projection, SoundContract.DBPlaylist.COLUMN_NAME + " = '" + playlistName + "'",
                        null, null, null, sortOrder);
            }
            case PLAYLIST_WITHOUT_ITEM:
            {
                return database.query(SoundContract.DBPlaylist.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case PLAYLIST_BY_ID:
            {
                String path = uri.getPath();
                String playlistid = path.substring(path.lastIndexOf('/') + 1);
                DLog.v(LOG_TAG, "path = " + path + ", playlist id = " + playlistid);
                return PlayItemByListIdQueryBuilder.query(database,
                        projection, SoundContract.DBPlaylist.TABLE_NAME+"."+ SoundContract.DBPlaylist._ID + " = '" + playlistid + "'",
                        null, null, null, sortOrder);
            }
            case PLAYLISTITEMS_WITH_PLAYLISTNAME:
            {
                String path = uri.getPath();
                String playlistName = path.substring(path.lastIndexOf('/') + 1);
                DLog.v(LOG_TAG, "path = " + path + ", playlistname = " + path);
                return PlayItemByListIdQueryBuilder.query(database,
                        projection, SoundContract.DBPlaylist.COLUMN_NAME + " = '" + playlistName + "'",
                        null, null, null, sortOrder);
            }
            case SOUNDFILE:
                DLog.v("SoundFile" + uri.toString());
                return database.query(DBSoundFile.TABLE_NAME, null, null,
                        null, null, null, sortOrder);
            case SOUNDFILE_ID:
                DLog.v("SoundFile_ID" + uri.toString());
                return database.query(DBSoundFile.TABLE_NAME, projection, DBSoundFile._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs, null, null, sortOrder);
            case PLAYLISTITEM:
                DLog.v("PlaylistItem" + uri.toString());
                return database.query(SoundContract.DBPlaylistItem.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case PLAYLISTITEM_ID:
                return database.query(SoundContract.DBPlaylistItem.TABLE_NAME, projection, SoundContract.DBPlaylistItem._ID + " = '" + ContentUris.parseId(uri) + "'",
                    null, null, null, sortOrder);
            case ABREPEAT:
                return database.query(SoundContract.DBABRepeat.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case ABREPEAT_ID:
                return database.query(SoundContract.DBABRepeat.TABLE_NAME, projection, SoundContract.DBABRepeat._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null, null, null, sortOrder);
            case ABREPEAT_WITH_SOUNDFILEID:
                return database.query(SoundContract.DBABRepeat.TABLE_NAME, projection, SoundContract.DBABRepeat.COLUMN_SOUNDFILE_ID + " = '" + ContentUris.parseId(uri) + "'",
                        null, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case PLAYLIST:
            case PLAYLIST_BY_NAME:
            case PLAYLISTITEMS_WITH_PLAYLISTNAME:
                return SoundContract.DBPlaylist.CONTENT_ITEM_TYPE;
            case SOUNDFILE:
            case SOUNDFILE_ID:
                return DBSoundFile.CONTENT_ITEM_TYPE;
            case ABREPEAT:
            case ABREPEAT_ID:
                return DBABRepeat.CONTENT_ITEM_TYPE;
            case PLAYLISTITEM:
            case PLAYLISTITEM_ID:
                return DBPlaylistItem.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database =  dbHelper.getWritableDatabase();
        Uri returnUri = null;
        long id=0;

        switch(sUriMatcher.match(uri))
        {
            case PLAYLIST:
                id = database.insert(SoundContract.DBPlaylist.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = SoundContract.DBPlaylist.buildUriPlaylistById(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case SOUNDFILE:
                id = database.insert(SoundContract.DBSoundFile.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = SoundContract.DBSoundFile.buildSoundfileUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case PLAYLISTITEM:
                id = database.insert(SoundContract.DBPlaylistItem.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = SoundContract.DBPlaylistItem.buildPlaylistItemUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case ABREPEAT:
                id = database.insert(SoundContract.DBABRepeat.TABLE_NAME, null, values);
                if ( id > 0 )
                    returnUri = DBABRepeat.buildABRepeatUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
        }

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case SOUNDFILE:
                rowsDeleted = db.delete(
                        SoundContract.DBSoundFile.TABLE_NAME, selection, selectionArgs);
                break;
            case PLAYLIST:
                rowsDeleted = db.delete(
                        SoundContract.DBPlaylist.TABLE_NAME, selection, selectionArgs);
                break;
            case PLAYLIST_BY_ID:
            {
                String path = uri.getPath();
                long playlistid = ContentUris.parseId(uri);
                String strplaylistid = String.valueOf(playlistid);
                DLog.v(LOG_TAG, "delete. path = " + path + ", playlistid = " + strplaylistid );

                //remove playitem
                String tableName = DBPlaylistItem.TABLE_NAME;
                selection = DBPlaylistItem.TABLE_NAME+"."+ DBPlaylistItem.COLUMN_PLAYLIST_ID+ " = '" + strplaylistid + "'";
                rowsDeleted = db.delete(tableName, selection, selectionArgs);

                //remove playlist
                tableName = SoundContract.DBPlaylist.TABLE_NAME;
                selection = SoundContract.DBPlaylist.TABLE_NAME+"."+ SoundContract.DBPlaylist._ID + " = '" + strplaylistid + "'";

                db.delete(tableName, selection, selectionArgs);
            }
                break;
            case PLAYLISTITEM_ID:
                {
                String path = uri.getPath();
                long playitemid = ContentUris.parseId(uri);
                String strplayitemid = String.valueOf(playitemid);
                DLog.v(LOG_TAG, "delete. path = " + path + ", playitemid = " + strplayitemid );

                //remove playitem
                String tableName = DBPlaylistItem.TABLE_NAME;
                selection = String.format("%s.%s = '%d'", DBPlaylistItem.TABLE_NAME, DBPlaylistItem._ID, playitemid);
                rowsDeleted = db.delete(tableName, selection, selectionArgs);
            }
            break;
            case PLAYLISTITEM_ALL:
            {
                DLog.v(LOG_TAG, "delete all playitem");
                rowsDeleted = db.delete(DBPlaylistItem.TABLE_NAME, null, null);
            }
            break;
            case ABREPEAT_ID:
            {
                String path = uri.getPath();
                long abrepeatId = ContentUris.parseId(uri);
                String strabrepeatid = String.valueOf(abrepeatId);
                DLog.v(LOG_TAG, "delete. path = " + path + ", abrepeatid = " + strabrepeatid );

                //remove playitem
                String tableName = DBABRepeat.TABLE_NAME;
                selection = String.format("%s.%s = '%d'", DBABRepeat.TABLE_NAME, DBABRepeat._ID, abrepeatId);
                rowsDeleted = db.delete(tableName, selection, selectionArgs);
            }
            break;
            case PLAYLISTITEM:
                rowsDeleted = db.delete(
                        DBPlaylistItem.TABLE_NAME, selection, selectionArgs);
                break;
            case ABREPEAT:
                rowsDeleted = db.delete(
                        DBABRepeat.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int result = 0;

        switch(match){
            case PLAYLISTITEM:
                result = db.update(DBPlaylistItem.TABLE_NAME, values, selection, selectionArgs);
                DLog.v("update playlistitem : " + String.valueOf(result) + ", " + values.toString() + ", " + selection + ", "+ selectionArgs);
                break;
            case PLAYLISTITEM_ID:
                result = db.update(DBPlaylistItem.TABLE_NAME, values, SoundContract.DBPlaylistItem._ID + "=" + ContentUris.parseId(uri), null);
                DLog.v("update by id playlistitem : " + String.valueOf(result) + ", " + values.toString() + ", " + ContentUris.parseId(uri));
                break;
        }
        // Because a null deletes all rows
        if (selection == null || result != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PLAYLISTITEM:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for (ContentValues value : values) {
                        long _id = db.insert(SoundContract.DBPlaylistItem.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }

    }

}
