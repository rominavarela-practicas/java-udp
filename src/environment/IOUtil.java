package environment;

import java.io.File;

public class IOUtil {
	/**
	 * cross-platform relative path file
	 * @param name
	 * @return
	 */
	public static File getFile(String name)
	{		
		File f=new File(name);
				
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			return f;
		
		if(f.exists())
			return f;
		else
		{
			File ROOT	= new File(IOUtil.class.getResource("/").getFile());
			f=new File(ROOT.getAbsolutePath().replace("%20", " ")+"/"+name);

			return f;
		}
	}
}
