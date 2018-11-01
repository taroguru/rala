package workshop.soso.jickjicke.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatSeekBar;
import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.ABRepeatList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2016. 12. 15..
 */
public class PlayerSeekbar extends AppCompatSeekBar{
    private ABRepeatList abRepeatList;
    private ABRepeat currentABRepeat;
    private float density;
    private boolean secondHalf;

    public PlayerSeekbar(Context context) {
        this(context, null);
    }

    public PlayerSeekbar(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.seekBarStyle);
    }

    public PlayerSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMember();
    }

    private void initMember()
    {
        abRepeatList = new ABRepeatList();
        currentABRepeat = null;
        density = Utility.getDentity(getContext());
        secondHalf = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Resources res = getResources();

        VectorDrawableCompat vectorrepeatPoint = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_location_on_black_20dp, null);
        VectorDrawableCompat vectorrepeatPointEnd = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_location_on_grey_20dp, null);

        int paddingStart = getPaddingLeft();
        int paddingEnd = getPaddingRight();
        int width = getWidth() - paddingStart - paddingEnd;

        int widthDP = Utility.convertDpToPx(20, density);
        int heightDP = Utility.convertDpToPx(20, density);
        float y  = getHeight() / 2 - heightDP; //seekbar위에 연결하기 위해서 절반에서 maker높이만큼 위에 그림

        if(currentABRepeat != null)
        {
            //drawRepeatMarker(canvas, vectorrepeatPointEnd, paddingStart, width, widthDP, heightDP, y, currentABRepeat);
            drawRepeatMarker(canvas, vectorrepeatPoint, null, paddingStart, width, widthDP, heightDP, y, currentABRepeat);
        }


        if(abRepeatList != null && abRepeatList.size() > 0) {
            for(ABRepeat abrepeat : (ArrayList<ABRepeat>)abRepeatList.getItemlist())
            {
                drawRepeatMarker(canvas, vectorrepeatPoint, vectorrepeatPointEnd, paddingStart, width, widthDP, heightDP, y, abrepeat);
            }
        }
        invalidate();

    }

    private void drawRepeatMarker(Canvas canvas, VectorDrawableCompat vectorrepeatPoint, VectorDrawableCompat vectorrepeatEndPoint,int paddingStart, int width, int widthDP, int heightDP, float y, ABRepeat abrepeat) {
        ABRepeat drawABRepeatPoint = new ABRepeat();
        if(!secondHalf)
        {
            drawABRepeatPoint.setStart(abrepeat.getStart());
            drawABRepeatPoint.setEnd(abrepeat.getEnd());
        }
        else
        {
            drawABRepeatPoint.setStart(abrepeat.getStart() - getMax());
            drawABRepeatPoint.setEnd(abrepeat.getEnd() - getMax());
        }

        float endX = (float)(((double)((float)drawABRepeatPoint.getEnd() / (float)getMax())) * width) + widthDP/2 - Utility.convertDpToPx(4.0f, density)/*가운데 정렬용*/;
        float startX = (float)(((double)((float)drawABRepeatPoint.getStart() / (float)getMax())) * width) + widthDP/2 - Utility.convertDpToPx(4.0f, density)/*가운데 정렬용*/;

        if(vectorrepeatEndPoint != null && 0 <= endX && endX <= width) {
            drawVectorRepeat(canvas, vectorrepeatEndPoint, widthDP, heightDP, endX, y);
        }
        if(vectorrepeatPoint != null && 0 <= startX && startX <= width) {
            drawVectorRepeat(canvas, vectorrepeatPoint, widthDP, heightDP, startX, y);
        }

    }

    private void drawVectorRepeat(Canvas canvas, VectorDrawableCompat vector, int width, int height, float x, float y, ColorFilter colorFilter)
    {

        vector.setBounds(0, 0, width, height);
        canvas.translate(x, y);
        vector.draw(canvas);
        canvas.translate(-x, -y);
    }

    private void drawVectorRepeat(Canvas canvas, VectorDrawableCompat vector, int width, int height, float x, float y)
    {
        vector.setBounds(0, 0, width, height);
        canvas.translate(x, y);
        vector.draw(canvas);
        canvas.translate(-x, -y);
    }

    public void setAbRepeatList(ArrayList<ABRepeat> newABRepeatList) {
        abRepeatList.setItemlist(newABRepeatList);
    }

    public void addAbRepeat(ABRepeat abRepeat) {
        abRepeatList.addItem(abRepeat);
    }

    public void setCurrentABRepeat(ABRepeat currentABRepeat) {
        this.currentABRepeat = currentABRepeat;
    }

    public void setSecondHalf(boolean isSecondHalf) {
        secondHalf = isSecondHalf;
    }

    public boolean isSecondHalf() {
        return secondHalf;
    }
}
