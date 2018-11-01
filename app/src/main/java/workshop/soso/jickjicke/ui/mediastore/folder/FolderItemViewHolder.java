package workshop.soso.jickjicke.ui.mediastore.folder;

import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import workshop.soso.jickjicke.AudioFolder;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.util.ExtendViewHolder;

/**
 * Created by taroguru on 2017. 5. 8..
 */

public class FolderItemViewHolder extends ExtendViewHolder<AudioFolder> {

    private TextView txtIndex;
    private ImageView imgExpandToggle;
    private TextView txtName;
    private TextView txtPath;
    private ImageView imgPlayButton;
    private ImageView imgMoreButton;
    private CardView imgPlayButtonClickArea;
    private CardView imgMoreButtonClickArea;


    @Override
    public void onBindView(final AudioFolder audioFolder, int position) {
        txtIndex.setText( String.valueOf(position + 1) );
        txtName.setText(audioFolder.getName());
        txtPath.setText(audioFolder.getPath());
        setExtended(isExtended());
    }

    public FolderItemViewHolder(View itemView) {
        super(itemView);
        txtIndex        = (TextView)    itemView.findViewById(R.id.txtIndex);
        imgExpandToggle = (ImageView)   itemView.findViewById(R.id.imgExpandToggle);
        txtName         = (TextView)    itemView.findViewById(R.id.txtName);
        txtPath         = (TextView)    itemView.findViewById(R.id.txtPath);
        imgPlayButton   = (ImageView) itemView.findViewById(R.id.imgPlayButton);
        imgMoreButton   = (ImageView) itemView.findViewById(R.id.imgMoreButton);
        imgPlayButtonClickArea   = (CardView) itemView.findViewById(R.id.imgPlayButtonClickArea);
        imgMoreButtonClickArea   = (CardView) itemView.findViewById(R.id.imgMoreButtonClickArea);
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

    public TextView getTxtPath() {
        return txtPath;
    }

    public void setTxtPath(TextView txtPath) {
        this.txtPath = txtPath;
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
