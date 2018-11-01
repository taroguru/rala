package workshop.soso.jickjicke.ui.util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder<ITEM> extends RecyclerView.ViewHolder {

    protected ImageLoad loadAlbumImgTask = null;

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindView(ITEM item, int position);

    public void clearAnimation(){
        itemView.clearAnimation();
    }

    public void setAlbumImage(Context context, ImageView imgAlbum, long albumId) {
        imgAlbum.setImageResource(0); //바인딩 전 이전 이미지 출력을 위한
        loadAlbumImgTask = new ImageLoad(context, imgAlbum, albumId);
        loadAlbumImgTask.execute();
    }

    public void cancelLoadImage()
    {
        if(loadAlbumImgTask != null )
            loadAlbumImgTask.cancel(true);
    }
}