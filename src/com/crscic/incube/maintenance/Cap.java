package com.crscic.incube.maintenance;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;

/**
 * 
 * @author zhaokai
 * 2017年11月20日 下午2:50:07
 */
public class Cap
{
	public static void main(String[] args)
	{
		ServerSocket ss = null;
		try
		{
			ss = new ServerSocket(10001);
			while (true)
			{
				Socket s = ss.accept();
				System.out.println("connected : " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
				Thread.sleep(1000);
				InputStream is = s.getInputStream();
				int len = 0;
				while ((len = is.available()) > 0)
				{
					// 虽然几率较低，但仍有可能在从while的条件判断到read之间有新的数据进来，造成少取了数据
					byte[] buff = new byte[len];
					if (-1 != is.read(buff, 0, len))
						System.out.println("recv:" + ByteUtils.byteToHexString(buff));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				ss.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
