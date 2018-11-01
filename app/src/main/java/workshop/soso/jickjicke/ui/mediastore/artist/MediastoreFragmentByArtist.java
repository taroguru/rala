//package workshop.soso.jickjicke.ui.mediastore.artist;
//
//import android.os.Bundle;
//import androidx.fragment.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.Button;
//import android.widget.ListView;
//
//import java.util.ArrayList;
//
//import workshop.soso.jickjicke.Artists;
//import workshop.soso.jickjicke.R;
//import workshop.soso.jickjicke.StateManager;
//import workshop.soso.jickjicke.db.mediastore.LoadMediaStore;
//import workshop.soso.jickjicke.sound.OnPlaySoundListener;
//import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
//import workshop.soso.jickjicke.ui.mediastore.AddPlayitemActivity;
//import workshop.soso.jickjicke.ui.mediastore.BaseArrayListFragment;
//import workshop.soso.jickjicke.ui.mediastore.FragmentChangeListener;
//import workshop.soso.jickjicke.util.DLog;
//
//
//public class MediastoreFragmentByArtist extends BaseArrayListFragment<Artists> implements OnClickListener{
//	public static final String FRAGMENT_ID = "123";
//	private static final String LOG_TAG = "MediastoreFragmentByArtist";
//	private StateManager stateManager;
//	private ListView mediaListView = null;
//    private MediaArrayAdapterByArtist adapter = null;
//
//	private OnPlaySoundListener playSoundListener;
//
//    private Button buttonSelectAll;
//	private Button buttonBack;
//	//private Button buttonPlay;
//    private Button buttonAdd;
//	private Button buttonClose;
//
//	public void setPlaySoundListener(OnPlaySoundListener listener)
//	{
//		playSoundListener = listener;
//	}
//
//    @Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//	}
//
//	@Override
//	public void onActivityCreated (Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		// Indicate that this fragment would like to influence the set of actions in the action bar.
//		setHasOptionsMenu(true);
//
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		View rootView = inflater.inflate(R.layout.mediastore_fragment_by_artist, container, false);
//
//		stateManager = (StateManager)getActivity().getApplicationContext();
//		String currentPath = stateManager.getCurrentPath().toString();
//		adapter = new MediaArrayAdapterByArtist(getActivity(), new ArrayList<Artists>());
//
//		LoadMediaStore<Artists> loadTask = new LoadMediaStore<Artists>(getActivity(), adapter);
//		loadTask.execute();
//
//		mediaListView = (ListView)rootView.findViewById(R.id.medialist);
//		mediaListView.setAdapter(adapter);
//
//
//		mediaListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//			//todo. 해당 artist에 대한 전체 곡목 로드
//			Artists artists = (Artists)adapter.getItem(position);
//			fragmentChageListener.onSwitchToNextFragment(artists);
//			}
//
//		});
//
//
//
//        //buttonSelectAll = (Button) rootView.findViewById(R.id.ButtonSelectAll);
//        //buttonSelectAll.setOnClickListener(this);
//
//		//buttonBack= (Button) rootView.findViewById(R.id.ButtonBack);
//		//buttonBack.setOnClickListener(this);
//
//        buttonAdd = (Button) rootView.findViewById(R.id.ButtonAdd);
//        buttonAdd.setOnClickListener(this);
//
//		buttonClose = (Button) rootView.findViewById(R.id.ButtonClose);
//		buttonClose.setOnClickListener(this);
//
//
//		getActivity().supportInvalidateOptionsMenu();
//
//
//		return rootView;
//	}
//
//
//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		DLog.v("Button Clicked"+v.toString());
//		switch(v.getId())
//		{
//            case R.id.ButtonSelectAll:
//				//todo. select all
//				checkAllPlayableItem();
////
//            break;
//			//현재 플레이리스트 지우고 선택된 파일로s
//			//clearCurrentPlaylistDB();
////            case R.id.ButtonPlay:
////                insertSelectedFileToDB();
////				((MainActivity)getActivity()).updateFileListDrawer();
////
////
////			break;
//            //동작 추가하기
////            case R.id.ButtonAdd:
////				addAudioToPlaylist();
////				break;
////			case R.id.ButtonBack:
////				//한 패스 위로 가기
////				File parentPath = stateManager.getCurrentPath().getParentFile();
////				if(parentPath != null){
////				//	changeCurrent(v, parentPath);
////				}
////				else
////				{
////					DLog.v(LOG_TAG, "parent path is null.");
////				}
////            break;
//			case R.id.ButtonClose://닫아부려.
//				getActivity().finishActivity(AddPlayitemActivity.RESULT_CODE_CLOSE);
//				break;
//		}
//	}
//
//	private void checkAllPlayableItem() {
//		adapter.checkAll();
//		adapter.notifyDataSetChanged();
//
//	}
//
//	private FragmentChangeListener fragmentChageListener;
//
//	public FragmentChangeListener getFragmentChageListener() {
//		return fragmentChageListener;
//	}
//
//	public void setFragmentChageListener(FragmentChangeListener fragmentChageListener) {
//		this.fragmentChageListener = fragmentChageListener;
//	}
//
//	public static Fragment newInstance(FragmentChangeListener fragmentChangeListener) {
//		Bundle bundle = new Bundle();
//		MediastoreFragmentByArtist fragment = new MediastoreFragmentByArtist();
//		fragment.setFragmentChageListener(fragmentChangeListener);
//		fragment.setArguments(bundle);
//
//		return fragment;
//	}
//
//	@Override
//	public AbstractMediaArrayAdapter getAdapter() {
//		return adapter;
//	}
//}
