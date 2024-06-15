package workshop.soso.jickjicke.ui.mediastore.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.appcompat.widget.PopupMenu;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.PopupHelper;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.PlayListHelper;
import workshop.soso.jickjicke.util.Utility;

public class AudioArrayAdapter extends AbstractMediaArrayAdapter {

    public static final int AUDIOTYPE = 1;
    public static final int CHILD = AUDIOTYPE;


    public AudioArrayAdapter(Context context, ArrayList<Row<?>> data) {
        super(context);
        setmRows( data );
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = null;
        BaseViewHolder vh = null;

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.mediastore_arrayitem_audio, parent, false);
        vh = new AudioViewHolder(view, getContext());
        return vh;
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        final Audio audio = getItem(position);
        AudioViewHolder audioViewHolder = (AudioViewHolder) holder;
        audioViewHolder.setContext(getContext());
        audioViewHolder.onBindView(audio, position);

        audioViewHolder.getEntireCardView().setOnClickListener( v -> {
            //현재 재생목록에 추가하긔.
            insertToCurrentPlaylistAndPlay(audio);
            Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_ALL_AUDIO_LIST), CONSTANTS.EVENT_LISTITEM_CLICK);
        });

        audioViewHolder.getEntireCardView().setOnLongClickListener(v -> {
            showChildPopupMenu(v, audio);
            Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_ALL_AUDIO_LIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK);
            return false;
        });

        audioViewHolder.getImgMoreButtonClickArea().setOnClickListener(v ->  {
            showChildPopupMenu(v, audio);
            Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_ALL_AUDIO_LIST), CONSTANTS.EVENT_LISTITEM_LONGCLICK);
        });

        setAnimation(holder.itemView, position);
    }


    @SuppressLint("NonConstantResourceId")
    public void showChildPopupMenu(final View view, final Audio audio)
    {
        final Context context = getContext();
        PopupMenu popup = new PopupMenu(context, view);

        ((MainActivity)(context)).getMenuInflater().inflate(R.menu.media_audio_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            final int itemId = item.getItemId();
            if (itemId == R.id.menu_add) {
                String msg = String.format("add button clicked");
                DLog.v(msg);
                PlayListHelper.showAudioToPlaylistDialog(getContext(), audio);
            }
            return false;
        });
        PopupHelper.showPopupWithIcon(context, popup, view);
    }
}

