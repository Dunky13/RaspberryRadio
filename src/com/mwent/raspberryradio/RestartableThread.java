package com.mwent.raspberryradio;

public class RestartableThread
{
	private Thread t;
	private Runnable backup;

	public RestartableThread(Runnable r)
	{
		backup = r;
		t = new Thread(r);
	}

	public void start()
	{
		this.t.start();
	}

	public void interrupt()
	{
		this.t.interrupt();
	}

	public void restart()
	{
		t.interrupt();
		t = new Thread(backup);
		t.start();
	}

}
