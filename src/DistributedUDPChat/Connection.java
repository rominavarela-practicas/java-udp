package DistributedUDPChat;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import environment.Console;
import environment.Env;

/**
 * Connection model
 * <ul>
 * <li>serverAddress
 * <li>serverPort
 * <li>address
 * <li>port
 * <li>socket
 * <li>packet
 * </ul>
 * @author romina
 */
public class Connection
{
	
	public InetAddress 		address;
	public int 				port;
	public byte[] 			buf;
	
	public DatagramSocket 	socket;
	public DatagramPacket 	packet;
	
	/**
	 * Open a connection
	 * @param serverMode Server uses serverPort, client uses following ports
	 * @throws Exception
	 */
	public Connection(boolean serverMode) throws Exception
	{
		this.address = Env.SERVER_ADDRESS;
		this.port = serverMode ? Env.SERVER_PORT : Env.SERVER_PORT+1;
		this.buf= new byte[Env.BUFFER_SIZE];
		
		//open connection
		while(true)
			try
			{
				socket = new DatagramSocket(port);
				packet = new DatagramPacket(buf, buf.length);
				Console.log("Connection opened in port "+port);
				break;
			}
			catch(Exception ex)
			{
				if((port >= 0xFFFF))
				{
					Console.log("Error @ Connection >> Available port numbers exhausted");
					System.exit(0);
				}
				else
				{
					Console.log("Port "+port+" unavailable");
					if(serverMode)
						throw ex;
					else
						port++;
				}
			}
		
	}
	
	public boolean isServer() {
		return this.port == Env.SERVER_PORT;
	}
}