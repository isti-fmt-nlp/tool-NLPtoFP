/**
 * @author Manuel Musetti
 */
package view;

import main.CMTConstants;
import main.OSUtils;
import main.TrayUtils;
import main.FeatureNode.FeatureTypes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.GroupNode.GroupTypes;
import view.EditorModel.ConstraintTypes;
import view.EditorView.ItemsType;
import view.EditorView.ActiveItems;
import main.*;

public class EditorController implements 
	ActionListener, WindowListener, MouseListener, MouseMotionListener, MouseWheelListener{

	/** variables used for debugging*/
	private static boolean debug=true;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=false;
	private static boolean debug5=false;
		
//	/** Suffix of the path where general loadable diagram files will be saved*/
//	private static String saveFilesSubPath="saved diagrams"; 
//	
//	/** Suffix of the path where SXFM exported files will be saved*/
//	private static String sxfmSubPath="_SXFM"; 
//	
//	/** Suffix of the path where SXFM exported files will be saved*/
//	private static String imagesSubPath="_IMAGES"; 

	/** Default name for a diagram*/
	public static final String DEFAULT_DIAGRAM_NAME="New Diagram";
	
	/** Name of the current diagram */
	private String diagramName= DEFAULT_DIAGRAM_NAME;	

	/** Path where diagram files will be saved*/
	private String diagramPath = null;		

	/** Project name*/
	private String projectName = null;

	/** Path where SXFM exported files will be saved*/
	private String sxfmPath = null;		
	
	/** Path where image exported files will be saved*/
	private String imagesPath = null;		
		
	private EditorView editorView = null;
		
	private EditorModel editorModel = null;
	
	/** Old name of the feature about to be renamed*/
	private String oldFeatureName=null;

	/** A logic grid, used to place features in the diagram when the view is automatically created from a model*/
	private boolean[][] logicGrid=null;

	/** Tells if something has been modified after last save*/
	private boolean modified=false;

	
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

//		e= new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
//			  		e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), false, e.getButton());

		JScrollBar bar = null;
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

//        editorView.setVerticalShift(-10*e.getWheelRotation());
        
        if (e.isShiftDown()) bar = editorView.getDiagramScroller().getHorizontalScrollBar();
        else bar = editorView.getDiagramScroller().getVerticalScrollBar();

        
        bar.setValue(bar.getValue()+(bar.getMaximum()/40)*e.getWheelRotation());
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}
	
	@Override
	public void mouseDragged(MouseEvent e) {

	  MouseEvent originalEvent = e;
	  e= new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
			  	(int)(e.getX()/editorView.getScaleFactor()), (int)(e.getY()/editorView.getScaleFactor()),
			  	(int)(e.getXOnScreen()/editorView.getScaleFactor()), (int)(e.getYOnScreen()/editorView.getScaleFactor()),
			  	e.getClickCount(), false, e.getButton());

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
//	      case DRAGGING_SELECTION_RECT:  editorView.dragSelectionRect(e); break;
	      case DRAGGING_SELECTION_RECT:  editorView.dragSelectionRect(originalEvent); break;
	      case DRAGGING_SELECTION_GROUP: editorView.dragSelectionGroup(e); break;
	      default: break;
		}
	  //event originated from the toolbar
	  else{ 	
		e=originalEvent;

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
	  MouseEvent originalEvent = e;

	  e= new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
		  		(int)(e.getX()/editorView.getScaleFactor()), (int)(e.getY()/editorView.getScaleFactor()),
		  		(int)(e.getXOnScreen()/editorView.getScaleFactor()), (int)(e.getYOnScreen()/editorView.getScaleFactor()),
		  		e.getClickCount(), false, e.getButton());

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

		if ( e.getButton() == MouseEvent.BUTTON3			//user asked for the popup menu
			 || (/*OSUtils.isMac() &&*/ e.getButton() == MouseEvent.BUTTON1 
			    && (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK)== MouseEvent.CTRL_DOWN_MASK)){
//			|| (/*OSUtils.isMac() &&*/ e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == MouseEvent.CTRL_DOWN_MASK)) {
          Component comp=editorView.getDiagramPanel().getComponentAt(e.getX(), e.getY());

          /* ***DEBUG*** */
          if(debug3){
          	if(comp!=null) System.out.println("rigth clicked on: "+comp.getName());
          	else System.out.println("rigth clicked on nothing. comp="+comp);
          }
          /* ***DEBUG*** */
		  
          //clicked on the diagram panel, not on an element
          if(comp==null || comp.getName()==null || comp.getName().compareTo("")==0 ||
        	 comp.getName().startsWith(EditorView.diagramPanelName)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemPrintModelDebug());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemFitDiagram());
              editorView.setDiagramElementsMenuPosX(originalEvent.getX());
              editorView.setDiagramElementsMenuPosY(originalEvent.getY());
              editorView.showDiagramElementsMenu();
        	  return;
          }

          System.out.println("stampo gli elementi della selezione, se ci sono...");
          for(JComponent compo : editorView.getSelectionGroup()){
        	System.out.println("compo.getName(): "+compo.getName());
          }
		  if ( editorView.getSelectionGroup().contains(comp) && comp.getName().startsWith(EditorView.featureNamePrefix) ){
	          System.out.println("Son nell'if");

			editorView.getDiagramElementsMenu().removeAll();
			editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemSearchFeature());
			editorView.setDiagramElementsMenuPosX(originalEvent.getX());
			editorView.setDiagramElementsMenuPosY(originalEvent.getY());
			editorView.showDiagramElementsMenu();
			editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			editorView.getDiagramElementsMenu().setFocusable(true);

			return;
		  }

          editorView.setPopUpElement((JComponent)comp);
          popupElement=editorView.getPopUpElement();
          
          /* ***DEBUG*** */
		  if(debug)System.out.println("clicked! popupElement: "+popupElement.getName());
          /* ***DEBUG*** */

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
          if(popupElement.getName().startsWith(EditorView.featureNamePrefix)
        	   && !editorView.getSelectionGroup().contains(popupElement)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDeleteFeature());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemRenameFeature());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemChangeColor());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemSearchFeature());
          }
//          //clicked on a feature of the group selection
//		  if (popupElement.getName().startsWith(EditorView.featureNamePrefix)
//			  && editorView.getSelectionGroup().contains(popupElement)){
//			editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemSearchFeature());
//		  }		  
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
          //clicked on a constraint control point
          if(popupElement.getName().startsWith(EditorView.constraintControlPointNamePrefix)){
        	editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemHideControlPoint());
          }

          editorView.setDiagramElementsMenuPosX(originalEvent.getX());
          editorView.setDiagramElementsMenuPosY(originalEvent.getY());
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
	  BufferedImage toolIcon = null;
	  double scaleFactor= 0.0;	  

	  MouseEvent originalEvent = e;
	  e= new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
		  		(int)(e.getX()/editorView.getScaleFactor()), (int)(e.getY()/editorView.getScaleFactor()),
		  		(int)(e.getXOnScreen()/editorView.getScaleFactor()), (int)(e.getYOnScreen()/editorView.getScaleFactor()),
		  		e.getClickCount(), false, e.getButton());

	  Point loc=null;
	  System.out.println("((Component)e.getSource()).getName(): "+((Component)e.getSource()).getName()); 
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

      	  String newFeatureName=
      		((FeaturePanel)popupElement).getLabelName().replaceAll("\n", " ").replaceAll("\\s{2,}+", " ").trim();
      	  
      	  ((FeaturePanel)popupElement).getTextArea().setText(newFeatureName);
      	  
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
//			  System.out.println("e.getModifiers(): "+e.getModifiersEx());
//			  switch(e.getModifiersEx()){
//			    case MouseEvent.ALT_DOWN_MASK: System.out.println("ALT_DOWN_MASK!"); break;
//			    case MouseEvent.ALT_GRAPH_DOWN_MASK: System.out.println("ALT_GRAPH_DOWN_MASK!"); break;
//			    case MouseEvent.SHIFT_DOWN_MASK: System.out.println("SHIFT_DOWN_MASK!"); break;
//			    case MouseEvent.CTRL_DOWN_MASK: System.out.println("CTRL_DOWN_MASK!"); break;
//			    case MouseEvent.META_DOWN_MASK: System.out.println("META_DOWN_MASK!"); break;
//			  }

			  if ( (e.getButton() == MouseEvent.BUTTON3			//user asked for the popup menu
				   || (/*OSUtils.isMac() &&*/ e.getButton() == MouseEvent.BUTTON1 
				   	  && (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK)== MouseEvent.CTRL_DOWN_MASK)) 
				  && ((Component)tmpNode.getElement()).getName().startsWith(EditorView.featureNamePrefix)){

		  		editorView.getDiagramElementsMenu().removeAll();
		  		editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemSearchFeature());
		        editorView.setDiagramElementsMenuPosX(originalEvent.getX());
		        editorView.setDiagramElementsMenuPosY(originalEvent.getY());
		        editorView.showDiagramElementsMenu();
		        editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);

		        /* ***DEBUG*** */
		        if(debug) System.out.println("clicked! popupElement: "+((JComponent)tmpNode.getElement()).getName());
		        /* ***DEBUG*** */

		        return;
			  }

			  /* ***DEBUG*** */
			  if(debug) System.out.println("Mouse Pressed on a selection group element!");
			  /* ***DEBUG*** */

			  editorView.setActiveItem(ActiveItems.DRAGGING_SELECTION_GROUP);	
			  editorView.moveSelectionGroupToTop();					
			  return;
			}
			else{//mouse pressed out of an element of the group selection			
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
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

				editorView.setActiveItem(ActiveItems.DRAGGING_EXTERN_ANCHOR);
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

				editorView.setActiveItem(ActiveItems.DRAGGING_EXTERN_GROUP);
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
				  
				editorView.setActiveItem(ActiveItems.DRAGGING_EXTERN_CONSTRAINT);
				editorView.setLastAnchorFocused(anchorPanel);
				editorView.setLastFeatureFocused(featurePanel);
				//the other end of the constraint is attached to a feature panel
				if(otherEndFeaturePanel.getName().startsWith(EditorView.featureNamePrefix) ){

				  /* ***DEBUG*** */
				  if(debug2) System.out.println("Trying to remove a constraint:"
							+"\nanchorPanelName: "+anchorPanelName+"featurePanel.getID(): "+featurePanel.getID()
							+"((FeaturePanel)otherEndFeaturePanel).getID(): "+((FeaturePanel)otherEndFeaturePanel).getID());
				  /* ***DEBUG*** */

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
				//the other end of the constraint is not attached to a feature panel
				else editorView.detachAnchor(featurePanel, anchorPanel);
					
			  }
			  //mouse directly pressed on a feature panel, not on an inner element
			  else{
				editorView.setActiveItem(ActiveItems.DRAGGING_FEATURE);
				editorView.setLastFeatureFocused((FeaturePanel)tmpNode.getElement());  
				editorView.moveComponentToTop(editorView.getLastFeatureFocused());
			  }
			}
			//mouse directly pressed on an anchor panel in the diagram panel
			else if(/*tmpNode.getElement().getClass().equals(AnchorPanel.class) &&*/(
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startMandatoryNamePrefix) ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endMandatoryNamePrefix)   ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startOptionalNamePrefix)  ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endOptionalNamePrefix) ) ){
			  editorView.setActiveItem(ActiveItems.DRAGGING_EXTERN_ANCHOR);
			  editorView.setLastAnchorFocused((AnchorPanel)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			  if( ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startMandatoryNamePrefix) ||
				  ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startOptionalNamePrefix) )
				editorView.startGroupOpeningAnimation();
			}
			//mouse directly pressed on a group panel in the diagram panel
			else if(/*tmpNode.getElement().getClass().equals(GroupPanel.class) &&*/
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.altGroupNamePrefix) ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.orGroupNamePrefix) ){
			  editorView.setActiveItem(ActiveItems.DRAGGING_EXTERN_GROUP);
			  editorView.setLastAnchorFocused((GroupPanel)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}
			//mouse directly pressed on a constraint panel in the diagram panel
			else if( ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startExcludesNamePrefix) ||
					 ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.startIncludesNamePrefix)  ||
					 ((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endExcludesNamePrefix) ||
					((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.endIncludesNamePrefix)){
			  editorView.setActiveItem(ActiveItems.DRAGGING_EXTERN_CONSTRAINT);
			  editorView.setLastAnchorFocused((ConstraintPanel)tmpNode.getElement());
			  editorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}
			//mouse directly pressed on a constraint control point in the diagram panel
			else if(((JComponent)tmpNode.getElement()).getName().startsWith(EditorView.constraintControlPointNamePrefix)){
			  editorView.setActiveItem(ActiveItems.DRAGGING_CONSTRAINT_CONTROL_POINT);
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

			//updating the modified field
			if(editorView.getActiveItem()!=ActiveItems.NO_ACTIVE_ITEM) modified=true;
 			
			return;
		  }
		  tmpNode=tmpNode.getNext();
		}

		//mouse directly pressed on the diagram panel
		if (editorView.getSelectionGroup().size()>0) editorView.getSelectionGroup().clear();	

		/* ***DEBUG*** */
		if(debug) System.out.println("editorView.getSelectionGroup().size(): "+editorView.getSelectionGroup().size());
		/* ***DEBUG*** */	  
		
		loc = new Point();				
		loc.x=(int)(originalEvent.getLocationOnScreen().getX()-editorView.getLocationOnScreen().getX());
		loc.y=(int)(originalEvent.getLocationOnScreen().getY()-editorView.getLocationOnScreen().getY());
//		loc.x=(int)(e.getLocationOnScreen().getX()-editorView.getLocationOnScreen().getX());
//		loc.y=(int)(e.getLocationOnScreen().getY()-editorView.getLocationOnScreen().getY());

		editorView.setStartSelectionRect(loc);
		editorView.getSelectionRect().setFrameFromDiagonal(loc, loc); 
		
		editorView.setActiveItem(ActiveItems.DRAGGING_SELECTION_RECT);

		/* ***DEBUG*** */
		if(debug2) System.out.println("Mouse pressed on: "+((Component)e.getSource()).getName());
		/* ***DEBUG*** */

      }
	  //event originated from the toolbar
	  else{
//		e=originalEvent;
		editorView.getDiagramElementsMenu().setVisible(false);
		editorView.getDiagramElementsMenu().setEnabled(false);
  		editorView.getDiagramElementsMenu().removeAll();
		editorView.setLastPositionX(originalEvent.getX());
		editorView.setLastPositionY(originalEvent.getY());
//		editorView.setLastPositionX(e.getX());
//		editorView.setLastPositionY(e.getY());
		  
		JComponent comp=(JComponent)e.getSource();
		ImageIcon imageLabel=(ImageIcon) ((JLabel)comp).getIcon();

		/* ***DEBUG*** */
		if(debug4) System.out.println("e.getSource(): "+e.getSource()+"\nimageLabel: "+imageLabel);
		System.out.println("e.getSource().getClass(): "+e.getSource().getClass());
		System.out.println("comp.getName(): "+comp.getName()+"\nimageLabel.getName(): "+imageLabel.getDescription());
		System.out.println("comp.getClass(): "+comp.getClass());
		/* ***DEBUG*** */

		try {
		  toolIcon = ImageIO.read(this.getClass().getResourceAsStream(editorView.getToolIconPath(((JComponent)comp).getName())));
		  scaleFactor = editorView.getScaleFactor();
		  if(scaleFactor != 1.0){
			toolIcon = ImageUtils.toBufferedImage(toolIcon.getScaledInstance(
			  (int)(toolIcon.getWidth()*scaleFactor), (int)(toolIcon.getHeight()*scaleFactor), Image.SCALE_SMOOTH));
		  }
		  editorView.setToolDragImage(toolIcon);
		  editorView.setToolDragPosition( new Point(
			(int)(comp.getLocationOnScreen().getX()-editorView.getLocationOnScreen().getX()
					+comp.getWidth()/2-imageLabel.getIconWidth()/2),
			(int)(comp.getLocationOnScreen().getY()-editorView.getLocationOnScreen().getY()
					+comp.getHeight()/2-imageLabel.getIconHeight()/2)
		  ));

		} catch (IOException e2) {
			System.out.println("toolDragImage is null");
			e2.printStackTrace();
			return;
		}
		if 		(((JComponent)comp).getName()==EditorView.TOOL_NEWFEATURE)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_NEWFEATURE);
		else if (((JComponent)comp).getName()==EditorView.TOOL_MANDATORY_LINK)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_MANDATORY_LINK);				
		else if (((JComponent)comp).getName()==EditorView.TOOL_OPTIONAL_LINK)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_OPTIONAL_LINK);
		else if (((JComponent)comp).getName()==EditorView.TOOL_ALT_GROUP)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_ALT_GROUP);
		else if (((JComponent)comp).getName()==EditorView.TOOL_OR_GROUP)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_OR_GROUP);
		else if (((JComponent)comp).getName()==EditorView.TOOL_INCLUDES)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_INCLUDES);
		else if (((JComponent)comp).getName()==EditorView.TOOL_EXCLUDES)
			editorView.setActiveItem(ActiveItems.DRAGGING_TOOL_EXCLUDES);

		editorView.repaintRootFrame();

		/* ***DEBUG*** */
		if (debug) System.out.println("mousePressed on: "+editorView.getToolDragPosition());
		/* ***DEBUG*** */
	  }
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	  MouseEvent originalEvent = e;
	  e= new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
		  		(int)(e.getX()/editorView.getScaleFactor()), (int)(e.getY()/editorView.getScaleFactor()),
		  		(int)(e.getXOnScreen()/editorView.getScaleFactor()), (int)(e.getYOnScreen()/editorView.getScaleFactor()),
		  		e.getClickCount(), false, e.getButton());

	  //event originated from the diagram panel
      if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) )
		switch(editorView.getActiveItem()){
		  case DRAGGING_FEATURE:
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  editorView.setLastFeatureFocused(null); break;
		  case DRAGGING_EXTERN_ANCHOR:
			  dropAnchor(e);
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_EXTERN_GROUP:
			  dropGroup(e);
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_EXTERN_CONSTRAINT:
			  dropConstraint(e);
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_CONSTRAINT_CONTROL_POINT:
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_SELECTION_RECT:
//			  dropSelectionRectangle(e);
			  dropSelectionRectangle(originalEvent);
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  break;
		  case DRAGGING_SELECTION_GROUP:
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
			  if(editorView.getSelectionGroup().size()>0) editorView.getSelectionGroup().clear();
			  editorView.repaintRootFrame();
			  break;
		  default: break;
	    }

	  //event originated from the toolbar
	  else{
		e = originalEvent;
		modified=true;		  
		switch(editorView.getActiveItem()){
	      case DRAGGING_TOOL_NEWFEATURE:
	    	  addNewFeature(e);
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_MANDATORY_LINK:
	    	  editorView.addConnectorToDiagram();
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_OPTIONAL_LINK:
	    	  editorView.addConnectorToDiagram();
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_INCLUDES:
	    	  editorView.addConstraintToDiagram();
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_EXCLUDES:
	    	  editorView.addConstraintToDiagram();
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_ALT_GROUP:
	    	  editorView.addAltGroupToDiagram();
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_OR_GROUP:
	    	  editorView.addOrGroupToDiagram();
			  editorView.setActiveItem(ActiveItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      default: break;
		}
	  }
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
      if(modified && editorView.confirmSaveDiagramDialog("Diagram "+diagramName+" modified. Do you want to save it?"))
    	saveDiagram();
      //removing instance from tray and closing it
      TrayUtils.removeInstanceFDE(diagramName);
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
//	  String diagDataPath=null;
	  ArrayList<String> modelDataPaths=null;
	  boolean done=false;
	  int operation=0;
	  int choice=0;
	  double currentScale=0.0;
	  EditorView currentView=null;
	  
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
        
        //updating the modified field
        modified=true;
      }
	  //popup menu command: Rename Feature
      else if(e.getActionCommand().equals("Rename Feature")){
    	oldFeatureName=((FeaturePanel)popupElement).getLabelName();
    	if(editorView.getMenuViewCommsOrVars().isSelected()){
    		  
    	  done=false;
    	  for(String tmp : editorView.getStartingCommonalities()) if(oldFeatureName.compareTo(tmp+"\n{C}")==0){
      		((FeaturePanel)popupElement).getTextArea().setText(oldFeatureName.substring(0, oldFeatureName.length()-3));
      		done=true;
    	  }
    	  
    	  if(!done) for(String tmp : editorView.getStartingVariabilities()) if(oldFeatureName.compareTo(tmp+"\n{V}")==0)
      		((FeaturePanel)popupElement).getTextArea().setText(oldFeatureName.substring(0, oldFeatureName.length()-3));
    	}    	  
    	
    	while(editorView.getScaleFactor()!=1.0) editorView.raiseScaleFactor();
    	
        ((FeaturePanel)popupElement).getTextArea().setEditable(true);
        ((FeaturePanel)popupElement).getTextArea().getCaret().setVisible(true);
        ((FeaturePanel)popupElement).getTextArea().grabFocus();
        
        //updating the modified field
        modified=true;
      }
	  //popup menu command: Change Color
      else if(e.getActionCommand().equals("Change Color")){    
    	Color color = ((FeaturePanel)popupElement).getBackground();
        color = JColorChooser.showDialog(null, "Choose Color", color);  
        if (color==null) return;
        else ((FeaturePanel)popupElement).setBackground(color);        

        //updating the modified field
        modified=true;
        editorView.repaintRootFrame();
      }
	  //popup menu command: Search Feature
      else if(e.getActionCommand().equals("Search Feature")){    	 
    	JFrame searchFrame = new JFrame("Search Feature in Input Files");
    	searchFrame.setPreferredSize(new Dimension(900, 700));
    	searchFrame.setSize(900, 700);
    	searchFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    	editorView.setSearchFrame(searchFrame);

    	searchFrame.add(editorView.getTabFeaturesCandidates());
    	searchFrame.setVisible(true);
      }     
	  //popup menu command: Ungroup Element
      else if(e.getActionCommand().equals("Ungroup")){
    	editorView.ungroupAnchor((AnchorPanel)popupElement);
        editorView.repaintRootFrame();

        //updating the modified field
        modified=true;
      }
	  //popup menu command: Show Control Point
      else if(e.getActionCommand().equals("Show Control Point")){
    	editorView.showControlPoint((ConstraintPanel)popupElement);

        //updating the modified field
        modified=true;
      }
	  //popup menu command: Hide Control Point
      else if(e.getActionCommand().equals("Hide Control Point")){
      	editorView.hideControlPoint(popupElement);    	  

        //updating the modified field
        modified=true;
      }
	  //popup menu command: Fit Diagram
      else if(e.getActionCommand().equals("Fit Diagram")){
    	editorView.fitDiagram();

    	//updating the modified field
    	modified=true;
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
	  //menuFiles command: Save Diagram
      else if(e.getActionCommand().equals("Save Diagram")){
  		saveDiagram();    	  
      }
	  //menuFiles command: New Diagram
      else if(e.getActionCommand().equals("New Diagram")){
    	//asking the user if the current diagram must be saved
    	if(modified){
    	  choice=editorView.confirmOrAbortSaveDiagramDialog("Diagram "+diagramName+" modified. Do you want to save it?");
    	  switch(choice){
    	    case 0: break;
    	    case 1: saveDiagram(); break;
    	    case 2: return;
    	  }
    	}
    	  
    	modified=false;    	  
    	  
    	//creating model
  		editorModel= new EditorModel();

  		//getting the close opearation for this frame
  		operation=editorView.getOnCloseOperation();

  		//creating view
  		currentView=editorView;
  		editorView= new EditorView();

  		//setting diagrams save path
  		setSavePath(null);
  		
  		//adding the view as observer to the model
  		editorModel.addObserver(editorView);

		//setting default close operation for the new frame
		editorView.setOnCloseOperation(operation);

		if(!editorView.prepareUI(this) ){
  		  System.out.println("Controller not set. Closing...");
  		  return;
  		}      	  

		//setting the default diagram name and updating tray
		setDiagramNameAndTray(fetchUniqueNewDiagramName(), true);

		//disposing of old frame
  		currentView.dispose();

  		//requesting focus if not automatically assigned by the system
  		editorView.requestFocus();
      }      
	  //menuFiles command: Load Diagram
      else if(e.getActionCommand().equals("Load Diagram")){
    	loadDiagram();

    	//requesting focus if not automatically assigned by the system
  		editorView.requestFocus();
      }
	  //menuFiles command: Import from SXFM
      else if(e.getActionCommand().equals("Import from SXFM")){

    	//asking the user if the current diagram must be saved
      	if(modified){
      	  choice=editorView.confirmOrAbortSaveDiagramDialog("Diagram "+diagramName+" modified. Do you want to save it?");
      	  switch(choice){
      	    case 0: break;
      	    case 1: saveDiagram(); break;
      	    case 2: return;
      	  }      	   
      	}
      	modified=false;    	  

  		String s = null;
  		if((s = editorView.loadXMLDialog("Import from SXFM", CMTConstants.getSaveDiagramDir()
  				+OSUtils.getFilePathSeparator()+"..")) == null) return;
    	
    	//creating model
    	try{
    	  editorModel= EditorModel.createModelFromSXFM(s);
    	}catch(Exception ex){
    	  ex.printStackTrace();
    	  editorView.errorDialog("Error while loading model.");
    	  return;
    	}
    	
  		//getting the close opearation for this frame
  		operation=editorView.getOnCloseOperation();

  		//creating an empty view
  		currentView=editorView;
  		editorView= new EditorView();

  		//setting diagrams save path
  		setSavePath(projectName);

  		//adding the view as observer to the model
  		editorModel.addObserver(editorView);

		//setting default close operation for the new frame
		editorView.setOnCloseOperation(operation);

  		if( !editorView.prepareUI(this) ){
  		  System.out.println("Controller not set. Closing...");
  		  return;
  		}

  		//creating view from model
  		createDiagramFromModel();

  		//disposing of old frame
  		currentView.dispose();    	

        //updating the modified field
        modified=true;

  		//requesting focus if not automatically assigned by the system
  		editorView.requestFocus();
      }
	  //menuFiles command: Export as SXFM
      else if(e.getActionCommand().equals("Export as SXFM")){
    	String s = null;			
    	if((s = editorView.exportAsSXFMDialog()) != null){
    	  modelDataPaths=editorModel.exportAsSXFM(sxfmPath, s);
    	  if(modelDataPaths==null) editorView.errorDialog("Error during save.");
    	  else try{
    		//checking if the SXFM files save directory must be created
      		File dir=new File(CMTConstants.getSaveDiagramDir());			
    		if(!dir.isDirectory() && !dir.mkdirs() ) 
    		  throw new IOException("Save Directory can't be created.");

    		PrintWriter pw1 = new PrintWriter(new BufferedWriter(
  					new FileWriter(CMTConstants.getSaveDiagramDir()+OSUtils.getFilePathSeparator()+s) ));
    		for(String path : modelDataPaths) pw1.println(path);
    		pw1.close();  	
    	  }catch (IOException ex){
    		System.out.println("Exception exportAsSXFM: " + ex.getMessage());
    		ex.printStackTrace();
    	  }
    	}    	      	  
      }
	  //menuFiles command: Export as PNG
      else if(e.getActionCommand().equals("Export as PNG")){
    	currentScale=editorView.getScaleFactor();
      	while(editorView.getScaleFactor()!=1.0) editorView.raiseScaleFactor();
    	editorView.exportAsImageFile(imagesPath, "png");
    	while(editorView.getScaleFactor()!=currentScale) editorView.lowerScaleFactor();
      }
	  //menuFiles command: Export as GIF
      else if(e.getActionCommand().equals("Export as GIF")){
      	currentScale=editorView.getScaleFactor();
      	while(editorView.getScaleFactor()!=1.0) editorView.raiseScaleFactor();
    	editorView.exportAsImageFile(imagesPath, "gif");
    	while(editorView.getScaleFactor()!=currentScale) editorView.lowerScaleFactor();
      }
	  //menuFiles command: Delete Diagram
      else if(e.getActionCommand().equals("Delete Diagram")){
        File tmpFile=null;
        String s = null;
        
        if((s = editorView.loadXMLDialog("Delete Diagram", CMTConstants.getSaveDiagramDir())) != null){
          //deleting chosen diagram save file
          tmpFile=new File(s);
          if (tmpFile.exists()){
        	System.out.println("general save file="+tmpFile.getPath()+" - file exists");
        	tmpFile.delete();
          }  
        }
      }
	  //menuFiles and Tray command: Exit
      else if(e.getActionCommand().equals("Exit")){
      	closeToolInstance();
      }
	  //Tray command: To Front
      else if(e.getActionCommand().equals("To Front")){
      	bringToFront();
      }
	  //Tray command: Maximize
      else if(e.getActionCommand().equals("Maximize")){    	
    	editorView.maximize();
      }
	  //Tray command: Minimize
      else if(e.getActionCommand().equals("Minimize")){
    	editorView.minimize();
      }
      //menuView command: View Commonality/Variability
      else if(e.getActionCommand().equals("View Commonality/Variability")){
//      	currentScale=editorView.getScaleFactor();
//      	while(editorView.getScaleFactor()!=1.0) editorView.raiseScaleFactor();

    	if( ((JCheckBoxMenuItem)editorView.getMenuViewCommsOrVars()).isSelected() )
    	  editorView.viewCommVarsDistinction(true);
    	else editorView.viewCommVarsDistinction(false);

//    	editorView.getDiagramPanel().validate();
//    	editorView.getDiagramPanel().repaint();
//    	editorView.getDiagramScroller().validate();
//    	editorView.getDiagramPanel().repaint();
//    	editorView.validate();
//    	editorView.repaint();
    	
//    	while(editorView.getScaleFactor()!=currentScale) editorView.lowerScaleFactor();
//    	editorView.getDiagramPanel().paint(editorView.getDiagramPanel().getGraphics());

      } 
	  //menuFiles and Tray command: ZoomUp +
      else if(e.getActionCommand().equals("ZoomUp +")){
    	editorView.raiseScaleFactor();
      }
	  //menuFiles and Tray command: ZoomDown -
      else if(e.getActionCommand().equals("ZoomDown -")){
      	editorView.lowerScaleFactor();
      }    
	}

	/**
	 * Returns a default name for a diagram that is not used yet in the tray.
	 * If a possible default name appears in the tray because it's used by this instance, that name can be returned.
	 * 
	 * @return a default name, unique in the tray, for a new diagram
	 */
	private String fetchUniqueNewDiagramName() {
		String newDiagramName=DEFAULT_DIAGRAM_NAME;
		//if the current diagram is already called with this name, it's ok
		if(diagramName.compareTo(newDiagramName)==0) return newDiagramName;

		int i=1;
		if(TrayUtils.isFDEInstancePresent(newDiagramName)){
		  newDiagramName=DEFAULT_DIAGRAM_NAME+"_"+i;
		  //if the current diagram is already called with this name, it's ok
		  if(diagramName.compareTo(newDiagramName)==0) return newDiagramName;
		  while(TrayUtils.isFDEInstancePresent(newDiagramName)){
			newDiagramName=DEFAULT_DIAGRAM_NAME+"_"+(++i);
		  }
		}
		
		return newDiagramName;
	}

	/**
	 * Returns a default name, not used yet in the tray, for a new diagram.
	 * 
	 * @return a default name, unique in the tray, for a new diagram
	 */
	public String fetchUniqueNameForNewDiagram() {
		String newDiagramName=DEFAULT_DIAGRAM_NAME;
		int i=1;
		if(TrayUtils.isFDEInstancePresent(newDiagramName)){
		  newDiagramName=DEFAULT_DIAGRAM_NAME+"_"+i;
		  while(TrayUtils.isFDEInstancePresent(newDiagramName)){
			newDiagramName=DEFAULT_DIAGRAM_NAME+"_"+(++i);
		  }
		}
		
		return newDiagramName;
	}

	/**
	 * Closes this tool instance.
	 */
	public void closeToolInstance() {
		int choice=0;
      	//asking the user if the current diagram must be saved
      	if(modified){
      	  choice=editorView.confirmOrAbortSaveDiagramDialog("Diagram "+diagramName+" modified. Do you want to save it?");
      	  switch(choice){
      	    case 0: break;
      	    case 1: saveDiagram(); break;
      	    case 2: return;
      	  }  
      	}

		//removing instance from tray and closing it
      	TrayUtils.removeInstanceFDE(diagramName);
      	if(editorView.getOnCloseOperation()==JFrame.DISPOSE_ON_CLOSE){
      	  editorView.dispose();
      	  editorView=null;
      	  editorModel=null;
      	}
      	else System.exit(0);
	}
	
	/**
	 * Loads a saved diagram.
	 */
	private void loadDiagram() {
		int operation, choice=0;
		EditorView currentView;
  		String projectName=null;
  		String savedDiagramName=null;

  		SAXParser saxParser = null;
  		InputStream stream = null;
  		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
  		FDEXMLHandler xmlHandler = new FDEXMLHandler();  		
  		
  		//asking the user if the current diagram must be saved
      	if(modified){
      	  choice=editorView.confirmOrAbortSaveDiagramDialog("Diagram "+diagramName+" modified. Do you want to save it?");
      	  switch(choice){
      	    case 0: break;
      	    case 1: saveDiagram(); break;
      	    case 2: return;
      	  }  
      	}
    	modified=false;    	  

  		//loading diagram save file
//  		String loadDirectory=CMTConstants.getSaveDiagramDir();
  		String loadDirectory=diagramPath;
  		
  		
  		String s = null;
  		if((s = editorView.loadXMLDialog("Load Diagram", loadDirectory)) != null) try{
  		  savedDiagramName=s.substring(s.lastIndexOf(OSUtils.getFilePathSeparator())+1, s.length());
  		  
  		  projectName=s.substring(0, s.lastIndexOf(OSUtils.getFilePathSeparator()));
  		  projectName=projectName.substring(projectName.lastIndexOf(OSUtils.getFilePathSeparator())+1, projectName.length());

  		  if(savedDiagramName.endsWith(".xml")) savedDiagramName=savedDiagramName.substring(0, savedDiagramName.length()-4);  	
  		  
  		  //if the selected diagram is not the current diagram and is already loaded, it is brought to front and this is closed
  		  if(TrayUtils.isFDEInstancePresent(savedDiagramName) && savedDiagramName.compareTo(diagramName)!=0){
  			TrayUtils.bringToFrontFDEInstance(savedDiagramName);
  			closeToolInstance();
  			return;  			
  		  }
  			
  		  stream=new FileInputStream(s);
  		  saxParser = saxFactory.newSAXParser();
  		  saxParser.parse(stream, xmlHandler);  			  			
  		}catch (Exception ex) {
  		  editorView.errorDialog("Error while reading save file");
  		  ex.printStackTrace();
  		  return;
  		}
  		else return;
  		  
  		//creating model
  		try{
  		  editorModel= EditorModel.loadSavedModel2(xmlHandler);
  		}catch(Exception ex){
  		  ex.printStackTrace();
  		  editorView.errorDialog("Error while loading model.");
  		  return;
  		}

  		//getting the close opearation for this frame
  		operation=editorView.getOnCloseOperation();

  		//creating an empty view
  		currentView=editorView;
  		editorView= new EditorView();

  		//setting diagram's save path
  		setSavePath(projectName);  		

  		//adding the view as observer to the model
  		editorModel.addObserver(editorView);

		//setting default close operation for the new frame
		editorView.setOnCloseOperation(operation);

  		if( !editorView.prepareUI(this) ){
  		  System.out.println("Controller not set. Closing...");
  		  return;
  		}

  		//disposing of old frame
  		currentView.dispose();

  		//loading saved view data
  		try{
  		  editorView.loadSavedDiagram2(xmlHandler);
  		}catch(Exception ex){
  		  ex.printStackTrace();
  		  editorView.errorDialog("Error while loading diagram.");
  		  return;
  		}

		//setting the default diagram name and updating tray
  		setDiagramNameAndTray(savedDiagramName, true);
	}

	/**
	 * Prompts a file dialog, asking the user to choose a save file, then tries to save the model and the view.
	 */
	private void saveDiagram() {
	  String diagDataPath = null;
	  ArrayList<String> modelDataPaths = null;
//	  String s = null;	
	  File s = null;	
	  String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?><DiagramData>";
	  boolean viewCommsOrVarsWasSelected=false;
	  String savedDiagramName=null;
		
	  //checking if the diagrams save directory must be created
	  File dir=new File(diagramPath);
	  if(!dir.isDirectory() && !dir.mkdirs() ){
		editorView.errorDialog("Save Directory can't be created.");
		return;
	  }
	  
	  if((s = editorView.saveDiagramDialog(diagramPath, diagramName)) != null){
		savedDiagramName=s.getName();
		if(savedDiagramName.endsWith(".xml")) savedDiagramName=savedDiagramName.substring(0, savedDiagramName.length()-4);  

		if (diagramName.compareTo(savedDiagramName)!=0 && s.exists() && s.isFile() && 
			!editorView.confirmSaveDiagramDialog("Diagram with the same name already exists. Do you want to overwrite it?"))			
			return;
		  
		diagDataPath=editorView.saveDiagramView(/*diagramPath, */s);
		modelDataPaths=editorModel.saveDiagramModel(/*diagramPath, */s);
		if(diagDataPath==null || modelDataPaths==null) editorView.errorDialog("Error during save.");
		else try{
		  for(String featureModel : modelDataPaths) xml+=featureModel;
		  xml+=diagDataPath;
		  xml+="</DiagramData>";
			
		  //temporary deactivating visual styles before save
		  if( ((JCheckBoxMenuItem)editorView.getMenuViewCommsOrVars()).isSelected() ){
			((JCheckBoxMenuItem)editorView.getMenuViewCommsOrVars()).setSelected(false);
			editorView.viewCommVarsDistinction(false);
			viewCommsOrVarsWasSelected=true;
		  }

		  PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(s)));

		  //printing general save data in the file
		  pw1.println(xml);
		  pw1.close();  
		  
		  //reactivating visual styles after save
		  if(viewCommsOrVarsWasSelected){
			((JCheckBoxMenuItem)editorView.getMenuViewCommsOrVars()).setSelected(true);
			editorView.viewCommVarsDistinction(true);
		  }

  		  //if an another open diagram was overwrited by save, we try to close instance 
  		  if(diagramName.compareTo(savedDiagramName)!=0 && TrayUtils.isFDEInstancePresent(savedDiagramName)){
  			TrayUtils.tryCloseFDEInstance(savedDiagramName);  			
  		  }

		  //updating the name and the modified field
		  setDiagramNameAndTray(savedDiagramName, true);
		  modified=false;
		  
		}catch (IOException ex){
		  System.out.println("Exception saveDiagram: " + ex.getMessage());
		  ex.printStackTrace();
		}
	  }
	}

	/**
	 * Adds a new unrooted feature to the diagram.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void addNewFeature(MouseEvent e) {
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
				/*EditorView.featureNamePrefix*/""+editorView.getFeaturesCount());
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
		//comp can be a feature, the diagram panel, or a group(if anchor is a starting anchor)
		if (comp!=null) System.out.println("comp.getName()= "+comp.getName());
		JComponent anchor=editorView.getLastAnchorFocused();
		JComponent otherEnd=((AnchorPanel)anchor).getOtherEnd();
		//anchor dropped directly on the diagram panel
		if (comp==null){
		  if (  anchor.getName().startsWith(EditorView.startMandatoryNamePrefix)
			 || anchor.getName().startsWith(EditorView.startOptionalNamePrefix) ){
			 editorView.stopGroupOpeningAnimation();
			 editorView.addNewCloserTimer(null, null, null);
		  }
		  return;
		}
		//anchor directly dropped on a feature inside the diagram panel
		else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
			
		  /* ***DEBUG*** */
		  if(debug) System.out.println("anchor.getName()= "+anchor.getName()
				  			+"\notherEnd.getParent().getName(): "+otherEnd.getParent().getName());
		  /* ***DEBUG*** */
			
		  //this is an ending anchor
		  if( anchor.getName().startsWith(EditorView.endMandatoryNamePrefix)||
			  anchor.getName().startsWith(EditorView.endOptionalNamePrefix) ){

			/* ***DEBUG*** */
			if(debug) System.out.println("otherEnd.getName(): "+otherEnd.getName()
				+"\notherEnd.getParent().getName(): "+otherEnd.getParent().getName());			
			/* ***DEBUG*** */

			if( ( otherEnd.getName().startsWith(EditorView.startMandatoryNamePrefix)
				 || otherEnd.getName().startsWith(EditorView.startOptionalNamePrefix) ) 
				&& otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix) ){
		      //about to link 2 features by a connector
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
		  //this is a starting anchor, and the other end of the connector is anchored to a feature
		  if ( (anchor.getName().startsWith(EditorView.startMandatoryNamePrefix)
				|| anchor.getName().startsWith(EditorView.startOptionalNamePrefix) )
			  && otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix)){			  
			//about to link 2 features by a connector
			if(anchor.getName().startsWith(EditorView.startMandatoryNamePrefix))				
				editorModel.addMandatoryLink( ((FeaturePanel)comp).getID(),
						((FeaturePanel)otherEnd.getParent()).getID() );
			else if(anchor.getName().startsWith(EditorView.startOptionalNamePrefix))
				editorModel.addOptionalLink( ((FeaturePanel)comp).getID(),
						((FeaturePanel)otherEnd.getParent()).getID() );

			return;
		  }
		  //this is a starting anchor, and the other end of the connector is not anchored to anything
		  if (otherEnd.getParent().getName().startsWith(EditorView.diagramPanelName) ){
			editorView.addAnchorToFeature();
			return;
		  }
		}
		//this is a starting anchor, and it has been dropped on a group
		else if (comp.getName().startsWith(EditorView.altGroupNamePrefix)
				 || comp.getName().startsWith(EditorView.orGroupNamePrefix)){
		  //about to merge a connector with a group
		  if(otherEnd.getName().startsWith(EditorView.orGroupNamePrefix))  type=GroupTypes.OR_GROUP;
		  else  type=GroupTypes.ALT_GROUP;
		  
		  //group is not owned by any feature
		  if(comp.getParent().getName()==null || comp.getParent().getName().startsWith(EditorView.diagramPanelName)){
			if(otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix))
			  editorModel.mergeConnectorWithGroup( null, ((FeaturePanel)otherEnd.getParent()).getID(), 
					  							   comp.getName(), type);
			else  editorModel.mergeConnectorWithGroup(null, null, comp.getName(), type);
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
//	  Component comp=editorView.dropGroupOnDiagram(e);
	  Component comp=editorView.dropGroupOrConstraintOnDiagram(e);
	  if (comp!=null) System.out.println("comp= "+comp.getName());
	  GroupPanel group=(GroupPanel)editorView.getLastAnchorFocused();
	  
	  //group dropped directly on the diagram panel
	  if (comp==null) return;
	  
	  //group dropped on a feature inside the diagram panel
	  else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
		
		if (group.getMembers().size()==0 ){//the group has no members
		  editorView.addAnchorToFeature(); 
		  return;
		}
		else{//about to add group to the feature comp
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
	  Component comp=editorView.dropGroupOrConstraintOnDiagram(e);
	  if (comp!=null) System.out.println("comp= "+comp.getName());
	  ConstraintPanel constraint=(ConstraintPanel)editorView.getLastAnchorFocused();
	  
	  //constraint dropped directly on the diagram panel
	  if (comp==null) return;
	  
	  //constraint dropped on a feature inside the diagram panel
	  else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
		  
		  
		if (constraint.getOtherEnd().getParent().getName().startsWith(EditorView.featureNamePrefix) ){
		  //the constraint links two features
		  System.out.println("about to add a constraint to the model");
		  if(constraint.getName()	.startsWith(EditorView.startIncludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)comp).getID(),
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				EditorModel.ConstraintTypes.INCLUDES,
				constraint.getName().substring(EditorView.startIncludesNamePrefix.length()));
			return;
		  }
		  if(constraint.getName().startsWith(EditorView.endIncludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				((FeaturePanel)comp).getID(),
				EditorModel.ConstraintTypes.INCLUDES,
				constraint.getName().substring(EditorView.endIncludesNamePrefix.length()));
			return;
		  }
		  if(constraint.getName().startsWith(EditorView.startExcludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)comp).getID(),
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				EditorModel.ConstraintTypes.EXCLUDES,
				constraint.getName().substring(EditorView.startExcludesNamePrefix.length()));
			return;
		  }
		  if(constraint.getName().startsWith(EditorView.endExcludesNamePrefix)){
			editorModel.addConstraint(
				((FeaturePanel)constraint.getOtherEnd().getParent()).getID(), 
				((FeaturePanel)comp).getID(),
				EditorModel.ConstraintTypes.EXCLUDES,
				constraint.getName().substring(EditorView.endExcludesNamePrefix.length()));
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

//	/**
//	 * Detach an anchor or group from the feature featurePanel, attaching it to the diagram.
//	 * 
//	 * @param feature - the feature from wich the anchor must be detached
//	 * @param anchor - the anchor to detach
//	 */
//	private void detachAnchor(FeaturePanel feature, JComponent anchor) {
//		JComponent anchorFocused;
//		int anchorPanelOnScreenX;
//		int anchorPanelOnScreenY;
//		anchorFocused=(JComponent)anchor;
//		editorView.setLastAnchorFocused(anchorFocused);
//
//		anchorPanelOnScreenX=(int)anchorFocused.getLocationOnScreen().getX();
//		anchorPanelOnScreenY=(int)anchorFocused.getLocationOnScreen().getY();
//		feature.remove(anchorFocused);
//		feature.validate();
//		
//		editorView.getLastAnchorFocused().setLocation(
//		  (int)(anchorPanelOnScreenX-editorView.getDiagramPanel().getLocationOnScreen().getX()),
//		  (int)(anchorPanelOnScreenY-editorView.getDiagramPanel().getLocationOnScreen().getY()));
//		editorView.getDiagramPanel().setLayer(editorView.getLastAnchorFocused(), 0);
//		editorView.getDiagramPanel().add(editorView.getLastAnchorFocused());
//		editorView.getDiagramPanel().setComponentZOrder(editorView.getLastAnchorFocused(), 0);
////		EditorView.moveComponentToTop(editorView.getLastAnchorFocused());
//		editorView.moveComponentToTop(editorView.getLastAnchorFocused());
//	}

	/**
	 * Sets the paths used for saving the project and for exported SXFM and images files.
	 * 
	 * @param projectName - the name of analisys project, used for saving the project. 
	 * If null, a default directory will be used.
	 */
	public void setSavePath(String projectName){		
		if(projectName!=null){
		  this.diagramPath=CMTConstants.getSaveDiagramDir()+OSUtils.getFilePathSeparator()+projectName;
		  this.projectName=projectName;
		}
		else{			
		  this.diagramPath=CMTConstants.getSaveDiagramDir()+OSUtils.getFilePathSeparator()+CMTConstants.customSaveDiagramDir;
		  this.projectName=CMTConstants.customSaveDiagramDir;
		}
		this.sxfmPath=diagramPath+CMTConstants.sxfmSubPath;
		this.imagesPath=diagramPath+CMTConstants.imagesSubPath;	
		System.out.println("setSavePath(): diagramPath="+diagramPath+"\nprojectName="
							+projectName+"\nthis.projectName="+this.projectName);
	}
	
	/**
	 * Returns the diagram name.
	 * 
	 * @return - a String representing the last name with which this diagram was saved
	 */
	public String getDiagramName(){
	  return diagramName;
	}
	
	/**
	 * Sets the diagram name.
	 * NOTE: This method should be called anytime the diagram name has to be set, 
	 * in order to update the tool's tray.
	 * 
	 * @param name - the new diagram name
	 * @param check - if true, the tray will be checked for the current instance name and if present,
	 *  instance name will be updated in tray too; if false, name will be updated but the new submenu is always added to tray
	 */
	public void setDiagramNameAndTray(String name, boolean check){
		if(!check || !TrayUtils.isFDEInstancePresent(diagramName)){
		  this.diagramName=name;
		  TrayUtils.createAndShowFDETray(this);
		}
		else{
		  TrayUtils.updateInstanceFDE(diagramName, name);
		  this.diagramName=name;			
		}		
	}
	
	/**
	 * Brings this instance root frame to front.
	 */
	public void bringToFront(){
//	  editorView.minimize();
//	  editorView.maximize();

	  editorView.setState(JFrame.ICONIFIED);		
	  try {
		Thread.sleep(350);
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
	  
	  editorView.setExtendedState(JFrame.MAXIMIZED_BOTH);
	  editorView.setAlwaysOnTop(true);
	  editorView.setAlwaysOnTop(false);		

	  //requesting focus if not automatically assigned by the system
	  editorView.requestFocus();
	      
	}

	/**
	 * Adds starting Commonalities and Variabilities to the diagram and model, using the ArrayLists in EditorView.
	 */
	public void addStartingfeatures() {
		ArrayList<String> startingCommonalities=editorView.getStartingCommonalities();			
		ArrayList<String> startingVariabilities=editorView.getStartingVariabilities();
		int i=0;
//		String[] strArr=projectName.split(OSUtils.getFilePathSeparator());
//		String rootName = strArr[strArr.length-1];
		String rootName = projectName.substring(projectName.lastIndexOf(OSUtils.getFilePathSeparator())+1, projectName.length());
		//adding root feature
		System.out.println("Adding root: "+rootName);
		editorModel.addUnrootedFeatureNoNotify(rootName, ""+0, /*1*/FeatureTypes.COMMONALITY);
		++i;
//		editorView.incrFeaturesCount();

		//adding starting commonalities
		for(String name : startingCommonalities){
		  System.out.println("Adding commonality: "+name);
		  editorModel.addSubFeatureNoNotify(name, ""+0, ""+i, /*1*/FeatureTypes.COMMONALITY);
		  ++i;
//		  editorView.incrFeaturesCount();			
		}

		/*		
		int i=5, j=10;
		for(String name : startingCommonalities){
		  if(i>=diagramPanel.getWidth()){ i=5; j+=5+editorView.getFeatureSize().height;}
		  position.x=i+(int)diagramPanel.getLocationOnScreen().getX();
		  position.y=j+(int)diagramPanel.getLocationOnScreen().getY();
		  editorView.setToolDragPosition(position);
		  editorView.setFeatureToAddName(name);
		  editorModel.addUnrootedNamedFeature(name, ""+editorView.getFeaturesCount(), FeatureTypes.COMMONALITY);
//		  editorModel.addUnrootedNamedFeature(name, 
//					EditorView.featureNamePrefix+editorView.getFeaturesCount(), FeatureTypes.COMMONALITY);
		  i+=5+editorView.getFeatureSize().width;
		}
		 */

		//adding starting variabilities
		for(String name : startingVariabilities){
		  System.out.println("Adding variability: "+name);
		  editorModel.addSubFeatureNoNotify(name, ""+0, ""+i, /*0*/FeatureTypes.VARIABILITY);
		  ++i;
//		  editorView.incrFeaturesCount();			
		}
		
		/*		
		for(String name : startingVariabilities){
		  if(i>=diagramPanel.getWidth()){ i=5; j+=5+editorView.getFeatureSize().height;}
		  position.x=i+(int)diagramPanel.getLocationOnScreen().getX();
		  position.y=j+(int)diagramPanel.getLocationOnScreen().getY();
		  editorView.setToolDragPosition(position);
		  editorView.setFeatureToAddName(name);
		  editorModel.addUnrootedNamedFeature(name, ""+editorView.getFeaturesCount(), FeatureTypes.VARIABILITY);
//		  editorModel.addUnrootedNamedFeature(name, 
//				  EditorView.featureNamePrefix+editorView.getFeaturesCount(), FeatureTypes.VARIABILITY);
		  i+=5+editorView.getFeatureSize().width;
		}
 		*/	

		createDiagramFromModel();
	}

	/**
	 * Creates a feature diagram from a feature model. The model must represent a single feature tree,
	 *  with only 1 feature without parent(the root feature).
	 */
	private void createDiagramFromModel() {
		int[] featureLogicCell=null;//coordinates of a cell in the logic grid
		int gridSize=0;
		ArrayList<Entry<FeatureNode, int[]>> nodesToExpand = null;//next feature nodes to elaborate
		Dimension featureSize=editorView.getFeatureSize();
//		JLayeredPane diagramPanel=editorView.getDiagramPanel();
		
		HashMap<String, FeatureNode> featuresList = editorModel.getFeaturesList();

		if(featuresList.size()%2==0) gridSize=featuresList.size()+1;
		else gridSize=featuresList.size();
		
		//initializing logicGrid
		logicGrid=new boolean[gridSize][gridSize];
		for(int i =0; i<logicGrid.length; ++i) for(int j =0; j<logicGrid.length; ++j) logicGrid[i][j]=false;
		
		editorView.getDiagramPanel().setSize(gridSize*(featureSize.width+10), gridSize*(featureSize.height+30));

		
		//getting root feature
		FeatureNode rootFeature = editorModel.getUniqueRootfeature();
		
		//logic position of root feature is at the top-middle of the grid
		featureLogicCell = new int[2];
		featureLogicCell[0]=gridSize/2;
		featureLogicCell[1]=0;
		
		if(rootFeature==null){
		  editorView.errorDialog("Cold not create diagram! Missing unique root feature in featureTree");
		  return;
		}
		
		//initializign the list of next nodes to expand
		nodesToExpand = new ArrayList<Map.Entry<FeatureNode,int[]>>();
	
		//adding root feature to the diagram
		Point rootLocationOnDiagram = new Point();
		rootLocationOnDiagram.x =featureLogicCell[0]*(featureSize.width+5);
		rootLocationOnDiagram.y=featureLogicCell[1]*(featureSize.width+5);
//		rootLocationOnDiagram.x+=(int)diagramPanel.getLocationOnScreen().getX();
//		rootLocationOnDiagram.y+=(int)diagramPanel.getLocationOnScreen().getY();

		editorView.directlyAddFeatureToDiagram(
				rootFeature.getName(), EditorView.featureNamePrefix+rootFeature.getID(), 
				rootLocationOnDiagram.x, rootLocationOnDiagram.y, featureSize.width, featureSize.height, null);
		
		editorView.incrFeaturesCount();			
		
		//adding starting element to the list, the root feature
		nodesToExpand.add(new AbstractMap.SimpleEntry<FeatureNode, int[]>(rootFeature, featureLogicCell));
		
		recBuildDiagram(nodesToExpand);

		//fitting diagram
		editorView.getDiagramPanel().validate();
		editorView.fitDiagram();		
		
	}

	/**
	 * Recursevely build the diagram elements representing the subtrees rooted in the nodes of nodesToExpand list.
	 * If the list contains the diagram root feature, the entire tree will be built.
	 * 
	 * @param nodesToExpand - list of feature nodes with their position in the logic grid.
	 */
	private void recBuildDiagram(ArrayList<Entry<FeatureNode, int[]>> nodesToExpand) {
//	  int currentNodesToExpand=0;
//	  ArrayList<FeatureNode> children = null;
	  int totalChildren=0, childrenPlaced=0;
	  int k=0;
	  FeatureNode currentNode=null;
	  GroupPanel groupPanel=null;
	  ConstraintPanel startConstraintPanel=null, endConstraintPanel=null;
	  JLabel controlConstraintPanel=null;
	  FeaturePanel featurePanel = null, endFeaturePanel = null;
	  AnchorPanel endAnchorPanel=null, startAnchorPanel=null;
	  int[] currentNodePosition=null, firstChildPosition=new int[2];
	  boolean correctlyPlaced=false;
	  int maxFeatureID=0;
	  int maxOrGroupID=0;
	  int maxAltGroupID=0;
	  int maxConstraintID=0;
	  int maxTmp=0;
	  Dimension featureSize=editorView.getFeatureSize();
	  Dimension anchorSize=editorView.getAnchorSize();
	  Point childLocationOnDiagram=new Point();
	  Point locationInFeature=new Point();
//	  JLayeredPane diagramPanel=editorView.getDiagramPanel();
	  ImageIcon groupLineLengthIcon = editorView.getGroupLineIcon();
		
	  //adding to the diagram the children of each node to expand
	  while(nodesToExpand.size()>0){
//		currentNodesToExpand=nodesToExpand.size();
//		for(int i=0; i<currentNodesToExpand; ++i){
		  currentNode=nodesToExpand.get(0).getKey();
		  System.out.println("currentNode: "+currentNode.getName()+"("+currentNode.getID()+")");

		  //updating maxFeatureID
		  try{
			  maxTmp=Integer.parseInt(currentNode.getID());
			  if(maxTmp>maxFeatureID) maxFeatureID=maxTmp;
		  }catch(NumberFormatException e){}

		  currentNodePosition=nodesToExpand.get(0).getValue();

		  //calculating total number of children
		  totalChildren=currentNode.getSubFeatures().size();
		  for(GroupNode group : currentNode.getSubGroups()) totalChildren+=group.getSubFeatures().size();

		  //calulating first child position in the logic grid
		  if(totalChildren%2==0) firstChildPosition[0]=currentNodePosition[0]-totalChildren/2+1;
		  else firstChildPosition[0]=currentNodePosition[0]-totalChildren/2;		  
		  firstChildPosition[1]=currentNodePosition[1]+1;

		  //trying to place the children in the logic grid
		  correctlyPlaced=false;
		  while(!correctlyPlaced){
			  //checking if there are some location already occupied in the logic grid
			  for(k=firstChildPosition[0]; k<firstChildPosition[0]+totalChildren; ++k){
				  System.out.println("logicGrid["+k+"]["+firstChildPosition[1]+"]="+logicGrid[k][firstChildPosition[1]]
						  			+"\nk="+k+"\tfirstChildPosition[1]="+firstChildPosition[1]);
				  if(logicGrid[k][firstChildPosition[1]]) break;			
			  }

			  //if there are occupied cells in this line, next line will be checked
			  if(k<firstChildPosition[0]+totalChildren) ++firstChildPosition[1];
			  else correctlyPlaced=true;
		  }

		  //placing the children in the logic grid
		  for(k=firstChildPosition[0]; k<firstChildPosition[0]+totalChildren; ++k)
			  logicGrid[k][firstChildPosition[1]]=true;		

		  //adding the children to the feature diagram
		  childrenPlaced=0;

		  //adding alternative groups
		  //initializing alternative group location
		  locationInFeature.x=featureSize.width/2-17; 
		  locationInFeature.y=featureSize.height-13;

		  for(GroupNode group : currentNode.getSubGroups()){
//			  if(group.getCardinality().y>1 || group.getCardinality().y<0) continue;//this is an or group
			  if(group.getDecompositionType()!=GroupTypes.ALT_GROUP) continue;//this is not an alternative group
			  else{

				  //updating maxOrGroupID
				  try{
					  maxTmp=Integer.parseInt(group.getID());
					  if(maxTmp>maxOrGroupID) maxOrGroupID=maxTmp;
				  }catch(NumberFormatException e){}				

				  //creating group				
				  groupPanel=(GroupPanel)editorView.buildConnectionDot(
						  ItemsType.ALT_GROUP_START_CONNECTOR, EditorView.altGroupNamePrefix+group.getID(),
						  locationInFeature.x, locationInFeature.y); 

				  //adding group to the feature				
				  editorView.directlyAddGroupToFeature(groupPanel,
						  editorView.getFeaturePanel(/*EditorView.featureNamePrefix+*/currentNode.getID()),
						  ItemsType.ALT_GROUP_START_CONNECTOR);

				  //adding group members
				  for(FeatureNode child : group.getSubFeatures()){
					  childLocationOnDiagram.x =(firstChildPosition[0]+childrenPlaced)*(featureSize.width+10);
					  childLocationOnDiagram.y=firstChildPosition[1]*(featureSize.height+30);
//					  childLocationOnDiagram.x+=(int)diagramPanel.getLocationOnScreen().getX();
//					  childLocationOnDiagram.y+=(int)diagramPanel.getLocationOnScreen().getY();

					  //adding child feature to the diagram
					  featurePanel = editorView.directlyAddFeatureToDiagram(
							  child.getName(), EditorView.featureNamePrefix+child.getID(), 
							  childLocationOnDiagram.x, childLocationOnDiagram.y, featureSize.width, featureSize.height, null);

					  //connecting child with the group
					  endAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
							  ItemsType.END_MANDATORY_CONNECTOR, EditorView.endMandatoryNamePrefix+editorView.getConnectorsCount(),
							  featureSize.width/2-anchorSize.width/2, 0);

					  editorView.directlyAddAnchorToFeature(endAnchorPanel,featurePanel);

					  //adding mutual references to the panel
					  groupPanel.getMembers().add(endAnchorPanel);
					  endAnchorPanel.setOtherEnd(groupPanel);

					  //adding the child to nodes to expand list
					  currentNodePosition=new int[2];
					  currentNodePosition[0]=firstChildPosition[0]+childrenPlaced;
					  currentNodePosition[1]=firstChildPosition[1];
					  nodesToExpand.add(new AbstractMap.SimpleEntry<FeatureNode, int[]>(child, currentNodePosition));

					  editorView.incrFeaturesCount();			
					  ++childrenPlaced;
				  }

				  if(groupPanel.getMembers().size()<2){//the group must have at least 2 ending dots to draw its arc

					  //adding missing dot to the group
					  endAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
							  ItemsType.END_MANDATORY_CONNECTOR, EditorView.endMandatoryNamePrefix+editorView.getConnectorsCount(),
							  groupPanel.getLocation().x+groupLineLengthIcon.getIconWidth(),
							  groupPanel.getLocation().y+groupLineLengthIcon.getIconHeight()+groupPanel.getHeight()-3);

					  editorView.directlyAddAnchorToDiagram(endAnchorPanel);

					  //adding mutual references to the panels
					  groupPanel.getMembers().add(endAnchorPanel);
					  endAnchorPanel.setOtherEnd(groupPanel);
				  }

				  /*groupLocationInFeature=*/nextAltGroupLocation(groupPanel.getSize(), featureSize, locationInFeature);			  
			  }

		  }

		  //adding mandatory and optional subfeatures		  
		  //initializing start panels location
		  locationInFeature.x=featureSize.width/2-5; 
		  locationInFeature.y=featureSize.height-13;

		  for(FeatureNode subFeature : currentNode.getSubFeatures()){
			  childLocationOnDiagram.x =(firstChildPosition[0]+childrenPlaced)*(featureSize.width+10);
			  childLocationOnDiagram.y=firstChildPosition[1]*(featureSize.height+30);
//			  childLocationOnDiagram.x+=(int)diagramPanel.getLocationOnScreen().getX();
//			  childLocationOnDiagram.y+=(int)diagramPanel.getLocationOnScreen().getY();

			  //adding sub feature to the diagram
			  featurePanel = editorView.directlyAddFeatureToDiagram(
					  subFeature.getName(), EditorView.featureNamePrefix+subFeature.getID(), 
					  childLocationOnDiagram.x, childLocationOnDiagram.y, featureSize.width, featureSize.height, null);

			  //getting start anchor panel
			  if(subFeature.getCardinality().x>0) startAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
					  ItemsType.START_MANDATORY_CONNECTOR, EditorView.startMandatoryNamePrefix+editorView.getConnectorsCount(),
					  locationInFeature.x, locationInFeature.y);				
			  else startAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
					  ItemsType.START_OPTIONAL_CONNECTOR, EditorView.startOptionalNamePrefix+editorView.getConnectorsCount(),
					  locationInFeature.x, locationInFeature.y);

			  //getting end anchor panel
			  if(subFeature.getCardinality().x>0) endAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
					  ItemsType.END_MANDATORY_CONNECTOR, EditorView.endMandatoryNamePrefix+editorView.getConnectorsCount(),
					  featureSize.width/2-anchorSize.width/2, 0);
			  else endAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
					  ItemsType.END_OPTIONAL_CONNECTOR, EditorView.endOptionalNamePrefix+editorView.getConnectorsCount(),
					  featureSize.width/2-anchorSize.width/2, 0);

			  //connecting sub feature with the parent feature
			  editorView.directlyAddAnchorToFeature(endAnchorPanel, featurePanel);
			  editorView.directlyAddAnchorToFeature(startAnchorPanel, 
					  editorView.getFeaturePanel(/*EditorView.featureNamePrefix+*/currentNode.getID()));

			  //adding mutual references to the panels
			  startAnchorPanel.setOtherEnd(endAnchorPanel);
			  endAnchorPanel.setOtherEnd(startAnchorPanel);

			  //adding sub feature to nodes to expand list
			  currentNodePosition=new int[2];
			  currentNodePosition[0]=firstChildPosition[0]+childrenPlaced;
			  currentNodePosition[1]=firstChildPosition[1];
			  nodesToExpand.add(new AbstractMap.SimpleEntry<FeatureNode, int[]>(subFeature, currentNodePosition));

			  editorView.incrFeaturesCount();			
			  ++childrenPlaced;
		  }

		  //adding or groups
		  //initializing alternative group location
		  locationInFeature.x=featureSize.width/2+7; 
		  locationInFeature.y=featureSize.height-13;		  
		  for(GroupNode group : currentNode.getSubGroups()){
//			  if(group.getCardinality().y==1) continue;//this is an alternative group
			  if(group.getDecompositionType()!=GroupTypes.OR_GROUP) continue;//this is not an or group
			  else{

				  //updating maxAltGroupID
				  try{
					  maxTmp=Integer.parseInt(group.getID());
					  if(maxTmp>maxAltGroupID) maxAltGroupID=maxTmp;
				  }catch(NumberFormatException e){}		

				  //creating group				
				  groupPanel=(GroupPanel)editorView.buildConnectionDot(
						  ItemsType.OR_GROUP_START_CONNECTOR, EditorView.altGroupNamePrefix+group.getID(),
						  locationInFeature.x, locationInFeature.y); 

				  //adding group to the feature				
				  editorView.directlyAddGroupToFeature(groupPanel,
						  editorView.getFeaturePanel(/*EditorView.featureNamePrefix+*/currentNode.getID()), 
						  ItemsType.OR_GROUP_START_CONNECTOR);

				  //adding group members
				  for(FeatureNode child : group.getSubFeatures()){
					  childLocationOnDiagram.x =(firstChildPosition[0]+childrenPlaced)*(featureSize.width+10);
					  childLocationOnDiagram.y=firstChildPosition[1]*(featureSize.height+30);
//					  childLocationOnDiagram.x+=(int)diagramPanel.getLocationOnScreen().getX();
//					  childLocationOnDiagram.y+=(int)diagramPanel.getLocationOnScreen().getY();

					  //adding child feature to the diagram
					  featurePanel = editorView.directlyAddFeatureToDiagram(
							  child.getName(), EditorView.featureNamePrefix+child.getID(), 
							  childLocationOnDiagram.x, childLocationOnDiagram.y, featureSize.width, featureSize.height, null);

					  //connecting child with the group
					  endAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
							  ItemsType.END_MANDATORY_CONNECTOR, EditorView.endMandatoryNamePrefix+editorView.getConnectorsCount(),
							  featureSize.width/2-anchorSize.width/2, 0);

					  editorView.directlyAddAnchorToFeature(endAnchorPanel,featurePanel);

					  //adding mutual references to the panels
					  groupPanel.getMembers().add(endAnchorPanel);
					  endAnchorPanel.setOtherEnd(groupPanel);

					  //adding the child to nodes to expand list
					  currentNodePosition=new int[2];
					  currentNodePosition[0]=firstChildPosition[0]+childrenPlaced;
					  currentNodePosition[1]=firstChildPosition[1];
					  nodesToExpand.add(new AbstractMap.SimpleEntry<FeatureNode, int[]>(child, currentNodePosition));

					  editorView.incrFeaturesCount();			
					  ++childrenPlaced;
				  }

				  if(groupPanel.getMembers().size()<2){//the group must have at least 2 ending dots to draw its arc

					  //adding missing dot to the group
					  endAnchorPanel=(AnchorPanel)editorView.buildConnectionDot(
							  ItemsType.END_MANDATORY_CONNECTOR, EditorView.endMandatoryNamePrefix+editorView.getConnectorsCount(),
							  groupPanel.getLocation().x+groupLineLengthIcon.getIconWidth(),
							  groupPanel.getLocation().y+groupLineLengthIcon.getIconHeight()+groupPanel.getHeight()-3);

					  editorView.directlyAddAnchorToDiagram(endAnchorPanel);

					  //adding mutual references to the panel
					  groupPanel.getMembers().add(endAnchorPanel);
					  endAnchorPanel.setOtherEnd(groupPanel);
				  }

				  /*groupLocationInFeature=*/nextOrGroupLocation(groupPanel.getSize(), featureSize, locationInFeature);			  
			  }
		  }

		  //removing current node from nodes to expand list
//		  nodesToExpand.remove(currentNode);
		  nodesToExpand.remove(0);
//		}
	  }

	  //adding constraints
	  for(String[] constraint : editorModel.getConstraints()){

		//checking how the panels are located in the diagram
		featurePanel = editorView.getFeaturePanel(/*EditorView.featureNamePrefix+*/constraint[1]);
		endFeaturePanel = editorView.getFeaturePanel(/*EditorView.featureNamePrefix+*/constraint[2]);

		//adding starting constraint panel
		if(endFeaturePanel.getX()>featurePanel.getX()){
		  locationInFeature.x=featureSize.width-10; locationInFeature.y=featureSize.height/2-12;
		}
		else{
		  locationInFeature.x=0; locationInFeature.y=featureSize.height/2-12;
		}

		if(constraint[0].startsWith(EditorModel.includesConstraintNamePrefix)){
		  startConstraintPanel = (ConstraintPanel)editorView.buildConnectionDot(
			ItemsType.START_INCLUDES_DOT, 
			EditorView.startIncludesNamePrefix+constraint[0].substring(EditorModel.includesConstraintNamePrefix.length()),
			locationInFeature.x, locationInFeature.y); 

		  //updating maxConstraintID
		  try{
			maxTmp=Integer.parseInt(constraint[0].substring(EditorModel.includesConstraintNamePrefix.length()));
			if(maxTmp>maxConstraintID) maxConstraintID=maxTmp;
		  }catch(NumberFormatException e){}					  
		}
		else{
		  startConstraintPanel = (ConstraintPanel)editorView.buildConnectionDot(
			ItemsType.START_EXCLUDES_DOT, 
			EditorView.startExcludesNamePrefix+constraint[0].substring(EditorModel.excludesConstraintNamePrefix.length()),
			locationInFeature.x, locationInFeature.y); 

		  //updating maxConstraintID
		  try{
			maxTmp=Integer.parseInt(constraint[0].substring(EditorModel.excludesConstraintNamePrefix.length()));
			if(maxTmp>maxConstraintID) maxConstraintID=maxTmp;
		  }catch(NumberFormatException e){}					  
		}
		
		editorView.directlyAddAnchorToFeature(startConstraintPanel, featurePanel);

		//adding ending constraint panel
		if(endFeaturePanel.getX()>featurePanel.getX()){
		  locationInFeature.x=0; locationInFeature.y=featureSize.height/2-12;
		}
		else{
		  locationInFeature.x=featureSize.width-10; locationInFeature.y=featureSize.height/2-12;
		}

		if(constraint[0].startsWith(EditorModel.includesConstraintNamePrefix))
		  endConstraintPanel = (ConstraintPanel)editorView.buildConnectionDot(
			ItemsType.END_INCLUDES_DOT, 
			EditorView.endIncludesNamePrefix+constraint[0].substring(EditorModel.includesConstraintNamePrefix.length()),
			locationInFeature.x, locationInFeature.y); 
		else
		  endConstraintPanel = (ConstraintPanel)editorView.buildConnectionDot(
			ItemsType.END_EXCLUDES_DOT, 
			EditorView.endExcludesNamePrefix+constraint[0].substring(EditorModel.excludesConstraintNamePrefix.length()),
			locationInFeature.x, locationInFeature.y); 

		editorView.directlyAddAnchorToFeature(endConstraintPanel, endFeaturePanel);

		//adding constraint control panel
		if(endFeaturePanel.getX()>featurePanel.getX()) locationInFeature.x=endFeaturePanel.getX();
		else locationInFeature.x=endFeaturePanel.getX()+featureSize.width-10; 
		locationInFeature.y=endFeaturePanel.getY()+featureSize.height/2-12;
		 
		controlConstraintPanel = (JLabel)editorView.buildConnectionDot(
			ItemsType.CONSTRAINT_CONTROL_POINT, 
			EditorView.constraintControlPointNamePrefix+editorView.getConstraintControlsCount(),
			locationInFeature.x, locationInFeature.y); 

		editorView.incrConstraintControlsCount();

		editorView.directlyAddAnchorToDiagram(controlConstraintPanel);

		//adding mutual references to the panels
		startConstraintPanel.setOtherEnd(endConstraintPanel);
		startConstraintPanel.setControlPoint(controlConstraintPanel);
		endConstraintPanel.setOtherEnd(startConstraintPanel);
		endConstraintPanel.setControlPoint(controlConstraintPanel);
		
		//setting counters
		if(maxFeatureID>editorView.getFeaturesCount()) editorView.setFeaturesCount(maxFeatureID); 
		if(maxOrGroupID>editorView.getOrGroupsCount()) editorView.setOrGroupsCount(maxOrGroupID); 
		if(maxAltGroupID>editorView.getAltGroupsCount()) editorView.setAltGroupsCount(maxAltGroupID); 
		if(maxConstraintID>editorView.getConstraintsCount()) editorView.setConstraintsCount(maxConstraintID); 
		
	  }
	}

	/**
	 * Sets the Point groupLocationInFeature to the next group location for an alternative group in a feature panel.
	 * 
	 * @param groupPanelSize - size of the group panel
	 * @param featureSizee - size of the feature panel
	 * @param groupLocationInFeature - previous group location
	 */
	private /*Point */void nextAltGroupLocation(Dimension groupPanelSize, Dimension featureSize, Point groupLocationInFeature) {
	  if(groupLocationInFeature.x-groupPanelSize.width-2>0)
		groupLocationInFeature.x-=groupPanelSize.width+2;
	  else if(groupLocationInFeature.y-groupPanelSize.height-2>featureSize.height/2)
		groupLocationInFeature.y-=groupPanelSize.height+2;
	  else{
		groupLocationInFeature.x=featureSize.width/2-17; 
		groupLocationInFeature.y=featureSize.height-13;		  
	  }
//		return null;
	}
	
	/**
	 * Sets the Point groupLocationInFeature to the next group location for an or group in a feature panel.
	 * 
	 * @param groupPanelSize - size of the group panel
	 * @param featureSizee - size of the feature panel
	 * @param groupLocationInFeature - previous group location
	 */
	private /*Point */void nextOrGroupLocation(Dimension groupPanelSize, Dimension featureSize, Point groupLocationInFeature) {
	  if(groupLocationInFeature.x+groupPanelSize.width+2<featureSize.width-10)
		groupLocationInFeature.x+=groupPanelSize.width+2;
	  else if(groupLocationInFeature.y-groupPanelSize.height-2>featureSize.height/2)
		groupLocationInFeature.y-=groupPanelSize.height+2;
	  else{
		groupLocationInFeature.x=featureSize.width/2+7; 
		groupLocationInFeature.y=featureSize.height-13;		  
	  }
//		return null;
	}
}
