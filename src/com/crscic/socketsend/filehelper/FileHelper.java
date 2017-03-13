package com.crscic.socketsend.filehelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHelper
{
	private File file = null;
	private int deepCount = 0;
	private long lineCnt = 0;

	public FileHelper()
	{
		super();
	}

	public FileHelper(String path)
	{
		super();
		load(path);
	}

	public void load(String path)
	{
		file = new File(path);
		lineCnt = 0;
	}
	
	/**
	 * 读取指定行的内容
	 *
	 * @param lineNum
	 * @return
	 * @author zhaokai
	 * @version 2017年2月15日 上午10:57:39
	 */
	public String readLine(long lineNum)
	{
		BufferedReader br = null;
		String res = null;
		try
		{
			FileReader reader = new FileReader(this.file);
			br = new BufferedReader(reader);
			String currLine = null;
			long lineCnt = 1;
			while ((currLine = br.readLine()) != null)
			{
				if (lineNum == lineCnt)
				{
					res = currLine;
					break;
				}
				lineCnt++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * 读取指定行之前的全部内容
	 *
	 * @param line
	 * @return
	 * @author zhaokai
	 * @version 2016年11月17日 上午11:21:08
	 */
	public List<String> head(long line)
	{
		List<String> res = new ArrayList<String>();
		BufferedReader br = null;
		try
		{
			FileReader reader = new FileReader(this.file);
			br = new BufferedReader(reader);
			String currLine = null;
			long lineNum = 1;
			while ((currLine = br.readLine()) != null)
			{
				res.add(currLine);
				if (lineNum == line)
					break;
				lineNum++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * 读取文件,返回内容每行是List中的一个元素
	 *
	 * @return
	 * @author zhaokai
	 * @version 2016年11月17日 上午11:18:50
	 */
	public List<String> read()
	{
		List<String> res = new ArrayList<String>();

		BufferedReader br = null;
		try
		{
			FileReader reader = new FileReader(this.file);
			br = new BufferedReader(reader);
			String currLine = null;
			while ((currLine = br.readLine()) != null)
			{
				res.add(currLine);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * 读取全部文件内容, 返回String
	 * 注意：由于按行读取，方法自己给每行后面加了“\r\n”，因此读取Unix文件时换行符会被格式化为Windows格式的
	 *
	 * @return
	 * @author zhaokai
	 * @version 2016年11月17日 上午11:19:15
	 */
	public String readAllFile()
	{
		StringBuffer res = new StringBuffer();
		BufferedReader br = null;
		try
		{
			FileReader reader = new FileReader(this.file);
			br = new BufferedReader(reader);
			String currLine = null; 
			while ((currLine = br.readLine()) != null)
			{
				res.append(currLine + "\r\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		return res.toString();
	}

	/**
	 * 获取当前文件夹的大小
	 *
	 * @param rootPath
	 * @author zhaokai
	 * @version 2016年11月17日 上午11:18:27
	 */
	public void du(String rootPath)
	{
		File root = new File(rootPath);
		Long size = getSize(root, 1);
		System.out.print("Total: ");
		formatSize(size);
	}

	/**
	 * 计算文件夹下文件大小
	 *
	 * @param parent 父文件夹
	 * @param deep 计算文件夹深度控制
	 * @return
	 * @author zhaokai
	 * @version 2017年2月15日 上午10:55:01
	 */
	private Long getSize(File parent, int deep)
	{
		deepCount++;
		Long totalSize = 0L;
		Map<String, Long> dirInfo = new HashMap<String, Long>();
		File[] children = parent.listFiles();
		if (children == null)
		{
			deepCount--;
			return 0L;
		}
		for (File child : children)
		{
			if (child.isDirectory())
			{
				Long dirSize = getSize(child, deep);
				dirInfo.put(child.getName(), dirSize);
				totalSize += dirSize;
			}
			else
				totalSize += child.length();
		}

		if (deep == deepCount)
		{
			for (String dirName : dirInfo.keySet())
			{
				System.out.print(dirName + ": ");
				formatSize(dirInfo.get(dirName));
			}
		}
		deepCount--;
		return totalSize;
	}

	/**
	 * 对大小进行格式化，以最合适的大小展示，并追加单位
	 *
	 * @param size
	 * @author zhaokai
	 * @version 2017年2月15日 上午10:54:16
	 */
	private void formatSize(Long size)
	{
		int type = 0;
		while (size > 1024)
		{
			size = size / 1024;
			type++;
		}
		switch (type)
		{
			case 0:
				System.out.println(size + "byte");
				break;
			case 1:
				System.out.println(size + "K");
				break;
			case 2:
				System.out.println(size + "M");
				break;
			case 3:
				System.out.println(size + "G");
				break;
			case 4:
				System.out.println(size + "T");
				break;
		}
	}
	
	/**
	 * 获取文件行数
	 * 文档发生变化后请重新load文件或使用新的对象，否则获得的值有可能是错误的
	 * @return
	 * @author zhaokai
	 * @version 2017年2月15日 上午11:11:14
	 */
	public long length()
	{
		//为防止重复调用计算统一个文件的行数增加性能负担，判断若之前计算过就用旧的值
		//lineCnt会在load新文件时清零
		//但有一个弊端就是在程序运行期间文档发生过变化，变化后没有重新load文件，则返回的行数会发生错误
		if (lineCnt != 0)
			return lineCnt;
		long Cnt = 0;
		BufferedReader br = null;
		try
		{
			FileReader reader = new FileReader(this.file);
			br = new BufferedReader(reader);
			@SuppressWarnings("unused")
			String currLine = null; 
			while ((currLine = br.readLine()) != null)
				Cnt++;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return Cnt;
	}

	public static void main(String[] args)
	{
		FileHelper f = new FileHelper("D:\\tower_db\\TOWER0927\\TOWER0927.DMP");
		List<String> r = f.head(2);
		try
		{
			for (String s : r)
			{
				byte[] b = s.getBytes("GBK");

				System.out.println(new String(b, "GBK"));
			}
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
