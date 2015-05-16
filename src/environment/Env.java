package environment;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

public class Env
{
	public static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	public static boolean			SERVER_LAUNCHED=false;
	public static long				LAST_SERVER_SYNC=0;
	public static int 				SERVER_TIMEOUT;
	
	public static InetAddress 		SERVER_ADDRESS;
	public static int 				SERVER_PORT;
	public static int 				BUFFER_SIZE;
	
	public static void init()
	{
		try
		{
			//load configuration
			Properties prop = new Properties();
			InputStream input = new FileInputStream(IOUtil.getFile("config/connection.config"));
			prop.load(input);
			input.close();
			
			SERVER_ADDRESS = Inet4Address.getByName(prop.getProperty("serverAddress"));
			SERVER_PORT = Integer.parseInt(prop.getProperty("serverPort"));
			BUFFER_SIZE = Integer.parseInt(prop.getProperty("buffSize"));
			SERVER_TIMEOUT = Integer.parseInt(prop.getProperty("serverTimeout"));
		}
		catch(Exception ex)
		{
			System.out.println("Error @ Env >> Could not load configuration file");
			System.exit(0);
		}
		
	}
	
	public static boolean IS_SERVER_SYNC()
	{
		return ( (System.currentTimeMillis() - Env.LAST_SERVER_SYNC) < Env.SERVER_TIMEOUT );
	}
}
