package com.crscic.socketsend.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.crscic.socketsend.SendDataFactory;

/**
 * C/S架构的服务端对象。
 */
public class SocketServer
{
	public static void main(String[] args)
	{
		int port = 7676;
		SocketServer server = new SocketServer(port);
		server.start();
	}

	private int port;
	private volatile boolean running = false;
	private Thread connWatchDog;

	public SocketServer(int port)
	{
		this.port = port;
	}

	public void start()
	{
		if (running)
			return;
		running = true;
		connWatchDog = new Thread(new ConnWatchDog());
		connWatchDog.start();
	}

	@SuppressWarnings("deprecation")
	public void stop()
	{
		if (running)
			running = false;
		if (connWatchDog != null)
			connWatchDog.stop();
	}

	class ConnWatchDog implements Runnable
	{
		public void run()
		{
			ServerSocket ss = null;
			try
			{
				ss = new ServerSocket(port, 5);
				while (running)
				{
					Socket s = ss.accept();
					// 新起一个线程去处理收到的消息
					SendDataFactory sdf = new SendDataFactory(s);
					new Thread(sdf).start();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				stop();
			}
		}
	}
}
