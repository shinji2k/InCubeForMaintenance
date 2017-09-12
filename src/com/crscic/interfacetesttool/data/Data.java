/**
 * 
 */
package com.crscic.interfacetesttool.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.crscic.interfacetesttool.exception.AppException;
import com.crscic.interfacetesttool.exception.GenerateDataException;
import com.crscic.interfacetesttool.log.Log;
import com.k.util.ByteUtils;
import com.k.util.CollectionUtils;
import com.k.util.StringUtils;
import com.k.util.filehelper.FileHelper;

/**
 * @author zhaokai
 *
 *         2017年9月12日 下午1:50:37
 */
public class Data
{
	private int fileOrderMem = 1;
	private Map<String, Integer> proFileOrderMem;
	private int fileRandomMem = 0;
	private int fileLength = 0;
	private List<PartMem> partMem; // 缓存需要补完的字段信息
	private Map<String, byte[]> quoteMap; // 缓存需要从请求中引用数据的字段和使用的数据
	
	public Data()
	{
		proFileOrderMem = new HashMap<String, Integer>();
		Random r = new Random();
		fileRandomMem = r.nextInt(500) % (499) + 1;
		proFileOrderMem = new HashMap<String, Integer>();
		partMem = new ArrayList<PartMem>();
	}

	public byte[] getSendData(ProtocolStructure proStruct) throws GenerateDataException
	{
		List<Part> partList = proStruct.getPart();

		// 根据协议初始化顺序读文件时的缓存
		if (proFileOrderMem.containsKey(proStruct.getProtocolName()))
			fileOrderMem = proFileOrderMem.get(proStruct.getProtocolName());
		else
			fileOrderMem = 1;
		// 引用类型
		this.quoteMap = new HashMap<String, byte[]>(); // TODO:quoteMap要放到参数中一起传进来

		// 根据配置开始生成数据
		Map<String, Byte[]> res = new LinkedHashMap<String, Byte[]>();
		for (Part part : partList)
		{
			String attrInfo = part.getAttribute().get("name");
			byte[] b = getPartData(part);
			res.put(attrInfo, CollectionUtils.byteToByte(b));
		}
		// 补完
		res = reFilling(res);
		List<Byte> resTmpList = new ArrayList<Byte>();
		for (Byte[] fieldValueArray : res.values())
		{
			for (Byte fieldValue : fieldValueArray)
				resTmpList.add(fieldValue);
		}

		byte[] data = new byte[resTmpList.size()];
		for (int i = 0; i < resTmpList.size(); i++)
			data[i] = resTmpList.get(i);
		fileOrderMem++;
		proFileOrderMem.put(proStruct.getProtocolName(), fileOrderMem);
		fileRandomMem = 0; // 重置为0，下次调用的时候判断要是0的话生成新的随机数，避免重复读取
		return data;
	}

	/**
	 * 对初步生成的数据进行再填充，将诸如长度，校验位等第一次生成时没有进行生成的字段进行补完
	 *
	 * @param res
	 *            第一次生成后的结果数据Map
	 * @return
	 * @author zhaokai
	 * @version 2017年3月3日 下午4:28:20
	 */
	private Map<String, Byte[]> reFilling(Map<String, Byte[]> res)
	{
		if (partMem.size() == 0) // 不需要补完
			return res;
		// 先计算长度，再生成校验
		PartMem m = new PartMem();
		m.setName("length");
		int lenIndex = partMem.indexOf(m);
		if (lenIndex > -1)
		{
			m = partMem.get(lenIndex);
			byte[] t = getLengthData(m, res);
			res.put(m.getName(), CollectionUtils.byteToByte(t));

			partMem.remove(m);
		}

		// 循环填充其他需要补完的字段，目前主要是校验
		for (PartMem mem : partMem)
		{
			String childTypeString = mem.getChildType();
			byte[] b = null;
			if (childTypeString.equals("xor"))
				b = getCheckData(mem, res);
			else if (childTypeString.equals("crc"))
				b = getCheckData(mem, res);
			res.put(mem.getName(), CollectionUtils.byteToByte(b));
		}
		return res;
	}

	/**
	 * 生成校验位数据
	 *
	 * @param type
	 * @param key
	 * @param content
	 * @return
	 * @author zhaokai
	 * @version 2017年3月3日 下午5:47:42
	 */
	private byte[] getCheckData(PartMem mem, Map<String, Byte[]> content)
	{
		List<String> rangeList = mem.getRangeList();
		List<Byte> data = new ArrayList<Byte>();
		for (String range : rangeList)
		{
			for (Byte b : content.get(range))
				data.add(b);
		}

		byte[] b = null;
		if (mem.getChildType().equals("xor"))
			b = getXorData(data);
		else if (mem.getChildType().equals("crc"))
			b = getCrcData(data);

		return b;
	}

	/**
	 * 获取CRC校验，长度2字节。采用CRC-16/MODBUS（x16+x15+x2+1）加密
	 *
	 * @param data
	 * @return
	 * @author zhaokai
	 * @version 2017年4月2日 上午9:57:20
	 */
	private byte[] getCrcData(List<Byte> data)
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
	private byte[] getXorData(List<Byte> data)
	{
		byte xor = 0;
		for (int i = 0; i < data.size(); i++)
			xor ^= data.get(i);
		return new byte[] { xor };
	}

	/**
	 * 生成长度数据
	 *
	 * @return
	 * @author zhaokai
	 * @version 2017年3月3日 下午4:50:46
	 */
	private byte[] getLengthData(PartMem mem, Map<String, Byte[]> content)
	{
		int len = 0;
		List<String> rangeList = mem.getRangeList();
		for (String range : rangeList)
		{
			if (range.equals("length"))
				len += 4;
			else
				len += content.get(range).length;
		}
		return ByteUtils.intToByteArray(len);
	}

	/**
	 * 递归调用，组装part节点中的内容
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年3月1日 上午10:17:00
	 * @throws AppException
	 */
	private byte[] getPartData(Part part) throws GenerateDataException
	{
		String typeString = part.getType();
		byte[] b = null;
		if (typeString.equals("aptotic"))
			b = getAptoticData(part);
		else if (typeString.equals("file")) // 读取文件
			b = getFileData(part);
		else if (typeString.equals("length"))
			// 先将计算长度相关参数缓存，统一最后再计算
			setPartMem(part);
		else if (typeString.equals("generate"))// 递归
			b = getGenerateData(part);
		else if (typeString.equals("time")) // 时间格式
			b = getTimeData(part);
		else if (typeString.equals("random")) // 随机选取
			b = getRandomData(part);
		else if (typeString.equals("check")) // 校验码
			setPartMem(part);
		else if (typeString.equals("quote")) // 引用请求中的字段
			b = getQuoteData(part);
		else
			GenerateDataException.nullNodeValueException(part.getAttribute().get("name"), "type");
		return b;

	}

	private byte[] getAptoticData(Part part)
	{
		byte[] b;
		// 获取value的值
		String valueString = part.getValue();
		// xml中value是16进制字符串，将其装换为byte
		String classString = part.getValueClass();
		b = getByteArrayByClass(valueString, classString);
		String lenString = part.getLen();
		int len = 0;
		if (!StringUtils.isNullOrEmpty(lenString))
			len = Integer.parseInt(lenString);
		// 检查有没有配置fill-byte，若配置了则说明需要填充
		String fillByteString = part.getFillByte();
		if (!StringUtils.isNullOrEmpty(fillByteString))
		{
			String fillDirectionString = part.getFillDirection();
			b = doFill(b, fillByteString, fillDirectionString, len);
		}
		if (len > 0 && (b.length != len || b.length > len))
			Log.warn("生成数据的长度与配置中不一致");
		return b;
	}

	/**
	 * 读取文件类型生成数据
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年2月14日 下午7:47:20
	 */
	private byte[] getFileData(Part part)
	{
		List<Part> childPartList = part.getChildNodeList();
		// 如果后期加入从多个文件中读取内容，此处的子节点数量需要修改为==0
		if (childPartList.size() != 1)
		{
			Log.warn(part.getAttribute().get("name") + "节点中缺少文件配置子节点或子节点数量不唯一");
			return null;
		}

		Part child = childPartList.get(0);
		String filePath = child.getValue();
		if (StringUtils.isNullOrEmpty(filePath))
		{
			Log.warn(part.getAttribute().get("name") + "节点指定的读取文件路径为空");
			return null;
		}
		// 取type确定获取文件中数据的方式
		String type = child.getType();
		FileHelper fh = new FileHelper(filePath);
		String line = null;
		// 设置文件最大行数，因读取的各文件条数需要保持一致，因此仅读取一次文件以提高性能
		if (fileLength == 0) // 为0即为第一次需要初始化最大行数
			fileLength = (int) fh.length();

		if (fileLength == 0) // 设置完最大行数仍为0则认为文件为空
		{
			Log.warn("文件为空，文件：" + filePath);
			return null;
		}

		if (type.equals("order"))
		{
			if (fileOrderMem > fileLength) // 超过最大行数后从第一行重新开始
				fileOrderMem = 1;
			line = fh.readLine(fileOrderMem);
		}
		else
		{
			// 若生成的随机数大于文件最大行数，重新计算随机数
			if (fileRandomMem > fileLength || fileRandomMem == 0)
			{
				Random r = new Random();
				fileRandomMem = r.nextInt(fileLength) % (fileLength - 1) + 1;
			}
			line = fh.readLine(fileRandomMem);
		}
		if (StringUtils.isNullOrEmpty(line))
		{
			Log.warn("文件中数据错误，文件：" + filePath + "，行号：" + (fileOrderMem));
			return null;
		}

		byte[] b = null;
		String classString = child.getValueClass();
		b = getByteArrayByClass(line, classString);
		// 长度校验
		String lenString = child.getLen();
		int len = 0;
		if (!StringUtils.isNullOrEmpty(lenString))
			len = Integer.parseInt(lenString);
		// 检查有没有配置fill-byte，若配置了则说明需要填充
		String fillByteString = child.getFillByte();
		if (!StringUtils.isNullOrEmpty(fillByteString))
		{
			String fillDirectionString = child.getFillDirection();
			b = doFill(b, fillByteString, fillDirectionString, len);
		}
		if (len > 0 && len != b.length)
			Log.lengthWarning(part.getAttribute().get("name"));

		return b;
	}

	private byte[] getGenerateData(Part target) throws GenerateDataException
	{
		List<Part> childPartList = target.getChildNodeList();
		String splitString = null;
		byte[] split = null;
		splitString = getSplitString(target.getSplit());
		if (!StringUtils.isNullOrEmpty(splitString))
			split = ByteUtils.getBytes(splitString);
		List<Byte> res = new ArrayList<Byte>();
		for (Part childPart : childPartList)
		{
			byte[] b = getPartData(childPart);
			CollectionUtils.copyArrayToList(res, b);
			CollectionUtils.copyArrayToList(res, split);
		}
		// 去掉末尾的分隔符
		int resLen = res.size();
		if (split != null)
		{
			resLen = res.size() - split.length;
		}
		byte[] ret = new byte[resLen];
		for (int i = 0; i < resLen; i++)
			ret[i] = res.get(i);
		return ret;
	}

	private byte[] getTimeData(Part part) throws GenerateDataException
	{
		String valueString = part.getValue();
		if (StringUtils.isNullOrEmpty(valueString))
			GenerateDataException.nullNodeValueException(part.getAttribute().get("name"), "value");
		String classString = part.getValueClass();
		if (StringUtils.isNullOrEmpty(classString))
			GenerateDataException.nullNodeValueException(part.getAttribute().get("name"), "class");

		SimpleDateFormat f = new SimpleDateFormat(valueString);
		String timeString = f.format(new Date());

		byte[] b = getByteArrayByClass(timeString, classString);
		// 长度校验
		int len = 0;
		String lenString = part.getLen();
		if (!StringUtils.isNullOrEmpty(lenString))
			len = Integer.parseInt(lenString);

		// 检查有没有配置fill-byte，若配置了则说明需要填充
		String fillByteString = part.getFillByte();
		if (!StringUtils.isNullOrEmpty(fillByteString))
		{
			String fillDirectionString = part.getFillDirection();
			b = doFill(b, fillByteString, fillDirectionString, len);
		}

		if (len > 0 && (b.length != len || b.length > len))
			Log.lengthWarning(part.getAttribute().get("name"));
		return b;
	}

	/**
	 * 生成随机类型的数据（type=random）
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年3月1日 下午4:29:40
	 */
	private byte[] getRandomData(Part part)
	{
		String valueString = part.getValue();
		// split节点
		String splitString = part.getSplit();
		if (StringUtils.isNullOrEmpty(splitString))
			splitString = ",";
		splitString = getSplitString(splitString);
		// 对给定范围内的值进行随机
		String[] randomArray = valueString.split(splitString);
		Random r = new Random();
		int randomIndex = r.nextInt(randomArray.length);
		String res = randomArray[randomIndex];

		byte[] b = null;
		// class节点，先取得随机数后在判断class
		String classString = part.getValueClass();
		b = getByteArrayByClass(res, classString);
		// 长度校验
		int len = 0;
		String lenString = part.getLen();
		if (!StringUtils.isNullOrEmpty(lenString))
			len = Integer.parseInt(lenString);

		// 检查有没有配置fill-byte，若配置了则说明需要填充
		String fillByteString = part.getFillByte();
		if (!StringUtils.isNullOrEmpty(fillByteString))
		{
			String fillDirectionString = part.getFillDirection();
			b = doFill(b, fillByteString, fillDirectionString, len);
		}

		if (len > 0 && (b.length != len || b.length > len))
			Log.lengthWarning(part.getAttribute().get("name"));
		return b;
	}

	/**
	 * 设置需要补完时生成字段的参数缓存
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年3月3日 下午3:11:29
	 */
	private void setPartMem(Part part) throws GenerateDataException
	{
		// value中子part检查
		List<Part> childPartList = part.getChildNodeList();
		if (childPartList.size() != 1)
		{
			Log.error(part.getAttribute().get("name") + "节点缺少子节点");
			throw new GenerateDataException();
		}

		Part childPart = childPartList.get(0);
		// 最后组装时，检查flag的值，true的需要进行组装
		// 取父part的属性作为flag的key，最后组装根据该part确定替换那部分的值
		String partName = part.getAttribute().get("name");
		// 检查子Part中type节点,并存入flag，补完时判断用
		String childTypeString = childPart.getType();
		if (StringUtils.isNullOrEmpty(childTypeString))
			GenerateDataException.nullNodeValueException(partName, "type");
		PartMem mem = new PartMem();
		mem.setName(partName);
		mem.setChildType(childTypeString);
		// length检查
		String lenString = part.getLen();
		if (!StringUtils.isNullOrEmpty(lenString))
			mem.setLength(lenString);
		// 检查合法性
		String rangeString = childPart.getValue();
		if (StringUtils.isNullOrEmpty(rangeString))
			GenerateDataException.nullNodeValueException(partName, "value");
		// 是否需要分割
		String splitString = getSplitString(childPart.getSplit());
		List<String> rangePart = new ArrayList<String>();
		if (!StringUtils.isNullOrEmpty(splitString))
		{
			String[] rangeParts = rangeString.split(splitString);
			for (String range : rangeParts)
				rangePart.add(range);
		}
		else
			rangePart.add(rangeString);
		mem.setRangeList(rangePart);
		partMem.add(mem);
	}

	/**
	 * 获取分隔符，若分隔符是特殊字符，则返回特殊字符，否则原样返回
	 *
	 * @param split
	 * @return
	 * @author zhaokai
	 * @version 2017年3月20日 下午4:36:27
	 */
	private String getSplitString(String split)
	{
		String res = split;
		if (StringUtils.isNullOrEmpty(res))
			return res;
		if (res.toLowerCase().equals("tab"))
			return "\t";
		return res;
	}

	/**
	 * 针对需要从请求中获取数据的字段，从成员中获取数据并返回
	 *
	 * @return
	 * @author zhaokai
	 * @version 2017年5月12日 下午4:17:14
	 */
	private byte[] getQuoteData(Part part)
	{
		String attr_name = part.getAttribute().get("name");
		if (this.quoteMap.containsKey(attr_name))
		{
			return this.quoteMap.get(attr_name);
		}
		return null;
	}

	/**
	 * 根据class节点的内容对数据进行处理并返回处理后的byte数组
	 *
	 * @param src
	 * @param classString
	 * @return
	 * @author zhaokai
	 * @version 2017年4月13日 上午10:07:42
	 */
	private byte[] getByteArrayByClass(String src, String classString)
	{
		byte[] b = null;
		if (classString != null)
		{
			if (classString.equals("byte"))
			{
				b = ByteUtils.hexStringToBytes(src);
			}
			else if (classString.equals("float"))
			{
				Float f = new Float(Float.parseFloat(src) * 100);
				// 按照铁标只需要float乘以100发送，不需要转int
				// b = ByteUtils.getBytes(f);
				b = ByteUtils.getBytes(f.intValue());
			}
			else
			{
				b = ByteUtils.getBytes(src);
			}
		}
		return b;
	}

	/**
	 * 长度不足时填充指定字节补足长度
	 *
	 * @param value
	 *            待填充的内容
	 * @param fillByteString
	 *            填充时使用的字节，16进制字符串形式
	 * @param fillDirect
	 *            填充方向，在左侧填充还是右侧填充
	 * @param len
	 *            填充后的长度
	 * @return
	 * @author zhaokai
	 * @version 2017年4月5日 下午4:29:44
	 */
	private byte[] doFill(byte[] value, String fillByteString, String fillDirect, int len)
	{
		if (len == 0)
			return value;
		int fillNum = len - value.length;
		byte[] res = new byte[len];
		byte[] b = ByteUtils.hexStringToBytes(fillByteString);
		if (b.length > 1)
			Log.warn("填充字符只能为1字节");
		byte fillCharByte = b[0];

		if (fillDirect.equals("right"))
		{
			for (int i = 0; i < len; i++)
			{
				if (i < value.length)
					res[i] = value[i];
				else
					res[i] = fillCharByte;
			}
		}
		else
		{
			for (int i = 0; i < len; i++)
			{
				if (i < fillNum)
					res[i] = fillCharByte;
				else
					res[i] = value[i - fillNum];
			}
		}
		return res;
	}

}
