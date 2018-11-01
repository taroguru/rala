package workshop.soso.jickjicke.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class SoundFileExtensionFilter implements FilenameFilter {

	//todo. 읽어드릴 파일 확장자 목록은 db로 별도 설정.
//	public String soundfileExtensions_1[] = {".mp3", ".3gp", ".mp4", ".aac", ".flac", ".mid", ".xmf", ".mxmf", ".rtttl",
//											".rtx", ".ota", ".imy", ".ogg", ".wav", ".ts", ".mkv"};
	ArrayList<String> soundfileExtensions;
	
	public SoundFileExtensionFilter() {

		soundfileExtensions = new ArrayList<String>();
		
		soundfileExtensions.add(".mp3");
		soundfileExtensions.add(".3gp");
		soundfileExtensions.add(".mp4");
		soundfileExtensions.add(".m4a");
		soundfileExtensions.add(".aac");
		soundfileExtensions.add(".flac");
		soundfileExtensions.add(".mid");
		soundfileExtensions.add(".xmf");
		soundfileExtensions.add(".mxmf");
		soundfileExtensions.add(".rtttl");
		soundfileExtensions.add(".rtx");
		soundfileExtensions.add(".ota");
		soundfileExtensions.add(".imy");
		soundfileExtensions.add(".ogg");
		soundfileExtensions.add(".wav");
		soundfileExtensions.add(".ts");
		soundfileExtensions.add(".mkv");
		
	}
	@Override
	public boolean accept(File dir, String filename) {

		for(String extension : soundfileExtensions)
		{
			if(filename.toLowerCase().endsWith(extension)){
				return true;
			}
		}
		
		return false;
	}

}
