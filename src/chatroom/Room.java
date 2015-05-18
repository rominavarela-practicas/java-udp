package chatroom;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	public AnchorPane 	pane;
	public String 		dstNickname;
	
	public Label 		NicknameLabel;
	public Label		StatusLabel1;
	public Label		StatusLabel2;
	public MenuButton	OptionsMenuButton;
	public MenuItem		OptionClearHistory;
	public MenuItem		OptionBlock;
	
	public ListView<String>			ChatHistory;
	public ObservableList<String> 	ChatHistoryContent;
	
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
	public Room(String dstNickname, AnchorPane pane) throws Exception
	{
		this.pane = pane;
		this.dstNickname= dstNickname;
		
	    NicknameLabel= (Label)pane.lookup("#NicknameLabel");
	    StatusLabel1= (Label)pane.lookup("#StatusLabel1");
	    StatusLabel2= (Label)pane.lookup("#StatusLabel2");
	    OptionsMenuButton= (MenuButton)pane.lookup("#OptionsMenuButton");
	    OptionClearHistory= OptionsMenuButton.getItems().get(0);
	    OptionBlock= OptionsMenuButton.getItems().get(1);
	    
	    ChatHistory= (ListView<String>)pane.lookup("#ChatHistory");
	    
	    MessagePane= (Pane)pane.lookup("#MessagePane");
		MessageTextField= (TextField)pane.lookup("#MessageTextField");
		MessageSendButton= (Button)pane.lookup("#MessageSendButton");
		MessageAttachButton= (Button)pane.lookup("#MessageAttachButton");
		
		AttachPane= (Pane)pane.lookup("#AttachPane");
		AttachTextField= (TextField)pane.lookup("#AttachTextField");
		AttachSelectButton= (Button)pane.lookup("#AttachSelectButton");
		AttachCancelButton= (Button)pane.lookup("#AttachCancelButton");
		AttachOkButton= (Button)pane.lookup("#AttachOkButton");
		
		//
	    NicknameLabel.setText(dstNickname);
 		ChatHistoryContent= FXCollections.observableList(new ArrayList<String>());
 		ChatHistory.setItems(ChatHistoryContent);
		
	}
	
	
}
