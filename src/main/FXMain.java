package main;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.JOptionPane;

import environment.IOUtil;

public class FXMain extends Application{
	
	public static Stage stage;
	
	public static void main(String args[])
	{
		try
		{
			launch();
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "ERROR @start\n"+ex.getMessage());
		}
	}
	
	/**
	 * Load ChatRoom UI
	 */
	@SuppressWarnings("deprecation")
	public void start(Stage stage) throws Exception {
		
		FXMain.stage= stage;
		File f= IOUtil.getFile("view/view.fxml");
		
		FXMLLoader fxmlLoader = new FXMLLoader(f.toURL());
	    Parent root = (Parent) fxmlLoader.load();
		Scene scene= new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

}