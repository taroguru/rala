package workshop.soso.jickjicke;

/**
 * Created by taroguru on 2016. 7. 23..
 */
public class CONSTANTS {
    //animation duration
    public static final int DURATION_LISTITEM_VISIBLE_SCROLL_DOWN = 300;
    public static final int DURATION_LISTITEM_VISIBLE_SCROLL_UP = DURATION_LISTITEM_VISIBLE_SCROLL_DOWN;//(int)(DURATION_LISTITEM_VISIBLE_SCROLL_DOWN/2);
    public static final int DURATION_START_ACTIVITY_TITLE_BOUNCE = 250;
    public static final int DURATION_BOTTOM_PLAYER_VISIBLE = 250;

    //page index
    public static final int PAGE_ABREPEATLIST = 0;
    public static final int PAGE_PLAYER = 1;
    public static final int PAGE_CURRENT_PLAYLIST = 2;
    public static final int PAGE_FOLDER_LIST = 3;
    public static final int PAGE_ALL_AUDIO_LIST = 4;
    public static final int PAGE_PLAYLIST = 5;

    public static String screenName(int pageNumber)
    {
        String thisScreen = "NOPAGE";
        switch(pageNumber)
        {
            case PAGE_ABREPEATLIST:
                thisScreen = "PAGE_ABREPEATLIST";
                break;
            case PAGE_PLAYER:
                thisScreen = "PAGE_PLAYER";
                break;
            case PAGE_CURRENT_PLAYLIST:
                thisScreen = "PAGE_CURRENT_PLAYLIST";
                break;
            case PAGE_FOLDER_LIST:
                thisScreen = "PAGE_FOLDER_LIST";
                break;
            case PAGE_ALL_AUDIO_LIST:
                thisScreen = "PAGE_ALL_AUDIO_LIST";
                break;
            case PAGE_PLAYLIST:
                thisScreen = "PAGE_PLAYLIST";
                break;
        }

        return thisScreen;
    }

    public static final String NOTICHANNELID = "SoundServiceNotification";
    public static final int THUMBNAIL_SIZE = 150;


    public static final String EVENT_BUTTON_CLICK = "Click Button";
    public static final String EVENT_LISTITEM_CLICK = "Click ListItem";
    public static final String EVENT_LISTITEM_LONGCLICK = "Longclick ListItem";
    public static final String EVENT_LISTITEM_MORE_CLICK = "Click ListItem More Button";
}
