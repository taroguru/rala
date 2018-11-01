package workshop.soso.jickjicke.ui.util;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

/**
 * Created by taroguru on 2017. 7. 23..
 */

public class PopupHelper {
    public static void showPopupWithIcon(Context context, PopupMenu popup, View v)
    {
        MenuPopupHelper menuHelper = new MenuPopupHelper(context, (MenuBuilder) popup.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        menuHelper.show();
    }
}
