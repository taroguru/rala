package workshop.soso.jickjicke.ui.player.drawer;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2017. 7. 9..
 */

class ABRepeatViewHolder extends BaseViewHolder<ABRepeat> {

    private ImageView speackerButton;
    private TextView txtIndex;
    private TextView txtName;
    private TextView txtTimeRange;
    private TextView txtDuration;
    private ImageView imgMoreButton;
    private RelativeLayout imgMoreButtonClickArea;


    public ABRepeatViewHolder(View itemView) {
        super(itemView);
        txtIndex        = (TextView)    itemView.findViewById(R.id.txtIndex);
        txtName         = (TextView)    itemView.findViewById(R.id.txtName);
        speackerButton  = (ImageView)   itemView.findViewById(R.id.imgPlaying);
        txtTimeRange = (TextView)    itemView.findViewById(R.id.txtArtist);
        txtDuration     = (TextView)    itemView.findViewById(R.id.txtDuration);
        imgMoreButton   = (ImageView) itemView.findViewById(R.id.imgMoreButton);
        imgMoreButtonClickArea   = (RelativeLayout) itemView.findViewById(R.id.imgMoreButtonClickArea);

    }

    @Override
    public void onBindView(ABRepeat abrepeat, int position) {
        txtName.setText(abrepeat.getName());
        txtIndex.setText(String.valueOf(position+1));

        if(abrepeat.isChecked())
        {
            speackerButton.setVisibility(View.VISIBLE);
        }
        else
        {
            speackerButton.setVisibility(View.INVISIBLE);
        }

        txtTimeRange.setText(abrepeat.getName());
        txtDuration.setText(String.valueOf(Utility.changeMSecToMSec(abrepeat.getEnd()-abrepeat.getStart())));
        //DLog.v(String.format("Play item name = %s, index = %d",name, position));
    }


    public TextView getTxtIndex() {
        return txtIndex;
    }

    public void setTxtIndex(TextView txtIndex) {
        this.txtIndex = txtIndex;
    }

    public TextView getTxtName() {
        return txtName;
    }

    public void setTxtName(TextView txtName) {
        this.txtName = txtName;
    }

    public TextView getTxtTimeRange() {
        return txtTimeRange;
    }

    public void setTxtTimeRange(TextView txtTimeRange) {
        this.txtTimeRange = txtTimeRange;
    }

    public TextView getTxtDuration() {
        return txtDuration;
    }

    public void setTxtDuration(TextView txtDuration) {
        this.txtDuration = txtDuration;
    }

    public ImageView getImgMoreButton() {
        return imgMoreButton;
    }

    public void setImgMoreButton(ImageButton imgMoreButton) {
        this.imgMoreButton = imgMoreButton;
    }

    public RelativeLayout getImgMoreButtonClickArea() {
        return imgMoreButtonClickArea;
    }
}
