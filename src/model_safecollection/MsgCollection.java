package model_safecollection;

import model_impl.Msg;

public class MsgCollection extends safecollection<Msg>{
	
	public MsgCollection(boolean NotifyPush)
	{
		super.NotifyPush= NotifyPush;
	}

}
