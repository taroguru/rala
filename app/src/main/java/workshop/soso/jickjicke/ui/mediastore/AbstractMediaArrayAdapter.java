package workshop.soso.jickjicke.ui.mediastore;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import workshop.soso.jickjicke.ArrayItem;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 2. 11..
 */

abstract public class AbstractMediaArrayAdapter<T extends ArrayItem> extends MultiItemAdapter {
    private Context context;
    protected int lastPosition = -1;

    public int getLastPosition() {
        return lastPosition;
    }

    public void initLastPosition(){
        setLastPosition(-1);
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }
    //private ArrayList<T> values;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public AbstractMediaArrayAdapter(Context context)
    {
        this.context = context;
    }

    public AbstractMediaArrayAdapter(Context context, List<Row<?>> values) {
        super();
        this.context = context;
        setmRows(values);
    }

    public void resetData(Cursor cursor) {
        List<Row<?>> data = new ArrayList<>();
        if(cursor != null)
        {
            DLog.v(String.format("Queried Data Number = %d", cursor.getCount()));
            int i = 0;
            for(boolean hasItem = cursor.moveToFirst();hasItem; hasItem = cursor.moveToNext())
            {
                T abstractAudio = getNewChild();
                //abstractAudio.setData(cursor);
                DLog.v(abstractAudio.toString());
                Row row = Row.create(i++, 0, abstractAudio);
                data.add(row);
            }
        }

        if(getmRows() != null)
        {
            getmRows().clear();
        }
        setmRows(data);
    }

    public T getNewChild()
    {
        return null;
    }

    public void insertToCurrentPlaylistAndPlay(Audio audio) {

        DBHelper.insertFileInfo(getContext().getContentResolver(), audio);
        Uri insertedPlayItemUri = DBHelper.insertPlayItemToCurrentPlaylist(getContext(), audio);
        PlayItem playitem = DBHelper.loadPlayItem(context, insertedPlayItemUri);

        if(playitem != null)
        {
            playitem.setAudio(audio);

            PlayList currentPlaylist = Utility.getCurrentPlayList(context);
            currentPlaylist.add(playitem);
            //빠른 피드백을 위해서 재생을 먼저하고 아이템을 추가하자.
            Utility.sendBroadcastPlayLastItem(context);

            Intent intent = new Intent(ACTION.AddPlayItemToCurrentPlayList);
            intent.putExtra(EXTRA_VALUE.PlayItem, playitem);
            intent.putExtra(EXTRA_VALUE.Audio, playitem.getAudio());
            Utility.sendIntentLocalBroadcast(context, intent);
        }

    }


    protected void setAnimation(View viewToAnimate, int position)
    {
        Animation animation = null;
        if(lastPosition <= position)	//스크롤 다운
        {
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_animation_rise_up);
            animation.setDuration(CONSTANTS.DURATION_LISTITEM_VISIBLE_SCROLL_DOWN);
        }
        else
        {
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_animation_fall_down);
            animation.setDuration(CONSTANTS.DURATION_LISTITEM_VISIBLE_SCROLL_UP);
        }
        viewToAnimate.startAnimation(animation);
        lastPosition = position;
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
        holder.clearAnimation();
        holder.cancelLoadImage();
    }



}
