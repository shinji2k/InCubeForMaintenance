/**
 * 
 */
package com.crscic.interfacetesttool.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import com.crscic.interfacetesttool.entity.ComConfig;
import com.crscic.interfacetesttool.log.Log;

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

	public ComConnector(ComConfig comCfg)
	{
		port = comCfg.getPort();
		baudrate = comCfg.getBaudrate();
		databit = comCfg.getDatabit();
		parity = comCfg.getParity();
		stopbit = comCfg.getStopbit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crscic.interfacetesttool.connector.Connector#start()
	 */
	@Override
	public void send()
	{
		// TODO Auto-generated method stub

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.crscic.interfacetesttool.connector.Connector#connect()
	 */
	@Override
	public void connect()
	{
		// SerialPort com = new
		Log.info("接口类型为串口，串口号：" + this.port + "，波特率：" + this.baudrate + "，数据位：" + this.databit + "，停止位：" + this.stopbit
				+ "，校验位：" + this.parity);
	}

	/**
	 * 查找所有可用端口
	 * 
	 * @return 可用端口名称列表
	 */
	public static final ArrayList<String> findPort()
	{

		// 获得当前所有可用串口
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

		ArrayList<String> portNameList = new ArrayList<String>();

		// 将可用串口名添加到List并返回该List
		while (portList.hasMoreElements())
		{
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}

		return portNameList;

	}

	/**
	 * 打开串口
	 * 
	 * @param portName
	 *            端口名称
	 * @param baudrate
	 *            波特率
	 * @return 串口对象
	 * @throws Exception
	 * @throws SerialPortParameterFailure
	 *             设置串口参数失败
	 * @throws NotASerialPort
	 *             端口指向设备不是串口类型
	 * @throws NoSuchPort
	 *             没有该端口对应的串口设备
	 * @throws PortInUse
	 *             端口已被占用
	 */
	public static final SerialPort openPort(String portName, int baudrate) throws Exception
	{

		try
		{

			// 通过端口名识别端口
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

			// 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
			CommPort commPort = portIdentifier.open(portName, 2000);

			// 判断是不是串口
			if (commPort instanceof SerialPort)
			{

				SerialPort serialPort = (SerialPort) commPort;

				try
				{
					// 设置一下串口的波特率等参数
					serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
				}
				catch (UnsupportedCommOperationException e)
				{
					throw new Exception();
				}

				// System.out.println("Open " + portName + " sucessfully !");
				return serialPort;

			}
			else
			{
				// 不是串口
				throw new Exception();
			}
		}
		catch (NoSuchPortException e1)
		{
			throw new Exception();
		}
		catch (PortInUseException e2)
		{
			throw new Exception();
		}
	}

	/**
	 * 关闭串口
	 * 
	 * @param serialport
	 *            待关闭的串口对象
	 */
	public static void closePort(SerialPort serialPort)
	{
		if (serialPort != null)
		{
			serialPort.close();
			serialPort = null;
		}
	}

	/**
	 * 往串口发送数据
	 * 
	 * @param serialPort
	 *            串口对象
	 * @param order
	 *            待发送数据
	 * @throws Exception
	 * @throws SendDataToSerialPortFailure
	 *             向串口发送数据失败
	 * @throws SerialPortOutputStreamCloseFailure
	 *             关闭串口对象的输出流出错
	 */
	public static void sendToPort(SerialPort serialPort, byte[] order) throws Exception
	{

		OutputStream out = null;

		try
		{

			out = serialPort.getOutputStream();
			out.write(order);
			out.flush();

		}
		catch (IOException e)
		{
			throw new Exception();
		}
		finally
		{
			try
			{
				if (out != null)
				{
					out.close();
					out = null;
				}
			}
			catch (IOException e)
			{
				throw new Exception();
			}
		}

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

}
