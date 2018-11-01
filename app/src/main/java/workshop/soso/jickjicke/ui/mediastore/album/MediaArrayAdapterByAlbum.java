package workshop.soso.jickjicke.ui.mediastore.album;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import workshop.soso.jickjicke.Album;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.ViewHolder;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.util.DLog;


public class MediaArrayAdapterByAlbum extends AbstractMediaArrayAdapter{

	public int getCount() {
		return getItemCount();
	}

	@Override
	public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		BaseViewHolder vh = null;


		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		view = inflater.inflate(R.layout.mediastore_arrayitem_album, parent, false);
		vh = new AlbumViewHolder(view);


		return vh;
	}

	public MediaArrayAdapterByAlbum(Context context)
	{
		super(context);
		setmRows(new ArrayList<Row<?>>());
	}

	@Override
	public Album getNewChild() {
		return new Album();
	}
}

