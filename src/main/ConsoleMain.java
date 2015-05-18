package main;

import javax.swing.JOptionPane;

import DistributedUDPChat.DistributedUDPChatClient;

public class ConsoleMain {
	public static void main(String[] args)
	{
		try
		{
			String nickname= JOptionPane.showInputDialog("your nickname");
			new DistributedUDPChatClient(nickname);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
