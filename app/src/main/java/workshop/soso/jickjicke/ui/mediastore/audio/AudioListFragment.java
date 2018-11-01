package workshop.soso.jickjicke.ui.mediastore.audio;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.mediastore.LoadMediaStore;
import workshop.soso.jickjicke.ui.player.drawer.OnDataSetChangedListener;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.ui.util.OnFloatingButtonStyleChange;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.GUIHelper;
import workshop.soso.jickjicke.util.Utility;

import static workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment.ARGUMENT.ORDER;
import static workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment.ARGUMENT.PROJECTION;
import static workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment.ARGUMENT.SELECTOR;
import static workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment.ARGUMENT.SELECTORARGS;
import static workshop.soso.jickjicke.ui.mediastore.audio.AudioListFragment.ARGUMENT.URI;

/**
 * Created by taroguru on 2017. 2. 11..
 */

public class AudioListFragment extends Fragment implements OnDataSetChangedListener, OnFloatingButtonStyleChange {//<Audio> implements OnDataSetChangedListener {
    public static final String FRAGMENT_ID = "ArtistListFragment";
    private Uri uri;
    private String[] projection;
    private String selector;
    private String[] selectorArgs;
    private String order;

    //private String title;
    private TextView       emptyTextView;
    protected RecyclerView listview;
    protected AudioArrayAdapter adapter;

    public AudioArrayAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(AudioArrayAdapter adapter) {
        this.adapter = adapter;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String[] getProjection() {
        return projection;
    }

    public void setProjection(String[] projection) {
        this.projection = projection;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String[] getSelectorArgs() {
        return selectorArgs;
    }

    public void setSelectorArgs(String[] selectorArgs) {
        this.selectorArgs = selectorArgs;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public interface ARGUMENT{
        String URI = "URI";
        String PROJECTION = "PROJECTION";
        String SELECTOR = "SELECTOR";
        String SELECTORARGS = "SELECTORARGS";
        String ORDER = "ORDER";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        DLog.v("");
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            uri = (Uri) bundle.getParcelable(URI);
            projection = bundle.getStringArray(PROJECTION);
            selector = bundle.getString(SELECTOR);
            selectorArgs = bundle.getStringArray(SELECTORARGS);
            order = bundle.getString(ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mediastore_fragment_for_audio, container, false);

        adapter =  new AudioArrayAdapter(getActivity(), new ArrayList<MultiItemAdapter.Row<?>>());

        listview = (RecyclerView) rootView.findViewById(R.id.audiolist);
        listview.setAdapter(adapter);
        emptyTextView = (TextView) rootView.findViewById(R.id.empty_view);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LoadMediaStore<Audio> loadTask = new LoadMediaStore(getActivity(), adapter);
        loadTask.setQueryParameters(uri, projection, selector, selectorArgs, order);
        loadTask.execute();
    }

    public static AudioListFragment newInstance(Uri uri, String[] projection, String selector, String[] selectorArgs, String order) {
        Bundle args = new Bundle();
        args.putParcelable(URI, uri);
        args.putStringArray(PROJECTION, projection);
        args.putString(SELECTOR, selector);
        args.putStringArray(SELECTORARGS, selectorArgs);
        args.putString(ORDER, order);
        AudioListFragment fragment = new AudioListFragment();
        fragment.setArguments(args);

        fragment.setUri(uri);
        fragment.setProjection(projection);
        fragment.setSelector(selector);
        fragment.setSelectorArgs(selectorArgs);
        fragment.setOrder(order);
        return fragment;
    }

    @Override
    public void onDataSetChanged() {
        Utility.setEmptyText(adapter.getmRows().isEmpty(), listview, emptyTextView);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void changeButton(FloatingActionButton floatingButton) {
        GUIHelper.changeFloatingButtonToSearch(getContext(), floatingButton, CONSTANTS.PAGE_ALL_AUDIO_LIST);
    }
}
