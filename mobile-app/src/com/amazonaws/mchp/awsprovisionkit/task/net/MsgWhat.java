package com.amazonaws.mchp.awsprovisionkit.task.net;

/**
 * 多线程 Handler Message通讯定义 1001 ~ 1009 TCP 消息 2001 ~ 2009 UDP 消息 3001 ~ 3009
 * 其它消息
 */
public class MsgWhat {
	/**
	 * UDP监听到有新接入
	 */
	public static final int UDP_NEW_PLUG_IN = 2001;

	/**
	 * 设备掉线
	 */
	public static final int PLUG_OFF_LINE = 2002;

	/**
	 * 打印日志到UI
	 */
	public static final int PRINT_LOG = 3001;

	/**
	 * 发送OTAU升级进度
	 */
	public static final int OTAU_PROGRESS = 4001;
	
	/**
	 * 温度预警
	 */
	public static final int TEMP_WARNING = 2003;
	
	/**
	 * 温度预警
	 */
	public static final String ACTION_PLUG_TEMP_WARNING = "com.smartplug.plug_twarning";

	/**
	 * 新设备接入
	 */
	public static final String ACTION_NEW_PLUG_IN = "com.smartplug.new_plug_in";
	/*
	 * 设备掉线
	 */
	public static final String ACTION_PLUG_OFF_LINE = "com.smartplug.plug_off_line";
	/**
	 * 打印日志
	 */
	public static final String ACTION_PRINT_LOG = "com.smartplug.print_log";
	
	/**
	 * OTAU升级 任务执行进度
	 */
	public static final String ACTION_OTAU_PROGRESS = "com.smartplug.progress_bar";
	
}
