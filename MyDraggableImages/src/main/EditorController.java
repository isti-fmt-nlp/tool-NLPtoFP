package main;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import main.EditorView.activeItems;

public class EditorController implements ActionListener, WindowListener, MouseListener, MouseMotionListener{

	/** variables used for debugging*/
	private static boolean debug=false;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=false;
		
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
	public void mouseMoved(MouseEvent e) {}
	
	@Override
	public void mouseDragged(MouseEvent e) {
	  //event originated from the diagram panel
	  if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) )
		switch(editorView.getActiveItem()){
	      case DRAGGING_FEATURE: EditorView.dragFeature(e); break;
	      case DRAGGING_EXTERN_ANCHOR: EditorView.dragAnchor(e); break;
	      case DRAGGING_EXTERN_GROUP: EditorView.dragAnchor(e); break;
	      default: break;
		}		
	  //event originated from the toolbar
	  else{ 	

		/* ***DEBUG*** */
		if(debug3) System.out.println("Evento Drag, isActiveItem= "+editorView.getActiveItem());
		/* ***DEBUG*** */

		switch(editorView.getActiveItem()){
		  case DRAGGING_TOOL_NEWFEATURE: EditorView.dragToolNewFeature(e); break;
		  case DRAGGING_TOOL_CONNECTOR: EditorView.dragToolConnector(e); break;
		  case DRAGGING_TOOL_ALT_GROUP: EditorView.dragToolAltGroup(e); break;
		  case DRAGGING_TOOL_OR_GROUP: EditorView.dragToolOrGroup(e); break;
		  default: break;
		}			  

	  }
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	  JComponent popupElement=null;
	  //event originated from the diagram panel
      if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) ){
		

    	/* ***DEBUG*** */
    	if(debug2) System.out.println("Button pressed: "+e.getButton()
    				+"Source e: "+e.getSource()
    				+"Source e.getName(): "+((Component)e.getSource()).getName());
    	/* ***DEBUG*** */

		editorView.getDiagramElementsMenu().removeAll();
        if (e.getButton() == MouseEvent.BUTTON3) {
          Component comp=editorView.getDiagramPanel().getComponentAt(e.getX(), e.getY());
        	
          /* ***DEBUG*** */
          if(debug3) System.out.println("rigth clicked on: "+comp.getName());
          /* ***DEBUG*** */

          if(comp.getName()==null || comp.getName()==""|| comp.getName().startsWith(EditorView.diagramPanelName)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemPrintModelDebug());
              editorView.setDiagramElementsMenuPosX(e.getX());
              editorView.setDiagramElementsMenuPosY(e.getY());
              editorView.showDiagramElementsMenu();
        	  return;
          }

          editorView.setPopUpElement((JComponent)comp);
          popupElement=editorView.getPopUpElement();
          //        	popUpElement=getUnderlyingComponent(e.getX(), e.getY());

          if(popupElement.getName().startsWith(EditorView.startConnectorsNamePrefix)
        		  || popupElement.getName().startsWith(EditorView.endConnectorsNamePrefix)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDelete());
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemUngroup());
          }
          if(popupElement.getName().startsWith(EditorView.featureNamePrefix)){
        	  editorView.getDiagramElementsMenu().add(editorView.getPopMenuItemDelete());
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
	  JComponent anchorFocused=null;
	  //event originated from the diagram panel
      if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) ){
		int featurePanelX=0, featurePanelY=0;
		int anchorPanelOnScreenX=0, anchorPanelOnScreenY=0;
		FeaturePanel featurePanel=null;
		JComponent otherEnd=null;
		JComponent otherEndFeaturePanel=null;
		JComponent anchorPanel=null;
		String anchorPanelName;
		OrderedListNode tmpNode=editorView.getVisibleOrderDraggables().getFirst();
		while(tmpNode!=null){
		  if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){
			editorView.setLastPositionX(e.getX());
			editorView.setLastPositionY(e.getY());

			//mouse pressed on a feature panel in the diagram panel
			if(tmpNode.getElement().getClass().equals(FeaturePanel.class) &&
					((FeaturePanel)tmpNode.getElement()).getName().startsWith(EditorView.featureNamePrefix) ){

			  featurePanel=(FeaturePanel)tmpNode.getElement();
			  featurePanelX=featurePanel.getX();
			  featurePanelY=featurePanel.getY();
			  anchorPanel=(JComponent)featurePanel.getComponentAt(e.getX()-featurePanelX, e.getY()-featurePanelY);
			  anchorPanelName=anchorPanel.getName();
			  //mouse pressed on an anchor inside the feature panel
			  if(anchorPanelName!=null && anchorPanel.getClass().equals(AnchorPanel.class) &&(
					  anchorPanelName.startsWith(EditorView.startConnectorsNamePrefix) ||
					  anchorPanelName.startsWith(EditorView.endConnectorsNamePrefix) ) ){

				editorView.setActiveItem(activeItems.DRAGGING_EXTERN_ANCHOR);
				editorView.setLastAnchorFocused(anchorPanel);
				editorView.setLastFeatureFocused(featurePanel);
				
				otherEnd=((AnchorPanel)anchorPanel).getOtherEnd();
				otherEndFeaturePanel=(JComponent)otherEnd.getParent();
				
				//the other end is attached to a feature
				if(otherEndFeaturePanel.getName().startsWith(EditorView.featureNamePrefix) ){
				  if(anchorPanelName.startsWith(EditorView.startConnectorsNamePrefix) )
					editorModel.removeLink(featurePanel.getName(), otherEndFeaturePanel.getName());
				  if(anchorPanelName.startsWith(EditorView.endConnectorsNamePrefix) ){
					if (otherEnd.getName().startsWith(EditorView.startConnectorsNamePrefix))
					  editorModel.removeLink(otherEndFeaturePanel.getName(), featurePanel.getName());
					if (otherEnd.getName().startsWith(EditorView.altGroupNamePrefix)
					    || otherEnd.getName().startsWith(EditorView.orGroupNamePrefix))
					  editorModel.removeFeatureFromGroup(otherEndFeaturePanel.getName(), featurePanel.getName(), otherEnd.getName());
				  }
				}
				//the other end is not attached to any feature
				else editorView.detachAnchor();
				break;
			  }
			  //mouse pressed on a group inside the feature panel
			  else if(anchorPanelName!=null && anchorPanel.getClass().equals(GroupPanel.class) && (
				anchorPanelName.startsWith(EditorView.altGroupNamePrefix) ||
				anchorPanelName.startsWith(EditorView.orGroupNamePrefix) ) ){

				editorView.setActiveItem(activeItems.DRAGGING_EXTERN_GROUP);
				editorView.setLastAnchorFocused(anchorPanel);
				editorView.setLastFeatureFocused(featurePanel);
				editorView.detachAnchor();
				break;
			  }
			  //mouse directly pressed on a feature panel, not on an inner anchor
			  editorView.setActiveItem(activeItems.DRAGGING_FEATURE);
			  editorView.setLastFeatureFocused((FeaturePanel)tmpNode.getElement());   
			  EditorView.moveComponentToTop(editorView.getLastFeatureFocused());
			}
			//mouse directly pressed on an anchor panel in the diagram panel
			else if(tmpNode.getElement().getClass().equals(AnchorPanel.class) &&(
					((AnchorPanel)tmpNode.getElement()).getName().startsWith(EditorView.startConnectorsNamePrefix) ||
					((AnchorPanel)tmpNode.getElement()).getName().startsWith(EditorView.endConnectorsNamePrefix) ) ){
			  editorView.setActiveItem(activeItems.DRAGGING_EXTERN_ANCHOR);
			  editorView.setLastAnchorFocused((AnchorPanel)tmpNode.getElement());
			  EditorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}
			//mouse directly pressed on a group panel in the diagram panel
			else if(tmpNode.getElement().getClass().equals(GroupPanel.class) &&
					//lastAnchorFocused?
					((GroupPanel)tmpNode.getElement()).getName().startsWith(EditorView.altGroupNamePrefix) ||
					((GroupPanel)tmpNode.getElement()).getName().startsWith(EditorView.orGroupNamePrefix) ){
			  editorView.setActiveItem(activeItems.DRAGGING_EXTERN_GROUP);
			  editorView.setLastAnchorFocused((GroupPanel)tmpNode.getElement());
			  EditorView.moveComponentToTop(editorView.getLastAnchorFocused());
			}

			/* ***DEBUG*** */
			if (debug2){
				System.out.println("Source dell'evento: "+e.getSource());
				OrderedListNode printTmp=editorView.getVisibleOrderDraggables().getFirst();
				System.out.println("Stampo l'ordine attuale nella lista, partendo da first.");
				while(printTmp!=null){
					System.out.println("-"+((Component)printTmp.getElement()).getName());							  
					printTmp=printTmp.getNext();
				}
			}
			/* ***DEBUG*** */

			break;
		  }

		  /* ***DEBUG*** */
		  if (debug2){
			  System.out.println("Current Panel: "+((Component)tmpNode.getElement()).getName());
		  }
		  /* ***DEBUG*** */

		  tmpNode=tmpNode.getNext();
		}

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
		else if (((JComponent)comp).getName()=="Connector Line")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_CONNECTOR);
		else if (((JComponent)comp).getName()=="Alternative Group")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_ALT_GROUP);
		else if (((JComponent)comp).getName()=="Or Group")
			editorView.setActiveItem(activeItems.DRAGGING_TOOL_OR_GROUP);

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
			  dropsAnchor(e);
			  EditorView.dropAnchorOnDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  case DRAGGING_EXTERN_GROUP:
			  EditorView.dropGroupOnDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
			  editorView.setLastAnchorFocused(null); break;
		  default: break;
	    }

	  //event originated from the toolbar
	  else 
		switch(editorView.getActiveItem()){
	      case DRAGGING_TOOL_NEWFEATURE:
	    	  addNewFeature(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_CONNECTOR:
	    	  EditorView.addConnectorToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_ALT_GROUP:
	    	  EditorView.addAltGroupToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      case DRAGGING_TOOL_OR_GROUP:
	    	  EditorView.addOrGroupToDiagram(e);
			  editorView.setActiveItem(activeItems.NO_ACTIVE_ITEM);
	    	  editorView.setToolDragImage(null); break;
	      default: break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	  //event originated from the diagram panel
	  if( ((Component)e.getSource()).getName().startsWith(EditorView.diagramPanelName) ) 

	    /* ***DEBUG *** */
	    if (debug2){
		  OrderedListNode tmpNode=editorView.getVisibleOrderDraggables().getFirst();
		  while(tmpNode!=null){
		    if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){
			  System.out.println("Sei passato su "+((Component)tmpNode.getElement()).getName()+"!");
			  return;
		    }
		    tmpNode=tmpNode.getNext();
		  }
	    }
  	    /* ***DEBUG *** */

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
	  //popup menu command: Delete Element
      if(e.getActionCommand().equals("Delete Element")){
        String elementName = null;
    	JComponent popupElement=editorView.getPopUpElement();
        if (popupElement!=null) elementName=editorView.getPopUpElement().getName();

        /* ***DEBUG*** */
        if(debug3) System.out.println("Popup Menu requested delete on "+elementName
      		  +"\ne = "+e
      		  /*+"\ncomp.getName()="+comp.getName()*/);
        /* ***DEBUG*** */
        
        if(elementName!=null && elementName.startsWith(EditorView.startConnectorsNamePrefix)){
      	  EditorView.deleteAnchor(popupElement);
      	  EditorView.deleteAnchor(((AnchorPanel)popupElement).getOtherEnd());
      	  editorView.repaintRootFrame();
        }
        if(elementName!=null && elementName.startsWith(EditorView.endConnectorsNamePrefix)){
      	  if(((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.startConnectorsNamePrefix)){
      		EditorView.deleteAnchor(popupElement);
      		EditorView.deleteAnchor(((AnchorPanel)popupElement).getOtherEnd());            		
      	  }
      	  else if( ( ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.orGroupNamePrefix)
      	   || ((AnchorPanel)popupElement).getOtherEnd().getName().startsWith(EditorView.altGroupNamePrefix) )
      	   && ((GroupPanel)((AnchorPanel)popupElement).getOtherEnd()).getMembers().size()>2 ){

      		((GroupPanel)((AnchorPanel)popupElement).getOtherEnd()).getMembers().remove(popupElement);
      		EditorView.deleteAnchor(popupElement);
      	  }	
          editorView.repaintRootFrame();
        }
        editorView.setPopUpElement(null);
      }
	  //popup menu command: Ungroup Element
      else if(e.getActionCommand().equals("Ungroup Element")){
        System.out.println("Ungroup not implemented yet! ");
      }
      else if(e.getActionCommand().equals("Print Model[DEBUG COMMAND]")){
  		System.out.println("\n\nPRINTING UNROOTED FEATURES");
    	for(Map.Entry<String,FeatureNode> feature : editorModel.getUnrootedFeatures().entrySet()){
    		System.out.println("---"+feature.getValue().getName());
    	}
      }

	}

	/**
	 * Adds a new unrooted feature to the diagram.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void addNewFeature(MouseEvent e) {
	  if (!EditorView.checkDroppedOnDiagram()) return;
	  else{

		/* ***DEBUG*** */
		if(debug) System.out.println("Mouse rilasciato(Drag relative) su: ("+e.getX()+", "+e.getY()+")."
				+"\nMouse rilasciato(Screen relative) su: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
				+"\nLocation dove verrà aggiunta la nuova Feature: "+editorView.getToolDragPosition());
		/* ***DEBUG*** */

		editorView.setLastPositionX(e.getX());
		editorView.setLastPositionY(e.getY());
		editorModel.addUnrootedFeature(EditorView.featureNamePrefix+editorView.getFeaturesCount());
	  }
	}

	/**
	 * Drops an anchor on the diagram, attaching it to the underlying component, if any.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private void dropsAnchor(MouseEvent e) {
		Component comp=EditorView.dropAnchorOnDiagram(e);
		if (comp!=null) System.out.println("comp= "+comp.getName());
		JComponent anchor=editorView.getLastAnchorFocused();
		JComponent otherEnd=((AnchorPanel)anchor).getOtherEnd();
		//anchor dropped directly on the diagram panel
		if (comp==null) return;
		//anchor dropped on a feature inside the diagram panel
		else if (comp.getName().startsWith(EditorView.featureNamePrefix)){
			
		  //the other end of the connector is not anchored to anything
		  if (otherEnd.getParent().getName().startsWith(EditorView.diagramPanelName) ){
			EditorView.addAnchorToFeature(); return;
		  }
		  if (anchor.getName().startsWith(EditorView.startConnectorsNamePrefix)
			  /*&& otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix)*/){
			//about to link 2 features by a connector
			editorModel.addMandatoryLink( comp.getName(), otherEnd.getParent().getName() );
			return;
		  }
	      if(anchor.getName().startsWith(EditorView.endConnectorsNamePrefix)){
	        if( otherEnd.getName().startsWith(EditorView.startConnectorsNamePrefix)/* &&
	        	otherEnd.getParent().getName().startsWith(EditorView.featureNamePrefix)*/){
	          //about to link 2 features by a connector
	          System.out.println("about to link 2 features by a connector");
	          editorModel.addMandatoryLink( otherEnd.getParent().getName(), comp.getName() );
	          return;
	        }
	        else if( ( otherEnd.getName().startsWith(EditorView.orGroupNamePrefix)
	        		|| otherEnd.getName().startsWith(EditorView.altGroupNamePrefix) )
	        		&& ((GroupPanel)otherEnd).getParent().getName().startsWith(EditorView.featureNamePrefix)){
		      //about to link 2 features by a group connector
	          System.out.println("about to link 2 features by a group connector");
	          editorModel.addFeatureToGroup( otherEnd.getParent().getName(), comp.getName(), 
	        		  						 otherEnd.getName());
	        }	
	      }

		}
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
		EditorView.moveComponentToTop(editorView.getLastAnchorFocused());
	}


	
}
