package workshop.soso.jickjicke.ui.mediastore;

import workshop.soso.jickjicke.ArrayItem;

/**
 * Created by taroguru on 2017. 2. 19..
 */

public interface FragmentChangeListener<T extends ArrayItem> {
    void onSwitchToNextFragment(T item);
}
