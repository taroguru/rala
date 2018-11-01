package workshop.soso.jickjicke.ui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.util.Utility;

public class ImageLoad extends AsyncTask<String, Void, Boolean> {
    public static final String TAG = "LoadFileABRepeatTask";

    //playlist 패칭
    private Context mContext;
    private long resourceId;
    private ImageView imageView;
    private Bitmap bm;

    public ImageLoad(Context context, ImageView view, long resourceId) {
        mContext = context;
        imageView = view;
        this.resourceId = resourceId;
        bm = null;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try
        {
            bm = Utility.getAlbumart(mContext, resourceId);
            if(bm == null)
                bm =  BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            return new Boolean(false);
        }

        return new Boolean(true);
    }

    @Override
    protected void onPostExecute(Boolean bSuccess) {
        if(bSuccess)
        {
            try{
                if(bm == null)
                    imageView.setImageResource(0);
                else
                    imageView.setImageBitmap(bm);
            }
            catch(NullPointerException e)
            {
                e.printStackTrace();
            }
        }
    }
}

