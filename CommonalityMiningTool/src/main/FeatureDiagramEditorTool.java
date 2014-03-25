package main;

import view.ControllerProject;
import view.EditorController;
import view.EditorModel;
import view.EditorView;
import view.ModelProject;
import view.ViewProject;

public class FeatureDiagramEditorTool {


	public static void main(String[] args){
		EditorModel editorModel = new EditorModel();
		EditorView editorView = new EditorView();		
		EditorController editorController = new EditorController(editorView, editorModel);
		editorController.setSavePath(null);
		
		editorModel.addObserver(editorView);
		
		if(!editorView.prepareUI(editorController) ){
		  System.out.println("Controller not set. Closing...");
		  return;
		}

//		editorView.setVisible(true);
		
		
	}
}
