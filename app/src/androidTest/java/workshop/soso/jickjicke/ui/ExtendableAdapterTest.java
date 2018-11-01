package workshop.soso.jickjicke.ui;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import workshop.soso.jickjicke.AudioFile;
import workshop.soso.jickjicke.AudioFolder;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.ui.mediastore.folder.MediaArrayAdapterByFolder;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;

import static junit.framework.Assert.assertEquals;

/**
 * Created by jeonghan on 2017-05-25.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ExtendableAdapterTest  {
    private Context context;
    private StateManager stateManager;


    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        stateManager = (StateManager) context.getApplicationContext();
        DBHelper.loadCurrentPlayList(context);
    }

    @Test
    public void test()
    {
        MediaArrayAdapterByFolder adapter = new MediaArrayAdapterByFolder(context, new ArrayList<MultiItemAdapter.Row<?>>());
        ArrayList<MultiItemAdapter.Row<?>> folderlist = createTestFolderList();
        assertEquals(folderlist.size(), 2);

        adapter.setValues(folderlist);

        assertEquals(adapter.getItemCount(), 2);
        assertEquals(adapter.getItemViewType(1), MediaArrayAdapterByFolder.FOLDERTYPE);

    }

    private ArrayList<MultiItemAdapter.Row<?>> createTestFolderList() {
        ArrayList<MultiItemAdapter.Row<?>> arrayItem = new ArrayList<>();
        MediaArrayAdapterByFolder.Row<AudioFolder> audioFolder = MediaArrayAdapterByFolder.Row.create(0, MediaArrayAdapterByFolder.FOLDERTYPE, new AudioFolder());
        audioFolder.addChild(
                MediaArrayAdapterByFolder.Row.create(0, MediaArrayAdapterByFolder.FILETYPE, new AudioFile())
        );


        arrayItem.add(audioFolder);
        arrayItem.add(MediaArrayAdapterByFolder.Row.create(1, MediaArrayAdapterByFolder.FOLDERTYPE, new AudioFolder()));

        return arrayItem;
    }
}
