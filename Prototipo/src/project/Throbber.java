package project;

import java.awt.*;
import javax.swing.*;

public class Throbber extends JPanel
{
	public Image imageThrobber = null;
	
	public Throbber()
	{
		setLayout(null);
		setBounds(60, 10, 500, 500);
	}
	
	@Override
	public void paint(Graphics g)
	{
		imageThrobber = Toolkit.getDefaultToolkit().getImage(
				"/Users/danielecicciarella/Desktop/Spinning_wheel_throbber.gif");	
		g.drawImage(imageThrobber, 200, 0, 32, 32, null);
	}
}
