package workshop.soso.jickjicke.ui.mediastore.folder;

import android.content.Context;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 5. 8..
 */

public class FileItemViewHolder extends BaseViewHolder<Audio> {
    private Context context;
    private CardView entireCardView;
    private ImageView imgAlbum;
    private TextView txtTitle;
    private TextView txtArtist;
    private TextView txtDuration;
    private CardView moreButtonClickArea;

    public FileItemViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
        entireCardView  = itemView.findViewById(R.id.mediastore_arrayitem_audiofile);
        imgAlbum        = itemView.findViewById(R.id.imgAlbum);
        txtTitle        = itemView.findViewById(R.id.txtTitle);
        txtArtist       = itemView.findViewById(R.id.txtArtist);
        txtDuration     = itemView.findViewById(R.id.txtDuration);
        moreButtonClickArea = itemView.findViewById(R.id.imgMoreButtonClickArea);
    }

    @Override
    public void onBindView(Audio playItem, int position) {
        onBind(position, playItem);
    }

    public void onBind(int position, Audio item) {
        //txtIndex.setText(String.valueOf(position+1) );
        //album image 삽입
        setAlbumImage(context, imgAlbum, item.getAlbumid());

        txtTitle.setText(String.valueOf(position+1) + "." + item.getName());
        txtArtist.setText(">> " + item.getArtist());
        txtDuration.setText(Utility.convertMsecToMin( (int)item.getDuration() ));

    }

    public CardView getImgMoreButtonClickArea() {
        return moreButtonClickArea;
    }

    public CardView getEntireCardView() {
        return entireCardView;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void clearAnimation()
    {
        entireCardView.clearAnimation();
    }
}
