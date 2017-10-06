/**
 * 
 */
package com.crscic.interfacetesttool;

import java.io.UnsupportedEncodingException;

import com.crscic.interfacetesttool.gui.GUI;

/**
 * 
 * @author zhaokai 2017年9月13日 下午5:34:16
 */
public class Main
{
	public static void main(String[] args) throws UnsupportedEncodingException
	{
//			DataFactory factory = new DataFactory("config\\config.xml");
//			Connector connector = factory.getConnector();
////			SendSetting sendSetting = factory.getSendSetting();
//			ReplySetting replySetting = factory.getReplySetting();
//			List<ProtocolConfig> proCfgList = null;
////			proCfgList = factory.getDataConfig(sendSetting.getSettingFilePath(),
////					sendSetting.getProtocolList());
//			proCfgList = factory.getDataConfig(replySetting.getSettingFilePath(),
//					replySetting.getResponseList());
//
//			Service service = new Service();
//			// service.startService(connector, sendSetting, dataConfig);
//			service.startReplyService(connector, replySetting, proCfgList);
			// service.startSendService(connector, sendSetting, proCfgList);
		GUI gui = new GUI();

	}
}
