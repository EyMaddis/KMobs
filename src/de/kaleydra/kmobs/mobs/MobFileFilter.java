package de.kaleydra.kmobs.mobs;

import java.io.File;
import java.io.FileFilter;

public class MobFileFilter implements FileFilter{
	
	@Override
	public boolean accept(File file) {
		if (file.isDirectory() || file.getName().endsWith(getExtension()))
        {
             return true;
        }
        return false;
	}
	public static String getExtension(){
		return ".mob";
	}
}
