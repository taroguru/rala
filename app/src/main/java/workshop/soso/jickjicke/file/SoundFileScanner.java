package workshop.soso.jickjicke.file;

import java.io.File;
import java.util.ArrayList;

import workshop.soso.jickjicke.Audio;
import workshop.soso.jickjicke.util.DLog;

public class SoundFileScanner {

	public static ArrayList<Audio> scanDirectoryNSoundFiles(String currentPath) {
		ArrayList<Audio> fileList;
		fileList = scanDirectory(currentPath, false);
		fileList.addAll(  scanSoundFile(currentPath, false) );
		return fileList;
	}
	
	public static ArrayList<Audio> scanSoundFile(String targetPath, boolean searchSubDirectory)
	{
		return scanFile(targetPath, searchSubDirectory, new SoundFileExtensionFilter());
	}
	
	public static ArrayList<Audio> scanDirectory(String targetPath, boolean searchSubDirectory)
	{
		return scanFile(targetPath, searchSubDirectory, new DirectoryFilter());
	}
	
	public static ArrayList<Audio> scanFile(String targetPath, boolean searchSubDirectory, Object fileFilter)
	{
		ArrayList<Audio> searchedFileList = new ArrayList<Audio>();

		File home = new File(targetPath);
		if(!home.isDirectory())
		{
			home = home.getParentFile();	//상위 폴더로 접근을 하는데 왜 하는지 모르겠네.. todo.
			DLog.v("fileinfo", "parent directory  = " + home.toString() );
		}
		File[] filteredFiles = null;
		//나중에 고치자 ㅡ_ㅠ... 객체지향 안령..
		if( fileFilter instanceof DirectoryFilter)
		{
			filteredFiles = home.listFiles((DirectoryFilter)fileFilter);
		}
		else
		{
			filteredFiles = home.listFiles((SoundFileExtensionFilter)fileFilter);
		}

		DLog.v("fileinfo", "scanned file list = " + String.valueOf(filteredFiles.length));
		
		if (filteredFiles.length > 0) {
			for (File file : filteredFiles) {
				Audio soundfile = new Audio();
				soundfile.setFile(file);
				searchedFileList.add(soundfile);
			}
		}

		//하위폴더 검색하기.
		return searchedFileList;
		
	}

}


