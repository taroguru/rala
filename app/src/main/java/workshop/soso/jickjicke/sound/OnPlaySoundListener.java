package workshop.soso.jickjicke.sound;

/**
 * Created by taroguru on 2015. 4. 21..
 */
public interface OnPlaySoundListener {
    public static final int FILE_STATE_NOT_GOOD = -1;
    /**
     * 플레이리스트의 특정 아이템을 재생
     * @param playlistIndex 플레이할 플레이리스트 인덱스
     * @param playOffset    플레이리스트 내의 인덱스
     */
    boolean onPlaySoundTrack(int playlistIndex, int playOffset);

    /**
     * 현재 플레이리스트의 아이템을 재생
     * @param playOffset
     */
    boolean onPlaySoundTrack(int playOffset);

    /**
     * 특정 abrepeat의 시작점으로 이동
     * @param position
     */
    void onMoveToABRepeat(int position);

    /**
     * onmovetoabrepeat과 동일. duplicated source
     * @param position
     */
    boolean onPlayABRepeat(int position);

    /**
     * 일지중지
     */
    void onPausePlayingMusic();

    /**
     * 현재 플레이리스트의 다음 아이템 재생
     */
    boolean onNextTrack();

    /**
     * 현재 플레이리스트의 이전 파일 재생
     */
    boolean onPrevTrack();

    /**
     * 재생 중지
     */
    boolean onStopPlay();

    /**
     * 현재 플레이 중인 파일의 특정 위치로 이동
     * @param position 특정 위치. millisecound(ms) 단위.
     */
    boolean onSeekTo(int position);

    /**
     * 현재 재생 중인 위치 반환
     * @return 위치(ms)
     */
    int onGetCurrentPosition();

    /**
     * 현재 mediaplayer 재생 여부
     * @return true/false
     */
    boolean onIsNowPlaying();

    /**
     * 일시 중지된 아이템 재생
     */
    boolean onResumePausedMusic();

    /**
     * 재생 중인 아이템 길이 반환
     * @return 길이. millisecond(ms).
     */
    int onGetDuration();

    /**
     * 이전 반복구간 재생
     */
    boolean onPlayPreviousABRepeat();

    /**
     * 다음 반복구간 재생
     */
    boolean onPlayNextABRepeat();
    /*
    한곡 반복 모드 설정
    */
    void onChangeLoopMode(boolean isLoop);

    /*
    * player statemachine의 상태 반환
    * */
    MediaPlayerStateMachine.State onGetPlayerState();


}

