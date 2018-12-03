package com.crscic.incube.maintenance.gui;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.crscic.incube.DataFactory;
import com.crscic.incube.connector.Connector;
import com.crscic.incube.data.ProtocolConfig;
import com.crscic.incube.entity.ParseSetting;
import com.crscic.incube.entity.Part;
import com.crscic.incube.entity.Setting;
import com.crscic.incube.exception.ParseXMLException;
import com.crscic.incube.log.Log;
import com.crscic.incube.maintenance.Service;
import com.k.util.StringUtils;

/**
 * 
 * @author ken_8 2017年10月1日 下午9:50:25
 */
public final class GUI
{
	private static GUI gui;
	private String selectedConfig;

	public static Image LOGO_IMG = SWTResourceManager.getImage(GUI.class,
			"/com/crscic/incube/maintenance/gui/resource/logo.png");
	private static Display display = Display.getDefault(); // 1.创建一个Display
	private static Shell shell = new Shell();
	private static Label deviceLabel = new Label(shell, SWT.NONE);
	private static final Combo deviceListCombo = new Combo(shell, SWT.READ_ONLY);
	private static Label cntLabel = new Label(shell, SWT.None);
	private static Text cntText = new Text(shell, SWT.BORDER);
	private static Button goButton = new Button(shell, SWT.CENTER);
	private static Button stopButton = new Button(shell, SWT.CENTER);
	public static StyledText outputText = new StyledText(shell,
			SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);

	private GUI() throws UnsupportedEncodingException
	{
		GridData gd_cntText = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gd_cntText.widthHint = 30;
		cntText.setLayoutData(gd_cntText);
		shell.setSize(800, 600);
		shell.setText("接口调试工具");
		shell.setImage(LOGO_IMG);
		shell.setLayout(new GridLayout(6, false));

		// 初始化
		cntLabel.setText("数量：");

		// 初始化设备列表
		deviceLabel.setText("设备：");
		Map<String, String> deviceMap = DataFactory.getDeviceInfo();
		if (deviceMap != null)
		{
			int idx = 0;
			for (String key : deviceMap.keySet())
			{
				deviceListCombo.add(key);
				deviceListCombo.setData(Integer.toString(idx++), deviceMap.get(key));
			}
		}
		deviceListCombo.addSelectionListener(new SelectDeviceComboEventHandler());

		cntText.setText("1");
		goButton.setText("开始");
		goButton.addSelectionListener(new StartServiceEventHandler());
		stopButton.setText("停止");
		stopButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Service.running = false;
			}
		});

		GridData gd_outputText = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		gd_outputText.heightHint = 493;
		outputText.setLayoutData(gd_outputText);

		shell.open();
		shell.layout();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}

	/**
	 * 选择设备列表下拉菜单的响应事件
	 * 
	 * @author ken_8
	 * @create 2017年10月12日 上午12:12:00
	 */
	class SelectDeviceComboEventHandler extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			selectedConfig = (String) deviceListCombo.getData(Integer.toString(deviceListCombo.getSelectionIndex()));
		}
	}

	class StartServiceEventHandler extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			try
			{
				if (StringUtils.isNullOrEmpty(selectedConfig))
				{
					Log.error("请先选择设备");
					return;
				}
				String sendLoopStr = cntText.getText();
				if (StringUtils.isNullOrEmpty(sendLoopStr))
				{
					Log.error("请设置设备数量");
					return;
				}
				int sentLoop = Integer.parseInt(sendLoopStr);
				// 读取通信配置
				DataFactory factory = new DataFactory("config\\config.xml");
				Setting setting = factory.initSetting();
				Map<String, Map<String, Part>> connParamMapper = factory.getConnParam();
				for (String connConf : connParamMapper.keySet())
				{
					Connector connector = factory.getConnector(connConf);
					ParseSetting parseSetting = factory.getParseSetting(selectedConfig);
					if (parseSetting == null)
					{
						Log.error("配置文件：" + selectedConfig + " 内容错误或不完整");
						return;
					}
					
					
					List<ProtocolConfig> proCfgList = null;
					Service service = new Service();
					if (parseSetting != null)
					{
						proCfgList = DataFactory.getDataConfig(parseSetting.getProtocolFile(), parseSetting.getRequest());
						service.startParseService(setting, connector, parseSetting, proCfgList, sentLoop);
					}
				}
			}
			catch (DocumentException e1)
			{
				e1.printStackTrace();
			}
			catch (ParseXMLException e1)
			{
				e1.printStackTrace();
			}
			catch (SecurityException e1)
			{
				Log.error("set方法作用域异常", e1);
			}
			catch (IllegalArgumentException e1)
			{
				Log.error("param.xml中属性类型与Part属性类型不匹配", e1);
			}
			catch (NoSuchMethodException e1)
			{
				Log.error("param.xml中属性找不到对应Part的set方法异常", e1);
			}
			catch (IllegalAccessException e1)
			{
				Log.error("param.xml中属性对应Part的set方法参数传递异常", e1);
			}
			catch (InvocationTargetException e1)
			{
				Log.error("param.xml中节点对应Part对象实例化异常", e1);
			}
		}
	}
	
	public static void start()
	{
		try
		{
			if (gui == null)
				gui = new GUI();
		}
		catch (UnsupportedEncodingException e)
		{
			Log.error("初始化界面失败", e);
		}
	}
}
