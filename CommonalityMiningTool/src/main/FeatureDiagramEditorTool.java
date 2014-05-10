package main;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import view.EditorController;
import view.EditorModel;
import view.EditorView;

public class FeatureDiagramEditorTool {


	public static void main(String[] args){

		if(OSUtils.isWindows()){
		  try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}else if (OSUtils.isMac()){			
		  //			  Application application = Application.getApplication();
		  //			  Image image = Toolkit.getDefaultToolkit().getImage("./src/DATA/Program/CMT.png");
		  //			  application.setDockIconImage(image);
		  try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}else if (OSUtils.isUnix()){
		  try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}else if (OSUtils.isSolaris()){
		  try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}

		
		EditorModel editorModel = new EditorModel();
		EditorView editorView = new EditorView();		
		EditorController editorController = new EditorController(editorView, editorModel);
		editorController.setSavePath(null);
		
		editorModel.addObserver(editorView);
		
		editorView.setOnCloseOperation(JFrame.EXIT_ON_CLOSE);

		System.out.println("initial DefaultCloseOperation: "+JFrame.EXIT_ON_CLOSE);
		
		if(!editorView.prepareUI(editorController) ){
		  System.out.println("Controller not set. Closing...");
		  return;
		}
		
		TrayUtils.createAndShowFDETray(editorController);


//		editorView.setVisible(true);
		
		
	}
}
