package workshop.soso.jickjicke.ui.mediastore.playlist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.db.LoadAllPlayListAtInitTask;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.permission.Permissions;
import workshop.soso.jickjicke.sound.OnPlaySoundListener;
import workshop.soso.jickjicke.ui.player.drawer.OnDataSetChangedListener;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.PlayListHelper;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;

public class PlayListFragment extends Fragment implements OnDataSetChangedListener, OnFloatingButtonStyleChange {

    /**
     * Remember the position of the selected item.
     */
    public static final String TAG = PlayListFragment.class.toString();
    private OnPlaySoundListener mCallbacks;

    private RelativeLayout mDrawerLinearLayout;
    private TextView mEmptyTextView;
    private RecyclerView mPlaylistView;

    private PlayListArrayAdapter adapter;
    private FloatingActionButton floatingButton = null;

    public PlayListArrayAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(PlayListArrayAdapter adapter) {
        this.adapter = adapter;
    }

    public PlayListFragment() {

    }

    public static PlayListFragment newInstance() {
        Bundle args = new Bundle();
        PlayListFragment playlistFragment = new PlayListFragment();
        playlistFragment.setArguments(args);
        return playlistFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (action.equals(ACTION.PlayNewItem)) {
                    //todo. 해당 위치까지 스크롤 밀기.
//                  int playlistIndex = intent.getIntExtra(ACTION.PlaylistPosition, 0);
//                  int position = intent.getIntExtra(ACTION.Position, 0);
//					long packedposition = mPlaylistView.getPackedPositionForChild(playlistIndex, position);
//					DLog.v(String.format("scroll to [%d/%d]. packedposition = %d", playlistIndex, position, packedposition));
//					mPlaylistView.expandGroup(playlistIndex);
//					mPlaylistView.smoothScrollToPositionFromTop((int)packedposition,0,500);
//					adapter.notifyDataSetChanged();
                } else if (action.equals(ACTION.RefreshScreen)) {
//					adapter.notifyDataSetChanged();
                } else if (action.equals(ACTION.AddNewPlayItem)) {
//					PlayItem playitem = (PlayItem)intent.getSerializableExtra(EXTRA_VALUE.PlayItem);
//					int playlistPosition = intent.getIntExtra(EXTRA_VALUE.PlayListPosition, 0);
//
//					adapter.addChild(playlistPosition, playitem);
//					if(adapter.getCount() == 1)	//최초 등록시점에서 emptyview 날림
//					{
                    onDataSetChanged();
//					}
                } else if (action.equals(ACTION.AddPlayList)) {
                    PlayList playList = (PlayList) intent.getSerializableExtra(EXTRA_VALUE.PlayList);
                    adapter.addChild(playList);
                    if (adapter.getCount() == 1)//최초 등록시점에서 emptyview 날림
                    {
                        onDataSetChanged();
                    }
                }
            }
        };
    }

    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ACTION.PlayNewItem);
        intentFilter.addAction(ACTION.RefreshScreen);
        intentFilter.addAction(ACTION.AddPlayList);
        intentFilter.addAction(ACTION.AddNewPlayItem);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

    }

    private View rootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.mediastore_fragment_by_playlist, container, false);

        mDrawerLinearLayout = (RelativeLayout) rootView;

        //0. 리스트 어뎁터 구성.
        adapter = new PlayListArrayAdapter(getActivity(), new ArrayList<>());
        adapter.setPlaylistFragment(this);

        mPlaylistView = (RecyclerView) mDrawerLinearLayout.findViewById(R.id.playlistRecyclerview);
        mPlaylistView.setAdapter(adapter);

        mEmptyTextView = (TextView) mDrawerLinearLayout.findViewById(R.id.empty_view);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LoadAllPlayListAtInitTask loadTask = new LoadAllPlayListAtInitTask(getContext(), this);
        loadTask.execute();
    }

    //
//
//	private void selectItem(int groupposition, int position) {
//
//		mCurrentSelectedGroupPosition = groupposition;
//		mCurrentSelectedPosition = position;
//		if (mPlaylistView != null) {
//			int index = mPlaylistView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupposition, position));
//			mPlaylistView.setItemChecked(index, true);
//
//		}
//		if (mCallbacks != null) {
//			playSoundTrack(groupposition, position);
//		}
//	}

//	private void playSoundTrack(int groupposition, int position)
//	{
//		mCallbacks.onPlaySoundTrack(groupposition, position);
//	}

    @Override
    public void onDataSetChanged() {
        adapter.onDataSetChanged();
        Utility.setEmptyText(adapter.isEmpty(), mPlaylistView, mEmptyTextView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == Permissions.EXTERNAL_WRITE) {
            DLog.v("external_write permission result");
            if (permissions != null && permissions.length >= 1) {
                String permission = permissions[0];
                int grantResult = grantResults[0];

                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    showFloatingButton(floatingButton);
                    DLog.v("external_write . permission granted");
                } else {
                    DLog.v("external_write . show request permissions");
                    //todo. rational
                }
            } else {
                DLog.v("external_write permissions is null");
            }


        }
    }

    public static void showAddPlaylistDiag(final Context context) {
        showAddPlaylistDiag(context, null);
    }

    public static void showAddPlaylistDiag(final Context context, final Audio audio) {

        final EditText playlistNameEditText;
        DLog.v("Floating Button pushed. addplaylistdiag");

        DLog.v("Write_external_storage permission had granted.");
        AlertDialog.Builder diagBuilder;
        playlistNameEditText = new EditText(context);
        diagBuilder = new AlertDialog.Builder(context);
        diagBuilder.setTitle(context.getString(R.string.addplaylist));
        diagBuilder.setMessage(context.getString(R.string.addPlaylistMsg));
        diagBuilder.setView(playlistNameEditText);
        diagBuilder.setPositiveButton(R.string.add_dialogbutton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //중복 확인
                String name = playlistNameEditText.getText().toString();
                StateManager stateManager = (StateManager) context.getApplicationContext();
                if (stateManager.hasPlaylist(name)) {
                    ShortTask.showSnack(context, context.getString(R.string.has_same_playlist_name));
                    DLog.i(TAG, "onClick: already has same playlist.");
                } else {

                    //playlist 생성하고 메모리, 디비에 저장하자.
                    PlayList playlist = new PlayList();
                    playlist.setName(name);

                    long id = DBHelper.insertPlayList(context, playlist);
                    playlist.setId(id);

                    stateManager.addPlaylist(playlist);

                    Intent intent = new Intent();
                    intent.setAction(ACTION.AddPlayList);
                    intent.putExtra(EXTRA_VALUE.PlayList, playlist);
                    Utility.sendIntentLocalBroadcast(context, intent);
                    //adapter.addChild(playlist);

                    if (audio != null) {
                        PlayListHelper.addPlayItemTo(context, 0, audio);
                    }
                    dialog.dismiss();    //추가에 성공했을 때만 dismiss 시키자.

                }
            }
        });
        diagBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog playitemAddDiag = diagBuilder.show();
        final Button positiveButton = playitemAddDiag.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);
        playlistNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    positiveButton.setEnabled(false);
                } else {
                    positiveButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void changeButton(FloatingActionButton floatingButton) {
        try {
            this.floatingButton = floatingButton;
            int permissionCheck = ContextCompat.checkSelfPermission((Activity)getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                //권한 없음
                DLog.v("Write_external_storage permission had denied.");
                //floating button이 activity안에 있으므로 requestpermissions도 activitycompat을 이용해 호출해준다
                if (isAdded()) {
                    this.requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Permissions.EXTERNAL_WRITE);
                } else {
                    DLog.v("This Fragment is not added");
                }
            } else {    //권한 있음
                showFloatingButton(floatingButton);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showFloatingButton(FloatingActionButton floatingButton) {
        try{
            //change icon
            floatingButton.hide();
            Drawable searchIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_add_white_30dp);
            floatingButton.setImageDrawable(searchIcon);
            floatingButton.show();

            //add playlist dialog
            floatingButton.setOnClickListener(v -> showAddPlaylistDiag(getContext()));
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
