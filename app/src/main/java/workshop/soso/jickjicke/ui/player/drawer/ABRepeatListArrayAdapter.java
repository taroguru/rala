package workshop.soso.jickjicke.ui.player.drawer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import workshop.soso.jickjicke.ABRepeat;
import workshop.soso.jickjicke.ABRepeatList;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.intent.EXTRA_VALUE;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.PopupHelper;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;


public class ABRepeatListArrayAdapter extends AbstractMediaArrayAdapter<ABRepeat> {

	private final String LOG_TAG = "ABRepeatListAdapter";
	public static final int ABREPEATTYPE = 1;
	public static final int CHILD = ABREPEATTYPE;
//	private OnPlaySoundListener soundListener;
	private workshop.soso.jickjicke.ui.player.drawer.ABRepeatFragment ABRepeatFragment;

//	public void setOnPlaySoundListener(OnPlaySoundListener listener)
//	{
//		soundListener = listener;
//	}

	public ABRepeatListArrayAdapter(Context context, ArrayList<Row<?>> data)
	{
		super(context);
		setmRows(data);
	}

	@Override
	public BaseViewHolder onCreateViewHolder(ViewGroup parent, int type) {
		View view = null;
		BaseViewHolder vh = null;

		LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.player_drawer_abrepeat_item, parent, false);
		vh = new ABRepeatViewHolder(view);
		return vh;
	}

	@Override
	public void onBindViewHolder(BaseViewHolder holder, final int position) {
		final ABRepeat abrepeat = getItem(position);
		ABRepeatViewHolder audioViewHolder = (ABRepeatViewHolder) holder;
		audioViewHolder.onBindView(abrepeat, position);

		((CardView)(audioViewHolder.getTxtName().getParent().getParent())).setOnClickListener(v -> {
			Intent onABRepeatMode = new Intent(ACTION.OnABRepeatMode);
			onABRepeatMode.putExtra(EXTRA_VALUE.IsABRepeatMode, true);
			onABRepeatMode.putExtra(EXTRA_VALUE.ABRepeatPosition, position);
			Utility.sendIntentLocalBroadcast(getContext(), onABRepeatMode);
			Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_ABREPEATLIST), CONSTANTS.EVENT_LISTITEM_CLICK);
        });

		((CardView)(audioViewHolder.getTxtName().getParent().getParent())).setOnLongClickListener( v->{
			showChildPopupMenu(v, position);
			Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_ABREPEATLIST), CONSTANTS.EVENT_LISTITEM_LONGCLICK);
			return false;
		});

		audioViewHolder.getImgMoreButtonClickArea().setOnClickListener( v->{
				showChildPopupMenu(v, position);
				Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_ABREPEATLIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK);

		});

		setAnimation( audioViewHolder.itemView, position);
	}


	public void showChildPopupMenu(final View view, final int childPosition)
	{
		Context context = getContext();
		PopupMenu popup = new PopupMenu(context, view);

		((MainActivity)(context)).getMenuInflater().inflate(R.menu.abrepeat_item_popup, popup.getMenu());

		popup.setOnMenuItemClickListener(item -> {
            switch(item.getItemId())
            {
                case R.id.delete_abrepeat:
                {
                    String msg = String.format("remove abrepeat(%d)", childPosition);
                    DLog.v(msg);
                    ABRepeatList abRepeatList = Utility.getABRepeatList(getContext());
                    ABRepeat deletingItem = (ABRepeat) abRepeatList.getItemlist().get(childPosition);
                    String abrepeatName = deletingItem.getName();
                    int count = DBHelper.deleteABRepeat(getContext(), deletingItem);

                    String sentence = getContext().getString(R.string.playitem_is_removed);
                    sentence = sentence.replace("#_#_#", abrepeatName);
                    //update statemanager
                    abRepeatList.remove(childPosition, getContext());

                    getmRows().remove(childPosition);
                    notifyItemRemoved(childPosition);
                    notifyItemRangeChanged(childPosition, getmRows().size());
//						notifyDataSetChanged();

                    if(getCount() == 0)
                    {
                        getABRepeatFragment().onDataSetChanged();
                    }
					ShortTask.showSnack(context, sentence);
                }
                break;

                default:
                    break;
            }
            return false;
        });
		PopupHelper.showPopupWithIcon(context, popup, view);
	}

	public void setABRepeatFragment(ABRepeatFragment ABRepeatFragment) {
		this.ABRepeatFragment = ABRepeatFragment;
	}

	public workshop.soso.jickjicke.ui.player.drawer.ABRepeatFragment getABRepeatFragment() {
		return ABRepeatFragment;
	}
}

