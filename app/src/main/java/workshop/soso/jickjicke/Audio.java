package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.io.Serializable;

import workshop.soso.jickjicke.db.SoundContract;

//audio = fileinfo.
public class Audio extends ArrayItem implements MediaStore.Audio.AudioColumns, Serializable{

    //audiofile
    private AudioFile audioFile;
    //own member
    private long id;
    private long  duration;       //재생시간
    //private boolean check;

    private String title;
    private String titleKey;
    private int bookmark;
    private int artistid;
    private String artist;
    private String albumArtist;
    private String complilation;
    private String artistKey;
    private String composer;
    private int albumid;
    private String album;
    private String albumKey;
    private String albumArt;
    private int track;
    private int year;
    private boolean isMusic;
    private boolean isPodcast;
    private boolean isRingtone;
    private boolean isAlarm;
    private boolean isNotification;

    public String getTitle() {
        if(title != null)
            return title;
        else
            return "";
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName(){return title;}

    public void setName(String name) {
        this.title = name;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public int getBookmark() {
        return bookmark;
    }

    public void setBookmark(int bookmark) {
        this.bookmark = bookmark;
    }

    public int getArtistid() {
        return artistid;
    }

    public void setArtistid(int artistid) {
        this.artistid = artistid;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum_artist() {
        return albumArtist;
    }

    public void setAlbum_artist(String album_artist) {
        this.albumArtist = album_artist;
    }

    public String getComplilation() {
        return complilation;
    }

    public void setComplilation(String complilation) {
        this.complilation = complilation;
    }

    public String getArtist_key() {
        return artistKey;
    }

    public void setArtist_key(String artist_key) {
        this.artistKey = artist_key;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public int getAlbumid() {
        return albumid;
    }

    public void setAlbumid(int albumid) {
        this.albumid = albumid;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbum_key() {
        return albumKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean is_music() {
        return isMusic;
    }

    public void setIs_music(boolean is_music) {
        this.isMusic = is_music;
    }

    public boolean is_podcast() {
        return isPodcast;
    }

    public void setIs_podcast(boolean is_podcast) {
        this.isPodcast = is_podcast;
    }

    public boolean is_ringtone() {
        return isRingtone;
    }

    public void setIs_ringtone(boolean is_ringtone) {
        this.isRingtone = is_ringtone;
    }

    public boolean is_alarm() {
        return isAlarm;
    }

    public void setIs_alarm(boolean is_alarm) {
        this.isAlarm = is_alarm;
    }

    public boolean is_notification() {
        return isNotification;
    }

    public void setIs_notification(boolean is_notification) {
        this.isNotification = is_notification;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    //functions
    public Audio() {
        super();
        initiate();
    }

    public Audio(File fileInfo) {
        super();
        initiate();
        audioFile.setFile(fileInfo);
    }

    private void initiate() {
        audioFile = new AudioFile();
        audioFile.setPlayCount(0);
        setDuration(0);
    }


    public int getPlayCount() {
        return audioFile.getPlayCount();
    }

    public void setPlayCount(int playCount) {
        audioFile.setPlayCount( playCount );
    }

    public AudioFile getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(AudioFile audioFile) {
        this.audioFile = audioFile;
    }

    public File getFile() {
        return getAudioFile().getFile();
    }

    public void setFile(File file) {
        getAudioFile().setMember(file);
    }


    /**
     * db cursor에서 값을 추출하여 fileinfo에 할당.
     * @param playListCursor
     */
    public void setAudioFileInfo(Cursor playListCursor) {
        audioFile.setData(playListCursor);
    }

    //mediastore용으로 바꾸자
    public void setIndex(Cursor fileDBCursor)
    {
        int idIdx = fileDBCursor.getColumnIndex(SoundContract.DBSoundFile._ID);
        long fileId = fileDBCursor.getLong(idIdx);
        setId(fileId);
    }


    public void setData(Cursor cursor)
    {

        int idx_id = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        if(idx_id != -1){
            setId(cursor.getInt(idx_id));
        }
        else
        {
            idx_id =cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            if(idx_id != -1) {
                setId(cursor.getInt(idx_id));
            }
        }

        int idx_artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        if(idx_artist != -1)    setArtist(cursor.getString(idx_artist));

        int idx_album_id = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        if(idx_album_id != -1)  setAlbumid( cursor.getInt(idx_album_id) );

        int idx_artist_key = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_KEY);
        if(idx_artist_key != -1)    setArtist_key(cursor.getString(idx_artist_key));

        int idx_title_key = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        if(idx_title_key != -1)    setTitle(cursor.getString(idx_title_key));

        int idx_duraiton = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        if(idx_duraiton != -1)    setDuration(cursor.getLong(idx_duraiton));


        String path = getPath(cursor);
        if(path != null && !path.isEmpty())
        {
            audioFile.setMember(new File(path));
        }
    }

    public static String getPath(Cursor cursor)
    {
        String path = null;
        int idx_data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        if(idx_data != -1)         path = cursor.getString(idx_data);
        return path;
    }

//	private String path="";
//	private String name="";	//소리 파일 제목. 
    //Todo 없으면 파일명으로 대체하자.?
//	public String getPath() {
//		return path;
//	}
//	public void setPath(String path) {
//		this.path = path;
//	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	public FileInfo(int id, String path){
//		this.setId(id);
//		this.path = path;
//	}

}
