package model;

import enums.MsgType;

public abstract class MsgModel extends model {
	
	public int id;
	public String srcNickname;
	public String destNickname;
	public MsgType msgType;
	public String content;
	public long timestamp;
	
}
