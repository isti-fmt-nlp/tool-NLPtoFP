import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

public final class Tool extends JFrame implements ActionListener, WindowListener
{
	/* JTree del tool */
	private TreeProject Tp = new TreeProject();
	
	/* TreePath utilizzato per catturare il nodo selezionato */
	private TreePath tpath = null;
	
	/* Path del file.txt contenente l'ultimo progetto aperto */
	private final String recent_txt = "/Users/danielecicciarella/Desktop/Recent.txt";
	
	/* JMenu utilizzato per visionare il file pdf selezionato */
	private JMenu v1 = null;
	
	/* JButton implementati nel tool */
	private JButton b1, b2, b3, b4, b5, b6;

	/* JPanel dove sarà inserito l'albero progetto */
	private JPanel jpan = new JPanel();
	
	private JPanel jpan1 = null;
	
	/*  */
	private TabPageHtml tph = null;
	
	/* */
	private ThreadHandler th = null;
	
	/* JPopupMenu rappresenta menù tasto destro */
	private	JPopupMenu jpop = new JPopupMenu("Menù");

	public Tool()
	{
		/* Settiamo chiusura frame */
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/* Settiamo dimensione frame */
		this.setSize(new Dimension(950,600));
		/* Settiamo disposizione oggetti a null*/
		this.setLayout(null);
		
		/* Creiamo barra dei menu */
		JMenuBar bar = new JMenuBar();
		
		/* Creiamo componenti della barra */
		JMenu bar1 = new JMenu("File");
		JMenu bar2 = new JMenu("View");
		
		/* Componenti menu file */
		JMenuItem f1 = new JMenuItem("New Project", new ImageIcon("/Users/danielecicciarella/Desktop/Immagini JMenuBar/IconNew.gif"));
		JMenuItem f2 = new JMenuItem("Open..", new ImageIcon("/Users/danielecicciarella/Desktop/Immagini JMenuBar/IconOpen.gif"));
		JMenuItem f3 = new JMenuItem("Open Recent");
		JMenuItem f4 = new JMenuItem("Save", new ImageIcon("/Users/danielecicciarella/Desktop/Immagini JMenuBar/IconSave.gif"));
		JMenuItem f5 = new JMenuItem("Save As..");
		JMenuItem f6 = new JMenuItem("Close");
		JMenuItem f7 = new JMenuItem("Exit");	
		
		/* Aggiungiamo ascoltatori */
		f1.addActionListener(this);
		f2.addActionListener(this);
		f3.addActionListener(this);
		f4.addActionListener(this);
		f5.addActionListener(this);
		f6.addActionListener(this);
		f7.addActionListener(this);	
		
		/* Aggiungiamo i componenti al menu file */
		bar1.add(f1);
		bar1.add(f2);
		bar1.add(f3);
		bar1.addSeparator();
		bar1.add(f4);
		bar1.add(f5);
		bar1.add(f6);
		bar1.addSeparator();
		bar1.add(f7);
		
		/* Componenti menu view */
		v1 = new JMenu("File ");
		
		JMenuItem v2 = new JMenuItem("Commonalities");
		JMenuItem v3 = new JMenuItem("Commonalities Select");
		JMenuItem v4 = new JMenuItem("Variabilities");
		JMenuItem v5 = new JMenuItem("Variabilities Select");
		
		/* Aggiungiamo ascoltatori */
		v1.addActionListener(this);
		v2.addActionListener(this);
		v3.addActionListener(this);
		v4.addActionListener(this);
		v5.addActionListener(this);
		
		/* Aggiungiamo i componenti al menu view */		
		bar2.add(v1);
		bar2.addSeparator();
		bar2.add(v2);
		bar2.add(v3);
		bar2.addSeparator();
		bar2.add(v4);
		bar2.add(v5);
		
		/* Aggiungiamo i menu a "bar" */
		bar.add(bar1);
		bar.add(bar2);
		
		this.setJMenuBar(bar);
		
		/** Fine JMenuBar **/
		
		/* Aggiungiamo i bottoni al tool */
		b1 = new JButton(new ImageIcon("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Immagini Bottoni/New.png"));
		b2 = new JButton(new ImageIcon("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Immagini Bottoni/Open.png"));
		b3 = new JButton(new ImageIcon("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Immagini Bottoni/Save.png"));
		b4 = new JButton(new ImageIcon("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Immagini Bottoni/Run.png"));
		b5 = new JButton(new ImageIcon("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Immagini Bottoni/View.png"));
		b6 = new JButton(new ImageIcon("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/Immagini Bottoni/Exit.png"));	
		
		/* Settiamo la loro dimensione */
		b1.setBounds(new Rectangle(20,10,48,48));
		b2.setBounds(new Rectangle(120,10,48,48));
		b3.setBounds(new Rectangle(220,10,48,48));
		b4.setBounds(new Rectangle(320,10,48,48));
		b5.setBounds(new Rectangle(420,10,48,48));
		b6.setBounds(new Rectangle(520,10,48,48));
		
		b1.setToolTipText("New Project");
		b2.setToolTipText("Open");
		b3.setToolTipText("Save");
		b4.setToolTipText("Run");
		b5.setToolTipText("Show");
		b6.setToolTipText("Exit");
		
		b1.setHorizontalTextPosition(SwingConstants.CENTER);
		b2.setHorizontalTextPosition(SwingConstants.CENTER);
		b3.setHorizontalTextPosition(SwingConstants.CENTER);
		b4.setHorizontalTextPosition(SwingConstants.CENTER);
		b5.setHorizontalTextPosition(SwingConstants.CENTER);
		b6.setHorizontalTextPosition(SwingConstants.CENTER);
		
		/* Aggiungiamo ascoltatori */
		b1.addActionListener(this);
		b2.addActionListener(this);
		b3.addActionListener(this);
		b4.addActionListener(this);
		b5.addActionListener(this);
		b6.addActionListener(this);
		
		/* Aggiungiamo bottoni al frame */
		this.add(b1);
		this.add(b2);
		this.add(b3);
		this.add(b4);
		this.add(b5);
		this.add(b6);	
		
		this.setLocationRelativeTo(null);
		
		/** Fine JButton**/
		this.jpan.setBounds(new Rectangle(18,70,300,450));
		this.jpan.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
							BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));	
		this.jpan.setLayout(null);
		this.add(jpan);
		
		/** Fine JPanel per JTree **/
		
		JMenuItem jmenu1 = new JMenuItem("View..");
		JMenuItem jmenu2 = new JMenuItem("Add File", new ImageIcon("/Users/danielecicciarella/Desktop/Immagini Popup/IconPdf.gif"));
		JMenuItem jmenu3 = new JMenuItem("Remove..");
		JMenuItem jmenu4 = new JMenuItem("Run..", new ImageIcon("/Users/danielecicciarella/Desktop/Immagini Popup/IconRun.gif"));
		
		jmenu1.addActionListener(this);
		jmenu2.addActionListener(this);
		jmenu3.addActionListener(this);
		jmenu4.addActionListener(this);
		
		this.jpop.add(jmenu1);
		this.jpop.addSeparator();
		this.jpop.add(jmenu2);
		this.jpop.add(jmenu3);
		this.jpop.addSeparator();
		this.jpop.add(jmenu4);
		this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		
		/** Fine JPopMenu **/
		
		this.addWindowListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		/* Azioni MenuBar e JButton */
		if(ae.getActionCommand() == "New Project" || ae.getSource().equals(b1))
		{
			if(this.Tp.Tree == null)
			{
				this.Tp.CreateTree();
				this.Tp.Tree.setBounds(5,5,290,440);
				this.Tp.Tree.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent me) 
				    {	
						ClickTree(me);
						
				        if(me.getButton() == 3)
						{
							jpop.show(me.getComponent(),me.getX(), me.getY());
							
							me.consume();
						}
				    }
				});
				this.jpan.add(this.Tp.Tree);
			}
			else 
			{
				if(this.Tp.proj_new)
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i = 
							JOptionPane.showOptionDialog(
									f,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);
					
					if(i == 1)
					{
						f.setVisible(false);
						
						JFrame frame = new JFrame("Save New Project");

					    FileDialog d = new FileDialog(frame);
					    
					    this.SaveWithDirectory(d);

					    if(d.getFile() != null)
					    {
					    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
							this.Tp.SaveTree();
							this.jpan.remove(Tp.Tree);
							this.Tp.Tree = null;
							this.Tp.CreateTree();
							this.Tp.Tree.setBounds(5,5,290,440);
							this.Tp.Tree.addMouseListener(new MouseAdapter()
							{
								public void mouseClicked(MouseEvent me) 
							    {	
									ClickTree(me);
									
							        if(me.getButton() == 3)
									{
										jpop.show( me.getComponent(),me.getX(), me.getY() );
										
										me.consume();
									}
							    }
							});
							this.jpan.add(Tp.Tree);
					    }
					}
					else
					{
						f.setVisible(false);
						
						this.jpan.remove(Tp.Tree);
						this.Tp.Tree = null;
						this.Tp.CreateTree();
						this.Tp.Tree.setBounds(5,5,290,440);
						this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
						this.jpan.add(Tp.Tree);
						this.jpan.repaint();
					}
					
				}
				else if(this.Tp.modify)
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i =
							JOptionPane.showOptionDialog(
									f,"Do you want to save?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);
					
					if(i == 1)
					{
						f.setVisible(false);
						
						this.Tp.SaveTree();	
						this.jpan.remove(Tp.Tree);
						this.Tp.Tree = null;
						this.Tp.CreateTree();
						this.Tp.Tree.setBounds(5,5,290,440);
						this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
						this.jpan.add(Tp.Tree);
					}
					else 
					{
						f.setVisible(false);
						
						this.jpan.remove(Tp.Tree);
						this.Tp.Tree = null;
						this.Tp.CreateTree();
						this.Tp.Tree.setBounds(5,5,290,440);
						this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
						this.jpan.add(Tp.Tree);
					}
				}	
				else /* Progetto non nuovo e non modificato */
				{
					this.jpan.remove(Tp.Tree);
					this.Tp.Tree = null;
					this.Tp.CreateTree();
					this.Tp.Tree.setBounds(5,5,290,440);
					this.Tp.Tree.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent me) 
					    {	
							ClickTree(me);
							
					        if(me.getButton() == 3)
							{
								jpop.show( me.getComponent(),me.getX(), me.getY() );
								
								me.consume();
							}
					    }
					});
					this.jpan.add(Tp.Tree);
				}
			}
		}
		else if(ae.getActionCommand() == "Open.." || ae.getSource().equals(b2))
		{
			if(this.Tp.Tree == null)
			{
				JFrame f = new JFrame("Open");
				
				FileDialog d = new FileDialog(f);
				
			    this.LoadWithDirectory(d);
			    
			    if(d.getFile() != null)
			    {
			    	this.Tp.path_xml = d.getDirectory() + d.getFile();
			    	this.Tp.LoadTree();
			    	this.Tp.Tree.setBounds(5,5,290,440);
			    	this.Tp.Tree.addMouseListener(new MouseAdapter()
					{
						public void mouseClicked(MouseEvent me) 
					    {	
							ClickTree(me);
							
					        if(me.getButton() == 3)
							{
								jpop.show( me.getComponent(),me.getX(), me.getY() );
								
								me.consume();
							}
					    }
					});
			    	this.jpan.add(this.Tp.Tree);
			    	
			    	for(int i=0; i < this.Tp.father.getChildAt(0).getChildCount(); i++)
			    		this.v1.add(new JMenuItem(this.Tp.father.getChildAt(0).getChildAt(i).toString()));
			    }
			}
			else
			{
				if(this.Tp.proj_new)
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i =
							JOptionPane.showOptionDialog(
									f,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);
					f.setVisible(false);
					
					if(i == 1)
					{
						JFrame frame = new JFrame("Save New Project");

					    FileDialog d = new FileDialog(frame);
					    
					    this.SaveWithDirectory(d);

					    if(d.getFile() != null)
					    {
					    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
							this.Tp.SaveTree();
					    }
					}

					JFrame fr = new JFrame("Open");
					
					FileDialog d = new FileDialog(fr);
					
				    this.LoadWithDirectory(d);
				    
				    if(d.getFile() != null)
				    {
				    	this.Tp.path_xml = d.getDirectory() + d.getFile();
				    	this.jpan.remove(this.Tp.Tree);
				    	this.Tp.Tree = null;
				    	this.Tp.LoadTree();
				    	this.Tp.Tree.setBounds(5,5,290,440);
				    	this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
				    	this.jpan.add(this.Tp.Tree);
				    	
				    	for(i=0; i < this.Tp.father.getChildAt(0).getChildCount(); i++)
				    		this.v1.add(new JMenuItem(this.Tp.father.getChildAt(0).getChildAt(i).toString()));
				    }					
				}
				else if(this.Tp.modify) 
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i =
							JOptionPane.showOptionDialog(
									f,"Do you want to save?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);
					f.setVisible(false);
					
					if(i == 1)
						this.Tp.SaveTree();
					
					JFrame fr = new JFrame("Open");
					
					FileDialog d = new FileDialog(fr);
					
				    this.LoadWithDirectory(d);
				    
				    if(d.getFile() != null)
				    {
				    	this.Tp.path_xml = d.getDirectory() + d.getFile();
				    	this.jpan.remove(this.Tp.Tree);
				    	this.Tp.Tree = null;
				    	this.Tp.LoadTree();
				    	this.Tp.Tree.setBounds(5,5,290,440);
				    	this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
				    	this.jpan.add(this.Tp.Tree);
				    	
				    	for(i=0; i < this.Tp.father.getChildAt(0).getChildCount(); i++)
				    		this.v1.add(new JMenuItem(this.Tp.father.getChildAt(0).getChildAt(i).toString()));
				    }
				}
				else 
				{
					JFrame fr = new JFrame("Open");
					
					FileDialog d = new FileDialog(fr);
					
				    this.LoadWithDirectory(d);
				    
				    if(d.getFile() != null)
				    {
				    	this.Tp.path_xml = d.getDirectory() + d.getFile();
				    	this.jpan.remove(this.Tp.Tree);
				    	this.Tp.Tree = null;
				    	this.Tp.LoadTree();
				    	this.Tp.Tree.setBounds(5,5,290,440);
				    	this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
				    	this.jpan.add(this.Tp.Tree);
				    	
				    	for(int i=0; i < this.Tp.father.getChildAt(0).getChildCount(); i++)
				    		this.v1.add(new JMenuItem(this.Tp.father.getChildAt(0).getChildAt(i).toString()));
				    }
				}
			}
		}
		else if(ae.getActionCommand() == "Open Recent")
		{
			File f = new File(this.recent_txt);
			
			if(f.exists())
			{
				/*
	               Apro in scrittura il file.txt scrivendoci
	               il contenuto di "text_UTF_8"
	            */
	            try 
	            {
	            	String tmp, s = "";
	            	
	            	BufferedReader reader =
	                        new BufferedReader(
	                             new FileReader(this.recent_txt));
	            	
	            	
	            	while((tmp = reader.readLine()) != null )
	            		s = s + tmp;

	            	reader.close();
	           	
	            	if(s != null)
	            	{
		            	if(this.Tp.Tree != null)
		            	{
		            		if(this.Tp.proj_new)
		            		{
		            			Object[] options = {"No","Yes"};
		    					
		    					JFrame jf = new JFrame();
		    			
		    					int i =
		    							JOptionPane.showOptionDialog(
		    									jf,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		    					
		    					jf.setVisible(true);
		    					jf.setVisible(false);
		    					
		    					if(i == 1)
		    					{
		    						JFrame frame = new JFrame("Save New Project");
		    						
		    					    FileDialog d = new FileDialog(frame);
		    					    
		    					    this.SaveWithDirectory(d);
		    					    
		    					    if(d.getFile() != null)
		    					    {
		    					    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
		    							this.Tp.SaveTree();
		    					    }	
		    					}
		    					this.jpan.remove(Tp.Tree);
		    					this.Tp.Tree = null;
		            		}
		            		
		            		else if(this.Tp.modify)
		            		{
		            			Object[] options = {"No","Yes"};
		    					
		    					JFrame ff = new JFrame();
		    			
		    					int i =
		    							JOptionPane.showOptionDialog(
		    									ff,"Do you want to save?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		    					
		    					ff.setVisible(true);
		    					ff.setVisible(false);
		    					
		    					if(i == 1)
		    						this.Tp.SaveTree();
		    					
		    					this.jpan.remove(Tp.Tree);
		    					this.Tp.Tree = null;
		            		}
		            		
		            		else if(!this.Tp.proj_new && !this.Tp.modify)
		            		{
		            			this.jpan.remove(Tp.Tree);
		    					this.Tp.Tree = null;
		            		}
		            	}
		            	this.Tp.path_xml = s;
		            	this.Tp.LoadTree();
		            	this.Tp.Tree.setBounds(5,5,290,440);
				    	this.Tp.Tree.addMouseListener(new MouseAdapter()
						{
							public void mouseClicked(MouseEvent me) 
						    {	
								ClickTree(me);
								
						        if(me.getButton() == 3)
								{
									jpop.show( me.getComponent(),me.getX(), me.getY() );
									
									me.consume();
								}
						    }
						});
		            	this.jpan.add(this.Tp.Tree);
		            	
		            	for(int i=0; i < this.Tp.father.getChildAt(0).getChildCount(); i++)
		            	{
		            		JMenuItem jmi = new JMenuItem(this.Tp.father.getChildAt(0).getChildAt(i).toString());
		            		jmi.addActionListener(this);
				    		this.v1.add(jmi);
		            	}
	            	}
				} 
	            catch (UnsupportedEncodingException e) 
				{

				} 
	            catch (FileNotFoundException e)
				{
					
				} 
	            catch (IOException e) 
				{

				}
			}
		}
		else if(ae.getActionCommand() == "Save" || ae.getSource().equals(b3))
		{
			if(this.Tp.Tree != null)
			{
				if(this.Tp.proj_new)
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i =
							JOptionPane.showOptionDialog(
									f,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);
					f.setVisible(false);
					
					if(i == 1)
					{
						JFrame frame = new JFrame("Save New Project");
						
					    FileDialog d = new FileDialog(frame);
					    
					    this.SaveWithDirectory(d);
					    
					    if(d.getFile() != null)
					    {
					    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
							this.Tp.SaveTree();
							this.Tp.proj_new = false;
					    }	
					}
				}
				else if(this.Tp.modify)
				{
					this.Tp.SaveTree();
					this.Tp.modify = false;
				}
			}
		}
		else if(ae.getActionCommand() == "Save As..")
		{
			if(this.Tp.Tree != null)
			{
				int i = 1;
				
				if(i == 1)
				{
					JFrame frame = new JFrame("Save New Project");
					
				    FileDialog d = new FileDialog(frame);
				    
				    this.SaveWithDirectory(d);
				    
				    if(d.getFile() != null)
				    {
				    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
						this.Tp.SaveTree();
						this.Tp.proj_new = false;
				    }	
				}
			}
		}
		else if(ae.getActionCommand() == "Close")
		{
			if(this.Tp.proj_new)
			{
				Object[] options = {"No","Yes"};
				
				JFrame f = new JFrame();
		
				int i =
						JOptionPane.showOptionDialog(
								f,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
				
				f.setVisible(true);
				f.setVisible(false);
				
				if(i == 1)
				{
					JFrame frame = new JFrame("Save New Project");

				    FileDialog d = new FileDialog(frame);
				    
				    this.SaveWithDirectory(d);
				    
				    if(d.getFile() != null)
				    {
				    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
						this.Tp.SaveTree();
						this.RefreshRecentTxt();
				    }
				}
				this.jpan.remove(this.Tp.Tree);
				this.Tp.Tree = null;
			}
			else if(this.Tp.modify)
			{
				Object[] options = {"No","Yes"};
				
				JFrame f = new JFrame();
		
				int i =
						JOptionPane.showOptionDialog(
								f,"Do you want to save?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
				
				f.setVisible(true);
				f.setVisible(false);
				
				if(i == 1)
					this.Tp.SaveTree();
				
				this.RefreshRecentTxt();
				this.jpan.remove(this.Tp.Tree);
				this.Tp.Tree = null;			
			}
			else if(!this.Tp.proj_new && !this.Tp.modify)
			{
				this.RefreshRecentTxt();
				this.jpan.remove(this.Tp.Tree);
				this.Tp.Tree = null;
			}
			
		}
		else if(ae.getActionCommand() == "Exit" || ae.getSource().equals(6))
		{
			if(this.Tp.Tree != null)
			{
				if(this.Tp.proj_new)
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i = 
							JOptionPane.showOptionDialog(
									f,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);				
					f.setVisible(false);
					
					if(i == 1)
					{
						JFrame frame = new JFrame("Save New Project");
		
					    FileDialog d = new FileDialog(frame);
					    
					    this.SaveWithDirectory(d);
					    
					    if(d.getFile() != null)
					    {
					    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
							this.Tp.SaveTree();	
							this.RefreshRecentTxt();
					    }				
					}
				}
				else if(this.Tp.modify)
				{
					Object[] options = {"No","Yes"};
					
					JFrame f = new JFrame();
			
					int i =
							JOptionPane.showOptionDialog(
									f,"Do you want to save?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
					
					f.setVisible(true);
					f.setVisible(false);
					
					if(i == 1)
						this.Tp.SaveTree();
					
					this.RefreshRecentTxt();
				}
				else if(!this.Tp.proj_new && !this.Tp.modify)
				{
					this.RefreshRecentTxt();
				}
			}
			this.dispose();
			
			System.exit(0);
		}
		
		/* Azione JButton non uguali alle azioni di JMenuBar */
		if(ae.getSource().equals(b5))
		{
			
		}
		/* Azione JMenuPopup */
		
		if(this.th != null)
		{
			for(int i = 0; i < this.th.Trp.length; i++)
			{
				if(this.Tp.run_record == true)
				{
					AbstractButton ab = (AbstractButton) v1.getMenuComponent(i);
							
					if(ae.getActionCommand() == ab.getText())
					{
						if(this.tph != null)
						{
							this.remove(tph);
							this.tph = null;
						}
						this.tph = new TabPageHtml(this.th.Trp[i].recordPdf);
						
						this.tph.setBounds(new Rectangle(320,70,630,450));
						this.add(tph);
					}
				}
			}
		}
		
		if(ae.getActionCommand() == "View.." )
		{
			if(this.Tp.run_record == true)
			{
				if(tpath.getLastPathComponent().toString().equals(this.Tp.father.getChildAt(1).getChildAt(0).toString()))
				{
					this.jpan1 = new JPanel();
					
					jpan1.setLayout(null);
					jpan1.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
								BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
					jpan1.setBounds(320,70,630,450);
					this.th.jTextCommonalities.setBounds(5, 5, 620, 440);
					jpan1.add(this.th.jTextCommonalities);
					this.add(jpan1);
				}
				else
				{
					for(int i = 0; i < this.th.Trp.length; i++)
					{
						File f = new File(this.th.Trp[i].recordPdf.getPathPdf());
						
						if(f.getName().equals(tpath.getLastPathComponent().toString()))
						{
							//Controllare se padre è input da father
							
							if(this.tph != null)
							{
								this.remove(tph);
								this.tph = null;
							}
							this.tph = new TabPageHtml(this.th.Trp[i].recordPdf);
							
							this.tph.setBounds(new Rectangle(320,70,630,450));
							this.add(tph);
						}
					}
				}
			}
		}	
		else if(ae.getActionCommand() == "Add File")
		{
			JFrame frame = new JFrame("Add File");

		    FileDialog d = new FileDialog(frame);
		    
		    d.setMode(FileDialog.LOAD);
		    d.setDirectory(".");
		    d.setFilenameFilter(new GetFilterPdf());
		    d.setVisible(true);
		    
		    if(d.getFile() != null)
		    {
		    	this.Tp.run_record = false;
		    	this.Tp.AddNode(d.getFile().toString(), d.getDirectory() +d.getFile().toString());
		    	this.v1.add(
		    			new JMenuItem(
		    					this.Tp.father.getChildAt(0).getChildAt(this.Tp.father.getChildAt(0).getChildCount()-1).toString()));
		    }
			
		}
		else if(ae.getActionCommand() == "Remove..")
		{
			for(int i = 0; i < this.Tp.Input_file.size(); i++)
			{
				AbstractButton ab = (AbstractButton) v1.getMenuComponent(i);
				
				if(ab.getText() == tpath.getLastPathComponent().toString())
					this.v1.remove(i);
			}			
			this.Tp.DeleteNode((DefaultMutableTreeNode) tpath.getLastPathComponent());
		}		
		else if(ae.getActionCommand() == "Run.." || ae.getSource().equals(b4))
		{		
			if(!this.Tp.run_record && this.Tp.Input_file != null && this.Tp.Input_file.size() >= 1)
			{
				Object[] options = {"No","Yes"};
				
				JFrame f = new JFrame();
		
				int j = 
						JOptionPane.showOptionDialog(
								f,"Do you want to begin the analysis?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
				
				f.setVisible(true);				
				f.setVisible(false);
				
				if(j == 1)
				{
					if(th != null)
					{
						try {
							this.th.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.th = null;
					}
					th = new ThreadHandler(this.Tp.Input_file);
					
					th.start();

					this.Tp.run_record = true;
				}
			}
		}
		this.repaint();		
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) 
	{}

	@Override
	public void windowClosed(WindowEvent arg0) 
	{}

	@Override
	public void windowClosing(WindowEvent arg0) 
	{
		if(this.Tp.Tree != null)
		{
			if(this.Tp.proj_new)
			{
				Object[] options = {"No","Yes"};
				
				JFrame f = new JFrame();
		
				int i = 
						JOptionPane.showOptionDialog(
								f,"Do you want to save the new project?","Save New Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
				
				f.setVisible(true);			
				f.setVisible(false);
				
				if(i == 1)
				{
					JFrame frame = new JFrame("Save File");
	
				    FileDialog d = new FileDialog(frame);
				    
				    this.SaveWithDirectory(d);
				    
				    if(d.getFile() != null)
				    {
				    	this.Tp.path_xml = d.getDirectory() + d.getFile().toString() + ".xml";
						this.Tp.SaveTree();
						this.RefreshRecentTxt();
				    }				
				}
			}
			else if(this.Tp.modify)
			{
				Object[] options = {"No","Yes"};
				
				JFrame f = new JFrame();
		
				int i = 
						JOptionPane.showOptionDialog(
								f,"Do you want to save?","Save", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
				
				f.setVisible(true);
				
				f.setVisible(false);
				
				if(i == 1)
					this.Tp.SaveTree();
				
				this.RefreshRecentTxt();
			}
			else if(!this.Tp.proj_new && !this.Tp.modify)
			{
				this.RefreshRecentTxt();
			}
		}
		this.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0){}

	@Override
	public void windowDeiconified(WindowEvent arg0) 
	{}

	@Override
	public void windowIconified(WindowEvent arg0) 
	{}

	@Override
	public void windowOpened(WindowEvent arg0) 
	{}
	
	private void ClickTree(MouseEvent me)
	{	
		this.tpath = this.Tp.Tree.getPathForLocation(me.getX(), me.getY());
	    
		this.Tp.Tree.setSelectionPath(this.tpath);
	    this.Tp.Tree.setEditable(true);
	    this.Tp.Tree.setRootVisible(true);
	    	
	    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)Tp.Tree.getCellRenderer();
	    
	    TreeCellEditor editor = new DefaultTreeCellEditor(Tp.Tree, renderer);
		    
		this.Tp.Tree.setCellEditor(editor);
	}
	
	private void SaveWithDirectory(FileDialog d)
	{
		d.setMode(FileDialog.SAVE);
	    d.setDirectory(".");
	    d.setVisible(true);
	}
	
	private void LoadWithDirectory(FileDialog d)
	{
		d.setMode(FileDialog.LOAD);
	    d.setFilenameFilter(new GetFilterXml());
	    d.setDirectory(".");
	    d.setVisible(true);
	}
	
	private void RefreshRecentTxt()
	{
		PrintStream fr = null;

        try
        {
            /*
               Apro in scrittura il 
               file "recent.txt"
            */
            fr =
                new PrintStream(
                    new FileOutputStream(this.recent_txt),false,"UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception Convert [0]: " + ex.getMessage());
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Exception Convert [1]: " + ex.getMessage());
        }

        fr.print(this.Tp.path_xml);
        fr.close();
	}
	
	class GetFilterPdf implements FilenameFilter 
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".pdf" );
	    }
	}	
	class GetFilterXml implements FilenameFilter 
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".xml" );
	    }
	}	

	public static void main(String[] args) 
	{
		Tool t = new Tool();
		
		t.setVisible(true);
	}
}
