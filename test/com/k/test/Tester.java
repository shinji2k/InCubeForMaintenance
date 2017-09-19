/**
 * 
 */
package com.k.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.SendService;
import com.crscic.interfacetesttool.config.ConfigHandler;
import com.crscic.interfacetesttool.config.ProtocolSetting;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.exception.ConnectException;

/**
 * 
 * @author zhaokai 2017年9月7日 下午3:41:38
 */
public class Tester
{
	@Test
	public void readConfigTest()
	{
		try
		{
			DataFactory df = new DataFactory("config/config.xml");
			// 获取连接器
			Connector conn = df.getConnector();
			// 获取各种配置
			ProtocolSetting proSetting = df.getProtocolSetting();
			ConfigHandler dataCfg = df.getDataConfig(proSetting);
			// 运行服务，包含发送和回复服务
			SendService service = new SendService();
			service.startService(conn, proSetting, dataCfg);
			// TODO: initResponse
			try
			{
				conn.closeConnect();
			}
			catch (ConnectException e)
			{
			}

		}
		catch (Exception e)
		{

		}
	}

	@Test
	public void socketService()
	{
		ServerSocket server = null;
		try
		{
			server = new ServerSocket(7676);
			while (true)
			{
				try
				{
					Socket socket = server.accept();
					System.out.println("Remote : " + socket.getRemoteSocketAddress());
					while (true)
					{
						OutputStream os = socket.getOutputStream();
						byte[] b = new byte[] { 0x01, 0x02, 0x03, 0x04 };
						os.write(b);
						os.flush();
						
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				catch (IOException e)
				{
					System.out.println("reAccept");
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
