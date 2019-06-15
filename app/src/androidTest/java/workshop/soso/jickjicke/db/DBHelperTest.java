package workshop.soso.jickjicke.db;

import android.content.Context;
import android.net.Uri;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by taroguru on 2017. 4. 23..
 */
@RunWith(AndroidJUnit4.class)
//@SmallTest
public class DBHelperTest {
    private Context context;
    private StateManager stateManager;


    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        stateManager = (StateManager) context.getApplicationContext();
        DBHelper.loadCurrentPlayList(context);
    }

    @Test
    public void testSelectFileInfo()  {
        DBHelper.loadCurrentPlayList(context);
        PlayList playlist = stateManager.getCurrentPlayList();
        if(playlist != null && playlist.getItemlist().size() > 0)
        {
            DLog.v("Playitem Size = " + playlist.getItemlist().size());

        }
    }

    @Test
    public void testAddSingleItem()  {
        PlayList currentPlaylist = Utility.getCurrentPlayList(context);
        int itemCount = currentPlaylist.size();

        //add single item
        ArrayList<Audio> audioList = DBHelper.getAllAudioInDevice(context);
        assertNotNull(audioList);
        assertEquals(true, audioList.size() > 0);

        Audio singleAudio = audioList.get(audioList.size()-1);

        Uri soundFileUri = DBHelper.insertFileInfo(context.getContentResolver(), singleAudio);
        DBHelper.insertPlayItemToCurrentPlaylist(context, singleAudio);

        //load item.
        DBHelper.loadCurrentPlayList(context);
        currentPlaylist = stateManager.getCurrentPlayList();
        int addedItemCount = currentPlaylist.size();
        //check item size
        assertEquals(currentPlaylist.toString(), true, addedItemCount > 0);
        assertEquals(itemCount+1, addedItemCount);

    }

    /**
     * 현재 플레이리스트에 하나의 아이템을 넣고 실행해보는 기능
     */
    @Test
    public void testAddSingleItemToCurrentPlaylist(){

        //전체 오디오 목록을 땡겨서
        ArrayList<Audio> audiolist = DBHelper.loadAllAudio(context);
        assertNotNull(audiolist);
        assertTrue(audiolist.size() > 0);

        //현재 목록의 길이를 확인하고
        PlayList currentPlaylist = Utility.getCurrentPlayList(context);
        int beforeSize = currentPlaylist.size();

        //하나를 추가해서
        Audio audio = audiolist.get(0);
        DBHelper.insertPlayItemToCurrentPlaylist(context, audio);

        //현재 플레이리스트를 갱신하고
        DBHelper.loadCurrentPlayList(context);
        currentPlaylist = Utility.getCurrentPlayList(context);

        //현재 플레이리스트가 하나 늘었는지 확인
        int afterSize = currentPlaylist.size();
        Audio addedAudio = (Audio)currentPlaylist.getLast();

        assertEquals(beforeSize+1, afterSize);
        assertEquals(addedAudio.getId(), audio.getId());
        

    }



}