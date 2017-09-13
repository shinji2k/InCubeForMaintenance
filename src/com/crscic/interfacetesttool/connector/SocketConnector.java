/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.crscic.interfacetesttool.config.SocketConfig;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.log.Log;

/**
 * 
 * @author zhaokai 2017年9月7日 下午3:12:32
 */
public class SocketConnector implements Connector
{
	private String type;
	private String ip;
	private int port;
	private boolean keepAlive;

	private Socket connector;
	private ServerSocket server;

	public SocketConnector(SocketConfig sockCfg)
	{
		this.type = sockCfg.getType();
		this.ip = sockCfg.getIp();
		this.port = Integer.parseInt(sockCfg.getPort());
		this.keepAlive = sockCfg.getKeepAlive();

		String logInfo;
		if (this.type.equals("server"))
			logInfo = "监听端口：" + this.port + ", 连接方式：" + (this.keepAlive ? "长连接" : "短连接");
		else
			logInfo = "服务器IP：" + this.ip + ", 服务器端口：" + this.port + ", 连接方式：" + (this.keepAlive ? "长连接" : "短连接");

		Log.info("接口类型为Socket-" + this.type + ", " + logInfo);
	}

	@Override
	public void send(byte[] data)
	{
		if (connector == null)
		{

		}
		else
		{
			OutputStream os = null;
			try
			{
				os = connector.getOutputStream();
				os.write(data, 0, data.length);
				os.flush();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				// TODO: 增加keepAlive的判断
				if (os != null && !keepAlive)
				{
					try
					{
						os.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void sendString(String str) throws IOException
	{
		OutputStream os = connector.getOutputStream();
		System.out.println("发送：" + str);
		os.write(str.getBytes());
		os.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crscic.interfacetesttool.connector.Connector#startReply()
	 */
	@Override
	public void startReply()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * 连接Socket实现
	 */
	@Override
	public void openConnect() throws ConnectException
	{
		try
		{
			if (type.toLowerCase().equals("client"))
			{
				connector = new Socket(ip, port);
			}
			else
			{
				server = new ServerSocket(port, 5);
				connector = server.accept(); // 不确定当离开这个方法后，server会不会销毁
			}
		}
		catch (UnknownHostException e)
		{
			Log.error("错误的主机地址", e);
			throw new ConnectException();
		}
		catch (IOException e)
		{
			Log.error("接口打开失败", e);
			throw new ConnectException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crscic.interfacetesttool.connector.Connector#closeConnect()
	 */
	@Override
	public void closeConnect() throws ConnectException
	{
		try
		{
			if (connector != null)
				connector.close();
			if (server != null)
				server.close();
			if (keepAlive)
			{
				// 如果是长连接的话，那么输入输出流应该是打开状态的，是否需要关闭一下呢？
			}

		}
		catch (IOException e)
		{
			Log.error("接口关闭失败", e);
			throw new ConnectException();
		}

	}
}
