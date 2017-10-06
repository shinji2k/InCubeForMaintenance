package com.crscic.interfacetesttool.gui;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import com.crscic.interfacetesttool.log.Log;

/**
 * 
 * @author ken_8 2017年10月1日 下午9:50:25
 */
public final class GUI
{
	private static Display display = Display.getDefault(); // 1.创建一个Display
	private static Shell shell = new Shell();
	private static Label iamLabel = new Label(shell, SWT.NONE);
	private static final Combo iamCombo = new Combo(shell, SWT.DROP_DOWN);
	private static Label connToLabel = new Label(shell, SWT.NONE);
	private static final Combo connToCombo = new Combo(shell, SWT.NONE);
	private static Button goButton = new Button(shell, SWT.CENTER);
	public static Text outputText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);

	public GUI() throws UnsupportedEncodingException
	{
		shell.setSize(800, 600);
		shell.setText("接口调试工具");
		shell.setLayout(new GridLayout(5, false));

		iamLabel.setText("我是");

		// 实例化控件，布局中的控件顺序是根据实例化的顺序决定的
		connToLabel.setText("连接");

		// 初始化
		iamCombo.add("平台");
		iamCombo.add("RTU");
		iamCombo.add("串口设备");
		iamCombo.setData("0", "config/platform.xml");
		iamCombo.setData("1", "config/rtu.xml");
		iamCombo.setData("2", "config/device.xml");
		iamCombo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String key = "" + iamCombo.getSelectionIndex();
				String value = iamCombo.getText();
				String data = (String) iamCombo.getData(key);
//				outputText.append("key:" + key + "\tvalue:" + value + "\tdata:" + data);
				Log.info("key:" + key + "\tvalue:" + value + "\tdata:" + data);
			}
		});

		goButton.setText("开始");
		// new Label(shell, SWT.NONE);
		// new Label(shell, SWT.NONE);
		// new Label(shell, SWT.NONE);
		// new Label(shell, SWT.NONE);
		// new Label(shell, SWT.NONE);

		GridData gd_outputText = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
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

		// Frame f = new Frame("接口调试工具");// 构造一个新的、最初不可见的、具有指定标题的 Frame 对象。
		//
		// f.setSize(500, 400);// 设置窗口大小,宽度500，高度400
		// f.setLocation(300, 200);// 设置窗口位置为距离屏幕左边水平方向300，上方垂直方向200
		// f.setVisible(true);// 设置窗体可见。
		// f.setLayout(new FlowLayout());// 设置窗体布局为流式布局。
		// String buttonStr = new String("开始".getBytes(), "GBK");
		// Button b = new Button(buttonStr);// 在窗口中添加一个按钮；
		// f.add(b);// 将按钮添加到窗口内；
	}
}
