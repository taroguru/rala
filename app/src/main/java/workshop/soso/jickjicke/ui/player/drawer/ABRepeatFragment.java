package workshop.soso.jickjicke.ui.player.drawer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.LoadFileABRepeatTask;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.sound.OnPlaySoundListener;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class ABRepeatFragment extends Fragment implements OnDataSetChangedListener, OnFloatingButtonStyleChange{

	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/**
	 * Per the design guidelines, you should show the drawer on launch until the user manually
	 * expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	private OnPlaySoundListener mCallbacks;

	private RecyclerView mABRepeatListView;
	private TextView mABRepeatListEmptyTextView;
	
	private ABRepeatListArrayAdapter adapter;

	private BroadcastReceiver broadcastReceiver;

	private void initBroadCastReceiver()
	{
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(action.equals(ACTION.ChangeABRepeatList))
				{
					DLog.v("ChangeABRepeatList Received");
					onDataSetChanged();
				}
			}
		};
	}


	//화면 갱신에 대한 intent이므로 resume, pause때만 걸어둠.
	@Override
	public void onResume() {
		StateManager stateManager = Utility.getStateManager(getContext());
		LoadFileABRepeatTask loadABRepeat = new LoadFileABRepeatTask(getContext(), stateManager);
		loadABRepeat.execute();

		IntentFilter filter = new IntentFilter(ACTION.ChangeABRepeatList);
		filter.addAction(ACTION.RefreshScreen);
		LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, filter);

		super.onResume();
	}

	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		//mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, true);

		if (savedInstanceState != null) {
		//	mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
		//	mFromSavedInstanceState = true;
		}

		initBroadCastReceiver();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        final View rootView = (LinearLayout) inflater.inflate(
				R.layout.player_drawer_abrepeat_fragment, container, false);
		mABRepeatListEmptyTextView = (TextView) rootView.findViewById(R.id.empty_view);
        mABRepeatListView = (RecyclerView) rootView.findViewById(R.id.player_drawer_playlistview);
        //animation
		int resId = R.anim.layout_animation_fall_down;
		LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
		animation.setDelay(2.0f);
		mABRepeatListView.setLayoutAnimation(animation);

		ArrayList<MultiItemAdapter.Row<?>> data = new ArrayList<>();

		adapter = new ABRepeatListArrayAdapter(getActivity(), data);
        adapter.setABRepeatFragment(this);

        mABRepeatListView.setAdapter(adapter);


		return rootView;
	}

	@Override
	public void onDataSetChanged() {
		refreshAdapter();
		Utility.setEmptyText(adapter.getmRows().isEmpty(), mABRepeatListView, mABRepeatListEmptyTextView);
		adapter.notifyDataSetChanged();
	}

	private void refreshAdapter()
	{
		try{
			ArrayList<ABRepeat> abRepeatList = Utility.getABRepeatList(getContext()).getItemlist();
			ArrayList<MultiItemAdapter.Row<?>> rows = new ArrayList<>();
			int index = 0;
			for (ABRepeat abrepeat : abRepeatList) {
				MultiItemAdapter.Row row = MultiItemAdapter.Row.create(index++, ABRepeatListArrayAdapter.ABREPEATTYPE, abrepeat);
				rows.add(row);
			}
			adapter.setmRows(rows);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}

	@Override
	public void changeButton(FloatingActionButton floatingButton) {
        floatingButton.hide();  //더 헷갈려
	}
}
