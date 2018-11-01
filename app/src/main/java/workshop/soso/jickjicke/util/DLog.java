package workshop.soso.jickjicke.util;

import android.util.Log;

import workshop.soso.jickjicke.StateManager;

public class DLog {

    static final String TAG = "DLog";


    /** Log Level Error **/
    public static final void e(String message) {
        if (StateManager.DEBUG) Log.e(TAG, buildLogMsg(message));
    }
    /** Log Level Warning **/
    public static final void w(String message) {
        if (StateManager.DEBUG)Log.w(TAG, buildLogMsg(message));
    }
    /** Log Level Information **/
    public static final void i(String message) {
        if (StateManager.DEBUG)Log.i(TAG, buildLogMsg(message));
    }
    /** Log Level Debug **/
    public static final void d(String message) {
        if (StateManager.DEBUG)Log.d(TAG, buildLogMsg(message));
    }
    /** Log Level Verbose **/
    public static final void v(String message) {
        if (StateManager.DEBUG)Log.v(TAG, buildLogMsg(message));
    }

    /** Log Level Error **/
    public static final void e(String tag, String message) {
        if (StateManager.DEBUG) Log.e(tag, buildLogMsg(message));
    }
    /** Log Level Warning **/
    public static final void w(String tag, String message) {
        if (StateManager.DEBUG)Log.w(tag, buildLogMsg(message));
    }
    /** Log Level Information **/
    public static final void i(String tag, String message) {
        if (StateManager.DEBUG)Log.i(tag, buildLogMsg(message));
    }
    /** Log Level Debug **/
    public static final void d(String tag, String message) {
        if (StateManager.DEBUG)Log.d(tag, buildLogMsg(message));
    }
    /** Log Level Verbose **/
    public static final void v(String tag, String message) {
        if (StateManager.DEBUG)Log.v(tag, buildLogMsg(message));
    }

    public static String buildLogMsg(String message) {

        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);

        return sb.toString();

    }

}