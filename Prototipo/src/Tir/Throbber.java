import java.awt.*;
import javax.swing.*;

public class Throbber extends JPanel
{
	public Throbber()
	{
		setLayout(null);
		setBounds(60, 10, 500, 500);
	}
	
	@Override
	public void paint(Graphics g)
	{
		Image im = Toolkit.getDefaultToolkit().getImage(
				"/Users/danielecicciarella/Desktop/Spinning_wheel_throbber.gif");
		
		g.drawImage(im, 200, 0, 32, 32, null);
	}
}
