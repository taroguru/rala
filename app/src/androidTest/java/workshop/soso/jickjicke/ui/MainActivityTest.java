package workshop.soso.jickjicke.ui;


import android.content.IntentFilter;
import android.os.SystemClock;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.asset.LocalBroadcastReceiverMock;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.ui.player.MainActivity;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
//@SmallTest
public class MainActivityTest {
    private LocalBroadcastReceiverMock receiver;
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setup() {
        IntentFilter filter = new IntentFilter(ACTION.PlayAudio);
        receiver = new LocalBroadcastReceiverMock();
        receiver.setReceiveAction(ACTION.PlayAudio);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(InstrumentationRegistry.getTargetContext());
        manager.registerReceiver(receiver, filter);

    }

    @Test
    public void selectSingleAudioInMediaStoreAll() {
        //librarytab 선택
        clickTabs("LIBRARY", R.id.tabs);

        //alltab 선택
        clickTabs("All", R.id.libraryTab);

        //첫번째 곡 선택. 여기서 재생까지 처리됨.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.audiolist), isCompletelyDisplayed()))
                .atPosition(0).perform(click());


        //마지막 아이템이 선택한 아이템인지 확인
        //현재 재생중인 곡이 마지막 아이템인지 확인
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(receiver.isReceived());


    }

    private Matcher<View> clickTabs(String tabName, int tabResourceId)
    {
        Matcher<View> matcher = allOf(withText( tabName),
                isDescendantOfA(withId(tabResourceId)));
        onView(matcher).perform(click());
        SystemClock.sleep(800); // Wait a little until the content is loaded

        return matcher;
    }

    @Test
    public void selectSingleAudioInCurrentPlaylist() {
        //librarytab 선택
        clickTabs("PLAYLIST", R.id.tabs);

        //첫번째 곡 선택. 여기서 재생까지 처리됨.
        onData(anything())
                .inAdapterView(allOf(withId(R.id.currentplaylist_recycler), isCompletelyDisplayed()))
                .atPosition(0).perform(click());

        //마지막 아이템이 선택한 아이템인지 확인
        //현재 재생중인 곡이 마지막 아이템인지 확인
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(receiver.isReceived());
    }

    @After
    public void after()
    {
        LocalBroadcastManager.getInstance(InstrumentationRegistry.getTargetContext()).unregisterReceiver(receiver);
    }

}