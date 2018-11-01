package workshop.soso.
        jickjicke.db;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;

import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.ABRepeatList;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.AudioFile;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2015. 4. 22..
 */
public class DBHelper {
    private static final String LOG_TAG = "DBHelper";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static ArrayList<PlayList> loadAllPlayList(Context context) {

        DLog.v("load entire playlist.");

        ArrayList<PlayList> playLists = Utility.getEntirePlayList(context);

        //1. db에서 전체  list를 읽어서
        ContentResolver contentResolver = context.getContentResolver();
        Cursor playListCursor = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null);

        if (playListCursor != null) {
            if (playListCursor.getCount() != 0) {
                playListCursor.moveToFirst();

                playLists.clear();
                while (!playListCursor.isAfterLast()) {
                    PlayList playlist = new PlayList();
                    playlist.setData(playListCursor);

                    playLists.add(playlist);

                    //3. 개별 playlist 에 대한 item을 가져온다.
                    long playlistId = playlist.getId();
                    Cursor playlistItemCursor = contentResolver.query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.getId()),
                            null, null, null, null, null);

                    if(playlistItemCursor != null && playListCursor.getCount() != 0)
                    {   //실제 데이터를 썰어넣음.
                        playlistItemCursor.moveToFirst();

                        while(!playlistItemCursor.isAfterLast())
                        {
                            PlayItem playitem = new PlayItem();
                            playitem.setData(playlistItemCursor);


                            playlist.addItem(playitem);
                            playlistItemCursor.moveToNext();
                        }
                    }
                    else
                    {
                        DLog.d(LOG_TAG, "loadAllPlayList: this playlist has no playableitem.");
                    }


                    playListCursor.moveToNext();
                }
                playListCursor.close();
            } else {
                DLog.d(LOG_TAG, "No playlist in db");
            }
        } else {
            DLog.d(LOG_TAG, "playlist is null.");
        }

        return playLists;
    }

    public static PlayList loadCurrentPlayList(Context context)
    {

        DLog.v("load current Playlist");

        PlayList playlist = Utility.getCurrentPlayList(context);

        //1. db에서 default playlist의 아이템을 긁어온다.
        ContentResolver contentResolver = context.getContentResolver();

        if(playlist.getId() == -1 )
        {
            playlist = loadCurrentPlaylist(contentResolver);
            StateManager stateManager = (StateManager)context.getApplicationContext();
            stateManager.setCurrentPlaylist(playlist);
        }
        DLog.d("current playlist id is " + playlist.getId());

        if(playlist.getId() == -1)
        {
            DLog.d("Default Playlist is not inserted in local DB");
            return null;
        }

        loadPlayItem(playlist, contentResolver);

        return playlist;
    }


    private static void loadPlayItem(PlayList playlist, ContentResolver contentResolver) {
        //query(@RequiresPermission.Read @NonNull Uri uri,
//        @Nullable String[] projection, @Nullable String selection,
//        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String sortOrder = SoundContract.DBPlaylistItem.COLUMN_ORDER + " ASC";
        Cursor playlistItemCursor = contentResolver.query(SoundContract.DBPlaylist.buildUriPlaylistById(playlist.getId()), SoundContract.ProjectionPlayListItemSet, null, null, sortOrder);

        if(playlistItemCursor != null && playlistItemCursor.getCount() > 0)
        {
            //현재 리스트는 날려버리고.
            if (playlist == null)   {
                playlist = new PlayList();
            }
            else {
                playlist.getItemlist().clear();
            }

            //실제 데이터를 썰어넣음.
            playlistItemCursor.moveToFirst();
            while(!playlistItemCursor.isAfterLast())
            {
                PlayItem playitem = new PlayItem();
                playitem.setPlayItemData(playlistItemCursor);
                int audioId = -1;
                int audioIdColumn = playlistItemCursor.getColumnIndex(SoundContract.DBPlaylistItem.COLUMN_SOUNDFILE_ID);
                if(audioIdColumn != -1)
                {
                    audioId = playlistItemCursor.getInt(audioIdColumn);

                    if(audioId != -1 )
                    {
                        Audio audio = loadAudio(contentResolver, audioId);
                        if(audio != null)
                        {
                            playitem.setAudio(audio);
                            playlist.addItem(playitem);
                        }
                        else    //파일이 삭제되거나 해당 오디오에 대한 정보를 찾아올 수 없으므로 해당 오디오를 목록에서 삭제
                        {
                            DLog.v("audio data in mediastore is null. audio id = " + String.valueOf(audioId));
                        }
                    }
                    else
                    {
                        DLog.v("audio id column is not in cursor.");
                    }
                }
                else
                {
                    DLog.v("soundfile id is not inserted in db.");
                }
                playlistItemCursor.moveToNext();
            }
            playlistItemCursor.close();
        }
        else
        {
            DLog.d(LOG_TAG, "this playlist has no playableitem.");
        }
    }

    private static Audio loadAudio(ContentResolver contentResolver, int audioId) {
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,  MediaStore.Audio.Media._ID+" = ?", new String[]{String.valueOf(audioId)}, null );
        Audio audio = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            audio = new Audio();
            audio.setData(cursor);

            cursor.close();
        }


        return audio;
    }

    private static PlayList loadCurrentPlaylist( ContentResolver contentResolver) {
        PlayList playlist = new PlayList();
        String selector = String.format("%s = '%s'", SoundContract.DBPlaylist.COLUMN_NAME, SoundContract.DEFAULT_PLAYLIST);
        Cursor defaultPlaylistCursor = contentResolver.query(SoundContract.DBPlaylist.buildUriPlaylistOnlyPlaylist(), null, selector, null, null);

        if(defaultPlaylistCursor != null && defaultPlaylistCursor.getCount() > 0)
        {
            defaultPlaylistCursor.moveToFirst();
            playlist.setData(defaultPlaylistCursor);
        }
        else
        {
            DLog.d("what? currenet playlist is not inserted in DB?");
        }
        defaultPlaylistCursor.close();
        return playlist;
    }

    /***
     * @param cr            contentresolver
     * @param audio      inout
     * @return
     */
    public static long selectFileinfo(ContentResolver cr, Audio audio) {
        long id = 0;
        Uri uri = SoundContract.DBSoundFile.buildSoundfileUri(audio.getAudioFile().getKey());
        Cursor fileinfoCursor = cr.query(uri, null, null, null, null);
        if(fileinfoCursor != null)
        {
            audio.setAudioFileInfo(fileinfoCursor);
            id = audio.getAudioFile().getId();
            fileinfoCursor.close();
        }

        return id;
    }

    public static Uri insertPlayItemToCurrentPlaylist(Context context, Audio audio) {
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = SoundContract.DBPlaylistItem.CONTENT_URI;
        Uri playitemUri = null;
        PlayList currentPlaylist = Utility.getCurrentPlayList(context);
        if(currentPlaylist != null && audio != null)
        {
            int lastOrder = getLastOrderCurrentPlaylist(contentResolver);
            long playlistID = currentPlaylist.getId();
            long soundfile_id = audio.getId();

            ContentValues values = new ContentValues();
            values.put(SoundContract.DBPlaylistItem.COLUMN_PLAYLIST_ID, playlistID);
            values.put(SoundContract.DBPlaylistItem.COLUMN_ORDER, Integer.valueOf(lastOrder) );
            values.put(SoundContract.DBPlaylistItem.COLUMN_SOUNDFILE_ID, soundfile_id);
            DLog.v("insert item value is : " + values.toString());

            playitemUri = contentResolver.insert(uri, values);
        }

        return playitemUri;
    }
    /**
     * 오디오 목록 전체를 플레이리스트에 추가.
     * @param context
     * @param audioList
     * @return
     */
    public static int insertPlayItemToCurrentPlaylist(Context context, ArrayList<Audio> audioList) {
        ContentResolver contentResolver = context.getContentResolver();

        int insertedCount = 0;
        PlayList currentPlaylist = Utility.getCurrentPlayList(context);
        if(currentPlaylist != null && audioList != null && audioList.size() > 0)
        {
            //getlastOrder
            //int lastOrder = getLastOrderCurrentPlaylist(contentResolver);
            for(Audio audio : audioList)
            {
                insertFileInfo(contentResolver, audio);
                insertPlayItemToCurrentPlaylist(context, audio);
                ++insertedCount;
            }
        }

        return insertedCount;
    }

    public static Uri insertPlayItem(ContentResolver contentResolver, long playlistIndex, PlayItem playItem) {
        //getlastOrder
        int lastOrder = getLastOrder(contentResolver, playlistIndex);

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistIndex);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(lastOrder) + 1);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, playItem.getAudioid());

        return contentResolver.insert(uri, values);
    }

    private static int getLastOrderCurrentPlaylist(ContentResolver contentResolver) {

        String[] cols = new String[] {
                "count(*)"
        };
        Uri uri = SoundContract.DBPlaylistItem.CONTENT_URI;
        Cursor cur = contentResolver.query(uri, cols, null, null, null);
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();

        return base;
    }


    private static int getLastOrder(ContentResolver contentResolver, long playlistIndex) {

        String[] cols = new String[]{
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistIndex);
        Cursor cur = contentResolver.query(uri, cols, null, null, null);
        if (cur != null) {
            cur.moveToFirst();
            final int base = cur.getInt(0);
            cur.close();
            return base;
        } else {
            return -1;
        }

    }

    public static Uri insertFileInfo(ContentResolver contentResolver, Audio audio) {
        Uri fileUri = null;

        Uri soundFileUri = SoundContract.DBSoundFile.CONTENT_URI;
        Uri soundFileUriById = SoundContract.DBSoundFile.buildSoundfileUri(audio.getId());
        Cursor soundFileCursor = contentResolver.query(soundFileUriById, null, null, null, null, null);

        //이미 있는 soundfile이므로 추가할 필요가 없다.
        if(soundFileCursor != null && soundFileCursor.getCount() > 0)
        {
            fileUri = soundFileUriById;
        }
        else    //없으므로 추가한다.
        {
            ContentValues fileValue = new ContentValues();

            AudioFile audioFile = audio.getAudioFile();

            if(audioFile != null)
            {
                audioFile = new AudioFile();

            }
            fileValue.put(SoundContract.DBSoundFile._ID, audio.getId());
            fileValue.put(SoundContract.DBSoundFile.COLUMN_PATH, audio.getAudioFile().getFile().getPath()); //not used
            fileValue.put(SoundContract.DBSoundFile.COLUMN_KEY, audio.getAudioFile().getKey());
            fileValue.put(SoundContract.DBSoundFile.COLUMN_PLAYCOUNT, audio.getAudioFile().getPlayCount() );
            fileValue.put(SoundContract.DBSoundFile.COLUMN_DURATION, audio.getDuration() );

            fileUri = contentResolver.insert(soundFileUri, fileValue);
        }
        soundFileCursor.close();


        return fileUri;
    }

    public static long insertABRepeatToDB(Context context, long fileid, ABRepeat abRepeat) {
        ContentResolver cr = context.getContentResolver();
        ContentValues value = new ContentValues();

        value.put(SoundContract.DBABRepeat.COLUMN_SOUNDFILE_ID, fileid);
        value.put(SoundContract.DBABRepeat.COLUMN_START_MSEC, abRepeat.getStart());
        value.put(SoundContract.DBABRepeat.COLUMN_END_MSEC, abRepeat.getEnd());

        Uri abrepeaturi = cr.insert(SoundContract.DBABRepeat.CONTENT_URI, value);

        return ContentUris.parseId(abrepeaturi);
    }

    public static boolean isValidCursor(Cursor cursor) {
        boolean retValue=false;
        if(cursor != null)
        {
            if(cursor.getCount() != 0)
            {
                retValue = true;
            }
            else {
                DLog.v(LOG_TAG, "cursor has no column");
            }
        }
        else
        {
            DLog.v(LOG_TAG, "cursor is null");
        }
        return retValue;
    }

    public static AudioFile loadAudioFile(Context context, long audioid)    //audioid == fileid
    {
        AudioFile audioFile = null;

        ContentResolver cr = context.getContentResolver();
        Uri uri = SoundContract.DBSoundFile.buildSoundfileUri(audioid);

        Cursor cursor = cr.query(uri, null, null, null, null, null);
        if(cursor != null && cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            audioFile.setData(cursor);
        }
        cursor.close();

        return audioFile;
    }
    /*
    *
    Cursor cursor = plist.getPlaylistTracks(getActivity(), playlist_id);
     // replace with your own method to get cursor
    ArrayList<String> audio_ids = new ArrayList<String>();

    // build up the array with audio_id's
    int i = 0;
    if (cursor != null && cursor.moveToFirst()) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String audio_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
            audio_ids.add(audio_id);
        }
        */
    //mediastore로 갈아탑시다
    public static long insertPlayList(Context context, PlayList playlist) {
        ContentResolver cr = context.getContentResolver();
        ContentValues value = new ContentValues();

        value.put(MediaStore.Audio.Playlists.NAME, playlist.getName());

        Uri playlistUri = cr.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, value);

        return ContentUris.parseId(playlistUri);
    }

    public static ABRepeatList loadFileABRepeatList(Context context, PlayItem currentPlayItem) {
        ABRepeatList abRepeatList = new ABRepeatList();

        try{
            long fileid = currentPlayItem.getAudioId();
            ContentResolver cr = context.getContentResolver();
            Cursor abrepeatlistCursor =  cr.query(SoundContract.DBABRepeat.buildABRepeatWithSoundFileIdUri(fileid), null, null, null, null);

            if(abrepeatlistCursor != null)
            {
                if(abrepeatlistCursor.getCount() > 0)
                {
                    abrepeatlistCursor.moveToNext();
                    while(!abrepeatlistCursor.isAfterLast())
                    {
                        ABRepeat abrepeat = new ABRepeat();
                        abrepeat.setData(abrepeatlistCursor);

                        abRepeatList.addItem(abrepeat);
                        abrepeatlistCursor.moveToNext();
                    }
                    abRepeatList.sort();
                }
                abrepeatlistCursor.close();
            }
        }
        catch(NullPointerException e)
        {
            DLog.d(e.toString());
        }

        return abRepeatList;
    }


    public static int deletePlaylist(Context context, long playlistidx) {
        DLog.v("delete playlist and all playitem. playlist index = " + String.valueOf(playlistidx));
        //android medialibrary 용.
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = {String.valueOf(playlistidx)};
        return resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);

        //inapp db. medialibrary용이 아님
        //ContentResolver cr = context.getContentResolver();
        //return cr.delete(SoundContract.DBPlaylist.buildUriPlaylistById(playlistidx), null, null);
    }

    public static int deletePlayItem(Context context, long playlistId,  long playitemid) {
        DLog.v("delete playlist and all playitem. (playlistidx,playitemidx) = " + String.valueOf(playlistId)+ ","+String.valueOf(playitemid));
        //android medialibrary 용.
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists.Members._ID + "=?";
        String[] whereVal = {String.valueOf(playitemid)};
        return resolver.delete(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), where, whereVal);



        //ContentResolver cr = context.getContentResolver();
        //return cr.delete(SoundContract.DBPlaylistItem.buildPlaylistItemUri(playitemid), null, null);
    }

    public static int deletePlayItemInCurrentPlaylist(Context context, long playitemid)
    {
        ContentResolver cr = context.getContentResolver();
        return cr.delete(SoundContract.DBPlaylistItem.buildPlaylistItemUri(playitemid), null, null);
    }

    public static int deleteABRepeat(Context context, ABRepeat deletingItem) {
        ContentResolver cr = context.getContentResolver();
        return cr.delete(SoundContract.DBABRepeat.buildABRepeatUri(deletingItem.getId()), null, null );
    }
// 30,000,000,000
    public static ArrayList<Audio> getAllAudioInDevice(Context context)
    {
        ArrayList<Audio> audioList = new ArrayList<Audio>();

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                //MediaStore.Audio.Media.DATA + " acs");
                null );

        if(cursor != null)
        {
            cursor.moveToFirst();
            do{
                Audio audio = new Audio();
                audio.setData(cursor);
                audioList.add(audio);
            }
            while(cursor.moveToNext());
            cursor.close();
        }

        return audioList;
    }

    //currentplaylist의 두 position의 playitem의 order를  swap함.
    public static void swapPlayItemOrder(Context mContext, int firstPlayItemPosition, int secondPlayItemPosition) {
        ContentResolver contentResolover = mContext.getContentResolver();
        PlayList currentPlaylist = Utility.getCurrentPlayList(mContext);
        PlayItem firstItem = (PlayItem) currentPlaylist.getItemlist().get(firstPlayItemPosition);
        PlayItem secondItem = (PlayItem) currentPlaylist.getItemlist().get(secondPlayItemPosition);
        int firstOrder = firstItem.getPlayorder();
        int secondOrder = secondItem.getPlayorder();
        Uri uri = SoundContract.DBPlaylistItem.CONTENT_URI;
        String where = String.format(SoundContract.DBPlaylistItem._ID + " = ? ");

        ContentValues valueFirst  = new ContentValues();
        ContentValues valueSecond = new ContentValues();

        valueFirst.put (SoundContract.DBPlaylistItem.COLUMN_ORDER, Integer.valueOf(secondOrder) );
        valueSecond.put(SoundContract.DBPlaylistItem.COLUMN_ORDER, Integer.valueOf(firstOrder) );

        String[] selectionArgsFirst  = {String.valueOf(firstItem.getId()) };
        String[] selectionArgsSecond = {String.valueOf(secondItem.getId())};

        contentResolover.update(uri, valueFirst,  where, selectionArgsFirst);
        contentResolover.update(uri, valueSecond, where, selectionArgsSecond );

    }

    public static ArrayList<Audio> loadAllAudio(Context context) {

        ArrayList<Audio> audioList = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();


        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection=null;
        String selection=null;
        String[] selectionarg=null;
        String order=null;

        Cursor cursor = cr.query(uri, projection, selection, selectionarg, order);

        if(cursor != null)
        {
            cursor.moveToFirst();
            do{
                Audio audio = new Audio();
                audio.setData(cursor);
                audioList.add(audio);
            }
            while(cursor.moveToNext());

            cursor.close();
        }

        return audioList;
    }

    public static void clearCurrentPlaylist(Context context) {
        //todo. 현재 플레이리스트 삭제.
        ContentResolver cr = context.getContentResolver();
        cr.delete(SoundContract.DBPlaylistItem.buildPlaylistItemAll(), null, null);
    }

    public static PlayItem loadPlayItem(Context context, Uri insertedPlayItemUri) {

        PlayItem item = null;
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(insertedPlayItemUri, null, null, null, null);
        if(cursor != null)
        {
            cursor.moveToFirst();
            item = new PlayItem();
            item.setData(cursor);   //todo. audio 정보 더 잘라서 넣기.
            cursor.close();
        }
        return item ;
    }

    public static String albumArt(Context context, long album_id) {
        ContentResolver musicResolve = context.getContentResolver();
        Uri smusicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor music =musicResolve.query(smusicUri, null, MediaStore.Audio.Albums.ALBUM_ID + "=" + String.valueOf(album_id),         //should use where clause(_ID==albumid)
                null, null);

        music.moveToFirst();            //i put only one song in my external storage to keep things simple
        int x=music.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
        return music.getString(x);
    }
}
