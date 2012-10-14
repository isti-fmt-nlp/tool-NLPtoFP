import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.swing.*;


public class JCheckBoxCV extends JFrame implements ActionListener
{
	private ArrayList <JCheckBox> Cb = new ArrayList<JCheckBox>();
	
	private JButton b1 = new JButton("Cancel");
	private JButton b2 = new JButton("OK");
	JTextArea jta = new JTextArea();
	JPanel p = new JPanel();
	public String s =  null;
	
	private JPanel CC = new JPanel();
	
	public void CheckBoxCommonalities()
	{
		this.CC.setLayout(null);
		
		this.Cb.add(new JCheckBox("AAAAAAAAAAAAAAAAAAAA"));
		this.Cb.add(new JCheckBox("BBBBBBBBBBBBBBBBBBB"));
		this.Cb.add(new JCheckBox("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"));
		this.Cb.add(new JCheckBox("DDDDDDDDDDDDDD"));
		
		this.Cb.get(0).setSelected(false);
		this.Cb.get(1).setSelected(false);
		this.Cb.get(2).setSelected(false);
		this.Cb.get(3).setSelected(false);
		
		this.Cb.get(0).setBounds(10, 10, 600, 20);
		this.Cb.get(1).setBounds(10, 60, 600, 20);
		this.Cb.get(2).setBounds(10, 110, 600, 20);
		this.Cb.get(3).setBounds(10, 160, 600, 20);
		
		
		
		p.setLayout(null);
		jta.setBounds(new Rectangle(10,150,300,300));
		
		JScrollPane scroller = new JScrollPane(jta);
		scroller.setBounds(new Rectangle(10,150,300,300));
	
		
		
		p.setBounds(new Rectangle(10,150,500,500));
		p.add(scroller);
		
		this.CC.add(this.Cb.get(0));
		this.CC.add(this.Cb.get(1));
		this.CC.add(this.Cb.get(2));
		this.CC.add(this.Cb.get(3));
		this.CC.add(p, BorderLayout.CENTER);
		
		this.b1.setBounds(600, 300, 50, 50);
		this.b2.setBounds(600, 400, 50, 50);
		
		this.b1.addActionListener(this);
		this.b2.addActionListener(this);
		
		this.CC.add(b1);
		this.CC.add(b2);
		
		this.add(CC);
	}
	
	public void CheckBoxVariabilites()
	{
		
	}
	
	public static void main(String s[]) throws UnsupportedEncodingException
	{
		JCheckBoxCV c = new JCheckBoxCV();
		
		c.CheckBoxCommonalities();
				
		c.setVisible(true);
		
	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if(ae.getSource().equals(b1))
		{
			this.remove(p);
			s = jta.getText();
			
			modify(s);
			this.repaint();
		}
		else if(ae.getSource().equals(b2))
		{
			
		}
	}

	private String modify(String s2) {
		return s2;
		
	}
}
