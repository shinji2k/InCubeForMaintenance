/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import com.crscic.interfacetesttool.Service;
import com.crscic.interfacetesttool.config.ComSetting;
import com.crscic.interfacetesttool.exception.ConnectException;
import com.crscic.interfacetesttool.log.Log;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * 
 * @author zhaokai 2017年9月7日 下午3:12:51
 */
public class ComConnector implements Connector
{
	private String port;
	private String baudrate;
	private String databit;
	private String parity;
	private String stopbit;
	private SerialPort serialPort;
	private boolean isOpen = false;
	private static final ComConnector connector = new ComConnector(); 
	
	public static ComConnector getInstance(ComSetting comCfg)
	{
		connector.setProperty(comCfg);
		if (!connector.isOpen)
			connector.openConnect();
		return connector;
	}

	public void setProperty(ComSetting comCfg)
	{
		port = comCfg.getPort();
		baudrate = comCfg.getBaudrate();
		databit = comCfg.getDatabit();
		parity = comCfg.getParity();
		stopbit = comCfg.getStopbit();
	}

	private ComConnector()
	{
		super();
	}
	

	@Override
	public void send(byte[] data) throws ConnectException
	{
		if (serialPort == null)
			openConnect();

		OutputStream out = null;
		try
		{
			out = serialPort.getOutputStream();
			out.write(data);
			out.flush();
		}
		catch (IOException e)
		{
			ConnectException.throwWriteErr(e);
		}
		finally
		{
			/**
			 * 暂时不关流看看 if (out != null) { try { out.close(); } catch
			 * (IOException e) { e.printStackTrace(); } }
			 **/
		}

	}

	@Override
	public void openConnect()
	{
		Log.debug("接口类型为串口，串口号：" + this.port + "，波特率：" + this.baudrate + "，数据位：" + this.databit + "，停止位：" + this.stopbit
				+ "，校验位：" + this.parity);
		try
		{
			// 通过端口名识别端口
			Log.debug("识别串口...");
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.port);
			Log.debug("打开串口...");
			// 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
			CommPort commPort = portIdentifier.open(this.port, 2000);
			Log.debug("串口打开成功");
			this.isOpen = true;
			// 判断是不是串口
			if (commPort instanceof SerialPort)
			{

				serialPort = (SerialPort) commPort;

				try
				{
					// 设置一下串口的波特率等参数
					// SerialPort.DATABITS_8
					// SerialPort.STOPBITS_1
					serialPort.setSerialPortParams(Integer.parseInt(this.baudrate), Integer.parseInt(this.databit),
							Integer.parseInt(this.stopbit), getParity());
				}
				catch (UnsupportedCommOperationException e)
				{
					Log.error("端口打开失败", e);
				}
			}
			else
			{
				// 不是串口
				Log.error("该端口号不是串口");
			}
		}
		catch (NoSuchPortException e1)
		{
			Log.error("该端口不存在", e1);
		}
		catch (PortInUseException e2)
		{
			Log.error("端口被占用", e2);
		}
	}

	private int getParity()
	{
		int ret = SerialPort.PARITY_NONE;
		if (this.parity.equals("even"))
			ret = SerialPort.PARITY_EVEN;
		else if (this.parity.equals("odd"))
			ret = SerialPort.PARITY_ODD;
		else if (this.parity.equals("space"))
			ret = SerialPort.PARITY_SPACE;
		else if (this.parity.equals("mark"))
			ret = SerialPort.PARITY_MARK;
		return ret;
	}

	/**
	 * 查找所有可用端口
	 * 
	 * @return 可用端口名称列表
	 */
	public static final ArrayList<String> listPort()
	{

		// 获得当前所有可用串口
		Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();

		ArrayList<String> portNameList = new ArrayList<String>();

		// 将可用串口名添加到List并返回该List
		while (portList.hasMoreElements())
		{
			String portName = ((CommPortIdentifier) portList.nextElement()).getName();
			portNameList.add(portName);
		}

		return portNameList;

	}

	/**
	 * 从串口读取数据
	 * 
	 * @param serialPort
	 *            当前已建立连接的SerialPort对象
	 * @return 读取到的数据
	 * @throws Exception
	 * @throws ReadDataFromSerialPortFailure
	 *             从串口读取数据时出错
	 * @throws SerialPortInputStreamCloseFailure
	 *             关闭串口对象输入流出错
	 */
	public static byte[] readFromPort(SerialPort serialPort) throws Exception
	{

		InputStream in = null;
		byte[] bytes = null;

		try
		{

			in = serialPort.getInputStream();
			int bufflenth = in.available(); // 获取buffer里的数据长度

			while (bufflenth != 0)
			{
				bytes = new byte[bufflenth]; // 初始化byte数组为buffer中数据的长度
				in.read(bytes);
				bufflenth = in.available();
			}
		}
		catch (IOException e)
		{
			throw new Exception();
		}
		finally
		{
			try
			{
				if (in != null)
				{
					in.close();
					in = null;
				}
			}
			catch (IOException e)
			{
				throw new Exception();
			}

		}

		return bytes;

	}

	/**
	 * 添加监听器
	 * 
	 * @param port
	 *            串口对象
	 * @param listener
	 *            串口监听器
	 * @throws Exception
	 * @throws TooManyListeners
	 *             监听类对象过多
	 */
	public static void addListener(SerialPort port, SerialPortEventListener listener) throws Exception
	{

		try
		{

			// 给串口添加监听器
			port.addEventListener(listener);
			// 设置当有数据到达时唤醒监听接收线程
			port.notifyOnDataAvailable(true);
			// 设置当通信中断时唤醒中断线程
			port.notifyOnBreakInterrupt(true);

		}
		catch (TooManyListenersException e)
		{
			throw new Exception();
		}
	}

	@Override
	public void closeConnect() throws ConnectException
	{
		if (serialPort != null)
		{
			serialPort.close();
			serialPort = null;
		}
		this.isOpen = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crscic.interfacetesttool.connector.Connector#receive()
	 */
	@Override
	public List<Byte> receive()
	{
		InputStream is = null;
		List<Byte> recvData = null;
		try
		{
			is = serialPort.getInputStream();
			recvData = new ArrayList<Byte>();
			int len = 0;
			while ((len = is.available()) > 0 && Service.running)
			{
				// 虽然几率较低，但仍有可能在从while的条件判断到read之间有新的数据进来，造成少取了数据
				byte[] buff = new byte[len];
				if (-1 != is.read(buff, 0, len))
					CollectionUtils.copyArrayToList(recvData, buff);
			}
			if (recvData.size() != 0)
				Log.debug("接收：" + ByteUtils.byteToHexString(recvData));
		}
		catch (IOException e)
		{
			Log.error("串口读取错误", e);
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
					is = null;
				}
			}
			catch (IOException e)
			{
				Log.error("关闭输入流失败", e);
			}

		}

		return recvData;
	}

	@Override
	public boolean isOpen()
	{
		return isOpen;
	}

}
