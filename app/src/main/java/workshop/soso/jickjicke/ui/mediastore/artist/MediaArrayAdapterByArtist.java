//package workshop.soso.jickjicke.ui.mediastore.artist;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.support.annotation.Nullable;
//import androidx.appcompat.widget.AppCompatTextView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import workshop.soso.jickjicke.ArrayItem;
//import workshop.soso.jickjicke.Artists;
//import workshop.soso.jickjicke.R;
//import workshop.soso.jickjicke.ui.ViewHolder;
//import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
//import workshop.soso.jickjicke.ui.mediastore.folder.MediaArrayAdapterByFolder;
//import workshop.soso.jickjicke.ui.util.BaseViewHolder;
//import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
//import workshop.soso.jickjicke.util.DLog;
//
//
//public class MediaArrayAdapterByArtist extends AbstractMediaArrayAdapter<MultiItemAdapter.Row<Artists>>{
//	@Override
//	public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		return null;
//	}
//
//	@Override
//	public Context getContext() {
//		return super.getContext();
//	}
//
//	@Override
//	public void setContext(Context context) {
//		super.setContext(context);
//	}
//
//
//	@Nullable
//	@Override
//	public ArrayItem getItem(int position) {
//		return super.getItem(position);
//	}
//
//	@Override
//	public void clear() {
//		super.clear();
//	}
//
//	@Override
//	public void resetData(Cursor cursor) {
//		super.resetData(cursor);
//	}
//
//	@Override
//	public void checkAll() {
//		super.checkAll();
//	}
//
//	@Override
//	public void resetData(HashMap<String, MediaArrayAdapterByFolder.Item> audioFolderMap) {
//		super.resetData(audioFolderMap);
//	}
//
//	@Override
//	public int getCount() {
//		return getValues().size();
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	public MediaArrayAdapterByArtist(Context context)
//	{
//		super(context);
//		setValues(new ArrayList<Artists>());
//	}
//
//	public MediaArrayAdapterByArtist(Context context, ArrayList<Artists> values) {
//		super(context, values);
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//
//		Artists item = (Artists)getItem(position);
//		if(item != null)
//		{
//			LayoutInflater inflater = (LayoutInflater) getContext()
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//			convertView = inflater.inflate(R.layout.mediastore_arrayitem_artist, parent, false);
//
//			TextView indexView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtIndex);
//			indexView.setText( String.valueOf(position+1));
//			AppCompatTextView artistView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtName);
//			artistView.setText(item.getARTIST());
//			TextView albumCountView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtAlbumCount);
//			albumCountView.setText( String.valueOf(item.getNUMBER_OF_ALBUMS()) );
//			TextView trackCountView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtTrackCount);
//			trackCountView.setText( String.valueOf(item.getNUMBER_OF_TRACKS()) );
//
//		}
//		else
//		{
//			DLog.v(String.format("%d.th item is null", position));
//		}
//
//
//		return convertView;
//	}
//
//	@Override
//	public Artists getNewChild() {
//		return new Artists();
//	}
//}
//
