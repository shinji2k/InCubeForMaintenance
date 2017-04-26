/**
 * byte类型相关工具类
 */
package com.crscic.socketsend.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author zhaokai 2016年9月30日 下午4:52:11
 */
public class ByteUtils
{

	/**
	 * 将16位的int高低位互换，返回2字节长度的byte数组。 主要应用于大端机报文发送时调整字节顺序
	 *
	 * @param src
	 * @return
	 * @author zhaokai
	 * @version 2017年4月2日 上午10:49:07
	 */
	public static byte[] swapHighLow(int src)
	{
		return new byte[]
		{ (byte) (src & 0xFF00), (byte) (src & 0x00FF) };
	}

	/**
	 * 将byte数组拼接为字符串(内容为16进制显示)返回
	 * 
	 * @param src
	 * @return
	 * @author zhaokai
	 * @date 2016年9月30日 下午4:57:17
	 */
	public static String byteArraytoHexString(byte[] src)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < src.length; i++)
			sb.append(" " + Integer.toHexString(src[i] & 0xFF));
		return sb.toString().substring(1);
	}

	/**
	 * 将16进制字符串转为byte数组
	 *
	 * @param hexString
	 * @return
	 * @author zhaokai
	 * @version 2016年11月9日 下午1:55:28
	 */
	public static byte[] hexStringToBytes(String hexString)
	{
		if (hexString == null || hexString.equals(""))
		{
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++)
		{
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * 字符型转byte
	 *
	 * @param c
	 * @return
	 * @author zhaokai
	 * @version 2016年11月9日 下午1:56:54
	 */
	private static byte charToByte(char c)
	{
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static byte[] getBytes(short data)
	{
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data & 0xff00) >> 8);
		return bytes;
	}

	public static byte[] getBytes(char data)
	{
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data);
		bytes[1] = (byte) (data >> 8);
		return bytes;
	}

	public static byte[] getBytes(int data)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((data >> 24) & 0xFF);
		bytes[1] = (byte) ((data >> 16) & 0xFF);
		bytes[2] = (byte) ((data >> 8) & 0xFF);
		bytes[3] = (byte) (data & 0xFF);
		return bytes;
	}

	public static byte[] getBytes(long data)
	{
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		bytes[4] = (byte) ((data >> 32) & 0xff);
		bytes[5] = (byte) ((data >> 40) & 0xff);
		bytes[6] = (byte) ((data >> 48) & 0xff);
		bytes[7] = (byte) ((data >> 56) & 0xff);
		return bytes;
	}

	public static byte[] getBytes(float data)
	{
		int intBits = Float.floatToIntBits(data);
		return getBytes(intBits);
	}

	public static byte[] getBytes(double data)
	{
		long intBits = Double.doubleToLongBits(data);
		return getBytes(intBits);
	}

	public static byte[] getBytes(String data, String charsetName)
	{
		Charset charset = Charset.forName(charsetName);
		return data.getBytes(charset);
	}

	/**
	 * 以默认的GBK编码格式将字符串转为byte数组。 若指定编码格式，请使用getBytes(String data, String
	 * charsetName)方法
	 *
	 * @param data
	 * @return
	 * @author zhaokai
	 * @version 2017年4月12日 下午3:58:17
	 */
	public static byte[] getBytes(String data)
	{
		return getBytes(data, "GBK");
	}

	public static short getShort(byte[] bytes)
	{
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	public static char getChar(byte[] bytes)
	{
		return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	public static int getInt(byte[] bytes)
	{
		return (int) (((bytes[0] & 0xFF) << 24) | ((bytes[0 + 1] & 0xFF) << 16) | ((bytes[0 + 2] & 0xFF) << 8)
				| (bytes[0 + 3] & 0xFF));
	}

	public static long getLong(byte[] bytes)
	{
		return (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8)) | (0xff0000L & ((long) bytes[2] << 16))
				| (0xff000000L & ((long) bytes[3] << 24)) | (0xff00000000L & ((long) bytes[4] << 32))
				| (0xff0000000000L & ((long) bytes[5] << 40)) | (0xff000000000000L & ((long) bytes[6] << 48))
				| (0xff00000000000000L & ((long) bytes[7] << 56));
	}

	public static float getFloat(byte[] bytes)
	{
		return Float.intBitsToFloat(getInt(bytes));
	}

	public static double getDouble(byte[] bytes)
	{
		long l = getLong(bytes);
		System.out.println(l);
		return Double.longBitsToDouble(l);
	}

	/**
	 * 字节转换为浮点
	 * 
	 * @param b
	 *            字节（至少4个字节）
	 * @param index
	 *            开始位置
	 * @return
	 */
	public static float byte2float(byte[] b, int index)
	{
		int l;
		l = b[index + 0];
		l &= 0xff;
		l |= ((long) b[index + 1] << 8);
		l &= 0xffff;
		l |= ((long) b[index + 2] << 16);
		l &= 0xffffff;
		l |= ((long) b[index + 3] << 24);
		return Float.intBitsToFloat(l);

		// int accum = 0;
		// accum = accum|(b[0] & 0xff) << 0;
		// accum = accum|(b[1] & 0xff) << 8;
		// accum = accum|(b[2] & 0xff) << 16;
		// accum = accum|(b[3] & 0xff) << 24;
		// System.out.println(accum);
		// return Float.intBitsToFloat(accum);
	}

	/**
	 * byte数组转为字符串，使用指定的编码进行转换
	 *
	 * @param b
	 * @param charSet
	 * @return
	 * @author zhaokai
	 * @version 2017年4月12日 下午3:55:39
	 */
	public static String byteArrayToString(byte[] b, String charSet)
	{
		String res = null;
		try
		{
			res = new String(b, charSet);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * 将byte数组的内容转为字符串，默认采用UTF-8的编码格式
	 *
	 * @param b
	 * @return
	 * @author zhaokai
	 * @version 2017年1月6日 下午2:08:19
	 */
	public static String byteArrayToString(byte[] b)
	{
		return byteArrayToString(b, "GBK");
	}

	/**
	 * 整型转换至byte[4]
	 *
	 * @param integer
	 * @return
	 * @author zhaokai
	 * @version 2017年3月3日 下午5:12:23
	 */
	public static byte[] intToByteArray(final int integer)
	{
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}
}
