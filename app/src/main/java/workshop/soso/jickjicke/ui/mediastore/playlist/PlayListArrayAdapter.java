package workshop.soso.jickjicke.ui.mediastore.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.PlayItem;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.R;
import workshop.soso.jickjicke.db.DBHelper;
import workshop.soso.jickjicke.ui.mediastore.AbstractMediaArrayAdapter;
import workshop.soso.jickjicke.ui.player.MainActivity;
import workshop.soso.jickjicke.ui.player.drawer.OnDataSetChangedListener;
import workshop.soso.jickjicke.ui.util.BaseViewHolder;
import workshop.soso.jickjicke.ui.util.CurrentPlaylistReseter;
import workshop.soso.jickjicke.ui.util.ItemExtender;
import workshop.soso.jickjicke.ui.util.MultiItemAdapter;
import workshop.soso.jickjicke.ui.util.PopupHelper;
import workshop.soso.jickjicke.util.DLog;
import workshop.soso.jickjicke.util.ShortTask;
import workshop.soso.jickjicke.util.Utility;


public class PlayListArrayAdapter extends AbstractMediaArrayAdapter implements OnDataSetChangedListener {

    private final String LOG_TAG = PlayListArrayAdapter.class.toString();

    public static final int PLAYLISTTYPE = 1;
    public static final int ITEMTYPE = 2;
    public static final int HEADER = PLAYLISTTYPE;
    public static final int CHILD = ITEMTYPE;
    private PlayListFragment playlistFragment;


    public PlayListArrayAdapter(Context context, ArrayList<MultiItemAdapter.Row<?>> childList) {
        super(context);
        setmRows(childList);
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = null;
        BaseViewHolder vh = null;

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (type) {
            case PLAYLISTTYPE: {
                view = inflater.inflate(R.layout.mediastore_arrayitem_playlist, parent, false);
                vh = new PlaylistItemViewHolder(view);
            }
            break;
            case ITEMTYPE: {
                view = inflater.inflate(R.layout.mediastore_arrayitem_playitem, parent, false);
                vh = new PlayitemViewHolder(view, getContext());
            }
            break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {
        final Row item = (Row) getmRows().get(position);
        int index = item.getIndex();

        switch (item.getItemViewType()) {
            case PLAYLISTTYPE: {
                //데이터를 긁어서
                final PlayList playlist = (PlayList) item.getItem();
                PlaylistItemViewHolder viewHolder = (PlaylistItemViewHolder) holder;

                //뷰에 붙이고
                viewHolder.refferalItem = item;
                viewHolder.setExtended(item.isExtened());
                viewHolder.onBindView(playlist, index);

                //뷰의 이벤트를 붙임
                ((CardView) (viewHolder.getImgExpandToggle().getParent().getParent())).setOnClickListener(new ItemExtender(item, viewHolder, this));
                ((CardView) (viewHolder.getImgExpandToggle().getParent().getParent())).setOnLongClickListener(v->{
                    showGroupPopupMenu(v, item, position);
                    Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_PLAYLIST), CONSTANTS.EVENT_LISTITEM_LONGCLICK+" Playlist");
                    return false;

                });

                viewHolder.getImgPlayButtonClickArea().setOnClickListener(new CurrentPlaylistReseter(getContext(), playlist));
                viewHolder.getImgMoreButtonClickArea().setOnClickListener(v->{
                    showGroupPopupMenu(v, item, position);    //position은 절대좌표
                    Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_PLAYLIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK+" Playlist");
                });

            }
            break;
            case ITEMTYPE: {
                final PlayItem playitem = (PlayItem) item.getItem();
                PlayitemViewHolder viewholder = (PlayitemViewHolder) holder;
                viewholder.setContext(getContext());
                viewholder.onBindView(playitem, index);

                viewholder.getEntireCardView().setOnClickListener(v -> {
                    insertToCurrentPlaylistAndPlay(playitem.getAudio());
                    Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_PLAYLIST), CONSTANTS.EVENT_LISTITEM_CLICK+" Playitem");
                });
                viewholder.getEntireCardView().setOnLongClickListener(v -> {
                    showChildPopupMenu(v, item, position);
                    Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_PLAYLIST), CONSTANTS.EVENT_LISTITEM_LONGCLICK +" Playitem");
                    return false;
                });
                viewholder.getImgMoreButtonClickArea().setOnClickListener(v -> {
                    showChildPopupMenu(v, item, position);
                    Utility.sendEventGoogleAnalytics(getContext(), CONSTANTS.screenName(CONSTANTS.PAGE_PLAYLIST), CONSTANTS.EVENT_LISTITEM_MORE_CLICK+" Playitem");
                });
            }
            break;
            default:
                break;
        }
        setAnimation(holder.itemView, position);
    }

    public void showChildPopupMenu(final View view, final Row<PlayItem> clickitem, final int position) {
        final Context context = getContext();
        PopupMenu popup = new PopupMenu(context, view);
        final PlayItem playitem = clickitem.getItem();

        ((MainActivity) (context)).getMenuInflater().inflate(R.menu.playlist_child_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.delete_playitem) {
                    String msg = String.format("remove playitem(%d).", position) + playitem.toString();
                    DLog.v(msg);

                    String playItemName = playitem.getName();
                    int count = DBHelper.deletePlayItem(context, playitem.getPlaylistId(), playitem.getId());
                    if (count > 0)//실제로 삭제 수행시
                    {
                        //화면 표시
                        String sentence = context.getString(R.string.playitem_is_removed);
                        sentence = sentence.replace("#_#_#", playItemName);
                        ShortTask.showSnack(context, sentence);

                        //adapter내부 index다시 계산
                        recalculateIndex(position, ITEMTYPE);

                        //update statemanager
                        PlayList playlistInEntire = Utility.getPlayList(context, (long) playitem.getPlaylistId());
                        playlistInEntire.getItemlist().remove(playitem);

                        //row정리.
                        removeRow(clickitem);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getmRows().size());

                        if (isEmpty()) {
                            playlistFragment.onDataSetChanged();    //empty text 표현 위해서.
                        }

                    } else {
                        DLog.e("playitem remove failed! PLAYLIST ID : " + playitem.getPlaylistId() + ", playitem id : " + playitem.getId());
                    }
                }
                return false;
            }
        });
        PopupHelper.showPopupWithIcon(getContext(), popup, view);

    }

    public void recalculateIndex(int position, int type) {
        if (type == ITEMTYPE) {
            for (int i = position; i < getmRows().size(); ++i) {
                Row item = getmRows().get(i);

                if (item.getItemViewType() == ITEMTYPE) {
                    item.setIndex(item.getIndex() - 1);
                } else    //ITEMTYPE이 아니면 해당 플레이리스트의 끝이므로 인덱스 조정 끝.
                {
                    break;
                }
            }
        } else if (type == PLAYLISTTYPE) {
            for (int i = position; i < getmRows().size(); ++i) {
                Row item = getmRows().get(i);

                if (item.getItemViewType() == PLAYLISTTYPE) {
                    item.setIndex(item.getIndex() - 1);
                }
            }
        }
    }

    public void showGroupPopupMenu(final View view, final Row<PlayList> clickedItem, final int position) {
        final Context context = getContext();
        PopupMenu popup = new PopupMenu(context, view);
        final PlayList playlist = clickedItem.getItem();

        ((MainActivity) (context)).getMenuInflater().inflate(R.menu.playlist_group_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.play_playlist) {
                    CurrentPlaylistReseter.resetCurrentPlaylist(context, playlist);
//                        String sentence = getContext().getString(R.string.playlist_is_playing);
//                        sentence = sentence.replace("##", playlist.getName());
//
//                        Snackbar.make(view, sentence, Snackbar.LENGTH_LONG).show();
                } else if (itemId == R.id.delete_playlist) {
                    PlayList deletingList = playlist;
                    String playlistName = deletingList.getName();
                    int count = DBHelper.deletePlaylist(context, deletingList.getId());

                    String sentence = context.getString(R.string.playlist_is_removed);
                    sentence = sentence.replace(new String("playlistname"), playlistName);
                    //adapter내부 index다시 계산
                    recalculateIndex(position, PLAYLISTTYPE);

                    //update statemanager
                    Utility.getEntirePlayList(context).remove(deletingList);
                    ShortTask.showSnack(context, sentence);

                    getmRows().remove(clickedItem);

                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getmRows().size());    //index 갱신

                    if (isEmpty()) {
                        playlistFragment.onDataSetChanged();    //empty text 표현 위해서.
                    }
                }

                return false;
            }
        });
        popup.show();
    }

    public void addChild(PlayList playlist) {
        try {
            int playlistIndex = 0;
            for (Row item : getmRows()) {
                if (item.getItemViewType() == PLAYLISTTYPE) {
                    ++playlistIndex;
                }
            }
            Row row = Row.create(playlistIndex, PLAYLISTTYPE, playlist);
            addRow(row);
            int count = getCount();
            notifyItemInserted(count);
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }

    }

    private Row getPlayListRow(int playlistPosition) {
        Row playlistRow = null;

        int playlistindexInRow = -1;
        int indexInRow = 0;
        for (indexInRow = 0; indexInRow < getmRows().size(); ++indexInRow) {
            Row item = getmRows().get(indexInRow);
            if (item.getItemViewType() == PLAYLISTTYPE) {
                ++playlistindexInRow;

                if (playlistindexInRow == playlistPosition) {
                    playlistRow = getmRows().get(indexInRow);        //여기. 여여기다! 열어 뒀으면 playlist를 땡겨올 수 있음. 망하는 것.
                }
            }
        }

        return playlistRow;
    }

    //플레이리스트의 실제 위치가 어디인지 return하는 함수.
    private int getPlayListPositionInRow(int playlistIndex) {
        int playlistindexInRow = -1;
        int indexInRow = 0;
        for (indexInRow = 0; indexInRow < getmRows().size(); ++indexInRow) {
            Row item = getmRows().get(indexInRow);
            if (item.getItemViewType() == PLAYLISTTYPE) {
                ++playlistindexInRow;

                if (playlistindexInRow == playlistIndex) {
                    break;
                }
            }
        }

        return indexInRow;
    }

    public void addChild(int playlistIndex, PlayItem playitem) {

        try {
            Row newPlayItemRow = Row.create(playlistIndex, ITEMTYPE, playitem);
            int playlistRowPosition = getPlayListPositionInRow(playlistIndex);
            Row playlistRow = getmRows().get(playlistRowPosition);
            PlayList playlist = (PlayList) playlistRow.getItem();

            //열려있으면 마지막 자식 다음 위치에 넣음
            if (playlistRow.isExtened()) {
                //child(playitem이 들어갈 위치 계산)
                DLog.v("playlistrowposition, playlistsize() : " + playlistRowPosition+", "+playlist.size());
                int posToInsert = playlistRowPosition + playlist.size();

                addRow(posToInsert, newPlayItemRow);
                //getmRows().add(posToInsert, newPlayItemRow);
                notifyItemInserted(posToInsert);
            } else    //닫혀있으면 숨겨진 차일드로 추가
            {
                playlistRow.addChild(newPlayItemRow);
            }
        } catch (java.lang.ClassCastException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onDataSetChanged() {
        List<Row<?>> mRows = new ArrayList();
        ArrayList<PlayList> entireplaylist = Utility.getEntirePlayList(getContext());

        int playlistIndex = 0;
        for (PlayList playlist : entireplaylist) {
            MultiItemAdapter.Row row = MultiItemAdapter.Row.create(playlistIndex, PlayListArrayAdapter.HEADER, playlist);
            int playitemIndex = 0;
            for (PlayItem playitem : (List<PlayItem>) playlist.getItemlist()) {

                MultiItemAdapter.Row childrow = MultiItemAdapter.Row.create(playitemIndex++, PlayListArrayAdapter.CHILD, playitem);
                row.addChild(childrow);
            }

            mRows.add(row);

            ++playlistIndex;
        }
        setmRows(mRows);

        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return getmRows().isEmpty();
    }

    public void setPlaylistFragment(PlayListFragment playlistFragment) {
        this.playlistFragment = playlistFragment;
    }
}

