package workshop.soso.jickjicke.ui.currentlist;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 5. 8..
 */
//http://tosslab.github.io/android/2016/04/09/MultiItemRecyclerAdapter.html
public class CurrentPlayItemViewHolder extends BaseViewHolder<PlayItem> {
    private Context context;
    private CardView entireCardView;
    private ImageView imgAlbum;
    private TextView txtTitle;
    private TextView txtArtist;
    private TextView txtDuration;
    private CardView moreButtonClickArea;

    public CurrentPlayItemViewHolder(View itemView) {
        super(itemView);
        entireCardView  = itemView.findViewById(R.id.currentplaylist_item);
        imgAlbum        = itemView.findViewById(R.id.imgAlbum);
        txtTitle        = itemView.findViewById(R.id.txtTitle);
        txtArtist       = itemView.findViewById(R.id.txtArtist);
        txtDuration     = itemView.findViewById(R.id.txtDuration);
        moreButtonClickArea = itemView.findViewById(R.id.imgMoreButtonClickArea);
    }

    @Override
    public void onBindView(PlayItem playItem, int position) {
        onBind(position, playItem);
    }

    public void onBind(int position, PlayItem item) {
        //txtIndex.setText(String.valueOf(position+1) );
        //album image 삽입
        setAlbumImage(context, imgAlbum, item.getAudio().getAlbumid());

        txtTitle.setText(String.valueOf(position+1) + "." + item.getName());
        txtArtist.setText(">> " + item.getArtist());
        txtDuration.setText(Utility.convertMsecToMin( (int)item.getDuration() ));

    }

    public CardView getMoreButtonClickArea() {
        return moreButtonClickArea;
    }

    public CardView getEntireCardView() {
        return entireCardView;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

}
