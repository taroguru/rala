//package workshop.soso.jickjicke.ui.mediastore.genre;
//
//import android.content.Context;
//import androidx.appcompat.widget.AppCompatTextView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//import workshop.soso.jickjicke.Genre;
//import workshop.soso.jickjicke.R;
//import workshop.soso.jickjicke.ui.ViewHolder;
//import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
//import workshop.soso.jickjicke.util.DLog;
//
//
//public class MediaArrayAdapterByGenre extends AbstractMediaArrayAdapter<Genre>{
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
//	public MediaArrayAdapterByGenre(Context context)
//	{
//		super(context);
//		setValues(new ArrayList<Genre>());
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//
//		Genre item = (Genre)getItem(position);
//		if(item != null)
//		{
//			LayoutInflater inflater = (LayoutInflater) getContext()
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//			convertView = inflater.inflate(R.layout.mediastore_arrayitem_genre, parent, false);
//
//			TextView indexView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtIndex);
//			indexView.setText( String.valueOf(position+1));
//			AppCompatTextView artistView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtName);
//			artistView.setText(item.getName());
////			TextView trackCountView = (AppCompatTextView)ViewHolder.get(convertView, R.id.txtTrackCount);
////			trackCountView.setText( String.valueOf(item.getNUMBER_OF_SONGS() ) );
//
//		}
//		else
//		{
//			DLog.v(String.format("%d.th item is null", position));
//		}
//
//		return convertView;
//	}
//
//	@Override
//	public Genre getNewChild() {
//		return new Genre();
//	}
//}
//
