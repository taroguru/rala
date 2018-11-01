package workshop.soso.jickjicke.ui.util;

import android.view.View;

/**
 * Created by jeonghan on 2017-06-22.
 */

public abstract class ExtendViewHolder<T> extends BaseViewHolder<T> {
    public static int Extended = 1;
    public static int Shorted = 2;
    private boolean extended = false;
    public MultiItemAdapter.Row refferalItem;

    public ExtendViewHolder(View itemView) {
        super(itemView);
    }

    public boolean isExtended()
    {
        return extended;
    }

    public void setExtended(boolean isExtend)
    {
        extended = isExtend;
    }

}
