package com.crscic.socketsend.xmlhelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.crscic.socketsend.data.PartMem;
import com.crscic.socketsend.exception.AppException;
import com.crscic.socketsend.filehelper.FileHelper;
import com.crscic.socketsend.utils.ByteUtils;
import com.crscic.socketsend.utils.CollectionUtils;
import com.crscic.socketsend.utils.StringUtils;

public class XmlHelper
{
	private SAXReader saxReader;
	private Document xmlDocument;
	private int fileOrderMem = 1;
	private Map<String, Integer> proFileOrderMem;
	private int fileRandomMem = 0;
	private int fileLength = 0;
	private List<PartMem> partMem; // 缓存需要补完的字段信息

	public XmlHelper()
	{
		super();
	}

	public XmlHelper(String xmlPath)
	{
		load(new File(xmlPath));
	}

	public XmlHelper(File xmlFile)
	{
		load(xmlFile);
	}

	public void load(File xmlFile)
	{
		// 初始化随机数，读文件使用，先随机500以内的整数
		Random r = new Random();
		fileRandomMem = r.nextInt(500) % (499) + 1;
		proFileOrderMem = new HashMap<String, Integer>();
		partMem = new ArrayList<PartMem>();
		saxReader = new SAXReader();
		try
		{
			xmlDocument = saxReader.read(xmlFile);
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 根据协议名称查找Xml中的配置，并生成发送数据
	 *
	 * @param protocolName
	 * @return 指定协议完整的数据，byte[]
	 * @throws AppException
	 * @author zhaokai
	 * @version 2017年2月13日 下午6:11:30
	 */
	@SuppressWarnings("unchecked")
	public byte[] getDataByProtocol(String protocolName) throws AppException
	{
		// 根据协议初始化顺序读文件时的缓存
		if (proFileOrderMem.containsKey(protocolName))
			fileOrderMem = proFileOrderMem.get(protocolName);
		else
			fileOrderMem = 1;
		// 初始化Xml对象
		if (xmlDocument == null)
			throw new AppException("Xml文档加载失败或未加载");
		if (StringUtils.isNullOrEmpty(protocolName))
			throw new AppException("未指定协议名称");
		Element root = xmlDocument.getRootElement();
		Element protocols = root.element("protocols");
		List<Element> pros = protocols.elements();
		// 检查xml中是否存在指定协议的配置，有则提取，无则抛出异常
		Element selectPro = null;
		for (Element pro : pros)
		{
			if (pro.getTextTrim().equals(protocolName.trim()))
			{
				selectPro = root.element(protocolName);
				break;
			}
		}
		if (selectPro == null)
			throw new AppException("Xml中不包含指定协议的配置。协议：" + protocolName);
		// 根据配置开始生成数据
		Map<String, Byte[]> res = new LinkedHashMap<String, Byte[]>();
		List<Element> parts = selectPro.elements();
		if (parts.size() == 0)
			throw new AppException("协议配置为空");
		for (Element part : parts)
		{
			String attrInfo = part.attributeValue("name");
			if (StringUtils.isNullOrEmpty(attrInfo))
				throw AppException.lostAttribute(part, "name");
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
		proFileOrderMem.put(protocolName, fileOrderMem);
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
		for (PartMem mem : partMem)
		{
			String childTypeString = mem.getChildType();
			byte[] b = null;
			if (childTypeString.equals("length"))
				b = getLengthData(mem, res);
			else if (childTypeString.equals("xor"))
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
		return b;
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
		return new byte[]
		{ xor };
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
			len += content.get(range).length;
		return ByteUtils.intToByteArray(len);
	}

	/**
	 * 递归调用，组装part节点中的内容
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年3月1日 上午10:17:00
	 */
	private byte[] getPartData(Element part) throws AppException
	{
		Element typeNode = part.element("type");
		if (null == typeNode)
			throw AppException.nullNodeErr(part, "type");
		String typeString = typeNode.getTextTrim();
		if (StringUtils.isNullOrEmpty(typeString))
			throw AppException.nodeErr(part, "type");
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
		else
			throw AppException.nodeErr(part, "type");
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
	private void setPartMem(Element part) throws AppException
	{
		// value节点检查
		Element valueNode = part.element("value");
		if (valueNode == null)
			throw AppException.nullNodeErr(part, "value");
		// length检查
		Element lenNode = part.element("len");
		if (lenNode == null)
			throw AppException.nullNodeErr(part, "len");
		String lenString = lenNode.getTextTrim();
		// value中子part检查
		Element childPart = valueNode.element("part");
		if (childPart == null)
			throw AppException.nullNodeErr(part, "子part");

		// 最后组装时，检查flag的值，true的需要进行组装
		// 取父part的属性作为flag的key，最后组装根据该part确定替换那部分的值
		String attrInfo = part.attributeValue("name");
		if (StringUtils.isNullOrEmpty(attrInfo))
			throw AppException.lostAttribute(part, "name");
		// 检查子Part中type节点,并存入flag，补完时判断用
		Element childTypeNode = childPart.element("type");
		if (null == childTypeNode)
			throw AppException.nullNodeErr(part, "子part中的type");
		String childTypeString = childTypeNode.getTextTrim();
		if (StringUtils.isNullOrEmpty(childTypeString))
			throw AppException.nodeErr(part, "子part中的type");
		PartMem mem = new PartMem();
		mem.setName(attrInfo);
		mem.setChildType(childTypeString);
		mem.setChildPart(childPart); // 暂时没用，存上备用
		if (!StringUtils.isNullOrEmpty(lenString))
			mem.setLength(lenString);
		// 检查合法性
		Element rangeNode = childPart.element("value");
		if (rangeNode == null)
			throw AppException.nullNodeErr(childPart, "value");
		String rangeString = rangeNode.getTextTrim();
		if (StringUtils.isNullOrEmpty(rangeString))
			throw AppException.nodeErr(childPart, "value");
		// 是否需要分割
		Element splitNode = childPart.element("split");
		if (splitNode == null)
			throw AppException.nullNodeErr(childPart, "split");
		String splitString = splitNode.getTextTrim();
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
	 * 生成随机类型的数据（type=random）
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年3月1日 下午4:29:40
	 */
	private byte[] getRandomData(Element part) throws AppException
	{
		String valueString = getNodeValue(part, "value");
		// split节点
		String splitString = getNodeValue(part, "split");
		// 对给定范围内的值进行随机
		String[] randomArray = valueString.split(splitString);
		Random r = new Random();
		int randomIndex = r.nextInt(randomArray.length);
		String res = randomArray[randomIndex];
		// class节点，先取得随机数后在判断class
		String classString = getNodeValue(part, "class");
		if (classString.equals("byte"))
			return ByteUtils.hexStringToBytes(res);
		return res.getBytes();
	}

	/**
	 * 获取时间类型的数据,value中是时间格式
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年3月1日 下午4:17:21
	 */
	private byte[] getTimeData(Element part) throws AppException
	{
		Element valueNode = part.element("value");
		if (null == valueNode)
			throw AppException.nullNodeErr(part, "value");
		String valueString = valueNode.getTextTrim();
		if (StringUtils.isNullOrEmpty(valueString))
			throw AppException.nodeErr(part, "value");
		Element classNode = part.element("class");
		if (null == classNode)
			throw AppException.nullNodeErr(part, "class");
		String classString = valueNode.getTextTrim();
		if (StringUtils.isNullOrEmpty(classString))
			throw AppException.nodeErr(part, "class");
		if (classString.equals("byte"))
			// 若是xml中定义数据类型是byte类型，则返回null
			// 按说type是time类型的，不应存在class是byte的可能，若并存只能是错误
			return null;
		SimpleDateFormat f = new SimpleDateFormat(valueString);
		String timeString = f.format(new Date());
		return timeString.getBytes();
	}

	/**
	 * 返回根据规则生成类型字段的数据
	 *
	 * @param part
	 * @author zhaokai
	 * @version 2017年2月16日 下午5:31:17
	 */
	private byte[] getGenerateData(Element target) throws AppException
	{
		List<Element> childPartNodes = getChildPart(target);
		String splitString = null;
		byte[] split = null;
		Element splitNode = target.element("split");
		if (null != splitNode)
		{
			splitString = splitNode.getTextTrim();
			if (!StringUtils.isNullOrEmpty(splitString))
				split = ByteUtils.getBytes(splitString);
		}
		List<Byte> res = new ArrayList<Byte>();
		for (Element childPart : childPartNodes)
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

	/**
	 * 固定值生成数据
	 *
	 * @param part
	 * @return
	 * @throws AppException
	 * @author zhaokai
	 * @version 2017年2月14日 下午7:44:40
	 */
	private byte[] getAptoticData(Element part) throws AppException
	{
		String lenString = getNodeValue(part, "len");
		int len = 0;
		if (!StringUtils.isNullOrEmpty(lenString))
			len = Integer.parseInt(lenString);
		byte[] b;
		// xml中value是16进制字符串，将其装换为byte
		String classString = getNodeValue(part, "class");
		if (StringUtils.isNullOrEmpty(classString))
			throw AppException.nodeErr(part, "class");
		if (classString.equals("byte"))
			b = ByteUtils.hexStringToBytes(part.element("value").getTextTrim());
		else // xml中value是ascii符号
			b = part.element("value").getTextTrim().getBytes();
		if (len > 0 && b.length != len)
			throw AppException.lengthErr(part);
		return b;
	}

	/**
	 * 读取文件类生成数据
	 *
	 * @param part
	 * @return
	 * @author zhaokai
	 * @version 2017年2月14日 下午7:47:20
	 */
	private byte[] getFileData(Element part) throws AppException
	{
		Element child = part.element("value");
		if (child == null)
			throw AppException.nullNodeErr(part, "value");
		child = child.element("part");
		if (child == null)
			throw AppException.nullNodeErr(part, "part");
		String filePath = getNodeValue(child, "value");
		if (StringUtils.isNullOrEmpty(filePath))
			throw AppException.filepathErr(child);
		// 取type确定获取文件中数据的方式
		String type = getNodeValue(child, "type");
		if (StringUtils.isNullOrEmpty(type))
			throw AppException.nodeErr(child, "type");
		FileHelper fh = new FileHelper(filePath);
		String line = null;
		// 设置文件最大行数，因读取的各文件条数需要保持一致，因此仅读取一次文件以提高性能
		if (fileLength == 0) // 为0即为第一次需要初始化最大行数
			fileLength = (int) fh.length();

		if (fileLength == 0) // 设置完最大行数仍为0则认为文件为空
			throw new AppException("文件中数据错误，文件：" + filePath);
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
			throw new AppException("文件中数据错误，文件：" + filePath + "，行号：" + (fileOrderMem));

		byte[] b = null;
		String classString = getNodeValue(child, "class");
		if (classString != null && classString.equals("byte"))
			b = ByteUtils.hexStringToBytes(line);
		else
			b = line.getBytes();
		// 长度校验
		String lenString = getNodeValue(child, "len");
		if (!StringUtils.isNullOrEmpty(lenString))
		{
			int len = Integer.parseInt(lenString);
			if (len != b.length)
				throw AppException.lengthErr(child);
		}

		return b;
	}

	/**
	 * 获取节点中的值
	 * 
	 * @author ken_8
	 * @version : 2017年3月4日,上午12:16:35
	 * @param parent
	 * @param nodeName
	 * @return
	 * @throws AppException
	 */
	private String getNodeValue(Element parent, String nodeName) throws AppException
	{
		Element node = parent.element(nodeName);
		if (null == node)
			throw AppException.nullNodeErr(parent, nodeName);
		String nodeString = node.getTextTrim();
		if (StringUtils.isNullOrEmpty(nodeString))
			return null;
		return nodeString;
	}

	/**
	 * 获得value节点内子part节点的集合
	 * 
	 * @author ken_8
	 * @version : 2017年3月4日,上午12:20:35
	 * @param parent
	 * @param nodeName
	 * @return
	 * @throws AppException
	 */
	@SuppressWarnings("unchecked")
	private List<Element> getChildPart(Element parent) throws AppException// bug
																			// here
	{
		Element valueNode = parent.element("value");
		if (null == valueNode)
			// 因为value节点不带属性，为了指明是哪个节点出的问题，使用其父part节点
			throw AppException.nullNodeErr(parent, "value");
		List<Element> childPartNode = valueNode.elements("part");
		if (childPartNode.size() == 0)
			throw AppException.nullNodeErr(parent, "value中的子Part");
		return childPartNode;
	}
}
