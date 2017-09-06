package com.crscic.socketsend.socket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import com.crscic.socketsend.SendDataFactory;
import com.crscic.socketsend.utils.ByteUtils;

/**
 * C/S架构的客户端对象，持有该对象，可以随时向服务端发送消息。
 */
public class SocketClient
{
	private String serverIp;
	private int port;
	private Socket socket;
	private boolean running = false;
	private SendDataFactory sdf;


	public SocketClient(SendDataFactory sdf)
	{
		this.sdf = sdf;
		this.serverIp = sdf.getSi().getIp();
		this.port = Integer.parseInt(sdf.getSi().getPort());
	}

	public void start() throws UnknownHostException, IOException
	{
		if (running)
			return;
		socket = new Socket(serverIp, port);
		System.out.println("本地端口：" + socket.getLocalPort());
		running = true;
		new Thread(sdf).start(); // 监听返回信息
	}

	public void stop()
	{
		if (running)
			running = false;
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void sendString(String str) throws IOException
	{
		OutputStream os = socket.getOutputStream();
		System.out.println("发送：\t" + str);
		os.write(str.getBytes());
		os.flush();
	}

	public void sendObject(Object obj) throws IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(obj);
		System.out.println("发送：\t" + obj);
		oos.flush();
	}

	class ReceiveWatchDog implements Runnable
	{
		public void run()
		{
			while (running)
			{
				try
				{
					InputStream in = socket.getInputStream();
					if (in.available() > 0)
					{
						BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
						DataInputStream dis = new DataInputStream(bis);
						int dataLen = dis.available();
						byte[] bytes = new byte[dataLen]; // 一次性读取
						dis.readFully(bytes);
						// System.out.println("get in hex : " +
						// ByteUtils.byteArraytoHexString(bytes));
						System.out.println("get in asc : " + ByteUtils.byteArrayToString(bytes));
					}
					else
					{
						Thread.sleep(10);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					SocketClient.this.stop();
				}
			}
		}
	}

}