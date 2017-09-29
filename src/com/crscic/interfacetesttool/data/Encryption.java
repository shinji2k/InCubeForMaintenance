package com.crscic.interfacetesttool.data;

import java.util.List;

import com.k.util.ByteUtils;

/**
 * 组装数据时所有的校验算法
 * 
 * @author zhaokai 2017年9月22日 下午4:58:50
 */
public class Encryption
{
	public static final byte[] ASCII = new byte[] { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44,
			0x45, 0x46 };
	/**
	 * 校验和码，被校验数取和之后模65536的余数取反加1
	 * 
	 * @param src
	 * @return 4位16进制校验码对应的ascii码
	 * @author ken_8 2017年9月23日 上午12:21:35
	 */
	public static byte[] getChkSum(List<Byte> src)
	{
		// 用来记录计算chksum时的中间转换变量
		int check = 0;
		// 用来存放chksum结果
		byte[] result = new byte[4];
		// 计算每一字节的和
		for (byte b : src)
		{
			check += (b & 0xff);
		}
		// 把和对65535求余数 取反 加1
		check = ~(check % 65536) + 1;
		// 把最后的结果分为四字节的ascii码
		result[0] = ASCII[((check >> 12) & 0x0000000f)];
		result[1] = ASCII[((check >> 8) & 0x0000000f)];
		result[2] = ASCII[((check >> 4) & 0x0000000f)];
		result[3] = ASCII[((check & 0x0000000f))];

		return result;
	}

	/**
	 * 获得lchksum，与lenid共同组成A接口中的length字段。
	 * 计算长度的后12位之和，模16的余数取反加1
	 * 
	 * @param src
	 * @return
	 * @author ken_8 2017年9月23日 下午9:47:11
	 */
	public static byte getLChkSum(byte[] src)
	{
		byte result;
		// 用来记录计算len_chksum时的中间转换变量
		int len_check = 0;
		// 计算len_chksum先计算长度每个字节所代表的ascii码的字符的真值和
		for (int i = 0; i < 3; i++)
		{
			if (src[i] > 0x39)
				len_check = len_check + (src[i] & 0xff) - 0x37; // -7
			else
				len_check = len_check + (src[i] & 0xff) - 0x30; // -0
		}
		// 把相应的和模16
		len_check %= 16;
		// 再把结果取反 +1
		len_check = ~len_check + 1;
		// 得到结果的ascii码的真值
		if (len_check == 16)
			result = 0;
		else
			result = ASCII[len_check];

		return result;
	}

	/**
	 * 获取CRC校验，长度2字节。采用CRC-16/MODBUS（x16+x15+x2+1）加密
	 *
	 * @param data
	 * @return
	 * @author zhaokai
	 * @version 2017年4月2日 上午9:57:20
	 */
	public static byte[] getCrcData(List<Byte> data)
	{
		int len = data.size();

		// 预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
		int crc = 0xFFFF;
		int i, j;
		for (i = 0; i < len; i++)
		{
			// 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
			crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (data.get(i) & 0xFF));
			for (j = 0; j < 8; j++)
			{
				// 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
				if ((crc & 0x0001) > 0)
				{
					// 如果移出位为 1, CRC寄存器与多项式A001进行异或
					crc = crc >> 1;
					crc = crc ^ 0xA001;
				}
				else
					// 如果移出位为 0,再次右移一位
					crc = crc >> 1;
			}
		}
		return ByteUtils.swapHighLow(crc);
	}

	/**
	 * 异或校验
	 * 
	 * @author ken_8
	 * @version : 2017年3月3日,下午11:23:54
	 * @param data
	 * @return
	 */
	public static byte[] getXorData(List<Byte> data)
	{
		byte xor = 0;
		for (int i = 0; i < data.size(); i++)
			xor ^= data.get(i);
		return new byte[] { xor };
	}
}
