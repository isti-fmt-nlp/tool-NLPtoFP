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
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.apache.pdfbox.util.ExtensionFileFilter;

public class NLPToLP extends JFrame implements ActionListener
{
	 private JTextField filename = new JTextField(), dir = new JTextField();
	
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
		
        ArrayList<String> s = new AnalysisText("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/CBTC Vendors Evaluation.pdf").Analysis();
        JLabel area = new JLabel("<html>" + s.get(1) + "</html>");
        setDefaultLookAndFeelDecorated(true);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(area);
        
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
    
    public static void main(String[] args)
    
    {
        NLPToLP f = new NLPToLP();
        f.setVisible(true);
    }
    


	

	
}
