package workshop.soso.jickjicke.ui.mediastore;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.ShortTask;

/**
 * Created by taroguru on 2017. 3. 24..
 */

public abstract class BaseMediaListFragment<T extends Audio> extends BaseArrayListFragment<T> {
    public static String LOG_TAG = "AbstractMEdiaListFragment";


    protected void addAudioToPlaylist()
    {
        StateManager stateManager = (StateManager)getActivity().getApplicationContext();
        int currentPlaylistPosition = stateManager.getCurrentPlayListPosition();	//

        Collection allPlaylist = stateManager.getEntirePlayList();

        MaterialDialog.Builder playlistSelectDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.title_select_play_list )
                .items(allPlaylist)
                .itemsCallbackSingleChoice(currentPlaylistPosition, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        //stateManager.setCurrentPlayListPosition(which);
                        addPlayItemTo(which);
                        return true;
                    }
                })
                .positiveText(R.string.choice_playlist);

        playlistSelectDialog.show();
    }

    protected void addPlayItemTo(int playlistPosition)
    {
        DLog.v(String.format("add file to playlist(%d)", playlistPosition));
        StateManager stateManager = (StateManager)getActivity().getApplicationContext();
        PlayList playlist = stateManager.getPlayList(playlistPosition);
        if(playlist != null && playlist.size() >= 0)
        {
            int playPosition = playlist.size();	//추가한 첫번째 곡(추가할 플레이리스트의 다음 자리)을 재생함.
            insertSelectedFileToDB(playlistPosition);

            finishActivity(playlistPosition, playPosition);

            //((MainActivity)getActivity()).updateFileListDrawer(playlistPosition, playPosition, this);
        }
        else
        {
            ShortTask.showSnack(getContext(), getString(R.string.has_no_playlist));
            DLog.v(String.format("playlist(%d) is null or has minus size.", playlistPosition));
        }
    }


    protected void insertSelectedFileToDB(int playlistPosition){
        DLog.v(LOG_TAG, "add selected file to current playlist db");
        ContentResolver contentResolver = getActivity().getContentResolver();
        StateManager stateManager = (StateManager)getActivity().getApplicationContext();
        //선택된 아이템 정보를 찾아서 저장하기
        //1.선택된 파일 뽑고,
        ArrayList<T> checkedAudioList = getSelectedPlayItemList();

        //파일 정보를 db에 갱신 후, playItem 정보를 db에 갱신
        for(int i = 0; i < checkedAudioList.size(); ++i) {
            T audio = checkedAudioList.get(i);
            File file = audio.getFile();

            //'1. file디비에 해당 파일이 있으면 해당 아이디 땡겨옴
            Uri fileUri;
            long fileId = DBHelper.selectFileinfo(contentResolver, audio);  //fileid는 medediastore의 audioid와 동일하다

            //'2. fileinfo디비에 해당 파일이 없으면 해당 파일 정보 추가.
            if(fileId <= 0)
            {
                fileUri = DBHelper.insertFileInfo(contentResolver, audio);
                fileId = ContentUris.parseId(fileUri);
            }

            //'3. fileid를 가지고 작업을 하자
            if(fileId > 0) {
                audio.getAudioFile().setId(fileId);
                //playItem 정보를 db에 갱신
                PlayList playlist = stateManager.getEntirePlayList().get(playlistPosition);
                long playlistId = playlist.getId();

                PlayItem playItem = new PlayItem();
                //플레이아이템의 플레이리스트 아이디 갱신
                playItem.setAudio(audio);

                Uri playitemUri = DBHelper.insertPlayItem(contentResolver, playlistId, playItem);

                if(playitemUri == null)
                {
                    DLog.d(LOG_TAG, "playitem insertion failed.");
                }
                else
                {
                    DLog.d(LOG_TAG, "PlayItem is succesfully inserted to db."+playitemUri.toString());
                    DLog.d(LOG_TAG, "soundfile id : " + String.valueOf(fileId));
                    DLog.d(LOG_TAG, "playlistId id : " + String.valueOf(playlistId));
                }
            }
            else {
                DLog.e(LOG_TAG, "fileinfo db 데이터 넣기 실패");
            }
        }
    }


    protected ArrayList<T> getSelectedPlayItemList() {
        ArrayList<T> selectedFile = new ArrayList<T>();

        AbstractMediaArrayAdapter adapter = getAdapter();
        for(int i = 0; i < adapter.getCount(); ++i)
        {
            T audio = (T)adapter.getItem(i);

            if(audio.isChecked())
            {
                selectedFile.add(audio);
            }
        }

        return selectedFile;
    }


    protected void finishActivity(int playlistPosition, int playPosition) {
        Intent intent = new Intent();
        intent.putExtra(ACTION.PlaylistPosition, playlistPosition);
        intent.putExtra(ACTION.Position, playPosition);
        getActivity().setResult(AddPlayitemActivity.RESULT_CODE_ADD_ITEM, intent);
        getActivity().finish();
    }

    //껍때기. adapter를 가져오기 위한 아이.
    public abstract AbstractMediaArrayAdapter getAdapter();

}
