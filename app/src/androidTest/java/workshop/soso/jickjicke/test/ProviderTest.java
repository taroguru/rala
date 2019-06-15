package workshop.soso.jickjicke.test;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.InstrumentationRegistry;

import workshop.soso.jickjicke.db.SoundContract;
import workshop.soso.jickjicke.util.DLog;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


public class ProviderTest {


    private static final String LOG_TAG = "ProviderTest";

    // brings our database to an empty state
    public void deleteAllRecords() {
        Context context = InstrumentationRegistry.getContext();
        
        context.getContentResolver().delete(
                SoundContract.DBABRepeat.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                SoundContract.DBPlaylistItem.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                SoundContract.DBPlaylist.CONTENT_URI,
                null,
                null
        );
        context.getContentResolver().delete(
                SoundContract.DBSoundFile.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = context.getContentResolver().query(
                SoundContract.DBPlaylist.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = context.getContentResolver().query(
                SoundContract.DBSoundFile.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = context.getContentResolver().query(
                SoundContract.DBABRepeat.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = context.getContentResolver().query(
                SoundContract.DBPlaylistItem.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

    }

    public void setUp() {
        deleteAllRecords();
    }

    public void testFullPlayItem(){
        Context context = InstrumentationRegistry.getContext();

        ContentValues value = TestDb.createFileInfoValues();
        ContentResolver contentResolver = context.getContentResolver();

        Uri insertedFileUri = contentResolver.insert(SoundContract.DBSoundFile.CONTENT_URI, value);
        long fileId = ContentUris.parseId(insertedFileUri);
        assertTrue(fileId != -1);


        ContentValues playlistvalue = TestDb.createPlayListValues();
        Uri insertedPlayListUri = contentResolver.insert(SoundContract.DBPlaylist.CONTENT_URI, playlistvalue);
        long playlistId = ContentUris.parseId(insertedPlayListUri);
        assertTrue(playlistId != -1);


        ContentValues playlistItemValue = new ContentValues();
        playlistItemValue.put(SoundContract.DBPlaylistItem.COLUMN_PLAYLIST_ID, playlistId);
        playlistItemValue.put(SoundContract.DBPlaylistItem.COLUMN_SOUNDFILE_ID, fileId);

        Uri insertedPlayListItemUri = contentResolver.insert(SoundContract.DBPlaylistItem.CONTENT_URI, playlistItemValue);
        long playlistItemId = ContentUris.parseId(insertedPlayListItemUri);
        assertTrue(playlistItemId != -1);


        DLog.v(LOG_TAG, "Play List Item Uri : " + SoundContract.DBPlaylist.buildUriByPlaylistItem(SoundContract.DEFAULT_PLAYLIST).toString());

        /*//                query(SQLiteDatabase db, String[] projectionPlayListItemSet,
//                String selection, String[] selectionArgs, String groupBy,
//                    String having, String sortOrder) {*/

        Cursor cursor = contentResolver.query(SoundContract.DBPlaylist.buildUriByPlaylistItem(SoundContract.DEFAULT_PLAYLIST),
                SoundContract.ProjectionPlayListItemSet, null, null, null);


        assertTrue(cursor != null);
        TestDb.validateCursorNotClose(cursor, value);
        TestDb.validateCursorNotClose(cursor, playlistItemValue);
        TestDb.validateCursor(cursor, playlistvalue);
    }


    public void testInsertOneItem() {
        Context context = InstrumentationRegistry.getContext();

        //파일 아이템 추가.
        ContentValues value = TestDb.createFileInfoValues();

        Uri insertedFileUri = context.getContentResolver().insert(SoundContract.DBSoundFile.CONTENT_URI, value);
        long fileId = ContentUris.parseId(insertedFileUri);

        // Verify we got a row back.
        assertTrue(fileId != -1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = context.getContentResolver().query(
                SoundContract.DBSoundFile.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, value);

        // Now see if we can successfully query if we include the row id
        cursor = context.getContentResolver().query(
                SoundContract.DBSoundFile.buildSoundfileUri(fileId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, value);

    }

}


