package workshop.soso.jickjicke.db.mediastore;

import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import workshop.soso.jickjicke.ArrayItem;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.AudioFile;
import workshop.soso.jickjicke.AudioFolder;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.mediastore.folder.MediaArrayAdapterByFolder;
import workshop.soso.jickjicke.ui.player.drawer.OnDataSetChangedListener;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.Utility;

import static android.R.attr.data;

/**
 * Created by taroguru on 2017. 2. 9..
 */

public class LoadMediaStoreGroupByFolder<T extends ArrayItem> extends LoadMediaStore<T> {

    //private HashMap<String, MediaArrayAdapterByFolder.Item> audioFolderMap;
    private ArrayList<MultiItemAdapter.Row<?>> dataList;
    private MediaArrayAdapterByFolder adapter;


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


    public String[] getProjection() {
        return projection;
    }

    public void setProjection(String[] projection) {
        this.projection = projection;
    }


    public LoadMediaStoreGroupByFolder(Context context, MediaArrayAdapterByFolder adapter)
    {
        super(context, adapter);
        this.adapter =   adapter;
        dataList = new ArrayList<>();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if(uri != null)
        {
            cursor = getmContext().getContentResolver().query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    order);
            DLog.v(toString());
            DLog.v(String.format("get count : %d", cursor.getCount()));

          //  adapter.resetData(cursor);
            int folderIndex = 0;
            //group by folder
            for(boolean hasItem = cursor.moveToFirst();hasItem; hasItem = cursor.moveToNext())
            {
                Audio audio = new Audio();
                audio.setData(cursor);
                String fullPath = Audio.getPath(cursor);
                if(fullPath != null && !fullPath.isEmpty())
                {
                    String folderPath = Utility.parseFolderPath(fullPath);
                    MultiItemAdapter.Row folderItem =  findAudioFolder(folderPath);

                    AudioFolder audioFolder = null;
                    //1. add new folder
                    if(folderItem == null)
                    {
                        String name = Utility.getNameFromFolderPath(folderPath);

                        audioFolder = new AudioFolder();
                        audioFolder.setName(name);
                        audioFolder.setPath(folderPath);
                        audioFolder.setNumber_of_song(0);   //first item is find!

                        folderItem = MultiItemAdapter.Row.create(0,  MediaArrayAdapterByFolder.FOLDERTYPE, audioFolder);
                        folderItem.setIndex(folderIndex++);
                        dataList.add(folderItem );

                    }
                    else
                    {
                        audioFolder = (AudioFolder) folderItem.getItem();
                    }

                    //2. add child
                    audioFolder.increaseNumberOfSong();
                    audioFolder.addChild(audio);
                    MultiItemAdapter.Row<?> fileItem = MultiItemAdapter.Row.create(0,  MediaArrayAdapterByFolder.FILETYPE, audio);
                    fileItem.setIndex( audioFolder.getNumber_of_song()-1);
                    folderItem.addChild(fileItem);
                    folderItem.setItem(audioFolder);
                }
            }
        }

        return true;
    }

    private MultiItemAdapter.Row<?> findAudioFolder(String folderPath) {
        MultiItemAdapter.Row<?> audioFolder = null;
        for(MultiItemAdapter.Row<?> currentitem : dataList) {

            if (((AudioFolder)currentitem.getItem()).getPath().equals(folderPath)) {
                audioFolder = currentitem;
                break;
            }
        }
        return audioFolder;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        DLog.v(toString());
        adapter.setmRows(dataList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public String toString() {
        return "LoadMediaStoreGroupByFolder{" +
                "arrayAdapter=" + adapter +
                ", mContext=" + getmContext() +
                ", projection=" + Arrays.toString(projection) +
                ", uri=" + uri +
                ", order='" + order + '\'' +
                ", selection='" + selection + '\'' +
                ", selectionArgs=" + Arrays.toString(selectionArgs) +
                ", cursor=" + cursor +
                '}';
    }
}
