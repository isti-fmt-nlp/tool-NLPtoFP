package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;

import view.EditorModel.GroupTypes;
import view.EditorModel.ConstraintTypes;
import view.EditorView.activeItems;
import main.*;
import main.FeatureNode.FeatureTypes;

public class EditorController implements 
	ActionListener, WindowListener, MouseListener, MouseMotionListener, MouseWheelListener{

	/** variables used for debugging*/
	private static boolean debug=true;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=false;
	private static boolean debug5=false;
		
	/** Suffix of the path where general loadable diagram files will be saved*/
	private static String saveFilesSubPath="saved diagrams"; 
	
	/** Suffix of the path where SXFM exported files will be saved*/
	private static String sxfmSubPath="_SXFM"; 
	
	/** Suffix of the path where SXFM exported files will be saved*/
	private static String imagesSubPath="_IMAGES"; 

	/** Path where diagram files will be saved*/
	private String diagramPath = null;		
	
	/** Old name of the feature about to be renamed*/
	private String oldFeatureName=null;

	/** Path where SXFM exported files will be saved*/
	private String sxfmPath = null;		
	
	/** Path where SXFM exported files will be saved*/
	private String imagesPath = null;		
		
	private EditorView editorView = null;
		
	private EditorModel editorModel = null;
	
	/** Costruttore
	 * 
	 * @param viewProject
	 * @param modelProject
	 */
	public EditorController(EditorView editorView, EditorModel featureModel) {
			this.editorView = editorView;
			this.editorModel = featureModel;
		}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
        System.out.println("MouseWheelListenerDemo.mouseWheelMoved");
        if (e.getWheelRotation() < 0) {
            System.out.println("!!Rotated Up... " + e.getWheelRotation());
        } else {
            System.out.println("!!Rotated Down... " + e.getWheelRotation());
        }

        //
        // Get scrolled unit amount
        //
        System.out.println("!!ScrollAmount: " + e.getScrollAmount());

        //
        // WHEEL_UNIT_SCROLL representing scroll by unit such as the
        // arrow keys. WHEEL_BLOCK_SCROLL representing scroll by block
        // such as the page-up or page-down key.
        //
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            System.out.println("!!MouseWheelEvent.WHEEL_UNIT_SCROLL");
        }

        if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            System.out.println("!!MouseWheelEvent.WHEEL_BLOCK_SCROLL");
        }		

        editorView.setVerticalShift(-10*e.getWheelRotation());
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}
	
	@Override
	public void mouseDragged(MouseEvent e) {
	  //event originated from the diagram panel
	  if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) )
		switch(editorView.getActiveItem()){
//	      case DRAGGING_FEATURE: EditorView.dragFeature(e); break;
//	      case DRAGGING_EXTERN_ANCHOR: EditorView.dragAnchor(e); break;
//	      case DRAGGING_EXTERN_GROUP: EditorView.dragAnchor(e); break;
//	      case DRAGGING_SELECTION_RECT:  EditorView.dragSelectionRect(e); break;
	      case DRAGGING_FEATURE: editorView.dragFeature(e); break;
	      case DRAGGING_EXTERN_ANCHOR: editorView.dragAnchor(e); break;
	      case DRAGGING_EXTERN_GROUP: editorView.dragAnchor(e); break;
	      case DRAGGING_EXTERN_CONSTRAINT: editorView.dragAnchor(e); break;
	      case DRAGGING_CONSTRAINT_CONTROL_POINT: editorView.dragAnchor(e); break;	      
	      case DRAGGING_SELECTION_RECT:  editorView.dragSelectionRect(e); break;
	      case DRAGGING_SELECTION_GROUP: 
			  System.out.println("dragging group!");
//			  EditorView.dragSelectionGroup(e); break;
			  editorView.dragSelectionGroup(e); break;
	      default: break;
		}		
	  //event originated from the toolbar
	  else{ 	

		/* ***DEBUG*** */
		if(debug3) System.out.println("Evento Drag, isActiveItem= "+editorView.getActiveItem());
		/* ***DEBUG*** */

		switch(editorView.getActiveItem()){
//		  case DRAGGING_TOOL_NEWFEATURE: EditorView.dragToolNewFeature(e); break;
//		  case DRAGGING_TOOL_CONNECTOR: EditorView.dragToolConnector(e); break;
//		  case DRAGGING_TOOL_ALT_GROUP: EditorView.dragToolAltGroup(e); break;
//		  case DRAGGING_TOOL_OR_GROUP: EditorView.dragToolOrGroup(e); break;
		  case DRAGGING_TOOL_NEWFEATURE: editorView.dragToolNewFeature(e); break;
		  case DRAGGING_TOOL_MANDATORY_LINK: editorView.dragToolConnector(e); break;
		  case DRAGGING_TOOL_OPTIONAL_LINK: editorView.dragToolConnector(e); break;		  
		  case DRAGGING_TOOL_ALT_GROUP: editorView.dragToolAltGroup(e); break;
		  case DRAGGING_TOOL_OR_GROUP: editorView.dragToolOrGroup(e); break;  
		  case DRAGGING_TOOL_INCLUDES: editorView.dragToolIncludes(e); break;
		  case DRAGGING_TOOL_EXCLUDES: editorView.dragToolExcludes(e); break;
		  default: break;
		}			  

	  }
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	  JComponent popupElement=null;
	  
	  
	  //event originated from the diagram panel
      if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) ){
//	  if(containsPoint(editorView.getDiagramPanel(), e.getLocationOnScreen())){
		

    	/* ***DEBUG*** */
    	if(debug2) System.out.println("Button pressed: "+e.getButton()
    				+"Source e: "+e.getSource()
    				+"Source e.getName(): "+((Component)e.getSource()).getName());
    	/* ***DEBUG*** */

		editorView.getDiagramElementsMenu().removeAll();

		
		switch(e.getButton()){
		  case MouseEvent.BUTTON1: System.out.println("BUTTON1!"); break;
		  case MouseEvent.BUTTON2: System.out.println("BUTTON2!"); break;
		  case MouseEvent.BUTTON3: System.out.println("BUTTON3!"); break;		  
		}

//		/* TEST */
//		if (e.getButton() == MouseEvent.BUTTON2) {
//		  System.out.println("Ma ci sono o no?");
//		  editorView.getDiagramPanel().setPreferredSize(new Dimension(10000, 10000));
//		  editorView.getDiagramPanel().setSize(new Dimension(10000, 10000));
////		  editorView.getDiagramPanel().invalidate();
////		  editorView.repaintRootFrame();
////		  editorView.getDiagramPanel().validate();
////		  editorView.getDiagramPanel().repaint();
//		}
//		/* TEST */

		if (e.getButton() == MouseEvent.BUTTON3) {//user asked for the popup menu
          Component comp=editorView.getDiagramPanel().getComponentAt(e.getX(), e.getY());
        	
          /* ***DEBUG*** */
          if(debug3) System.out.println("rigth clicked on: "+comp.getName());
          /* ***DEBUG*** */

          //clicked on the diagram panel, not on an element
          if(comp.getName()==null || comp.getName()==""|| comp.getName().startsWith(EditorView.diagramPanelName)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemPrintModelDebug());
              editorView.setDiagramElementsMenuPosX(e.getX());
              editorView.setDiagramElementsMenuPosY(e.getY());
              editorView.showDiagramElementsMenu();
        	  return;
          }

          editorView.setPopUpElement((JComponent)comp);
          popupElement=editorView.getPopUpElement();
		  System.out.println("clicked! popupElement: "+popupElement.getName());

          //        	popUpElement=getUnderlyingComponent(e.getX(), e.getY());

//          if(popupElement.getName().startsWith(EditorView.startConnectorsNamePrefix)
//        		  || popupElement.getName().startsWith(EditorView.endConnectorsNamePrefix)){
//        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDelete());
//          }

          //clicked on a start connector dot
          if(popupElement.getName().startsWith(EditorView.startMandatoryNamePrefix)
             || popupElement.getName().startsWith(EditorView.startOptionalNamePrefix)
             || popupElement.getName().startsWith(EditorView.endMandatoryNamePrefix)
             || popupElement.getName().startsWith(EditorView.endOptionalNamePrefix)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDeleteConnector());
          }
          //clicked on an end connector dot
          if( (popupElement.getName().startsWith(EditorView.endMandatoryNamePrefix)
               || popupElement.getName().startsWith(EditorView.endOptionalNamePrefix) )
			   && ( ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.altGroupNamePrefix)
					|| ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.orGroupNamePrefix) ) ){
	    	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemUngroup());			  
		  }
          //clicked on a feature
          if(popupElement.getName().startsWith(EditorView.featureNamePrefix)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDeleteFeature());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemRenameFeature());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemChangeColor());
          }
          //clicked on a group
          if(popupElement.getName().startsWith(EditorView.altGroupNamePrefix)
        	 || popupElement.getName().startsWith(EditorView.orGroupNamePrefix)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDeleteGroup());
          }
          //clicked on a constraint
          if(popupElement.getName().startsWith(EditorView.startIncludesNamePrefix)
             || popupElement.getName().startsWith(EditorView.endIncludesNamePrefix)
        	 || popupElement.getName().startsWith(EditorView.startExcludesNamePrefix)
        	 || popupElement.getName().startsWith(EditorView.endExcludesNamePrefix)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDeleteConstraint());
        	  if(((ConstraintPanel)popupElement).getControlPoint().isVisible())
        	    editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemHideControlPoint());
        	  else editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemShowControlPoint());
          }

          editorView.setDiagramElementsMenuPosX(e.getX());
          editorView.setDiagramElementsMenuPosY(e.getY());
          editorView.showDiagramElementsMenu();
        }

		/* ***DEBUG *** */
		if(debug) System.out.println("splitterPanel.getDividerLocation(): "
				   					 +editorView.getSplitterPanel().getDividerLocation());
		/* ***DEBUG *** */

		  
		/* ***DEBUG *** */
		if (!debug) return;
		OrderedListNode tmpNode=editorView.getVisibleOrderDraggables().getFirst();
		while(tmpNode!=null){
			if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){
				System.out.println("Hai cliccato su "+((Component)tmpNode.getElement()).getName()+"!"+
					"\nLocation: "+((Component)tmpNode.getElement()).getLocationOnScreen()+"!");						
				return;
			}
			tmpNode=tmpNode.getNext();
		}
		/* ***DEBUG *** */

      }
		
	  //event originated from the toolbar
	  else{
		System.out.println("Source e: "+e.getSource());
		System.out.println("Source e.getName(): "+((Component)e.getSource()).getName());

		/* ***DEBUG*** */
		if (debug3) System.out.println("mouse clicked on a tool, Divider at: "+editorView.getSplitterPanel().getDividerLocation());
		/* ***DEBUG*** */
	  }
	}

	@Override
	public void mousePressed(MouseEvent e) {
	  //event originated from the diagram panel
	  if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) ){          

		/* ***DEBUG*** */
		if(debug) System.out.println("((Component)e.getSource()).getName(): "+((Component)e.getSource()).getName()
				  					  +"\ne.getSource().getClass(): "+e.getSource().getClass());
		/* ***DEBUG*** */

      	//checking if a feature must be renamed
      	if(oldFeatureName!=null){
      		
      	  JComponent popupElement=editorView.getPopUpElement();
      	  editorView.setOldFeatureName(oldFeatureName);
      	  
      	  ((FeaturePanel)popupElement).getTextArea().setEditable(false);
      	  ((FeaturePanel)popupElement).getTextArea().getCaret().setVisible(false);	  

      	  String newFeatureName=((FeaturePanel)popupElement).getLabelName();
      	  
      	  editorModel.changeFeatureName(((FeaturePanel)popupElement).getID(), newFeatureName);
      	  
      	  oldFeatureName=null;
      	  return;
      	}    	  
    	  
		int featurePanelX=0, featurePanelY=0;
		FeaturePanel featurePanel=null;
		JComponent otherEnd=null;
		JComponent otherEndFeaturePanel=null;
		JComponent anchorPanel=null;
		String anchorPanelName=null;
		OrderedListNode tmpNode=editorView.getVisibleOrderDraggables().getFirst();
		while(tmpNode!=null){

		  /* ***DEBUG*** */
		  if(debug) System.out.println("Testing for pressed element: "+((Component)tmpNode.getElement()).getName()
				  	+"\ne.getLocation: "+e.getX()+"."+e.getY()+" - elemtn: "+((Component)tmpNode.getElement()).getBounds());			  
		  /* ***DEBUG*** */

		  if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){

			/* ***DEBUG*** */
			if(debug) System.out.println("Pressed point got by element: "+((Component)tmpNode.getElement()).getName());
			/* ***DEBUG*** */

			editorView.setLastPositionX(e.getX());
			editorView.setLastPositionY(e.getY());
			
			//mouse pressed on an element of the group selection
			if(editorView.getSelectionGroup().contains(tmpNode.getElement())){

			  /* ***DEBUG*** */
			  if(debug) System.out.println("Mouse Pressed on a selection group element!");
			  /* ***DEBUG*** */

			  editorView.setActiveItem(activeItems.DRAGGING_SELECTION_GROUP);	
			  editorView.moveSelectionGroupToTop();					
			  return;
			}
			else{//mouse pressed out of an element of the group selection			
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  if (editorView.getSelectionGroup().size()>0) editorView.getSelectionGroup().clear();	
			}
			
			//mouse pressed on a feature panel in the diagram panel
			if( ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.featureNamePrefix) ){

			  featurePanel=(FeaturePanel)tmpNode.getElement();
			  featurePanelX=featurePanel.getX();
			  featurePanelY=featurePanel.getY();
			  anchorPanel=(JComponent)featurePanel.getComponentAt(e.getX()-featurePanelX, e.getY()-featurePanelY);
			  anchorPanelName=anchorPanel.getName();

			  /* ***DEBUG*** */
			  if(debug) System.out.println("Mouse pressed on "+featurePanel.getName()+", on anchor "+anchorPanelName);
			  /* ***DEBUG*** */

			  //mouse pressed on an anchor inside the feature panel
			  if(anchorPanelName!=null &&
				  ( anchorPanelName.startsWith(EditorView.startMandatoryNamePrefix) ||
					anchorPanelName.startsWith(EditorView.endMandatoryNamePrefix)   || 
					anchorPanelName.startsWith(EditorView.startOptionalNamePrefix)  ||
					anchorPanelName.startsWith(EditorView.endOptionalNamePrefix) )   ){

				editorView.setActiveItem(activeItems.DRAGGING_EXTERN_ANCHOR);
				editorView.setLastAnchorFocused(anchorPanel);
				editorView.setLastFeatureFocused(featurePanel);
				
				otherEnd=((AnchorPanel)anchorPanel).getOtherEnd();
				otherEndFeaturePanel=(JComponent)otherEnd.getParent();
				
				//the other end is attached to a feature
				if(otherEndFeaturePanel.getName().startsWith(EditorView.featureNamePrefix) ){
				  if(anchorPanelName.startsWith(EditorView.startMandatoryNamePrefix) 
					 || anchorPanelName.startsWith(EditorView.startOptionalNamePrefix) )
					editorModel.removeLink(featurePanel.getID(), ((FeaturePanel)otherEndFeaturePanel).getID());
				  if(anchorPanelName.startsWith(EditorView.endMandatoryNamePrefix) 
					 || anchorPanelName.startsWith(EditorView.endOptionalNamePrefix) ){
					if (otherEnd.getName().startsWith(EditorView.startMandatoryNamePrefix) 
						|| otherEnd.getName().startsWith(EditorView.startOptionalNamePrefix))
					  editorModel.removeLink(((FeaturePanel)otherEndFeaturePanel).getID(), featurePanel.getID());
					if (otherEnd.getName().startsWith(EditorView.altGroupNamePrefix)
					    || otherEnd.getName().startsWith(EditorView.orGroupNamePrefix))
					  editorModel.removeFeatureFromGroup(((FeaturePanel)otherEndFeaturePanel).getID(),
							  featurePanel.getID(), otherEnd.getName());
				  }
				}
				//the other end is a group not owned by a feature
				else if (otherEnd.getName().startsWith(EditorView.altGroupNamePrefix)
					|| otherEnd.getName().startsWith(EditorView.orGroupNamePrefix))
				  editorModel.removeFeatureFromGroup(null, featurePanel.getID(), otherEnd.getName());

				//the other end is not attached to any feature
				else editorView.detachAnchor(featurePanel, anchorPanel);
			  }
			  //mouse pressed on a group inside the feature panel
			  else if(anchorPanelName!=null &&
					  ( anchorPanelName.startsWith(EditorView.altGroupNamePrefix) ||
						anchorPanelName.startsWith(EditorView.orGroupNamePrefix) ) ){

				editorView.setActiveItem(activeItems.DRAGGING_EXTERN_GROUP);
				editorView.setLastAnchorFocused(anchorPanel);
				editorView.setLastFeatureFocused(featurePanel);
				
				//the group has no members
				if(((GroupPanel)anchorPanel).getMembers().size()==0) editorView.detachAnchor(featurePanel, anchorPanel);
				//the group has members				
				else editorModel.removeGroupFromFeature(featurePanel.getID(), anchorPanel.getName());
			  }
			  //mouse pressed on a constraint inside the feature panel
			  else if(anchorPanelName!=null &&
					  (  anchorPanelName.startsWith(EditorView.startIncludesNamePrefix) ||
						 anchorPanelName.startsWith(EditorView.endIncludesNamePrefix)   ||
						 anchorPanelName.startsWith(EditorView.startExcludesNamePrefix) ||
						 anchorPanelName.startsWith(EditorView.endExcludesNamePrefix) ) ){
				  
				otherEnd=((ConstraintPanel)anchorPanel).getOtherEnd();
				otherEndFeaturePanel=(JComponent)otherEnd.getParent();
				  
				editorView.setActiveItem(activeItems.DRAGGING_EXTERN_CONSTRAINT);
				editorView.setLastAnchorFocused(anchorPanel);
				editorView.setLastFeatureFocused(featurePanel);
				System.out.println("Trying to remove a constraint");
				//the other end of the constraint is attached to a feature panel
				if(otherEndFeaturePanel.getName().startsWith(EditorView.featureNamePrefix) ){
			      if(anchorPanelName.startsWith(EditorView.startIncludesNamePrefix))
					editorModel.removeConstraint(featurePanel.getID(),
						((FeaturePanel)otherEndFeaturePanel).getID(), ConstraintTypes.INCLUDES);
			      if(anchorPanelName.startsWith(EditorView.endIncludesNamePrefix))
					editorModel.removeConstraint(((FeaturePanel)otherEndFeaturePanel).getID(),
							featurePanel.getID(), ConstraintTypes.INCLUDES);
			      if(anchorPanelName.startsWith(EditorView.startExcludesNamePrefix))
					editorModel.removeConstraint(featurePanel.getID(),
						((FeaturePanel)otherEndFeaturePanel).getID(), ConstraintTypes.EXCLUDES);
			      if(anchorPanelName.startsWith(EditorView.endExcludesNamePrefix))
					editorModel.removeConstraint(((FeaturePanel)otherEndFeaturePanel).getID(),
							featurePanel.getID(), ConstraintTypes.EXCLUDES);
				}
				else editorView.detachAnchor(featurePanel, anchorPanel);
					
			  }
			  //mouse directly pressed on a feature panel, not on an inner element
			  else{
				editorView.setActiveItem(activeItems.DRAGGING_FEATURE);
				editorView.setLastFeatureFocused((FeaturePanel)tmpNode.getElement());  
				editorView.moveComponentToTop(editorView.getLastFeatureFocused());
			  }
			}
			//mouse directly pressed on an anchor panel in the diagram panel
			else if(/*tmpNode.getElement().getClass().equals(AnchorPanel.class) &&*/(
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startMandatoryNamePrefix) ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endMandatoryNamePrefix)  ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startOptionalNamePrefix)  ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endOptionalNamePrefix) ) ){
			  editorView.setActiveItem(activeItems.DRAGGING_EXTERN_ANCHOR);
			  editorView.setLastAnchorFocused((AnchorPanel)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}
			//mouse directly pressed on a group panel in the diagram panel
			else if(/*tmpNode.getElement().getClass().equals(GroupPanel.class) &&*/
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.altGroupNamePrefix) ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.orGroupNamePrefix) ){
			  editorView.setActiveItem(activeItems.DRAGGING_EXTERN_GROUP);
			  editorView.setLastAnchorFocused((GroupPanel)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}
			//mouse directly pressed on a constraint panel in the diagram panel
			else if( ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startExcludesNamePrefix) ||
					 ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startIncludesNamePrefix)  ||
					 ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endExcludesNamePrefix) ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endIncludesNamePrefix)){
			  editorView.setActiveItem(activeItems.DRAGGING_EXTERN_CONSTRAINT);
			  editorView.setLastAnchorFocused((ConstraintPanel)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}
			//mouse directly pressed on a constraint control point in the diagram panel
			else if(((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.constraintControlPointNamePrefix)){
			  editorView.setActiveItem(activeItems.DRAGGING_CONSTRAINT_CONTROL_POINT);
			  editorView.setLastAnchorFocused((JComponent)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}

			/* ***DEBUG*** */
			if (debug2){
				System.out.println("Event Source: "+e.getSource());
				OrderedListNode printTmp=editorView.getVisibleOrderDraggables().getFirst();
				System.out.println("Printing draggables list from first:");
				while(printTmp!=null){
					System.out.println("-"+((Component)printTmp.getElement()).getName());							  
					printTmp=printTmp.getNext();
				}
			}
			/* ***DEBUG*** */

			return;
		  }
		  tmpNode=tmpNode.getNext();
		}

		//mouse directly pressed on the diagram panel
		if (editorView.getSelectionGroup().size()>0) editorView.getSelectionGroup().clear();	

		/* ***DEBUG*** */
		if(debug) System.out.println("editorView.getSelectionGroup().size(): "+editorView.getSelectionGroup().size());
		/* ***DEBUG*** */

		editorView.setStartSelectionRect(e.getLocationOnScreen().getLocation());
//		editorView.setEndSelectionRect(e.getLocationOnScreen().getLocation());

		editorView.getSelectionRect().setFrameFromDiagonal(e.getLocationOnScreen().getLocation(),
				e.getLocationOnScreen().getLocation());  	  

		editorView.setActiveItem(activeItems.DRAGGING_SELECTION_RECT);

		/* ***DEBUG*** */
		if(debug2) System.out.println("Mouse pressed on: "+((Component)e.getSource()).getName());
		/* ***DEBUG*** */

      }
	  //event originated from the toolbar
	  else{
		editorView.getDiagramElementsMenu().setVisible(false);
		editorView.getDiagramElementsMenu().setEnabled(false);
		editorView.setLastPositionX(e.getX());
		editorView.setLastPositionY(e.getY());
		  
		Component[] compList=editorView.getToolsPanel().getComponents();
		JComponent comp=(JComponent)e.getSource();
		JComponent imageLabel=(JComponent)comp.getComponent(0);

		/* ***DEBUG*** */
		if(debug4) System.out.println("e.getSource(): "+e.getSource()+"\nimageLabel: "+imageLabel);
		/* ***DEBUG*** */

		try {
			editorView.setToolDragImage(
			    ImageIO.read(this.getClass().getResourceAsStream(editorView.getToolIconPath(((JComponent)comp).getName()))));
			editorView.setToolDragPosition(
					new Point((int)imageLabel.getLocationOnScreen().getX(), (int)imageLabel.getLocationOnScreen().getY()));

		} catch (IOException e2) {
			System.out.println("toolDragImage is null");
			e2.printStackTrace();
			return;
		}
		if (((JComponent)comp).getName()=="New Feature")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_NEWFEATURE);
		else if ( ((JComponent)comp).getName()=="Mandatory Link")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_MANDATORY_LINK);				
		else if ( ((JComponent)comp).getName()=="Optional Link" )
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_OPTIONAL_LINK);
		else if (((JComponent)comp).getName()=="Alternative Group")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_ALT_GROUP);
		else if (((JComponent)comp).getName()=="Or Group")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_OR_GROUP);
		else if (((JComponent)comp).getName()=="Includes")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_INCLUDES);
		else if (((JComponent)comp).getName()=="Excludes")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_EXCLUDES);

		editorView.repaintRootFrame();

		/* ***DEBUG*** */
		if (debug) System.out.println("mousePressed on: "+editorView.getToolDragPosition());
		/* ***DEBUG*** */

		/* ***DEBUG*** */
		if (debug3) System.out.println("mousePressed, components are "+compList.length);
		/* ***DEBUG*** */

	  }


	}

	@Override
	public void mouseReleased(MouseEvent e) {
	  //event originated from the diagram panel
      if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) )
		switch(editorView.getActiveItem()){
		  case DRAGGING_FEATURE:
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastFeatureFocused(null); break;
		  case DRAGGING_EXTERN_ANCHOR:
			  dropAnchor(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_EXTERN_GROUP:
			  dropGroup(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_EXTERN_CONSTRAINT:
			  dropConstraint(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_CONSTRAINT_CONTROL_POINT:
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_SELECTION_RECT:
			  dropSelectionRectangle(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  break;
		  case DRAGGING_SELECTION_GROUP:
			  System.out.println("released group drag!");
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  if(editorView.getSelectionGroup().size()>0) editorView.getSelectionGroup().clear();
			  editorView.repaintRootFrame();
			  break;
		  default: break;
	    }

	  //event originated from the toolbar
	  else 
		switch(editorView.getActiveItem()){
	      case DRAGGING_TOOL_NEWFEATURE:
	    	  addNewFeature(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_MANDATORY_LINK:
//	    	  EditorView.addConnectorToDiagram(e);
	    	  editorView.addConnectorToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_OPTIONAL_LINK:
//	    	  EditorView.addConnectorToDiagram(e);
	    	  editorView.addConnectorToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_INCLUDES:
//	    	  EditorView.addConnectorToDiagram(e);
	    	  editorView.addConstraintToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_EXCLUDES:
//	    	  EditorView.addConnectorToDiagram(e);
	    	  editorView.addConstraintToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_ALT_GROUP:
//	    	  EditorView.addAltGroupToDiagram(e);
	    	  editorView.addAltGroupToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_OR_GROUP:
//	    	  EditorView.addOrGroupToDiagram(e);
	    	  editorView.addOrGroupToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      default: break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Checks if the rectangle of comp contains the point location
	 * @param comp - JComponent that can contains location
	 * @param location - Point that can be contained in comp
	 * @return
	 */
	private boolean containsPoint(JComponent comp, Point location) {		
	  System.out.println("Clicked on position: "+location
		  +"\ncomp.getVisibleRect()"+comp.getVisibleRect());
	  if (comp.getVisibleRect().contains(location)) return true;
	  else return false;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	  String diagDataPath=null;
	  ArrayList<String> modelDataPaths=null;
	  //commands from diagram popup menu
	  JComponent popupElement=editorView.getPopUpElement();
	  //popup menu command: Delete Element
      if(e.getActionCommand().equals("Delete Element")){
        String elementName = null;
        if (popupElement!=null) elementName=popupElement.getName();

        /* ***DEBUG*** */
        if(debug3) System.out.println("Popup Menu requested delete on "+elementName+"\ne = "+e);
        /* ***DEBUG*** */
        
        if(elementName!=null && ( elementName.startsWith(EditorView.startMandatoryNamePrefix)
           || elementName.startsWith(EditorView.startOptionalNamePrefix) )){
          editorView.deleteAnchor(popupElement);
          editorView.deleteAnchor(((AnchorPanel)popupElement).getOtherEnd());
      	  editorView.repaintRootFrame();
        }
        if(elementName!=null && ( elementName.startsWith(EditorView.startIncludesNamePrefix)
           || elementName.startsWith(EditorView.endIncludesNamePrefix)
           || elementName.startsWith(EditorView.startExcludesNamePrefix)
           || elementName.startsWith(EditorView.endExcludesNamePrefix) )){
        	
          if(((ConstraintPanel)popupElement).getControlPoint().isVisible())
        	editorView.deleteAnchor(((ConstraintPanel)popupElement).getControlPoint());

          editorView.deleteAnchor(popupElement);
          editorView.deleteAnchor(((ConstraintPanel)popupElement).getOtherEnd());
          editorView.repaintRootFrame();
        }
        else if(elementName!=null && ( elementName.startsWith(EditorView.endMandatoryNamePrefix)
           || elementName.startsWith(EditorView.endOptionalNamePrefix) )){
      	  if( ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.startMandatoryNamePrefix)
      		 || ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.startOptionalNamePrefix)){
      		editorView.deleteAnchor(popupElement);
      		editorView.deleteAnchor(((AnchorPanel)popupElement).getOtherEnd());            		
      	  }
      	  else if( ( ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.orGroupNamePrefix)
      	   || ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.altGroupNamePrefix) )
      	   && ((GroupPanel)((AnchorPanel)popupElement).getOtherEnd()).getMembers().size()>2 ){

      		((GroupPanel)((AnchorPanel)popupElement).getOtherEnd()).getMembers().remove(popupElement);
      		editorView.deleteAnchor(popupElement);
      	  }	
          editorView.repaintRootFrame();
        }
        else if(elementName!=null && (elementName.startsWith(EditorView.altGroupNamePrefix)
        		|| elementName.startsWith(EditorView.orGroupNamePrefix)) ){
          editorModel.deleteUnattachedGroup(elementName);
          editorView.repaintRootFrame();
        }
        else if(elementName!=null && elementName.startsWith(EditorView.featureNamePrefix)){
          editorModel.deleteFeature(((FeaturePanel)popupElement).getID());
          editorView.repaintRootFrame();
        }
        editorView.setPopUpElement(null);
      }
	  //popup menu command: Rename Feature
      else if(e.getActionCommand().equals("Rename Feature")){
    	oldFeatureName=((FeaturePanel)popupElement).getLabelName();
    	
        System.out.println("Renaming: "+popupElement.getName()); 
        ((FeaturePanel)popupElement).getTextArea().setEditable(true);
        
      }
	  //popup menu command: Change Color
      else if(e.getActionCommand().equals("Change Color")){    	  
        Color color = JColorChooser.showDialog(null, "Choose Color", Color.white);  
        if (color==null) return;
        else ((FeaturePanel)popupElement).setBackground(color);        
      }
	  //popup menu command: Ungroup Element
      else if(e.getActionCommand().equals("Ungroup")){
        System.out.println("Ungrouping: "+popupElement.getName());
        System.out.println("Other end: "+((AnchorPanel)popupElement).getOtherEnd().getName());
    	editorView.ungroupAnchor((AnchorPanel)popupElement);
        editorView.repaintRootFrame();
      }
      else if(e.getActionCommand().equals("Show Control Point")){
    	editorView.showControlPoint((ConstraintPanel)popupElement);
      }
      else if(e.getActionCommand().equals("Hide Control Point")){
      	editorView.hideControlPoint((ConstraintPanel)popupElement);    	  
      }
	  //popup menu command: Print Model[DEBUG COMMAND]
      else if(e.getActionCommand().equals("Print Model[DEBUG COMMAND]")){
    	editorModel.printModel();
    	
    	/* ***DEBUG*** */
    	if(debug5){
    	  System.out.println("\n\nPRINTING VISIBLE ORDER DRAGGABLES");
    	  OrderedListNode drag = editorView.getVisibleOrderDraggables().getFirst();
    	  while(drag!=null){
    		if(((Component)drag.getElement()).getName().startsWith(EditorView.featureNamePrefix))
    		  System.out.println("\n"+((FeaturePanel)drag.getElement()).getID());
    		else System.out.println("\n"+((Component)drag.getElement()).getName());
    		drag=drag.getNext();
    	  }
    	  System.out.println("\n\nPRINTING VISIBLE ORDER DRAGGABLES IN REVERSE ORDER");
    	  drag = editorView.getVisibleOrderDraggables().getLast();
    	  while(drag!=null){
    		if(((Component)drag.getElement()).getName().startsWith(EditorView.featureNamePrefix))
    		  System.out.println("\n"+((FeaturePanel)drag.getElement()).getID());
    		else System.out.println("\n"+((Component)drag.getElement()).getName());
    		drag=drag.getPrev();
    	  }

    	}
    	/* ***DEBUG*** */
    	
      }
	  //commands from editor menu bar
      else if(e.getActionCommand().equals("Save Diagram")){
  		String s = null;			
  		if((s = editorView.assignNameDiagramDialog()) != null){
  		  diagDataPath=editorView.saveDiagram(diagramPath, s);
  		  modelDataPaths=editorModel.saveModel(diagramPath, s);
  		  if(diagDataPath==null || modelDataPaths==null) 
  			editorView.errorDialog("Error during save.");
  		  else try{
  			//checking if the diagrams save directory must be created
  			File dir=new File(diagramPath+"/"+saveFilesSubPath);		
  			if(!dir.isDirectory() && !dir.mkdir() ) 
  			  throw new IOException("Save Directory can't be created.");
  				
  			PrintWriter pw1 = new PrintWriter(new BufferedWriter(
  					new FileWriter(diagramPath+"/"+saveFilesSubPath+"/"+s) ));
  			pw1.println(diagDataPath);
  			for(String path : modelDataPaths) pw1.println(path);
  			pw1.close();  	
  			editorView.setModified(false);
  			editorModel.setModified(false);
  		  }catch (IOException ex){
  			System.out.println("Exception saveDiagram: " + ex.getMessage());
  			ex.printStackTrace();
  		  }
  		}    	  
      }
      else if(e.getActionCommand().equals("Open Diagram")){
  		String s = null;
  		if((s = editorView.loadXMLDialog(diagramPath)) != null)
  		  System.out.println("You selected "+s+" file.");
//  				s.substring(0, s.length() - 4)
    	  
      }
      else if(e.getActionCommand().equals("Export as SXFM")){
    	String s = null;			
    	if((s = editorView.assignNameSXFMDialog()) != null){
    	  modelDataPaths=editorModel.exportAsSXFM(sxfmPath, s);
    	  if(modelDataPaths==null) editorView.errorDialog("Error during save.");
    	  else try{
    		//checking if the SXFM files save directory must be created
    		File dir=new File(sxfmPath+"/"+saveFilesSubPath);		
    		if(!dir.isDirectory() && !dir.mkdir() ) 
    		  throw new IOException("Save Directory can't be created.");

    		PrintWriter pw1 = new PrintWriter(new BufferedWriter(
    				new FileWriter(sxfmPath+"/"+saveFilesSubPath+"/"+s) ));
    		for(String path : modelDataPaths) pw1.println(path);
    		pw1.close();  	
    	  }catch (IOException ex){
    		System.out.println("Exception exportAsSXFM: " + ex.getMessage());
    		ex.printStackTrace();
    	  }
    	}    	  
    	  
      }
      else if(e.getActionCommand().equals("Export as PNG")){
    	editorView.exportAsImageFile(imagesPath, "PNG");
      }
      else if(e.getActionCommand().equals("Export as GIF")){
    	editorView.exportAsImageFile(imagesPath, "GIF");
      }
      else if(e.getActionCommand().equals("Delete Diagram")){
    	  
      }
      else if(e.getActionCommand().equals("Exit")){
    	  
      }

	}


	/**
	 * Recursively print the feature model rooted in feature, indenting the lower levels.
	 * 
	 * @param indent - String printed before the name of root element, for the lower leves <br>
	 * it will be printed a number of times equals to 1+depth.
	 */
	private void treePrint(FeatureNode feature, String indent) {
		System.out.println(indent+feature.getName()+"("+feature.getID()+")");
		for(FeatureNode child : feature.getSubFeatures()) treePrint(child, indent+">");
		for(GroupNode group : feature.getSubGroups()) 
		  for(FeatureNode member : group.getMembers()) 
		  treePrint(member, indent+"|"); 
	}

	/**
	 * Adds a new unrooted feature to the diagram.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void addNewFeature(MouseEvent e) {
//	  if (!EditorView.checkDroppedOnDiagram()) return;
	  if (!editorView.checkDroppedOnDiagram()) return;
	  else{

		/* ***DEBUG*** */
		if(debug) System.out.println("Mouse rilasciato(Drag relative) su: ("+e.getX()+", "+e.getY()+")."
				+"\nMouse rilasciato(Screen relative) su: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
				+"\nLocation dove verrà aggiunta la nuova Feature: "+editorView.getToolDragPosition());
		/* ***DEBUG*** */

		editorView.setLastPositionX(e.getX());
		editorView.setLastPositionY(e.getY());
		editorModel.addUnrootedCommonality(EditorView.featureNamePrefix+editorView.getFeaturesCount(), 
					EditorView.featureNamePrefix+editorView.getFeaturesCount());
	  }
	}

	/**
	 * Drops an anchor on the diagram, attaching it to the underlying component, if any.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void dropAnchor(MouseEvent e) {
		GroupTypes type=null;
		Component comp=editorView.dropAnchorOnDiagram(e);
		if (comp!=null) System.out.println("comp.getName()= "+comp.getName());
		JComponent anchor=editorView.getLastAnchorFocused();
		JComponent otherEnd=((AnchorPanel)anchor).getOtherEnd();
		//anchor dropped directly on the diagram panel
		if (comp==null) return;
		//anchor directly dropped on a feature inside the diagram panel
		else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
		  System.out.println("anchor.getName()= "+anchor.getName()
				  			+"\notherEnd.getParent().getName(): "+otherEnd.getParent().getName());
			
		  if( anchor.getName().startsWith(EditorView.endMandatoryNamePrefix)||
			  anchor.getName().startsWith(EditorView.endOptionalNamePrefix) ){
			System.out.println("otherEnd.getName(): "+otherEnd.getName());
			System.out.println("otherEnd.getParent().getName(): "+otherEnd.getParent().getName());
			if( ( otherEnd.getName().startsWith(EditorView.startMandatoryNamePrefix)
				 || otherEnd.getName().startsWith(EditorView.startOptionalNamePrefix) ) 
				&& otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix) ){
		      //about to link 2 features by a connector
			  System.out.println("about to link 2 features by a connector_R");
//			  editorModel.addMandatoryLink( (otherEnd.getParent().getName(), comp.getName() );
			  if(anchor.getName().startsWith(EditorView.endMandatoryNamePrefix))
				editorModel.addMandatoryLink( ((FeaturePanel)otherEnd.getParent()).getID(), 
					((FeaturePanel)comp).getID() );
			  else if(anchor.getName().startsWith(EditorView.endOptionalNamePrefix))
				editorModel.addOptionalLink( ((FeaturePanel)otherEnd.getParent()).getID(), 
					((FeaturePanel)comp).getID() );
			  return;
			}
			else if( otherEnd.getName().startsWith(EditorView.orGroupNamePrefix)
					 || otherEnd.getName().startsWith(EditorView.altGroupNamePrefix) ){
			  //about to add a feature to a group
			  System.out.println("about to add a feature to a group");
			  if(otherEnd.getName().startsWith(EditorView.orGroupNamePrefix)) 
				type=GroupTypes.OR_GROUP;
			  else 
				type=GroupTypes.ALT_GROUP;
			  
			  if ( ((GroupPanel)otherEnd).getParent().getName().startsWith(EditorView.featureNamePrefix))
				editorModel.addFeatureToGroup( ((FeaturePanel)otherEnd.getParent()).getID(),
											   ((FeaturePanel)comp).getID(), otherEnd.getName(), type );
			  else
				editorModel.addFeatureToGroup( null, ((FeaturePanel)comp).getID(), otherEnd.getName(), type);
			  return;
			}	
		  }
		  if ( (anchor.getName().startsWith(EditorView.startMandatoryNamePrefix)
				|| anchor.getName().startsWith(EditorView.startOptionalNamePrefix) )
			  && otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix)){
			//about to link 2 features by a connector
			System.out.println("about to link 2 features by a connector");

			if(anchor.getName().startsWith(EditorView.startMandatoryNamePrefix))				
				editorModel.addMandatoryLink( ((FeaturePanel)comp).getID(),
						((FeaturePanel)otherEnd.getParent()).getID() );
			else if(anchor.getName().startsWith(EditorView.startOptionalNamePrefix))
				editorModel.addOptionalLink( ((FeaturePanel)comp).getID(),
						((FeaturePanel)otherEnd.getParent()).getID() );

			return;
		  }
		  //the other end of the connector is not anchored to anything
		  if (otherEnd.getParent().getName().startsWith(EditorView.diagramPanelName) ){
			System.out.println("about to add an anchor to a feature");
//			EditorView.addAnchorToFeature(); return;
			editorView.addAnchorToFeature(); return;
		  }
		}
		
		else if (comp.getName().startsWith(EditorView.altGroupNamePrefix)
				 || comp.getName().startsWith(EditorView.orGroupNamePrefix)){
		  //about to merge a connector with a group
		  System.out.println("about to merge a connector with a group");
		  if(otherEnd.getName().startsWith(EditorView.orGroupNamePrefix)) 
			type=GroupTypes.OR_GROUP;
		  else 
			type=GroupTypes.ALT_GROUP;
		  
		  //group is not owned by any feature
		  if(comp.getParent().getName()==null || comp.getParent().getName().startsWith(EditorView.diagramPanelName)){
			if(otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix))
			  editorModel.mergeConnectorWithGroup( null, ((FeaturePanel)otherEnd.getParent()).getID(), 
					  							   comp.getName(), type);
			else 
			  editorModel.mergeConnectorWithGroup(null, null, comp.getName(), type);				
//			  editorModel.mergeConnectorWithGroup( null, otherEnd.getParent().getName(), comp.getName());
//			  else editorModel.mergeConnectorWithGroup( comp.getParent().getName(), otherEnd.getParent().getName(), comp.getName());
		  }
		  //group is owned by a feature
		  else{
			if(otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix))
			  editorModel.mergeConnectorWithGroup(
					  	((FeaturePanel)comp.getParent()).getID(),
					  	((FeaturePanel)otherEnd.getParent()).getID(), comp.getName(), type);
			else 
			  editorModel.mergeConnectorWithGroup(
					  	((FeaturePanel)comp.getParent()).getID(),
					  	null, comp.getName(), type);	
		  }
		  return;		  
		}
	}

	/**
	 * Drops a group on the diagram, attaching it to the underlying component, if any.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void dropGroup(MouseEvent e) {
	  Component comp=editorView.dropGroupOnDiagram(e);
	  if (comp!=null) System.out.println("comp= "+comp.getName());
	  GroupPanel group=(GroupPanel)editorView.getLastAnchorFocused();
	  
	  //group dropped directly on the diagram panel
	  if (comp==null) return;
	  //group dropped on a feature inside the diagram panel
	  else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
		  
		//the group has no members
		if (group.getMembers().size()==0 ){
		  editorView.addAnchorToFeature(); return;
		}
		else{
		  //about to add group to the feature comp
		  System.out.println("about to add a group to a feature");
		  editorModel.addGroupToFeature( ((FeaturePanel)comp).getID(), group.getName() );
		  return;
			
		}
	  }
	}

	/**
	 * Drops a constraint on the diagram, attaching it to the underlying component, if any.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void dropConstraint(MouseEvent e) {
	  Component comp=editorView.dropConstraintOnDiagram(e);
	  if (comp!=null) System.out.println("comp= "+comp.getName());
	  ConstraintPanel constraint=(ConstraintPanel)editorView.getLastAnchorFocused();
	  
	  //constraint dropped directly on the diagram panel
	  if (comp==null) return;
	  
	  //constraint dropped on a feature inside the diagram panel
	  else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
		  
		  
		if (constraint.getOtherEnd().getParent().getName().startsWith(EditorView.featureNamePrefix) ){
		  //the constraint links two features
		  System.out.println("about to add a constraint to the model");
		  if(constraint.getName().startsWith(EditorView.startIncludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)comp).getID(),
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				EditorModel.ConstraintTypes.INCLUDES);
			return;
		  }
		  if(constraint.getName().startsWith(EditorView.endIncludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				((FeaturePanel)comp).getID(),
				EditorModel.ConstraintTypes.INCLUDES);
			return;
		  }
		  if(constraint.getName().startsWith(EditorView.startExcludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)comp).getID(),
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				EditorModel.ConstraintTypes.EXCLUDES);
			return;
		  }
		  if(constraint.getName().startsWith(EditorView.endExcludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				((FeaturePanel)comp).getID(),
				EditorModel.ConstraintTypes.EXCLUDES);
			return;
		  }
		}
		else{
		  editorView.addAnchorToFeature(); return;
		}
	  }
	}

	/**
	 * Drops the selection rectangle on the diagram, selecting a group of elements.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void dropSelectionRectangle(MouseEvent e) {
	    editorView.createSelectionGroup(e);
	}

	/**
	 * Detach an anchor or group from the feature featurePanel, attaching it to the diagram.
	 * 
	 * @param feature - the feature from wich the anchor must be detached
	 * @param anchor - the anchor to detach
	 */
	private void detachAnchor(FeaturePanel feature, JComponent anchor) {
		JComponent anchorFocused;
		int anchorPanelOnScreenX;
		int anchorPanelOnScreenY;
		anchorFocused=(JComponent)anchor;
		editorView.setLastAnchorFocused(anchorFocused);

		anchorPanelOnScreenX=(int)anchorFocused.getLocationOnScreen().getX();
		anchorPanelOnScreenY=(int)anchorFocused.getLocationOnScreen().getY();
		feature.remove(anchorFocused);
		feature.validate();
		
		editorView.getLastAnchorFocused().setLocation(
		  (int)(anchorPanelOnScreenX-editorView.getDiagramPanel().getLocationOnScreen().getX()),
		  (int)(anchorPanelOnScreenY-editorView.getDiagramPanel().getLocationOnScreen().getY()));
		editorView.getDiagramPanel().setLayer(editorView.getLastAnchorFocused(), 0);
		editorView.getDiagramPanel().add(editorView.getLastAnchorFocused());
		editorView.getDiagramPanel().setComponentZOrder(editorView.getLastAnchorFocused(), 0);
//		EditorView.moveComponentToTop(editorView.getLastAnchorFocused());
		editorView.moveComponentToTop(editorView.getLastAnchorFocused());
	}

	/**
	 * Sets the path used for saving the project and that used to export SXFM files into.
	 * @param pathProject - the path used for saving the project
	 */
	public void setSavePath(String pathProject) {
		this.diagramPath=pathProject;		
		this.sxfmPath=pathProject+sxfmSubPath;
		this.imagesPath=pathProject+imagesSubPath;
	}

	/**
	 * Adds starting Commonalities and Variabilities to the diagram and model, using the ArrayLists in EditorView.
	 */
	public void addStartingfeatures() {
		ArrayList<String> startingCommonalities=editorView.getStartingCommonalities();			
		ArrayList<String> startingVariabilities=editorView.getStartingVariabilities();
		JLayeredPane diagramPanel=editorView.getDiagramPanel();
		Point position=new Point();
		//adding starting commonalities
		int i=10, j=10;
		for(String name : startingCommonalities){
		  if(i>=diagramPanel.getWidth()){ i=10; j+=55;}
		  position.x=i+(int)diagramPanel.getLocationOnScreen().getX();
		  position.y=j+(int)diagramPanel.getLocationOnScreen().getY();
		  editorView.setToolDragPosition(position);
		  editorView.setFeatureToAddName(name);
		  editorModel.addUnrootedNamedFeature(name, 
				  EditorView.featureNamePrefix+editorView.getFeaturesCount(), FeatureTypes.COMMONALITY);
		  i+=70;
		}
		//adding starting variabilities
		for(String name : startingVariabilities){
		  if(i>=diagramPanel.getWidth()){ i=10; j+=55;}
		  position.x=i+(int)diagramPanel.getLocationOnScreen().getX();
		  position.y=j+(int)diagramPanel.getLocationOnScreen().getY();
		  editorView.setToolDragPosition(position);
		  editorView.setFeatureToAddName(name);
		  editorModel.addUnrootedNamedFeature(name, 
				  EditorView.featureNamePrefix+editorView.getFeaturesCount(), FeatureTypes.VARIABILITY);
		  i+=70;
		}
		editorView.fitDiagram();
	}
	
}
