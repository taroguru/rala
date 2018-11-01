package workshop.soso.jickjicke.asset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by taroguru on 2017. 5. 8..
 */

public class LocalBroadcastReceiverMock extends BroadcastReceiver{
    private boolean isReceived = false;
    private String action;

    public void setReceiveAction(String action)
    {
        this.action = action;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action != null && !action.isEmpty() && this.action.equals(action))
        {
            isReceived = true;
        }

    }

    public boolean isReceived() {
        return isReceived;
    }
}
