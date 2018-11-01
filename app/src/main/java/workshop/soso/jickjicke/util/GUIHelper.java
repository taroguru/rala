package workshop.soso.jickjicke.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.ui.util.ImageLoad;

public class GUIHelper {

    public static void changeFloatingButtonToSearch(Context context, FloatingActionButton floatingButton, int pagenumber) {
        try {
            //change action
            floatingButton.setOnClickListener(v -> {
                Intent moveIntent = new Intent(ACTION.SearchList);
                moveIntent.putExtra(EXTRA_VALUE.PageNumber, pagenumber);
                Utility.sendIntentLocalBroadcast(context, moveIntent);
                Utility.sendEventGoogleAnalytics(context, "FloatingButtonSearch", CONSTANTS.screenName(pagenumber));
            });

            //change icon
            floatingButton.hide();
            Drawable searchIcon = ContextCompat.getDrawable(context, R.drawable.ic_search_white_30dp);
            floatingButton.setImageDrawable(searchIcon);
            floatingButton.show();

        } catch (NullPointerException e) {
            e.printStackTrace();

        }

    }

    public static void setAlbumImage(Context context, ImageView imgAlbum, long albumId) {
        imgAlbum.setImageResource(0); //바인딩 전 이전 이미지 출력을 위한
        ImageLoad loadAlbumImgTask = new ImageLoad(context, imgAlbum, albumId);
        loadAlbumImgTask.execute();
    }
}
