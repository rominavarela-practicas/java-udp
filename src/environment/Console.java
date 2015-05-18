package environment;

import java.util.Date;

import javafx.scene.control.TextArea;

public class Console
{
	public static TextArea	ConsoleTextArea;
	
	public static void init(TextArea ConsoleTextArea)
	{
		Console.ConsoleTextArea= ConsoleTextArea;
	}
	
	public static synchronized void log(String s)
	{
		if(ConsoleTextArea==null)
			System.out.println("["+Env.dateFormat.format(new Date())+"] "+s+"\n");
		else
			ConsoleTextArea.insertText(0, "["+Env.dateFormat.format(new Date())+"] "+s+"\n");
	}
}
