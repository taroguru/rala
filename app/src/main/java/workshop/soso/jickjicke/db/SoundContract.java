package workshop.soso.jickjicke.db;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

//mediasotre
interface MediaStoreProjections{
    public static final String[] Playlist = {};
}

public class SoundContract {
    public static final String DEFAULT_PLAYLIST = "Playlist";
    public static final String[] ProjectionPlayListItemSet = {
            //playlistitem
            SoundContract.DBPlaylistItem.TABLE_NAME+"."+SoundContract.DBPlaylistItem._ID,
            SoundContract.DBPlaylistItem.TABLE_NAME+"."+SoundContract.DBPlaylistItem.COLUMN_SOUNDFILE_ID,
            SoundContract.DBPlaylistItem.TABLE_NAME+"."+SoundContract.DBPlaylistItem.COLUMN_PLAYLIST_ID,
            SoundContract.DBPlaylistItem.TABLE_NAME+"."+ DBPlaylistItem.COLUMN_ORDER,
            //playlist
            //SoundContract.DBPlaylist.TABLE_NAME+"."+SoundContract.DBPlaylist._ID,
            SoundContract.DBPlaylist.TABLE_NAME+"."+SoundContract.DBPlaylist.COLUMN_NAME,
            //file
            //SoundContract.DBSoundFile.TABLE_NAME+"."+SoundContract.DBSoundFile._ID,
            SoundContract.DBSoundFile.TABLE_NAME+"."+SoundContract.DBSoundFile.COLUMN_PATH,
            SoundContract.DBSoundFile.TABLE_NAME+"."+SoundContract.DBSoundFile.COLUMN_KEY,
            SoundContract.DBSoundFile.TABLE_NAME+"."+SoundContract.DBSoundFile.COLUMN_PLAYCOUNT,
            SoundContract.DBSoundFile.TABLE_NAME+"."+SoundContract.DBSoundFile.COLUMN_DURATION,
    };

    public static final String CONTENT_AUTHORITY = "workshop.soso.jickjicke";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SOUNDFILE = "soundfile";
    public static final String PATH_ABREPEAT = "abrepeat";
    public static final String PATH_PLAYLIST = "playlist";
    public static final String PATH_PLAYLISTITEM = "playlistitem";


	public SoundContract(){}
	
	public static final class DBSoundFile implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SOUNDFILE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SOUNDFILE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SOUNDFILE;


        public static final String TABLE_NAME = "soundfile";
		public static final String COLUMN_PATH = "path";
		public static final String COLUMN_KEY = "key";
        public static final String COLUMN_PLAYCOUNT = "playcount";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_AUDIO_ID = "audio_id";


        public static final Uri buildSoundfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final Uri buildSoundfileUri(String key)
        {
            //return CONTENT_URI.buildUpon().appendPath("soundfile").appendPath(String.valueOf(id)).build();
            return CONTENT_URI.buildUpon().appendPath("key").appendPath(key).build();
        }

		public static final String _CREATEQUERY =
				"create table " + TABLE_NAME +" ("
				+ _ID			+ " integer primary key, "  //audio_id와 같게
				+ COLUMN_PATH + " text not null , "
                + COLUMN_KEY + " text not null , "
				+ COLUMN_PLAYCOUNT + " integer not null , "
                + COLUMN_DURATION + " integer not null); ";
		public static final String _DELETEQUERY = 
				"DROP TABLE IF EXISTS " + TABLE_NAME;
	}

	//구간 반복
	public static final class DBABRepeat implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ABREPEAT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SOUNDFILE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SOUNDFILE;

        public static final Uri buildABRepeatUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(String.valueOf(id)).build();
        }

        public static final Uri buildABRepeatWithSoundFileIdUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("soundfile").appendPath(String.valueOf(id)).build();
        }

        public static final String TABLE_NAME = "abrepeat";
		public static final String COLUMN_SOUNDFILE_ID = "soundfileid";
		public static final String COLUMN_START_MSEC = "startmsec";
		public static final String COLUMN_END_MSEC = "endmsec";
		
		public static final String _CREATEQUERY = 
				"create table " + TABLE_NAME +" ("
				+ _ID			+ " integer primary key autoincrement, "
				+ COLUMN_SOUNDFILE_ID + " integer not null , "
				+ COLUMN_END_MSEC + " integer not null , "
				+ COLUMN_START_MSEC + " integer not null );";
		public static final String _DELETEQUERY = 
				"DROP TABLE IF EXISTS " + TABLE_NAME;
	}

    //플레이 리스트
    public static final class DBPlaylist implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYLIST).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLIST;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLIST;

        public static final Uri buildUriPlaylistOnlyPlaylist()
        {
            return CONTENT_URI.buildUpon().appendPath("noitem").build();
        }

        public static final Uri buildUriPlaylistById(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(String.valueOf(id)).build();
        }


        public static final Uri buildUriPlaylistByName(String playlistName) {
            return CONTENT_URI.buildUpon().appendPath("name").appendPath(playlistName).build();
        }

        public static final Uri buildUriByPlaylistItem(String playlistName) {
            return CONTENT_URI.buildUpon().appendPath("items").appendPath(playlistName).build();
        }



        public static final String TABLE_NAME = "playlist";
        public static final String COLUMN_NAME = "name";

        public static final String _CREATEQUERY =
                "create table " + TABLE_NAME +" ("
                        + _ID			+ " integer primary key autoincrement, "
                        + COLUMN_NAME + " string not null );";
        public static final String _DELETEQUERY =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    //플레이 리스트 아이디
    public static final class DBPlaylistItem implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYLISTITEM).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLISTITEM;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLISTITEM;

        public static final Uri buildPlaylistItemUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(String.valueOf(id)).build();
        }


        public static final Uri buildPlaylistItemAll() {
            return CONTENT_URI.buildUpon().appendPath("all").build();
        }

        public static final String TABLE_NAME = "playlistitem";
        public static final String COLUMN_SOUNDFILE_ID = MediaStore.Audio.Playlists.Members.AUDIO_ID;
        public static final String COLUMN_PLAYLIST_ID = MediaStore.Audio.Playlists.Members.PLAYLIST_ID;
        public static final String COLUMN_ORDER = MediaStore.Audio.Playlists.Members.PLAY_ORDER;

        public static final String _CREATEQUERY =
                "create table " + TABLE_NAME +" ("
                        + _ID			+ " integer primary key autoincrement, "
                        + COLUMN_SOUNDFILE_ID + " integer not null , "
                        + COLUMN_ORDER + " integer not null , "
                        + COLUMN_PLAYLIST_ID + " integer not null);";

        public static final String _DELETEQUERY =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    //todo. 이력 관리

}
