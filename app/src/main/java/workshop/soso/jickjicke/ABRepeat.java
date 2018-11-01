package workshop.soso.jickjicke;

import android.database.Cursor;

import java.io.Serializable;

import workshop.soso.jickjicke.db.SoundContract;
import workshop.soso.jickjicke.util.Utility;

public class ABRepeat extends ArrayItem implements Serializable{
	private int start=0;
	private int end=0;
	private boolean checked = false;
	public ABRepeat()
	{
		start = 0;
		end   = 0;
		checked = false;
	}

	public boolean isChecked()
	{
		return checked;
	}

	public void setChecked(boolean bCheck)
	{
		checked = bCheck;
	}

    public ABRepeat(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setData(Cursor cursor)
	{
		int startmSec = cursor.getInt(cursor.getColumnIndex(SoundContract.DBABRepeat.COLUMN_START_MSEC));
		int endmSec = cursor.getInt(cursor.getColumnIndex(SoundContract.DBABRepeat.COLUMN_END_MSEC));
		long id = cursor.getLong(cursor.getColumnIndex(SoundContract.DBABRepeat._ID));
		setStart(startmSec);
		setEnd(endmSec);
		setId(id);
	}

	@Override
	public String getName()
	{
		return String.format("%s~%s", Utility.changeMSecToMSec(start), Utility.changeMSecToMSec(end));
	}
}
