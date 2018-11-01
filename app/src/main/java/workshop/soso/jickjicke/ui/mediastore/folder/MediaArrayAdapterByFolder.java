package workshop.soso.jickjicke.ui.mediastore.folder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.AudioFolder;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.CurrentPlaylistReseter;
import workshop.soso.jickjicke.ui.util.ItemExtender;
import workshop.soso.jickjicke.ui.util.PopupHelper;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.PlayListHelper;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;


public class MediaArrayAdapterByFolder extends AbstractMediaArrayAdapter {

	public static final int FOLDERTYPE = 1;
	public static final int FILETYPE = 2;
	public static final int HEADER = FOLDERTYPE;
	public static final int CHILD = FILETYPE;

	public void setValues(ArrayList<Row<?>> rows)
	{
		setmRows(rows);
	}

	public List<Row<?>> getValues()
	{
		return getmRows();
	}

	public MediaArrayAdapterByFolder(Context context, ArrayList<Row<?>> data) {
		super(context);
		setmRows(new ArrayList<Row<?>>());
		setValues( data );

	}


	@Override
	public BaseViewHolder onCreateViewHolder(ViewGroup parent, int type) {
		View view = null;
		BaseViewHolder vh = null;

		LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		switch (type) {
			case FOLDERTYPE:
			{
				view = inflater.inflate(R.layout.mediastore_arrayitem_audiofolder, parent, false);
				vh = new FolderItemViewHolder(view);
			}
			break;
			case FILETYPE:
			{
				view = inflater.inflate(R.layout.mediastore_arrayitem_audiofile, parent, false);
				vh = new FileItemViewHolder( view, getContext());
			}
			break;
		}
		return vh;
	}

	@Override
	public void onBindViewHolder(BaseViewHolder holder, int position) {
		final Row item = (Row)getValues().get(position);
		switch (item.getItemViewType()) {
			case FOLDERTYPE:
			{
				final AudioFolder audioFolder = (AudioFolder)item.getItem();
				final FolderItemViewHolder itemController = (FolderItemViewHolder) holder;

				//holder.get
				itemController.refferalItem = item;
				itemController.setExtended(item.isExtened());
				itemController.onBindView(audioFolder, item.getIndex());
				((CardView)(itemController.getTxtName().getParent().getParent())).setOnClickListener(new ItemExtender(item, itemController, this));
				((CardView)(itemController.getTxtName().getParent().getParent())).setOnLongClickListener(v -> {
                    showFolderPopupMenu(v, audioFolder);
					Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_FOLDER_LIST), CONSTANTS.EVENT_LISTITEM_LONGCLICK);
                    return false;
                });

				itemController.getImgPlayButtonClickArea().setOnClickListener(new CurrentPlaylistReseter(getContext(), audioFolder.getAudioList()));

				itemController.getImgMoreButtonClickArea().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//todo. 해당 아이템을 playlist로 변경하는 팝업 추가
						showFolderPopupMenu(v, audioFolder);
						Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_FOLDER_LIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK + " Folder");
					}
				});
			}
			break;
			case CHILD:
			{
				final Audio audio = (Audio) item.getItem();
				FileItemViewHolder viewholder = (FileItemViewHolder)holder;
				viewholder.onBindView(audio, item.getIndex());
				viewholder.setContext(getContext());

				viewholder.getEntireCardView().setOnClickListener(v -> {
                    //현재 재생목록에 추가하긔.
                    insertToCurrentPlaylistAndPlay(audio);
					Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_FOLDER_LIST), CONSTANTS.EVENT_LISTITEM_CLICK + " File");
				});
				viewholder.getImgMoreButtonClickArea().setOnClickListener(v -> {
					showAudioFilePopup(v, audio);
					Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_FOLDER_LIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK + " File");
				});
				viewholder.getEntireCardView().setOnLongClickListener(v -> {
                    showAudioFilePopup(v, audio);
					Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_FOLDER_LIST), CONSTANTS.EVENT_LISTITEM_LONGCLICK + " File");
                    return false;
                });

			}
			break;
			default:
				break;
		}
		setAnimation(holder.itemView, position);
	}

	private void showAudioFilePopup(final View v, final Audio audio) {
		Context context = getContext();

		PopupMenu popup = new PopupMenu(context, v);

		((MainActivity)(context)).getMenuInflater().inflate(R.menu.media_folder_file_popup, popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId())
				{
					case R.id.menu_add:
					{
						String msg = String.format("add button clicked");
						DLog.v(msg);
						PlayListHelper.showAudioToPlaylistDialog(getContext(), audio);
					}
					break;


					default:
						break;
				}
				return false;
			}
		});

		PopupHelper.showPopupWithIcon(context, popup, v);
	}

	@SuppressLint("RestrictedApi")
	private void showFolderPopupMenu(final View view, final AudioFolder audioFolder) {
		Context context = getContext();

		PopupMenu popup = new PopupMenu(context, view);

		((MainActivity)(context)).getMenuInflater().inflate(R.menu.media_folder_popup, popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId())
				{
					case R.id.menu_play:
					{
						String msg = String.format("folder play button clicked");
						DLog.v(msg);
						CurrentPlaylistReseter.resetCurrentPlaylist(getContext(), audioFolder.getAudioList());

						String sentence = getContext().getString(R.string.folder_is_playing);
						sentence = sentence.replace("#_#_#", audioFolder.getName());

						ShortTask.showSnack(getContext(), sentence);
					}
					break;

					default:
						break;
				}
				return false;
			}
		});

		MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popup.getMenu(), view);
		menuHelper.setForceShowIcon(true);
		menuHelper.setGravity(Gravity.END);
		menuHelper.show();

	}


}
