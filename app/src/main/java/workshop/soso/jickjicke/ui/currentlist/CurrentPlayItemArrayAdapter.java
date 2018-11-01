package workshop.soso.jickjicke.ui.currentlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.PopupHelper;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.PlayListHelper;
import workshop.soso.jickjicke.util.Utility;


public class CurrentPlayItemArrayAdapter extends AbstractMediaArrayAdapter implements ItemTouchHelperAdapter
{

	public static final int AUDIOTYPE = 1;
	public static final int CHILD = AUDIOTYPE;


	private BroadcastReceiver broadcastReceiver;

	public CurrentPlayItemArrayAdapter(Context context, ArrayList<Row<?>> data)
	{
		super(context);
		setmRows( data );

		initBroadCastReceiver();
	}

	private void initBroadCastReceiver()
	{

		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				try {


					DLog.v("CurrentPlayItemArrayAdapter gets an Intent : " + action + ", datasize : " + getItemCount());
					if (action.equals(ACTION.AddPlayItemToCurrentPlayList)) {
						//not used
						PlayItem playitem = (PlayItem) intent.getSerializableExtra(EXTRA_VALUE.PlayItem);
						Audio audio = (Audio) intent.getSerializableExtra(EXTRA_VALUE.Audio);
						playitem.setAudio(audio);
						int index = getItemCount();
						Row row = Row.create(index, AUDIOTYPE, playitem);
						//getmRows().add(row);
						addRow(row);
						notifyItemInserted(index);

						DLog.e("CurrentPlayItemArrayAdapter insert item. size : " + getItemCount());
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter(ACTION.AddPlayItemToCurrentPlayList);
		LocalBroadcastManager.getInstance(getContext()).registerReceiver((broadcastReceiver), intentFilter);
	}


	@Override
	protected void finalize() throws Throwable {
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
		super.finalize();
	}

	@Override
	public void onBindViewHolder(BaseViewHolder holder, int position) {
		if(getmRows() != null && getItemCount() >= position)
		{
			final PlayItem item = getItem(position);
			CurrentPlayItemViewHolder playitemViewHolder = (CurrentPlayItemViewHolder) holder;
			playitemViewHolder.setContext(getContext());
			playitemViewHolder.onBind(position, item);

			playitemViewHolder.getEntireCardView().setOnClickListener(v -> {
                Row item1 = getRow(position);
                int playIndex = item1.getIndex();
                Utility.sendBroadcastPlayAudio(getContext(), playIndex);
				Utility. sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_CURRENT_PLAYLIST), CONSTANTS.EVENT_LISTITEM_CLICK);
                DLog.v(String.format("Click Position:Index : %d/%d", position, playIndex));
            });

			playitemViewHolder.getMoreButtonClickArea().setOnClickListener( v->{
				//show menu
				showPlayItemPopup(v, item, position);
				Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_CURRENT_PLAYLIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK);
			});
		}
		setAnimation( holder.itemView, position);
	}

	private void showPlayItemPopup(final View v, final PlayItem playItem, final int position) {
		DLog.v("showPlayItemPopup. position/size = "+ String.valueOf(position) + "/" + String.valueOf(getItemCount()));
		final Context context = getContext();

		PopupMenu popup = new PopupMenu(context, v);

		((MainActivity)(context)).getMenuInflater().inflate(R.menu.current_playlist_popup, popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId())
				{
					case R.id.menu_delete_from_currentplaylist:
					{
						removePlayItem(position, context, playItem);
					}
					break;
					case R.id.menu_add_playlist:
					{
						String msg = String.format("add button clicked");
						DLog.v(msg);
						PlayListHelper.showAudioToPlaylistDialog(context, playItem.getAudio());
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

	private void removePlayItem(int position, Context context, PlayItem playItem) {
		DLog.v(String.format("removePlayItem, position/size = %d/%d", position, getItemCount()));

		try{
			//playlist와 row 정리
			PlayList currentPlayList = Utility.getCurrentPlayList(context);
			currentPlayList.getItemlist().remove(position);
			removeRowBoolean(position);
			realignIndex(position);

			DLog.v(String.format("removePlayItem, position/size = %d/%d", position, getItemCount()));
			notifyItemRemoved(position);
			int size = getItemCount();
			if(size != 0)
			{
				notifyItemRangeChanged(position, getItemCount());
			}

			//db remove
			DBHelper.deletePlayItemInCurrentPlaylist(context, playItem.getId());

		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}


	}

	private void realignIndex(int position) {
		for(int i = position; i < getmRows().size(); ++i) {
			getmRows().get(i).decreaseIndex();
		}
	}

	@Override
	public CurrentPlayItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.currentplaylist_item, parent, false);

		CurrentPlayItemViewHolder viewHolder  = new CurrentPlayItemViewHolder(v);

		return viewHolder;
	}

	//여기서 디비변경도 처리합시다.
	@Override
	public boolean onItemMove(int fromPosition, int toPosition) {
		if (fromPosition < toPosition) {
			for (int i = fromPosition; i < toPosition; i++) {
				swapPlayItem(i, i+1);
			}
		} else {
			for (int i = fromPosition; i > toPosition; i--) {
				swapPlayItem(i, i-1);
			}
		}

		//index 갱신을 위해서 새로 그림
		notifyItemMoved(fromPosition, toPosition);	//아이템 이동 처리
		notifyItemChanged(fromPosition);			//onbindviewholder를 호출하는 것 같다. index 교체를 위해 호출.
		notifyItemChanged(toPosition);

		int currentPosition = Utility.getCurrentPosition(getContext());
		if(fromPosition == currentPosition)
		{
			Utility.setCurrentPosition(getContext(), toPosition);
		}
		return true;
	}

	private void swapPlayItem(int firstIndex, int nextIndex)
	{
		DLog.v("update position [fisrt:next] = " + firstIndex + ":" + nextIndex);
		DBHelper.swapPlayItemOrder(getContext(), firstIndex, nextIndex);

		PlayList currentPlaylist = Utility.getCurrentPlayList(getContext());
		DLog.v("before swap : []:[first,second] = " + ((PlayItem) currentPlaylist.getItemlist().get(firstIndex)).getName() +", "+ ((PlayItem)currentPlaylist.getItemlist().get(nextIndex)).getName()+" [][] " + ((PlayItem) currentPlaylist.getItemlist().get(firstIndex)).getPlayorder() +", "+ ((PlayItem)currentPlaylist.getItemlist().get(nextIndex)).getPlayorder());
		DLog.v("before swap : []:[first,second] = " + getmRows().get(firstIndex).getItem().getName() +", "+ getmRows().get(nextIndex).getItem().getName()+" [][] " + getmRows().get(firstIndex).getIndex() +", "+ getmRows().get(nextIndex).getIndex() );

		swapOrder(firstIndex, nextIndex, currentPlaylist.getItemlist());
		Collections.swap(currentPlaylist.getItemlist(), firstIndex, nextIndex);

		swapRowIndex(firstIndex, nextIndex, getmRows());
		Collections.swap(getmRows(), firstIndex, nextIndex);
		Collections.swap(getOriginalValue(), firstIndex, nextIndex);

		currentPlaylist = Utility.getCurrentPlayList(getContext());
		DLog.v("after swap : []:[first,second] = " + ((PlayItem) currentPlaylist.getItemlist().get(firstIndex)).getName() +", "+ ((PlayItem)currentPlaylist.getItemlist().get(nextIndex)).getName()+" [][] " + ((PlayItem) currentPlaylist.getItemlist().get(firstIndex)).getPlayorder() +", "+ ((PlayItem)currentPlaylist.getItemlist().get(nextIndex)).getPlayorder());
		DLog.v("after swap : []:[first,second] = " +  getmRows().get(firstIndex).getItem().getName() +", "+ getmRows().get(nextIndex).getItem().getName()+" [][] " + getmRows().get(firstIndex).getIndex() +", "+ getmRows().get(nextIndex).getIndex());

	}

	private void swapOrder(int firstIndex, int nextIndex, ArrayList<PlayItem> currentPlaylist) {
		PlayItem firstItem = (PlayItem)currentPlaylist.get(firstIndex);
		PlayItem secondItem = (PlayItem)currentPlaylist.get(nextIndex);

		int firstOrder = firstItem.getPlayorder();
		int secondOrder = secondItem.getPlayorder();

		firstItem.setPlayorder(secondOrder);
		secondItem.setPlayorder(firstOrder);

		currentPlaylist.set(firstIndex, firstItem);
		currentPlaylist.set(nextIndex, secondItem);
	}

	private void swapRowIndex(int firstIndex, int nextIndex, List<Row<?>> currentPlaylist) {
		Row firstRow = currentPlaylist.get(firstIndex);
		Row secondRow = currentPlaylist.get(nextIndex);

		int firstRowIndex  = firstRow.getIndex();
        int secondRowIndex = secondRow.getIndex();
		firstRow.setIndex(secondRowIndex);
		secondRow.setIndex(firstRowIndex);

		currentPlaylist.set(firstIndex, firstRow);
		currentPlaylist.set(nextIndex, secondRow);

	}
}
