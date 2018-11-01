package workshop.soso.jickjicke.ui.mediastore;

import android.content.Intent;
import androidx.fragment.app.Fragment;

import workshop.soso.jickjicke.ArrayItem;
import workshop.soso.jickjicke.intent.ACTION;

/**
 * Created by taroguru on 2017. 3. 24..
 */

public abstract class BaseArrayListFragment<T extends ArrayItem> extends Fragment {
    public static String LOG_TAG = "BaseArrayListFragment";

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
