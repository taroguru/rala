package workshop.soso.jickjicke.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.permission.Permissions;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.ShortTask;

public class StartActivity extends AppCompatActivity {

    //private boolean allPermissionClear = false;
    private boolean hasExtReadPermission = false;
    private boolean hasExtWritePermission = false;
    private boolean hasReadPhoneState = false;

    private List<TextView> titleTextList;
    private TextView underLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        titleTextList = new ArrayList<>();

        titleTextList.add((TextView)findViewById(R.id.app_first));
        titleTextList.add((TextView)findViewById(R.id.app_second));
        titleTextList.add((TextView)findViewById(R.id.app_third));
        titleTextList.add((TextView)findViewById(R.id.app_fourth));
        underLine = (TextView)findViewById(R.id.underlinetext);

        requestAllPermission();
    }

    private void requestAllPermission() {
        //전체 권한이 있으면 실행, 아니면 권한 요청
        if(checkAllPermission()){
            startMainActivity();
        } else {
            requestPermissions( new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private boolean checkAllPermission(){
        //android 29부터는 mediafile read에 별도 퍼미션 필요 없음
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            hasExtReadPermission = true;
        }else{
            hasExtReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            hasExtWritePermission = true;
        }else{
            hasExtWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        hasReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;

        return hasExtReadPermission && hasExtWritePermission && hasReadPhoneState;
    }

    private void startMainActivity() {
        //AnimationSet animationSet = new AnimationSet(true);



//        animationSet.addAnimation(animation);
//
//        animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.item_animation_down);
//        animation.setDuration(CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE);
//        animationSet.addAnimation(animation);
//        titleTextList.get(0).startAnimation(animation);

        for(int i = 0 ; i < titleTextList.size(); ++i)
        {
            TextView view = titleTextList.get(i);

            Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.item_animation_up);
            animation.setDuration(CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE);
            animation.setStartOffset((long)(i*CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE*0.25));
            Animation animationDown = AnimationUtils.loadAnimation(getBaseContext(), R.anim.item_animation_down);
            animationDown.setDuration(CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE);
//            animationDown.setStartOffset((i+1)*CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.startAnimation(animationDown);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
//            AnimationSet set = new AnimationSet(true);
//            set.addAnimation(animation);
//            set.addAnimation(animationDown);


            view.startAnimation(animation);
        }
        Animation animationUnderline = AnimationUtils.loadAnimation(getBaseContext(), R.anim.animation_expend_right);
        animationUnderline.setDuration(CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE*3);
        underLine.startAnimation(animationUnderline);

        Handler handler = new Handler();
        Log.d("StartActivity", "start MainActivity");
        handler.postDelayed(() -> {
//                Intent intent = new Intent(StartActivity.this, MainActivity.class);
//                startActivity(intent);
            Intent intent = new Intent();
            intent.setClass(StartActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            finish();
        }, (long)(CONSTANTS.DURATION_START_ACTIVITY_TITLE_BOUNCE*5));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Permissions.EXTERNAL_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasExtReadPermission = true;
                } else {
                    finishAffinity();
                }
            }
            break;
            case Permissions.EXTERNAL_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasExtWritePermission = true;
                } else {
                    finishAffinity();
                }
            }

            break;
            case Permissions.READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasReadPhoneState = true;

                } else {
                    ShortTask.showSnack(this, R.string.cannotPauseWhenIncoming);
                }
            break;
        }
        if(hasExtReadPermission && hasExtWritePermission && hasReadPhoneState) {
            startMainActivity();
        }
    }
}
