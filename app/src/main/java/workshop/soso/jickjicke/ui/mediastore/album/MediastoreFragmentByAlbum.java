package workshop.soso.jickjicke.ui.mediastore.album;

import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import workshop.soso.jickjicke.Album;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.mediastore.LoadMediaStore;
import workshop.soso.jickjicke.sound.OnPlaySoundListener;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.mediastore.BaseArrayListFragment;
import workshop.soso.jickjicke.ui.mediastore.FragmentChangeListener;
import workshop.soso.jickjicke.util.DLog;


public class MediastoreFragmentByAlbum extends BaseArrayListFragment<Album> implements OnClickListener{
	public static final String FRAGMENT_ID = "123";
	private static final String LOG_TAG = "AlbumList";
	private StateManager stateManager;
	private RecyclerView mediaListView = null;
    private MediaArrayAdapterByAlbum adapter = null;

	private OnPlaySoundListener playSoundListener;

    private Button buttonSelectAll;
	private Button buttonBack;
	//private Button buttonPlay;
    private Button buttonAdd;
	private Button buttonClose;

	public void setPlaySoundListener(OnPlaySoundListener listener)
	{
		playSoundListener = listener;
	}

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.mediastore_fragment_by_album, container, false);

		stateManager = (StateManager)getActivity().getApplicationContext();

		adapter = new MediaArrayAdapterByAlbum(getActivity());

		LoadMediaStore<Album> loadTask = new LoadMediaStore<Album>(getActivity(), adapter);
		loadTask.setQueryParameters(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.ALBUM + " ASC");
		loadTask.execute();

		mediaListView = (RecyclerView) rootView.findViewById(R.id.albumlist);
		mediaListView.setAdapter(adapter);

		mediaListView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Album album = (Album)adapter.getItem(0);	//todo 나중에 포지션 수정
				fragmentChageListener.onSwitchToNextFragment(album);
			}
		});


        //buttonSelectAll = (Button) rootView.findViewById(R.id.ButtonSelectAll);
        //buttonSelectAll.setOnClickListener(this);

		//buttonBack= (Button) rootView.findViewById(R.id.ButtonBack);
		//buttonBack.setOnClickListener(this);

//        buttonAdd = (Button) rootView.findViewById(R.id.ButtonAdd);
//        buttonAdd.setOnClickListener(this);
//
//		buttonClose = (Button) rootView.findViewById(R.id.ButtonClose);
//		buttonClose.setOnClickListener(this);


		getActivity().supportInvalidateOptionsMenu();


		return rootView;
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		DLog.v("Button Clicked"+v.toString());
		switch(v.getId())
		{
////            case R.id.ButtonSelectAll:
////				//todo. select all
////				checkAllPlayableItem();
////
//            break;
			//현재 플레이리스트 지우고 선택된 파일로s
			//clearCurrentPlaylistDB();
//            case R.id.ButtonPlay:
//                insertSelectedFileToDB();
//				((MainActivity)getActivity()).updateFileListDrawer();
//
//
//			break;
            //동작 추가하기
//            case R.id.ButtonAdd:
//				addAudioToPlaylist();
//				break;
//			case R.id.ButtonBack:
//				//한 패스 위로 가기
//				File parentPath = stateManager.getCurrentPath().getParentFile();
//				if(parentPath != null){
//				//	changeCurrent(v, parentPath);
//				}
//				else
//				{
//					DLog.v(LOG_TAG, "parent path is null.");
//				}
//            break;
//			case R.id.ButtonClose://닫아부려.
//				getActivity().finishActivity(AddPlayitemActivity.RESULT_CODE_CLOSE);
//				break;
		}
	}

	private void checkAllPlayableItem() {
		adapter.checkAll();
		adapter.notifyDataSetChanged();

	}

	private FragmentChangeListener fragmentChageListener;

	public FragmentChangeListener getFragmentChageListener() {
		return fragmentChageListener;
	}

	public void setFragmentChageListener(FragmentChangeListener fragmentChageListener) {
		this.fragmentChageListener = fragmentChageListener;
	}

	public static Fragment newInstance(FragmentChangeListener fragmentChangeListener) {
		Bundle bundle = new Bundle();
		MediastoreFragmentByAlbum fragment = new MediastoreFragmentByAlbum();
		fragment.setFragmentChageListener(fragmentChangeListener);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public AbstractMediaArrayAdapter getAdapter() {
		return adapter;
	}
}
