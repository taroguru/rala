package workshop.soso.jickjicke.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Set;

import workshop.soso.jickjicke.db.SoundContract;
import workshop.soso.jickjicke.db.SoundContract.DBPlaylist;
import workshop.soso.jickjicke.db.SoundDBHelper;
import workshop.soso.jickjicke.util.DLog;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by taroguru on 2015. 3. 13..
 */
@RunWith(AndroidJUnit4.class)
public class TestDb {

    private static final String LOG_TAG = "TestDB";


    public static ContentValues createFileInfoValues() {
        ContentValues value = new ContentValues();

        value.put(SoundContract.DBSoundFile.COLUMN_PLAYCOUNT, 0);
        value.put(SoundContract.DBSoundFile.COLUMN_KEY, 0); //KEY = PATH + filesize
        value.put(SoundContract.DBSoundFile.COLUMN_PATH, "/storage/sdcard0/Music/PAGODA_TOEIC_LC_ALL/Part 1/Track P1-014.MP3");
        return value;
    }

    public static ContentValues createPlayListValues() {
        ContentValues value = new ContentValues();

        value.put(SoundContract.DBPlaylist.COLUMN_NAME, SoundContract.DEFAULT_PLAYLIST);
        return value;
    }

    public static ContentValues createABRepeatValues() {
        ContentValues value = new ContentValues();

        value.put(SoundContract.DBABRepeat.COLUMN_END_MSEC, 10);
        value.put(SoundContract.DBABRepeat.COLUMN_START_MSEC, 100);
        value.put(SoundContract.DBABRepeat.COLUMN_SOUNDFILE_ID, 0);
        return value;
    }

    public static ContentValues createPlayListItemValues() {
        ContentValues value = new ContentValues();

        value.put(SoundContract.DBPlaylistItem.COLUMN_SOUNDFILE_ID, 0);
        value.put(SoundContract.DBPlaylistItem.COLUMN_PLAYLIST_ID,  0);

        return value;
    }

    //@brief : cursor의 데이터와 content value 사이의 합일을 확인해주는 벨리데이터
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        validateCursorNotClose(valueCursor, expectedValues);
        valueCursor.close();
    }

    //@brief : cursor의 데이터와 content value 사이의 합일을 확인해주는 벨리데이터
    static void validateCursorNotClose(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }

    public void testCreateDb() throws Throwable {
        Context context = InstrumentationRegistry.getContext();
        context.deleteDatabase(SoundDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new SoundDBHelper(context).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        Context context = InstrumentationRegistry.getContext();
        SoundDBHelper dbHelper = new SoundDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createFileInfoValues();

        long locationRowId;
        locationRowId = db.insert(SoundContract.DBSoundFile.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        DLog.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                SoundContract.DBSoundFile.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues playlistValues = createPlayListValues();

        long playlistRowId = db.insert(SoundContract.DBPlaylist.TABLE_NAME, null, playlistValues);
        assertTrue(playlistRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                DBPlaylist.TABLE_NAME,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(weatherCursor, playlistValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues playlistItemValues = createPlayListItemValues();

        long playlistItemRowId = db.insert(SoundContract.DBPlaylist. TABLE_NAME, null, playlistItemValues);
        assertTrue(playlistRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor playlistitemCursor = db.query(
                DBPlaylist.TABLE_NAME,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(playlistitemCursor, playlistValues);



        // Fantastic.  Now that we have a location, add some weather!
        ContentValues abRepeatValues = createPlayListValues();

        long abRepeatRowId = db.insert(SoundContract.DBPlaylist.TABLE_NAME, null, abRepeatValues);
        assertTrue(playlistRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor abRepeatCursor = db.query(
                DBPlaylist.TABLE_NAME,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(abRepeatCursor, playlistValues);

//        DBHelper.insertPlayList();
//        Cursor playlistItemCursor = contentResolver.query(SoundContract.DBPlaylist.buildUriPlaylistById(1),
//                SoundContract.ProjectionPlayListItemSet, null, null, null, null);

//        validateCursor(playlistItemCursor, playlistItemValues);

        dbHelper.close();
    }


}
