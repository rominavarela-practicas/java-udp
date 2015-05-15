package chatroom;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class Room
{
	public AnchorPane 	room;
	public String 		nickname;
	
	public Label 		NicknameLabel;
	public Label		StatusLabel1;
	public Label		StatusLabel2;
	public MenuButton	OptionsMenuButton;
	public MenuItem		OptionClearHistory;
	public MenuItem		OptionBlock;
	
	public ListView<String>	ChatHistory;
	
	public Pane			MessagePane;
	public TextField	MessageTextField;
	public Button		MessageSendButton;
	public Button		MessageAttachButton;
	
	public Pane			AttachPane;
	public TextField	AttachTextField;
	public Button		AttachSelectButton;
	public Button		AttachCancelButton;
	public Button		AttachOkButton;
	
	@SuppressWarnings("unchecked")
	public Room(String nickname, AnchorPane room) throws Exception
	{
		this.room = room;
		this.nickname= nickname;
		
	    NicknameLabel= (Label)room.lookup("#NicknameLabel");
	    StatusLabel1= (Label)room.lookup("#StatusLabel1");
	    StatusLabel2= (Label)room.lookup("#StatusLabel2");
	    OptionsMenuButton= (MenuButton)room.lookup("#OptionsMenuButton");
	    OptionClearHistory= OptionsMenuButton.getItems().get(0);
	    OptionBlock= OptionsMenuButton.getItems().get(1);
	    
	    ChatHistory= (ListView<String>)room.lookup("#ChatHistory");
	    
	    NicknameLabel.setText(nickname);
	    
	    MessagePane= (Pane)room.lookup("#MessagePane");
		MessageTextField= (TextField)room.lookup("#MessageTextField");
		MessageSendButton= (Button)room.lookup("#MessageSendButton");
		MessageAttachButton= (Button)room.lookup("#MessageAttachButton");
		
		AttachPane= (Pane)room.lookup("#AttachPane");
		AttachTextField= (TextField)room.lookup("#AttachTextField");
		AttachSelectButton= (Button)room.lookup("#AttachSelectButton");
		AttachCancelButton= (Button)room.lookup("#AttachCancelButton");
		AttachOkButton= (Button)room.lookup("#AttachOkButton");
	}
}
