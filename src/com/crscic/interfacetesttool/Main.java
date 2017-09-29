/**
 * 
 */
package com.crscic.interfacetesttool;

import java.util.List;

import org.dom4j.DocumentException;

import com.crscic.interfacetesttool.config.ReplySetting;
import com.crscic.interfacetesttool.config.SendSetting;
import com.crscic.interfacetesttool.connector.Connector;
import com.crscic.interfacetesttool.data.ProtocolConfig;
import com.crscic.interfacetesttool.exception.ParseXMLException;

/**
 * 
 * @author zhaokai 2017年9月13日 下午5:34:16
 */
public class Main
{
	public static void main(String[] args)
	{
		try
		{
			DataFactory factory = new DataFactory("config\\config.xml");
			Connector connector = factory.getConnector();
//			SendSetting sendSetting = factory.getSendSetting();
			ReplySetting replySetting = factory.getReplySetting();
			List<ProtocolConfig> proCfgList = null;
//			proCfgList = factory.getDataConfig(sendSetting.getSettingFilePath(),
//					sendSetting.getProtocolList());
			proCfgList = factory.getDataConfig(replySetting.getSettingFilePath(),
					replySetting.getResponseList());

			Service service = new Service();
			// service.startService(connector, sendSetting, dataConfig);
			service.startReplyService(connector, replySetting, proCfgList);
			// service.startSendService(connector, sendSetting, proCfgList);
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
		}
		catch (ParseXMLException e)
		{
			e.printStackTrace();
		}
		// catch (GenerateDataException e)
		// {
		// e.printStackTrace();
		// }
		// catch (ConnectException e)
		// {
		// e.printStackTrace();
		// }

	}
}
