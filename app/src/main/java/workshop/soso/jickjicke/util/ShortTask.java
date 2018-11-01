package workshop.soso.jickjicke.util;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.Toast;

import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;

/**
 * @author taroguru
 * 짧은 업무를 빠르게 처리하기 위한 유틸클래스.
 */

public class ShortTask {
	public static void Log(String message){
		DLog.v("General", message);
	}
	
	public static void showToast(Context context, CharSequence text)
	{	
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	public static void showToast(Context context, int stringId)
	{
		int duration = Toast.LENGTH_SHORT;

		String text = context.getString(stringId);
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public static void showSnack(View view, CharSequence text)
	{
		Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
	}

	public static void showSnack(Context context, int stringResourceId)
	{
		showSnack(context, context.getString(stringResourceId));
	}

	public static void showSnack(Context context, CharSequence text)
	{
		try
		{
			Intent intent = new Intent();
			intent.setAction(ACTION.ShowSnackBar);
			intent.putExtra(EXTRA_VALUE.Message, text);
			Utility.sendIntentLocalBroadcast(context, intent);
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		catch(RuntimeException e)
		{
			e.printStackTrace();
		}
	}


}
