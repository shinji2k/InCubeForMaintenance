/**
 * 
 */
package com.k.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.DocumentException;
import org.junit.Test;

import com.crscic.interfacetesttool.DataFactory;
import com.crscic.interfacetesttool.Service;
import com.crscic.interfacetesttool.config.SendSetting;
import com.crscic.interfacetesttool.data.Data;
import com.crscic.interfacetesttool.data.Encryption;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.exception.ParseXMLException;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;

/**
 * 
 * @author zhaokai 2017年9月7日 下午3:41:38
 */
public class Tester
{

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

	@Test
	public void myGetSingleRequestTest()
	{
		byte[] aa = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e,
				0x0f, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x0e, 0x0f };
		List<Byte> al = new ArrayList<Byte>();
		al = CollectionUtils.copyArrayToList(al, aa);

		String head = "0102";
		String tail = "0e0f";
		/**
		 * List<List<Byte>> subList = Service.getFullRequest(al, head,
		 * tail); System.out.println("out : " + al);
		 * 
		 * for (List<Byte> sub : subList) {
		 * System.out.println(ByteUtils.bytetoHexString(CollectionUtils.toByteArray(sub)));
		 * }
		 **/
	}

	@Test
	public void rtuTest() throws UnknownHostException, IOException, InterruptedException
	{
		Socket s = new Socket("192.168.175.16", 10001);
		System.out.println("connected");
		OutputStream os = s.getOutputStream();
		os.write("test".getBytes());
		os.flush();

		InputStream is = s.getInputStream();
		while (true)
		{
			byte[] b = new byte[1024];
			while (is.read(b, 0, is.available()) != -1)
			{
				System.out.println(ByteUtils.byteToHexString(b));
			}

			Thread.sleep(500);
		}

	}

	@Test
	public void hexToByteTest()
	{
		System.out.println(ByteUtils.byteToHexString("60".getBytes()));
	}
	
	@Test
	public void chksumTest()
	{
//		String srcString = "20014043E00200";
//		byte[] src = srcString.getBytes();
//		List<Byte> list = new ArrayList<Byte>();
//		list = CollectionUtils.copyArrayToList(list, src);
//		byte[] encrp = Encryption.getChkSum(list);
//		System.out.println(ByteUtils.bytetoHexString(encrp));
		//fd3b
		
		int len = 62;
		byte[] lenid = new byte[3];
		lenid[0] =(byte) (len >> 8 & 0xF);
		lenid[1] =(byte) (len >> 4 & 0xF);
		lenid[2] =(byte) (len & 0xF);
		System.out.println("lenid : " + ByteUtils.byteToHexString(lenid));
		byte res = Encryption.getLChkSum(lenid);
		System.out.println(ByteUtils.byteToHexString(res));
	}

	@Test
	public void createDataTest() throws GenerateDataException, DocumentException, ParseXMLException
	{
		Data data = new Data();
		DataFactory df = new DataFactory("config/config.xml");
		SendSetting setting = df.getSendSetting();
		List<ProtocolConfig> config = df.getDataConfig(setting.getSettingFilePath(), setting.getProtocolList());
		Service service = new Service();
		byte[] b = data.getSendData(service.getProtocolConfigByProtocolName(config, "analog"), new HashMap<String, byte[]>());
		System.out.println(ByteUtils.byteToHexString(b));
	}
	
	@Test
	public void pubTest()
	{
		byte[] intByte = ByteUtils.getBytes(225); // 返回的是4字节的数组
		// 为适用A接口，将4字节缩为2字节
		byte[] b = new byte[2];
		b[0] = (byte)((intByte[0] << 8) | intByte[1]);
		b[1] = (byte)((intByte[2] << 8) | intByte[3]);
		System.out.println(ByteUtils.byteToHexString(ByteUtils.byteToAsc(b)));
	}
}
