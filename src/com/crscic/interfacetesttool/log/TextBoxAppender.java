package com.crscic.interfacetesttool.log;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.crscic.interfacetesttool.gui.GUI;

/**
 * 
 * @author ken_8 2017年10月6日 下午11:32:47
 */
@Plugin(name = "TextBoxAppender", category = "Core", elementType = "appender", printObject = true)
public class TextBoxAppender extends AbstractAppender
{

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock =  rwLock.readLock();

	protected TextBoxAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
			final boolean ignoreExceptions)
	{
		super(name, filter, layout, ignoreExceptions);
	}

	@Override
	public void append(LogEvent event)
	{
		readLock.lock();
        try {
            final byte[] bytes = getLayout().toByteArray(event);//日志二进制文件，输出到指定位置就行
            //下面这个是要实现的自定义逻辑
            GUI.outputText.append(new String(bytes));
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            readLock.unlock();
        }
	}

	@PluginFactory
	public static TextBoxAppender createAppender(@PluginAttribute("name") String name,
			@PluginElement("Filter") final Filter filter,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions)
	{
		if (name == null)
		{
			LOGGER.error("No name provided for MyCustomAppenderImpl");
			return null;
		}
		if (layout == null)
		{
			layout = PatternLayout.createDefaultLayout();
		}
		return new TextBoxAppender(name, filter, layout, ignoreExceptions);
	}
}
