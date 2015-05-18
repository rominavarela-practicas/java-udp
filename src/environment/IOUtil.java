package environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

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
	
	public static void write(File f, String s) throws Exception
	{
		BufferedWriter writer= new BufferedWriter(new FileWriter(f));
		writer.write(s);
		writer.close();
	}
	
	public static void write(File f, byte[] data) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.close();
	}
	
	public static String read(File f) throws Exception
	{
		String s="";
		
		BufferedReader reader= new BufferedReader(new FileReader(f));
		while(reader.ready())
			s+=reader.readLine();
		reader.close();
		
		return s;
	}
}
