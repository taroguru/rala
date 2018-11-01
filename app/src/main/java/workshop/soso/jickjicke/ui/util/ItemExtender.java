package workshop.soso.jickjicke.ui.util;

import android.view.View;

import java.util.ArrayList;

import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.util.DLog;

/**
 * Created by jeonghan on 2017-06-22.
 */

public class ItemExtender implements View.OnClickListener {
    public static final int HEADER = 1;
    public static final int CHILD = 2;

    private MultiItemAdapter.Row item;
    private ExtendViewHolder itemController;
    private AbstractMediaArrayAdapter adapter;

    public ItemExtender(MultiItemAdapter.Row clickedItem, ExtendViewHolder clickedViewHolder, AbstractMediaArrayAdapter adapter)
    {
        item = clickedItem;
        itemController = clickedViewHolder;
        this.adapter = adapter;
    }

    @Override
    public void onClick(View v) {
        try {
            //문제:확장시 첫번째 아이템이 스크롤 업 애니메이션이 나옴.
            //scroll down/up의 판단기준이 lastposition이므로 클릭 위치를 lastposition으로 설정해
            //확장시 scrolldown animation이 나오게 하자
            int pos = adapter.getmRows().indexOf(itemController.refferalItem);

            if (itemController.isExtended()) {    // 열려있는 상태
                shortenItem();
                adapter.setLastPosition(pos);
            } else {                                        //닫혀있는상태
                adapter.setLastPosition(pos);
                expendItem();
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void shortenItem() {
        item.setInvisibleChildren(  new ArrayList<MultiItemAdapter.Row>());
        int count = 0;
        int pos = adapter.getmRows().indexOf(itemController.refferalItem);
        DLog.v("shorten position : " + pos);
        while (adapter.getmRows().size() > pos + 1 && ((MultiItemAdapter.Row)adapter.getmRows().get(pos + 1)).getItemViewType() == CHILD) {	//사실 다소 무식하기는 하다...
            DLog.v("remove position : " + pos);
            MultiItemAdapter.Row row = adapter.removeRow(pos + 1);
            if(row != null)
                item.getInvisibleChildren().add(row);
            count++;
        }
        adapter.notifyItemRangeRemoved(pos + 1, count);
        itemController.setExtended(false);
    }

    private void expendItem() {
        int pos = adapter.getmRows().indexOf(itemController.refferalItem);

        int index = pos + 1;
        //좌식이 없으면 늘리지 말자.
        if(item != null && item.getInvisibleChildren() != null )
        {
            if(item.getInvisibleChildren().size() >= 1)
            {
                for (Object it : item.getInvisibleChildren())
                {
                    adapter.addRow(index, (MultiItemAdapter.Row)it);
                    index++;
                }

                adapter.notifyItemRangeInserted(pos + 1, index - pos - 1);
                itemController.setExtended(true);
                item.setInvisibleChildren(null);
            }
            else
            {
                //snack으로 현재 parent
            }

        }

    }

}
