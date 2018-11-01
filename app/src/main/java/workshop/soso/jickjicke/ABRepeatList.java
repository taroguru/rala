package workshop.soso.jickjicke;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;

import workshop.soso.jickjicke.util.Utility;

/**
 * Created by taroguru on 2015. 1. 31..
 */
public class ABRepeatList extends ItemList {

    public ABRepeatList() {
        itemlist = new ArrayList<ABRepeat>();
    }

    public void sort()
    {
        Collections.sort(itemlist, new ABRepeatCompre());
    }

    public int addItem(ABRepeat item) {

        int position = -1;
        boolean result = super.addItem(item);
        //추가에 따라 abrepeat 추가 구현.
        if (result) {
            Collections.sort(getItemlist(), new ABRepeatCompre());
            position = getItemlist().indexOf(item);
        }

        return position;
    }

    public boolean remove(int childPosition, Context context) {
        boolean result = false;
        if(itemlist != null && itemlist.size() > childPosition)
        {
            itemlist.remove(childPosition);
            //현재 ABrepeatposition 재정렬.
            int currentABRepeatPosition = Utility.getCurrentABRepeatPosition(context);
            if(childPosition > currentABRepeatPosition )
            {
                //do notthing
            }
            else //if(childPosition <=currentABRepeatPosition)
            {
                Utility.setCurrentABRepeatPosition(context, --currentABRepeatPosition);
            }

            result = true;
        }
        return result;
    }
}
