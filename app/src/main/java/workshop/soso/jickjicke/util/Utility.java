package workshop.soso.jickjicke.util;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import workshop.soso.jickjicke.ABRepeatList;
import workshop.soso.jickjicke.CONSTANTS;
import workshop.soso.jickjicke.ItemList;
import workshop.soso.jickjicke.PlayList;
import workshop.soso.jickjicke.StateManager;
import workshop.soso.jickjicke.intent.ACTION;
import workshop.soso.jickjicke.sound.MediaPlayerStateMachine;

/**
 * Created by taroguru on 2015. 1. 30..
 */
public class Utility {
    public static final String LOG_TAG = "Utility";
    private static PlayList currentPlaylist;

    public static StateManager getStateManager(Context context){
        return ((StateManager)context.getApplicationContext());
    }

    public static int getCurrentPosition(Context context){
        int currentPosition = 0;
        try {
            currentPosition = ((StateManager)context.getApplicationContext()).getCurrentPosition();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return currentPosition;
    }


    public static void setCurrentPosition(Context context, int position){
        ((StateManager)context.getApplicationContext()).setCurrentPosition(position);
    }

    public static ArrayList<PlayList> getEntirePlayList(Context context){
        return ((StateManager)context.getApplicationContext()).getEntirePlayList();
    }
    public static void setEntirePlayList(Context context, ArrayList<PlayList> playlists)
    {
       ((StateManager)context.getApplicationContext()).setEntirePlayList(playlists);
    }

    public static void sendBroadcastPlayNewItem(Context context, int playlistIndex, int position)
    {
        Intent intent = new Intent(ACTION.PlayNewItem);
        intent.putExtra(ACTION.Position, position );
        intent.putExtra(ACTION.PlaylistPosition, playlistIndex);
        Utility.sendIntentLocalBroadcast(context, intent);
    }

    //current playlist의 아이템을 실행.
    public static void sendBroadcastPlayLastItem(Context context)
    {
        PlayList currentPlaylist = getCurrentPlayList(context);

        int position = currentPlaylist.size()-1;
        if(position >= 0)
        {
            Intent intent = new Intent(ACTION.PlayAudio);
            intent.putExtra(ACTION.Position, currentPlaylist.size()-1);
            intent.putExtra(ACTION.PlaylistPosition, (int)currentPlaylist.getId());
            Utility.sendIntentLocalBroadcast(context, intent);
        }
        else
        {
            DLog.v("CurrentPlaylist Is Empty. Maybe database is loading. TOBECHANGING!!!");
        }
    }

    /**
     * currentplaylist의 position위치 곡 재생
     * @param context
     * @param position
     */
    public static void sendBroadcastPlayAudio(Context context, int position)
    {
        PlayList currentPlaylist = getCurrentPlayList(context);

        Intent intent = new Intent(ACTION.PlayAudio);
        intent.putExtra(ACTION.Position, position );
        intent.putExtra(ACTION.PlaylistPosition, (int)currentPlaylist.getId());
        Utility.sendIntentLocalBroadcast(context, intent);
    }

    public static void sendBroadcastPlayAudio(Context context, int playlistIndex, int position)
    {
        Intent intent = new Intent(ACTION.PlayAudio);
        intent.putExtra(ACTION.Position, position );
        intent.putExtra(ACTION.PlaylistPosition, playlistIndex);
        Utility.sendIntentLocalBroadcast(context, intent);
    }


    public static int getCurrentPlayListPosition(Context context){
        return ((StateManager)context.getApplicationContext()).getCurrentPlayListPosition();
    }

    public static void setCurrentPlaylist(Context context, PlayList currentPlaylist) {
        ((StateManager)context.getApplicationContext()).setCurrentPlaylist(currentPlaylist);
    }


    public static PlayList getCurrentPlayList(Context context){
        return ((StateManager)context.getApplicationContext()).getCurrentPlayList();
    }

    public static ABRepeatList getABRepeatList(Context context) {
        try{
            return ((StateManager)context.getApplicationContext()).getAbRepeatList();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        return null;
    }

    public static String convertMsecToMin(int mSec){
        String outputString;

        int minute = mSec / 1000 / 60;
        int second = (mSec - (1000 * 60 * minute)) / 1000;

        if(minute < 60)
        {
            outputString  = String.format("%d:%02d", minute, second);
        }
        else
        {
            int hour = minute / 60 ;
            minute = (minute - hour  * 60);

            outputString =  String.format("%d:%02d:%02d", hour, minute, second);
        }

        return outputString;
    }

    // ##:##.##
    public static String changeMSecToMSec(int totalMSec) {
        return changeMSecToMSec(totalMSec, 2);
    }

    public static String changeMSecToMSec(int totalMSec, int pointCount) {

        int min = totalMSec / (1000 * 60);
        int sec = (totalMSec - min * (1000 * 60)) / 1000;
        int msec = (totalMSec - (min * (1000 * 60) + sec * 1000)) / 100;

        String formatString ="%02d:%02d.%0"+String.valueOf(pointCount)+"d";
        String mSec = String.format(formatString, min, sec, msec);

        return mSec;
    }

    public static void launchAppFirstTime(Context context) {
        ((StateManager)context.getApplicationContext()).launchAppFirstTime();
    }

    public static long getCurrentPlayListIndex(Context context) {
        return getStateManager(context).getCurrentPlayListID();
    }

    public static PlayList getPlayList(Context context, int playlistPosition) {
        return  getStateManager(context).getPlayList(playlistPosition);
    }

    public static PlayList getPlayList(Context context, long playlistId) {
        return  getStateManager(context).getPlayList(playlistId);
    }

    /***
     * 파일의 전체 경로로부터 폴더 경로만 뽑아낸다
     * @param fullpath 파일 전체 경로
     * @return 폴더 경로
     *
     *
     *  **0*\*1*.*2*
     */
    public static String parseFolderPath(String fullpath) {
        DLog.v("Parsing Path : "+fullpath);
        String path ="";
        Pattern pattern = Pattern.compile("^(.+)/([^/]+)$");
        Matcher matcher = pattern.matcher(fullpath);
        if (matcher.find())
        {
            path = matcher.group(1);    //0 = entire string, 1 = first group, 2 = second group ...
        }

        return path;
    }

    public static String getNameFromFolderPath(String folder) {
        String path ="";
        Pattern pattern = Pattern.compile("^(.+)/([^/]+)$");
        Matcher matcher = pattern.matcher(folder);
        if (matcher.find())
        {
            path = matcher.group(2);
        }
        DLog.d(matcher.group());
        int size = matcher.groupCount();
        for(int i = 0; i < size; ++i)
        {
            DLog.d(String.format("%d th group is : %s", i , matcher.group(i+1)) );
        }
        return path;
    }

    public static void showMarket(Context parent) {
        final String appPackageName = parent.getPackageName(); // getPackageName() from Context or Activity object
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            parent.startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            parent.startActivity(intent);
        }
    }

    public static void showMovieAdvertisement() {
        //todo.
    }

    public static void setEmptyText(boolean empty, RecyclerView recyclerView, View emptyShowingView) {
        if(empty)
        {
            emptyShowingView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            emptyShowingView.setVisibility(View.GONE);
        }
    }

    public static boolean hasItem(ItemList itemList, int position) {
        boolean result = false;
        if( itemList != null && position >= 0 && itemList.getItemlist() != null && itemList.getItemlist().size() > position )
        {
            result = true;
        }
        return result;
    }

    public static int getCurrentABRepeatPosition(Context context) {
        return getStateManager(context).getCurrentABRepeatPosition();
    }

    public static void setCurrentABRepeatPosition(Context context, int position) {
        getStateManager(context).setCurrentABRepeatPosition(position);
    }

    public enum CopyType {BACKUP, RESTORE}    ;  //BACKUP : data->sdcard, RESTORE : sdcard->data
    public static void copyDBFile(String dbPath, String sdcardPath, CopyType type) {
        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

//            if (sd.canWrite()) {
                File dbInData  = new File(data, "//data//workshop.soso.jickjicke//databases/sound.db");
                File dbInSdcard = new File(sd, "DBBackup/sound.db");
                File currentDB;
                File backupDB;

                FileChannel src;
                FileChannel dst;
                if(type == CopyType.BACKUP)
                {
                    //dtata->sd
                    src = new FileInputStream( new File(data, dbPath)).getChannel();
                    dst = new FileInputStream( new File(sd, sdcardPath)).getChannel();
                }
                else
                {
                    //sd->data
                    src = new FileInputStream( new File(sd, sdcardPath)).getChannel();
                    dst = new FileInputStream( new File(data, dbPath)).getChannel();
                }

                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                DLog.v(LOG_TAG, "DB is Copyed");

//            }
//            else
//            {
//                DLog.v(LOG_TAG, "Copy failed");
////            }
        } catch (Exception e) {
            DLog.e(LOG_TAG, "DB is failed exception" );
            DLog.e(LOG_TAG, e.toString());
        }
    }

    public static void forTestCopyDBFileToSDCard(Context context) {
        try {


            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                File BackupDir = new File(sd, "DBBackup");
                BackupDir.mkdir();

                File currentDB = new File(data, "//data//workshop.soso.jickjicke//databases/sound.db");
                File backupDB = new File(sd, "DBBackup/sound.db");

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                ShortTask.showSnack(context, "DB is Copyed");

            }
        } catch (Exception e) {
            ShortTask.showSnack(context, "Exception is displayed.");
        }
    }

    public static void sendIntentLocalBroadcast(Context context, String action)
    {
        try{
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
            Intent intent = new Intent(action);
            broadcaster.sendBroadcast(intent);
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    public  static void sendIntentLocalBroadcast(Context context, Intent intent)
    {
        try{
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
            broadcaster.sendBroadcast(intent);
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    public static long getDuration(String filePath)
    {
        long dur = 0;
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try
        {
            //filePath is media file.
            metaRetriever.setDataSource(filePath);  //filepath가 이상하면 IllegalArgumentException을 던져요.

            // convert duration to minute:seconds
            String hasAudio = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            if(hasAudio != null && !hasAudio.isEmpty())
            {
                DLog.v(String.format("%s has Audio meta data:%s",filePath, hasAudio));
                if(hasAudio.toLowerCase().equals("yes"))
                {
                    String duration =
                            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    if(duration != null && !duration.isEmpty()) {
                        DLog.v(filePath + "-duration : " + duration);
                        dur = Long.parseLong(duration);
                    }
                }
            }
            metaRetriever.release();
        }
        catch(IllegalArgumentException exception)
        {
            DLog.v(String.format("%s = %s", filePath, exception.toString()));
        }
        catch(RuntimeException exception)
        {
            DLog.v(String.format("%s = %s", filePath, exception.toString()));
        }


        return dur;
    }

    public static int convertDpToPx(Context context, float dp) {
        try{
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        return 0;
    }

    public static float getDentity(Context context)
    {
        try{
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return metrics.density;
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        return 0;
    }

    public static float convertDpToPx(float dp, float density)
    {
        return dp*density;
    }

    public static int convertDpToPx(int dp, float density)
    {
        return (int)(dp*density+0.5);
    }

    public static boolean isPlayButtonState(MediaPlayerStateMachine.State state)
    {
        boolean result = false;

        if(state == MediaPlayerStateMachine.State.PAUSED
                || state == MediaPlayerStateMachine.State.STOPPED
                || state == MediaPlayerStateMachine.State.END
                || state == MediaPlayerStateMachine.State.ERROR)
        {
            result = true;
        }

        return result;
    }


    public static Bitmap getAlbumart(Context context, Long album_id) {
        Bitmap albumArtBitMap = null;
        Resources resource = null;

        try {
            resource = context.getResources();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(options, dpToPixels(24, resource), dpToPixels(24, resource));     // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;


            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFileDescriptor(fd, null,
                        options), CONSTANTS.THUMBNAIL_SIZE, CONSTANTS.THUMBNAIL_SIZE);
                pfd = null;
                fd = null;
            }
        } catch(FileNotFoundException e){
            DLog.v(LOG_TAG, album_id.toString() + "'s album art image file is not found.");
        }
        catch (NullPointerException e){
            DLog.v(LOG_TAG, album_id.toString() + "'s album art is not found.");
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error ee) {
        }

        if (null != albumArtBitMap) {
            return albumArtBitMap;
        }

        return getDefaultAlbumArtEfficiently(resource);
    }


    public static int dpToPixels(float dp, Resources resources){
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int)px;
    }

    public static Bitmap getDefaultAlbumArtEfficiently(Resources resource) {
//        Bitmap defaultBitmapArt = null;
//
//        if (defaultBitmapArt == null) {
//            defaultBitmapArt =
//        }
        return null;//decodeSampledBitmapFromResource(resource,R.drawable.ic_audiotrack_black_24dp, dpToPixels(24, resource), dpToPixels(24, resource));
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);     // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resId, options);

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {     // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void showDeveloperPage(Context activity)
    {
        //FLAG_ACTIVITY_NEW_TASK
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.naver.com/ateliersoso/221093575334" ));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean hasPermission(Context context, String permissionName)
    {
        boolean result = true;
        int permissionCheck = ContextCompat.checkSelfPermission(context, permissionName);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            result = false;
        }

        return result;
    }
    public static void sendEventGoogleAnalytics(Context context, String title, String message)
    {
        try{
            StateManager stateManager = Utility.getStateManager(context);
            stateManager.sendEventGoogleAnalytics(title , message );
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

}
