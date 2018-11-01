package workshop.soso.jickjicke.ui.mediastore.album;

import android.view.View;
import android.widget.TextView;

import workshop.soso.jickjicke.Album;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;

/**
 * Created by jeonghan on 2017-05-29.
 */

class AlbumViewHolder extends BaseViewHolder<Album> {

    private TextView txtIndex;
    private TextView txtName;
    private TextView txtTrackCount;


    @Override
    public void onBindView(Album album, int position)
    {
        txtIndex.setText( String.valueOf(position) );
        txtName.setText(album.getName());
        txtTrackCount.setText(album.getNUMBER_OF_SONGS());
    }

    public AlbumViewHolder(View view) {
        super(view);

        txtIndex        = (TextView) view.findViewById(R.id.txtIndex);
        txtName         = (TextView) view.findViewById(R.id.txtName);
        txtTrackCount   = (TextView) view.findViewById(R.id.txtTrackCount);
    }
}
