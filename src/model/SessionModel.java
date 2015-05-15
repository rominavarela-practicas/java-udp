package model;

import java.net.InetAddress;

public abstract class SessionModel extends model{
	
	public int			ID;
	public String 		nickname;
	public InetAddress 	address;
	public int 			port;
	public long			timestamp;
	
}
