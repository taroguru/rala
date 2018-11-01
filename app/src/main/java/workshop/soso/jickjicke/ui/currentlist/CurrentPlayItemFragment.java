package workshop.soso.jickjicke.ui.currentlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.LoadCurrentPlayListTask;
import workshop.soso.jickjicke.db.LoadCurrentPlayListTaskWithPlay;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.ui.player.drawer.OnDataSetChangedListener;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.GUIHelper;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 2. 11..
 */

public class CurrentPlayItemFragment extends Fragment implements OnDataSetChangedListener, OnFloatingButtonStyleChange{
    public static final String FRAGMENT_ID = "CurrentPlaylistFragment";
    private View rootView = null;
    private BroadcastReceiver broadcastReceiver;
    private CurrentPlayItemArrayAdapter adapter;
    private RecyclerView recyclerView;
    private TextView     recyclerEmptyTextView;

    public CurrentPlayItemArrayAdapter getAdapter() {
        return adapter;
    }

    public static CurrentPlayItemFragment newInstance() {
        Bundle args = new Bundle();
        CurrentPlayItemFragment fragment = new CurrentPlayItemFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        DLog.v("");
        super.onCreate(savedInstanceState);
        initBroadCastReceiver();
    }

    private void initBroadCastReceiver()
    {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                DLog.v("MainActivity get an Intent : " + action);
                if(action.equals(ACTION.RefreshCurrentPlaylist))
                {
                    //data base 갱신
                    LoadCurrentPlayListTaskWithPlay loadTask = new LoadCurrentPlayListTaskWithPlay();
                    loadTask.setAdapter(adapter);
                    loadTask.setContext(context);
                    loadTask.setPlayIndex(0);
                    loadTask.execute();
                }else if(action.equals(ACTION.PlayNewItem)){

                    int position = intent.getIntExtra(ACTION.Position, 0);

                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                        @Override protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }

                        @Override
                        protected int getHorizontalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }
                    };
                    smoothScroller.setTargetPosition(position);
//                    recyclerView.smoothScrollToPosition(position);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
//                    lm.scrollToPositionWithOffset(position, 1);
                    lm.startSmoothScroll(smoothScroller);
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        DLog.v("");
        rootView = inflater.inflate(R.layout.currentplaylist_fragment, container, false);

        //getActivity().setTitle(());
        adapter =  new CurrentPlayItemArrayAdapter(getActivity(), new ArrayList<>());
        recyclerEmptyTextView = (TextView) rootView.findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.currentplaylist_recycler);

        recyclerView.setAdapter(adapter);

//        recyclerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = recyclerView.getChildAdapterPosition(v);
//                Utility.sendBroadcastPlayAudio(getContext(), position);
//            }
//        });

        ItemTouchHelper.Callback callback = new PlayItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    private void playSelectedItem(AdapterView<?> parent, View view, int position) {
        PlayList playlist = Utility.getCurrentPlayList(getContext());
        Utility.sendBroadcastPlayAudio(getContext(), StateManager.CurrentPlaylistPosition, position);
    }

    private void loadPlaylist()
    {
        LoadCurrentPlayListTask task = new LoadCurrentPlayListTask();
        task.setContext(getContext());
        task.setAdapter(adapter);
        task.execute();
    }

    //최초 실행시점에서 soundservice가 널 일때만 생성.
    @Override
    public void onStart(){
        super.onStart();

        loadPlaylist();

        IntentFilter intentFilter = new IntentFilter(ACTION.RefreshCurrentPlaylist);
        intentFilter.addAction(ACTION.PlayNewItem);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((broadcastReceiver), intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

//    @Override
//    public AbstractMediaArrayAdapter getAdapter()
//    {
//        return (AbstractMediaArrayAdapter) adapter;
//    }

    @Override
    public void onDataSetChanged() {
        //이게문제인것같은데.
//        DLog.v("Current Playlist is loaded.");
//        adapter.setData(Utility.getCurrentPlayList(getContext()).getItemlist());
//        Utility.setEmptyText(adapter.getData().isEmpty(), recyclerView, recyclerEmptyTextView);
//        adapter.notifyDataSetChanged();

        DLog.v("Current Playlist is loaded.");
        Utility.setEmptyText(adapter.getmRows().isEmpty(), recyclerView, recyclerEmptyTextView);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void changeButton(FloatingActionButton floatingButton) {
        GUIHelper.changeFloatingButtonToSearch(getContext(), floatingButton, CONSTANTS.PAGE_CURRENT_PLAYLIST);
    }

}
