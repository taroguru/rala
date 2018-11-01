package workshop.soso.jickjicke.ui.mediastore.playlist;

import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.util.ExtendViewHolder;

/**
 * Created by taroguru on 2017. 5. 8..
 */

public class PlaylistItemViewHolder extends ExtendViewHolder<PlayList> {

    private TextView txtIndex;
    private ImageView imgExpandToggle;
    private TextView txtName;
    private TextView txtDescription;
    private ImageView imgPlayButton;
    private ImageView imgMoreButton;
    private CardView imgPlayButtonClickArea;
    private CardView imgMoreButtonClickArea;


    @Override
    public void onBindView(final PlayList playlist, int position) {
        txtIndex.setText( String.valueOf(position + 1) );
        txtName.setText(playlist.getName());
        setExtended(isExtended());
//        txtDescription.setText(audioFolder.getPath());
    }

    public PlaylistItemViewHolder(View itemView) {
        super(itemView);
        txtIndex        = (TextView)    itemView.findViewById(R.id.txtIndex);
        imgExpandToggle = (ImageView)   itemView.findViewById(R.id.imgExpandToggle);
        txtName         = (TextView)    itemView.findViewById(R.id.txtName);
        txtDescription = (TextView)    itemView.findViewById(R.id.txtDescription);
        imgPlayButton   = (ImageView) itemView.findViewById(R.id.imgPlayButton);
        imgMoreButton   = (ImageView) itemView.findViewById(R.id.imgMoreButton);
        imgPlayButtonClickArea = (CardView) itemView.findViewById(R.id.imgPlayButtonClickArea);
        imgMoreButtonClickArea = (CardView) itemView.findViewById(R.id.imgMoreButtonClickArea);
    }

    @Override
    public void setExtended(boolean isExtend) {
        super.setExtended(isExtend);
        if(isExtend)
        {
            imgExpandToggle.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        }
        else{
            imgExpandToggle.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }
    }

    public TextView getTxtIndex() {
        return txtIndex;
    }

    public void setTxtIndex(TextView txtIndex) {
        this.txtIndex = txtIndex;
    }

    public ImageView getImgExpandToggle() {
        return imgExpandToggle;
    }

    public void setImgExpandToggle(ImageView imgExpandToggle) {
        this.imgExpandToggle = imgExpandToggle;
    }

    public TextView getTxtName() {
        return txtName;
    }

    public void setTxtName(TextView txtName) {
        this.txtName = txtName;
    }

    public TextView getTxtDescription() {
        return txtDescription;
    }

    public void setTxtDescription(TextView txtDescription) {
        this.txtDescription = txtDescription;
    }

    public ImageView getImgPlayButton() {
        return imgPlayButton;
    }

    public void setImgPlayButton(ImageView imgPlayButton) {
        this.imgPlayButton = imgPlayButton;
    }

    public ImageView getImgMoreButton() {
        return imgMoreButton;
    }

    public void setImgMoreButton(ImageView imgMoreButton) {
        this.imgMoreButton = imgMoreButton;
    }

    public CardView getImgPlayButtonClickArea() {
        return imgPlayButtonClickArea;
    }

    public CardView getImgMoreButtonClickArea() {
        return imgMoreButtonClickArea;
    }
}
