import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ThreadThrobber extends Thread 
{
	private Throbber throbber = new Throbber();
	
	private boolean runThrobber = false;
	
	private JFrame jFrameThrobber = new JFrame();

	@Override 
	public void run()
	{
		JLabel jl = new JLabel("A few minutes the analysis ends...");
		
		jl.setBounds(new Rectangle(20,10,250,30));
		
		jFrameThrobber.setLayout(null);
		jFrameThrobber.setBackground(Color.WHITE);
		jFrameThrobber.setBounds(550, 375, 350, 80);
		jFrameThrobber.add(jl);
		jFrameThrobber.add(throbber);
		jFrameThrobber.setVisible(true);
		
		while(!isRunThrobber())
			jFrameThrobber.repaint();
		
		jFrameThrobber.setVisible(false);
		jFrameThrobber.removeAll();
		jFrameThrobber.setBounds(new Rectangle(0,0,0,0));
		
		JOptionPane.showMessageDialog(
						jFrameThrobber,"The analysis file is finished...","Finish Analysis", JOptionPane.WARNING_MESSAGE);
		
		jFrameThrobber.setVisible(false);
		jFrameThrobber.dispose();
	}
	
	public void setRunThrobber(boolean runThrobber)
	{
		this.runThrobber = runThrobber;
	}
	
	public boolean isRunThrobber()
	{
		return this.runThrobber;
	}
}
