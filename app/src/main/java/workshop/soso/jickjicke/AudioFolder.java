package workshop.soso.jickjicke;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.ArrayList;

import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2017. 2. 9..
 */

public class AudioFolder extends ArrayItem {//implements MediaStore.Audio.AlbumColumns {

    private String name;    //genre
    private String path;    //full path of audio file
    private int number_of_song;
    private ArrayList<Audio> audioList = new ArrayList<>();

    public ArrayList<Audio> getAudioList() {
        return audioList;
    }

    public void setAudioList(ArrayList<Audio> audioList) {
        this.audioList = audioList;
    }

    /**
     * 폴더의 자식 오디오 정보를 추가한다.
     * @param audio 폴더에 있는 오디오 정보
     */
    public void addChild(Audio audio)
    {
        audioList.add(audio);
    }

    public int increaseNumberOfSong(){  return ++number_of_song;}
    public int getNumber_of_song() {
        return number_of_song;
    }

    public void setNumber_of_song(int number_of_song) {
        this.number_of_song = number_of_song;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public AudioFolder() {
        name = "";
        path = "";
        number_of_song=0;
    }

    //deprecated?
    public void setData(Cursor cursor) {    //is it available?
        DLog.v("Set Genre Data from cursor : "+cursor.toString());

        int idx_id = cursor.getColumnIndex(BaseColumns._ID);
        setId(cursor.getInt(idx_id));

        int idx_genre = cursor.getColumnIndex(MediaStore.Audio.GenresColumns.NAME);
        setName(cursor.getString(idx_genre));

    }

    @Override
    public String toString() {
        return "AudioFolder{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", number_of_song=" + number_of_song +
                '}';
    }

    //path가 같으면 같은 녀석으로.
    @Override
    public boolean equals(Object obj) {

        boolean result = false;
        if(obj instanceof String)
        {
            String path= (String)obj;
            result = getPath().equals(path);
        }
        return result;
    }
}
