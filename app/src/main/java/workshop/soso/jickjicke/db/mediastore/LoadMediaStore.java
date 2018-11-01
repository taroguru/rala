package workshop.soso.jickjicke.db.mediastore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import workshop.soso.jickjicke.ArrayItem;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.mediastore.audio.AudioArrayAdapter;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.util.DLog;

/**
 * Created by taroguru on 2017. 2. 9..
 */

public class LoadMediaStore<T extends ArrayItem> extends AsyncTask<String, Void, Boolean> {

    public interface ARTISTQUERY
    {
        String[] projection =
            {
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
            };
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String order = MediaStore.Audio.Artists.ARTIST + " ASC";
    }


    private final Context mContext;
    //mediastore query parameters

    protected AbstractMediaArrayAdapter adapter;

    protected Uri uri = ARTISTQUERY.uri;
    protected String[] projection = ARTISTQUERY.projection;
    protected String order = ARTISTQUERY.order;
    protected String selection = null;
    protected String[] selectionArgs = null;
    protected Cursor cursor;

    List<MultiItemAdapter.Row<?>> data = new ArrayList<>();

    public void setAdapter(AudioArrayAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * mediastore로드용
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param order
     */
    public void setQueryParameters(Uri uri, String[] projection, String selection, String[] selectionArgs, String order)
    {
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.order = order;
    }

    public Context getmContext() {
        return mContext;
    }

    public String[] getProjection() {
        return projection;
    }

    public void setProjection(String[] projection) {
        this.projection = projection;
    }

    public LoadMediaStore(Context context, AbstractMediaArrayAdapter adapter)
    {
        mContext = context;

        this.adapter = adapter;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        if(uri != null)
        {
            cursor = mContext.getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    order);
        }

        if(cursor != null && cursor.getCount() > 0)
        {
            DLog.v(String.format("get count : %d", cursor.getCount()));
            cursor.moveToFirst();

            int index = 0;
            do {
                Audio audio = new Audio();
                audio.setData(cursor);
                MultiItemAdapter.Row item = MultiItemAdapter.Row.create(index, AudioArrayAdapter.AUDIOTYPE, audio);
                data.add(item);
            }while(cursor.moveToNext());
        }
        else
        {
            DLog.v("No Audio File in this device.");
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        adapter.setmRows(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public String toString() {
        return "LoadMediaStore{" +
                "mContext=" + mContext +
                ", projection=" + Arrays.toString(projection) +
                ", uri=" + uri +
                ", order='" + order + '\'' +
                ", selection='" + selection + '\'' +
                ", selectionArgs=" + Arrays.toString(selectionArgs) +
                ", cursor=" + cursor +
                '}';
    }
}
