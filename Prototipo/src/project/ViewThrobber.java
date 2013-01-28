package project;

import java.util.Observable;

public class ViewThrobber extends Observable implements ViewThrobberI
{
	private ThreadThrobber threadThrobber = null;

	@Override
	public void startThrobber() 
	{
		threadThrobber = new ThreadThrobber();
		threadThrobber.start();
	}

	@Override
	public void stopThrobber() 
	{
		try 
		{
			threadThrobber.setRunThrobber(false);
			threadThrobber.join();
		} 
		catch (InterruptedException e) 
		{

		}
	}
}
