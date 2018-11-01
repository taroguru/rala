package workshop.soso.jickjicke;

import android.database.Cursor;

import java.io.File;

import workshop.soso.jickjicke.db.SoundContract;

/**
 * Created by taroguru on 2017. 3. 25..
 */

public class AudioFile extends ArrayItem{
    private String name;
    private File file;          //파일정보
    private int playCount;      //재생횟수
    private String key;         //식별자. 다양한 프레임워크와 환경으로부터 저를 구원해주세요.

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    //key = filename concat filesize
    private void setDefaultKey()
    {
        key = new String(getName() + String.valueOf( file.getTotalSpace()) );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMember(File file) {
        setFile(file);

        //이름은 파일명만 뽑아서 사용
        String filename = file.toString().substring(file.toString().lastIndexOf("/") + 1);
        setName(filename);

        //디폴트 키를 설정함.
        setDefaultKey();
    }

    public void setData(Cursor cursor)
    {
        int idIdx = cursor.getColumnIndex(SoundContract.DBSoundFile._ID);
        int id = cursor.getInt(idIdx);
        setId(id);

        int playCountIdx = cursor.getColumnIndex(SoundContract.DBSoundFile.COLUMN_PLAYCOUNT);
        int playCount = cursor.getInt(playCountIdx);
        setPlayCount(playCount);

        int keyIdx = cursor.getColumnIndex(SoundContract.DBSoundFile.COLUMN_KEY);
        String key = cursor.getString(keyIdx);
        setKey(key);


        int pathIdx = cursor.getColumnIndex(SoundContract.DBSoundFile.COLUMN_PATH);
        String path = cursor.getString(pathIdx);
        File file = new File(path);
        setMember(file);

    }
}
