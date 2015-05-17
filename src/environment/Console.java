package environment;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.scene.control.TextArea;

public class Console
{
	public static TextArea	ConsoleTextArea;
	public static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
	
	public static void init(TextArea ConsoleTextArea)
	{
		Console.ConsoleTextArea= ConsoleTextArea;
	}
	
	public static synchronized void log(String s)
	{
		if(ConsoleTextArea==null)
			System.out.println("["+sdf.format(new Date())+"] "+s+"\n");
		else
			ConsoleTextArea.insertText(0, "["+sdf.format(new Date())+"] "+s+"\n");
	}
}
