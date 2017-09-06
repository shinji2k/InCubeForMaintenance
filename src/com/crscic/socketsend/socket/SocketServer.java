package com.crscic.socketsend.socket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.crscic.interfacetesttool.SendDataFactory;
import com.crscic.socketsend.utils.ByteUtils;
import com.crscic.socketsend.utils.StringUtils;

/**
 * C/S架构的服务端对象。
 */
public class SocketServer
{
	public static void main(String[] args)
	{
		int port = 7676;
		try
		{
			ServerSocket ss = new ServerSocket(port, 5);
			Socket s = ss.accept();
			System.out.println("recv:" + s.getRemoteSocketAddress());
			while(true)
			{
				InputStream is = s.getInputStream();
				if (is.available() > 0)
				{
					System.out.print("receive data: ");
					BufferedInputStream bis = new BufferedInputStream(is);
					DataInputStream dis = new DataInputStream(bis);
					int dataLen = dis.available();
					byte[] bytes = new byte[dataLen]; // 一次性读取
					dis.readFully(bytes);
					System.out.println(ByteUtils.byteArrayToString(bytes));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
//		SocketServer server = new SocketServer(port);
//		server.start();
	}

	private int port;
	private volatile boolean running = false;
	private Thread connWatchDog;
	private SendDataFactory sdf;

	public SocketServer(SendDataFactory sdf)
	{
		this.sdf = sdf;
		this.port = Integer.parseInt(sdf.getSi().getPort());
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
			if (StringUtils.isNullOrEmpty(sdf.getConfigPath()))
			{
				try
				{
					throw new Exception("没有设置配置文件路径");
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
					return;
				}
			}
			ServerSocket ss = null;
			try
			{
				ss = new ServerSocket(port, 5);
				while (running)
				{
					Socket s = ss.accept();
					sdf.setSocket(s);
					// 新起一个线程去处理收到的消息
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
