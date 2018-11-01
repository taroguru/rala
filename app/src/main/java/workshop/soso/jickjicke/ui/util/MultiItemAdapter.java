package workshop.soso.jickjicke.ui.util;

import androidx.recyclerview.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import workshop.soso.jickjicke.ArrayItem;
import workshop.soso.jickjicke.util.DLog;

public abstract class MultiItemAdapter extends RecyclerView.Adapter<BaseViewHolder> implements Filterable {

    private List<Row<?>> originalValue = new ArrayList<>();
    private List<Row<?>> mRows = new ArrayList<>();

    public List<Row<?>> getmRows() {
        return mRows;
    }

    public List<Row<?>> getOriginalValue() {
        return originalValue;
    }

    public void setmRows(List<Row<?>> mRows) {
        this.mRows = mRows;
        originalValue.clear();
        originalValue.addAll(this.mRows);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        holder.onBindView(getItem(position), position);
    }

    public <ITEM> ITEM getItem(int position) {
        return (ITEM) mRows.get(position).getItem();
    }

    public Row getRow(int position) {
        return mRows.get(position);
    }
//
//    public void setRows(List<Row<?>> mRows) {
//        mRows.clear();
//        mRows.addAll(mRows);
//    }

    public void addRow(Row<?> row) {
        mRows.add(row);
        originalValue.add(row);
    }


    public void addRow(int position, Row<?> row) {
//        if(position == mRows.size())
//        {
//            mRows.add(row);
//            originalValue.add(row);
//        }
//        else
//        {
        mRows.add(position, row);
        originalValue.add(position, row);
//        }

    }

    public boolean removeRow(Row row) {
        boolean result;
        boolean result1 = false;
        try {
            result = mRows.remove(row);
            result1 = originalValue.remove(row);
        } catch (NullPointerException e) {
            e.printStackTrace();
            result = false;
        }

        return result && result1;
    }

    public Row removeRow(int position) {
        Row row = null;
        try {
            DLog.v("mrows size, originalvalue size = " + mRows.size() + ", " + originalValue.size());
//            if (mRows.size() == originalValue.size()) {
            row = mRows.remove(position);
            originalValue.remove(position);
//            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return row;

    }

    public boolean removeRowBoolean(int position) {
        return removeRow(position) != null;
    }

    @Override
    public int getItemCount() {
        return mRows.size();
    }

    public int getCount() {
        return getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position).getItemViewType();
    }

    /**
     * recyclerview 의 기본 데이터 아이템.
     *
     * @param <ITEM> 데이터. ArrayItem 상속
     */
    public static class Row<ITEM extends ArrayItem> {
        private ITEM item;
        private int itemViewType;
        private ArrayList<Row<ITEM>> invisibleChildren;
        private int index;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void decreaseIndex(){--index;}
        protected Row(ITEM item, int itemViewType) {
            invisibleChildren = new ArrayList<>();
            this.item = item;
            this.itemViewType = itemViewType;
        }

        public static <T extends ArrayItem> Row create(T item, int itemViewType) {
            return new Row(item, itemViewType);
        }

        public static <T extends ArrayItem> Row create(int index, int itemViewType, T item) {
            Row result = new Row(item, itemViewType);
            result.setIndex(index);
            return result;
        }


        public ITEM getItem() {
            return item;
        }

        public int getItemViewType() {
            return itemViewType;
        }

        public void setItem(ITEM item) {
            this.item = item;
        }

        public void setItemViewType(int itemViewType) {
            this.itemViewType = itemViewType;
        }

        public boolean setChecked(boolean check) {
            boolean result = false;
            if (item != null) {
                item.setChecked(check);
                result = true;
            }

            return result;
        }

        public boolean addChild(Row<ITEM> child) {

            boolean result = false;
            try {
                if (!isExtened())
                    result = invisibleChildren.add(child);
            } catch (NullPointerException e) {
                e.printStackTrace();
                result = false;
            }

            return result;
        }

        public ArrayList<Row<ITEM>> getInvisibleChildren() {
            return invisibleChildren;
        }

        public void setInvisibleChildren(ArrayList<Row<ITEM>> invisibleChildren) {
            this.invisibleChildren = invisibleChildren;
        }

        public boolean isExtened() {
            return invisibleChildren == null;
        }
    }

    public void checkAll() {
        for (Row<?> item : getmRows()) {
            item.setChecked(true);
        }

    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Row<?>> results = new ArrayList<>();
                if (originalValue == null)
                    originalValue = new ArrayList<>(mRows);
                if (constraint != null && constraint.length() > 0) {
                    if (originalValue != null && originalValue.size() > 0) {
                        for (final Row cd : originalValue) {
                            String objectValue = cd.getItem().getName();
                            String queryValue = constraint.toString();
                            if (objectValue.toLowerCase().contains(queryValue.toLowerCase()) ||    //알파벳 대소문자 구분x코드
                                    objectValue.contains((queryValue)))                             //그외 언어 검색
                                results.add(cd);
                        }
                    }
                    oReturn.values = results;
                    oReturn.count = results.size();
                } else {
                    oReturn.values = originalValue;
                    oReturn.count = originalValue.size();
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(final CharSequence constraint,
                                          FilterResults results) {
                mRows = new ArrayList<>((ArrayList<Row<?>>) results.values);
                // FIXME: 8/16/2017 implement Comparable with sort below
                ///Collections.sort(itemList);
                notifyDataSetChanged();
            }
        };
    }

}
