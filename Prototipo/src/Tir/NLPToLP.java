/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;


public class NLPToLP extends JFrame implements ActionListener
{
	private TreePath tp = null;
	public TreeProject T = new TreeProject();
	JTextComponent dir = null;
	JTextComponent filename = null;
	JTextField t = null;
	
	private JPanel	panel;
	private	JPopupMenu	popupMenu;
	private static boolean interrupted;
	private static boolean interrupted2;


	public NLPToLP()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		
		setSize(100,100);
		
		JButton b1 = new JButton("Prova1");
		JButton b2 = new JButton("Prova2");
		
		JPanel p1 = new JPanel();
		p1.setLayout(null); 
		p1.setBackground(Color.WHITE);
		b1.setBounds(10, 0, 60, 40);
		b2.setBounds(70, 0, 60, 40);
		p1.add(b1);
		p1.add(b2);

		JMenuBar barra = new JMenuBar();
		JMenu file= new JMenu("File");

		JMenuItem apri=new JMenuItem("New Project..");
		JMenuItem load=new JMenuItem("Load Project..");
		JMenuItem save=new JMenuItem("Save Project..");
		JMenuItem run=new JMenuItem("run..");
		apri.addActionListener(this);
		load.addActionListener(this);
		save.addActionListener(this);
		run.addActionListener(this);
		file.add(apri);
		file.add(load);
		file.add(save);
		file.add(run);
		barra.add(file);
		setJMenuBar(barra);

		
		/* Tasto Destro */
		
		panel = new JPanel();
		panel.setLayout( null );
		
		
		
		
		// Cosa vogliamo
		JMenuItem Rename = new JMenuItem("Rename Project");
		JMenuItem Carica = new JMenuItem( "  Add File", new ImageIcon("/Users/danielecicciarella/Desktop/pdf_small.gif"));
		JMenuItem Analisi = new JMenuItem("  Remove File", new ImageIcon("/Users/danielecicciarella/Desktop/trash.gif"));
		
		popupMenu = new JPopupMenu( "Menu" );
		popupMenu.add( Carica );
		popupMenu.addSeparator();
		popupMenu.add( Analisi );
		popupMenu.add(Rename);
		
		b1.addActionListener(this);
		b2.addActionListener(this);
		
		p1.setBounds(0, 0, 300, 50);
		
		this.add(p1);
		
		// Ascolto
			enableEvents( AWTEvent.MOUSE_EVENT_MASK );
			Carica.addActionListener( this );
			Analisi.addActionListener( this );
			Rename.addActionListener(this);
			
			


    }
	

	public void foo2() throws IOException
	{
     /*
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditorKit(new HTMLEditorKit());

        String filename = "/Users/danielecicciarella/Desktop/CBTC Vendors Evaluation3.html";

        FileReader reader = new FileReader(filename);
        editorPane.read(reader, filename);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);

        setSize(300, 150);
*/
  /*
		JLabel area = new JLabel(s.AHtml.get(3));
        setDefaultLookAndFeelDecorated(true);
        System.out.println(s.AHtml.get(0).length());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        area.setBounds(this.getX(), this.getY(), 700, 700);
        add(area);
		*/
		
		
	}

	public void foo()
	{
		/*
		  JPanel panel=new JPanel();
		  JTextArea jt= new JTextArea("Welcome Roseindia",5,20);
		  JScrollPane pa = new JScrollPane(panel);
		  add(pa);

		  panel.add(jt);
		  setSize(250,200);
		  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
*/
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		/* Nuovo Progetto */
		if(e.getActionCommand().compareTo("New Project..")==0)
		{ 
			T.CreateTree();
			T.Tree.setBounds(0, 50, 250, 500);			
			T.Tree.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me) 
			    {	
					doMouseClicked(me);
			        
			        if(me.getButton() == 3)
					{
						popupMenu.show( me.getComponent(),me.getX(), me.getY() );
						
						me.consume();
					}
			    }
			});	
			
			/*
			T.Tree.addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent evt) 
				{
					if(evt.getKeyCode() == KeyEvent.VK_ENTER)
				 	{

							System.out.println("siiii\n");

				 	}
				}
			});*/
			this.add(T.Tree);
		}
		else if(e.getActionCommand().compareTo("Save Project..")==0)
		{
			JFrame frame = new JFrame("Add File");

		    FileDialog d = new FileDialog(frame);
		    
		    d.setMode(FileDialog.SAVE);
		    d.setDirectory(".");
		    d.setVisible(true);

			T.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
			T.SaveTree();
		}
		else if(e.getActionCommand().compareTo("run..")==0)
		{
			
		}
		else if(e.getActionCommand().compareTo("Load Project..")==0)
		{
			JFrame frame = new JFrame("Add File");
			
			if(T.Tree != null)
			{
				JFrame f = new JFrame();
				
				Object[] options = {"No",
	                    "Yes"};
				int i = JOptionPane.showOptionDialog(f,"Vuoi salvare?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
				System.out.println(i);
				f.setVisible(true);
				if(i == 1)
					this.T.SaveTree();
				
				f.setVisible(false);
				
			}
			
			FileDialog d = new FileDialog(frame);
		    d.setMode(FileDialog.LOAD);
		    d.setDirectory(".");
		    d.setFilenameFilter( new geFilter1() );
		    d.setVisible(true);

			T.path_xml = d.getDirectory() + d.getFile().toString();
			this.remove(T.Tree);
			T.LoadTree();
			T.Tree.setBounds(0, 50, 250, 500);			
			T.Tree.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me) 
			    {	
					doMouseClicked(me);
			        
			        if(me.getButton() == 3)
					{
						popupMenu.show( me.getComponent(),me.getX(), me.getY() );
						
						me.consume();
					}
			    }
			});
			this.add(T.Tree);
		}
		/* Aggiungiamo nuovi file pdf */
		else if(e.getActionCommand().compareTo("  Add File")==0)
		{
			JFrame frame = new JFrame("Add File");

		    FileDialog d = new FileDialog(frame);
		    d.setMode(FileDialog.LOAD);
		    d.setDirectory(".");
		    d.setFilenameFilter( new geFilter() );
		    d.setVisible(true);

			T.AddNode(d.getFile().toString(), d.getDirectory() +d.getFile().toString());
		}
		/* Rimuoviamo il file pdf selezionato */
		else if(e.getActionCommand().compareTo("  Remove File")==0)
		{
			T.DeleteNode((DefaultMutableTreeNode) tp.getLastPathComponent());
		}
		else if(e.getActionCommand().compareTo("Rename Project")==0)
		{   
			
			
			
		}
		/* Ridisegno */
		this.repaint();
	}	
	
	void TextClick(KeyEvent evt)
	{
		String s = t.getText();
		
		this.remove(t);
		
		T.ModificyNode(s);
		
		this.repaint();
	}

	class geFilter implements FilenameFilter 
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".pdf" );
	    }
	}	
	class geFilter1 implements FilenameFilter 
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".xml" );
	    }
	}	
	 /*
	 public void CreateTree()
	 {
		 DefaultMutableTreeNode p = new DefaultMutableTreeNode("Progetto0");
		 DefaultMutableTreeNode f;
		 DefaultMutableTreeNode n;
		 DefaultMutableTreeNode n1;

		 for(int i = 0; i < 4; i++)
		 {
			 f = new DefaultMutableTreeNode("file"+i+".pdf");
			 p.add(f);
		 }
		 
		 f = new DefaultMutableTreeNode("Commonalities");
		 n = new DefaultMutableTreeNode();
		 n1 =  new DefaultMutableTreeNode("prova");
		 f.add(n);
		 f.add(n1);
		 p.add(f);

		 t = new JTree(p);
		 
		 t.setLayout(null);
		 
		 t.setBounds(0, 50, 150, 500);
		 
		 this.add(t);

		 t.addMouseListener(new MouseAdapter(){
		      public void mouseClicked(MouseEvent me) {
		        doMouseClicked(me);
		        
		        if( me.getButton() == 3   )
				{
					popupMenu.show( me.getComponent(),
										me.getX(), me.getY() );
					
					me.consume();
				}
		        
		      }
		    });

	 }
	 */
	 void doMouseClicked(MouseEvent me) {
		    tp = T.Tree.getPathForLocation(me.getX(), me.getY());
		    T.Tree.setSelectionPath(tp);
		    
		    T.Tree.setEditable(true);
		    T.Tree.setRootVisible(true);
		    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)T.Tree.getCellRenderer();
		    TreeCellEditor editor = new DefaultTreeCellEditor(T.Tree, renderer);
		    T.Tree.setCellEditor(editor);
		
		    
		    if (tp != null)
		    System.out.println(tp.getLastPathComponent());
		    else
		    	System.out.println(" ");

	
		    
		  }

    public static void main(String[] args) throws IOException, InterruptedException
    {     

 
    	//AbstractButton ab = (AbstractButton) v1.getMenuComponent(0);
		
		//System.out.println(ab.getText() );
    	
    	ExtractTerm e = null;
        String [] A = { "/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Alstom.pdf",
                        "/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Ansaldo.pdf",
                        "/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Bombardier.pdf"};

        RecordPdf R[] = new RecordPdf[A.length];
        
        ThreadRecordPdf T[] = new ThreadRecordPdf[A.length];

        for(int i = 0; i < A.length; i++)
        {
            T[i] = new ThreadRecordPdf(A[i]);
            T[i].start();
        }
        
        

        for(int i = 0; i < A.length; i++)
        {
        	T[i].stop();
        	
            T[i].join();
            
            System.out.println(T[i].getState());

            R[i] = T[i].recordPdf;
        }
        
        e = new ExtractTerm(R);
        
        e.RunExtract();
    	 
        
        
    }

	
}