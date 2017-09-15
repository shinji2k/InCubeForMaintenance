/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.crscic.interfacetesttool.config.SocketConfig;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.log.Log;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;

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
	public void send(byte[] data) throws ConnectException
	{
		if (connector == null)
		{
			openConnect();
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
				if (!keepAlive)
				{
					if (os != null)
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

					closeConnect();
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

	@Override
	public byte[] receive()
	{
		InputStream is = null;
		byte[] res = null;
		try
		{
			is = connector.getInputStream();
			byte[] buff = new byte[1024];
			List<Byte> recvData = new ArrayList<Byte>();
			if (-1 != is.read(buff, 0, buff.length))
			{
				CollectionUtils.copyArrayToList(recvData, buff);
			}
			res = CollectionUtils.toByteArray(recvData);
			Log.info("接收：" + ByteUtils.byteArraytoHexString(res));
		}
		catch (IOException e)
		{
			Log.error("无法获取输入数据", e);
		}
		return res;
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
				Log.info("开始连接服务: " + ip + ":" + port);
				connector = new Socket(ip, port);
				Log.info("连接成功");
			}
			else
			{
				Log.info("启动服务...");
				server = new ServerSocket(port, 5);
				Log.info("等待客户端连接...");
				connector = server.accept(); // 不确定当离开这个方法后，server会不会销毁
				Log.info("连接客户端：" + connector.getRemoteSocketAddress().toString().substring(1));
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
