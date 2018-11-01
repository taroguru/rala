package workshop.soso.jickjicke.ui.mediastore.folder;

import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import workshop.soso.jickjicke.AudioFolder;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.mediastore.LoadMediaStoreGroupByFolder;
import workshop.soso.jickjicke.ui.player.drawer.OnDataSetChangedListener;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.GUIHelper;
import workshop.soso.jickjicke.util.Utility;

public class MediastoreFragmentByFolder extends Fragment implements OnDataSetChangedListener, OnFloatingButtonStyleChange{
	public static final String FRAGMENT_ID = "MediastoreFragmentByFolder";
	private static final String LOG_TAG = "AudioFolderList";
	private static final String KEY_ADAPTER = "KEY_ADAPTER";
	private RecyclerView mediaListView = null;
	private TextView mediaListEmptyView = null;
    private MediaArrayAdapterByFolder adapter = null;
    public MultiItemAdapter getAdapter(){return adapter;}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//state restore
//		if(savedInstanceState == null)
//		{
			adapter = new MediaArrayAdapterByFolder(getContext(), new ArrayList<>());
//		}
//		else
//		{
//			adapter = (MediaArrayAdapterByFolder) savedInstanceState.getSerializable(KEY_ADAPTER);
//		}

		//create view
		View rootView = inflater.inflate(R.layout.mediastore_fragment_by_audiofolder, container, false);


		mediaListView = (RecyclerView) rootView.findViewById(R.id.audiofolderlist);
		mediaListView.setAdapter(adapter);
		//todo : extendable view에 대한 item animator
//		ItemAnimator itemAnimator = new ItemAnimator();
//		mediaListView.setItemAnimator(itemAnimator);

		mediaListEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		LoadMediaStoreGroupByFolder<AudioFolder> loadTask = new LoadMediaStoreGroupByFolder<AudioFolder>(getActivity(), adapter);
		loadTask.setQueryParameters(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DATA  + " ASC");
		loadTask.execute();
	}

	public static Fragment newInstance() {
		Bundle bundle = new Bundle();
		MediastoreFragmentByFolder fragment = new MediastoreFragmentByFolder();
		fragment.setArguments(bundle);

		return fragment;
	}

    @Override
    public void onDataSetChanged() {
		Utility.setEmptyText(adapter.getmRows().isEmpty(), mediaListView, mediaListEmptyView);
		adapter.notifyDataSetChanged();
    }

	@Override
	public void changeButton(FloatingActionButton floatingButton) {
		GUIHelper.changeFloatingButtonToSearch(getContext(), floatingButton, CONSTANTS.PAGE_FOLDER_LIST);
	}

}
