package workshop.soso.jickjicke.file;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter {
	@Override
	public boolean accept(File pathname) {
		boolean ret = false;
		if(pathname != null)
			ret =  pathname.isDirectory();
		return ret;
	}
}
