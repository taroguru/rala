//package workshop.soso.jickjicke.broadcastreceiver;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import workshop.soso.jickjicke.intent.Intents;
//import workshop.soso.jickjicke.ui.player.MainActivity;
//
///**
// * Created by taroguru on 2016. 7. 23..
// */
//public class PhoneCallReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        //전화가 오면 현재 플레이 일시정지 함
//
//        String name = intent.getAction();
//
//        if(name.equals( Intent.ACTION_NEW_OUTGOING_CALL))
//        {
//                //전화 걸때도 잠깐 멈출까.
//
//        }
//        else    //받는 전화
//        {
//            Intent pausePlayerIntent = new Intent(context, MainActivity.class);
//            context.sendBroadcast(new Intent(Intents.Pause));
//
//        }
//    }
//}
