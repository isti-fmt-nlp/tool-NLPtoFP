/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Tir;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


import javax.swing.*;


public class NLPToLP extends JFrame implements ActionListener
{
	 /**
	 *
	 */


	public NLPToLP()
	{

		/*
		setSize(100,100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar barra = new JMenuBar();
		JMenu file= new JMenu("File");

		JMenuItem apri=new JMenuItem("Apri..");
		apri.addActionListener(this);
		file.add(apri);
		barra.add(file);
		setJMenuBar(barra);

		dir.setEditable(false);
	    filename.setEditable(false);
		*/


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
		

		AnalysisText s = new AnalysisText("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/CBTC Vendors Evaluation.pdf");
		s.LoadAnalysis();
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
		  JPanel panel=new JPanel();
		  JTextArea jt= new JTextArea("Welcome Roseindia",5,20);
		  JScrollPane pa = new JScrollPane(panel);
		  add(pa);

		  panel.add(jt);
		  setSize(250,200);
		  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().compareTo("Apri..")==0)
		{
			JFrame frame = new JFrame();

		    FileDialog d = new FileDialog(frame);
		    d.setDirectory(".");
		    d.setFilenameFilter( new geFilter() );
		    d.setVisible(true);
		    System.out.println(d.getDirectory() + d.getFile());
		}
			/*
			JFileChooser c = new JFileChooser();


			FileFilter filter1 = new ExtensionFileFilter(new String[] { "PDF", "TXT" }, "*.pdf, *.txt");
			c.setFileFilter(filter1);
		      // Demonstrate "Open" dialog:
		      int rVal = c.showOpenDialog(this);
		      if (rVal == JFileChooser.APPROVE_OPTION) {
		        filename.setText(c.getSelectedFile().getName());
		        System.out.println(filename.getText());
		        dir.setText(c.getCurrentDirectory().toString());
		      }
		      if (rVal == JFileChooser.CANCEL_OPTION) {
		        filename.setText("You pressed cancel");
		        dir.setText("");
		      }
		    }
		    */
		  }

	 class geFilter implements FilenameFilter {
	        public boolean accept(File dir, String name) {
	                return name.endsWith( ".pdf" ) || name.endsWith( ".txt" );
	        }
	}

    public static void main(String[] args) throws IOException

    {
        NLPToLP f = new NLPToLP();
        f.foo2();
        f.setVisible(true);
    }






}
