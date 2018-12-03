package com.amazonaws.mchp.awsprovisionkit.task.net;

import com.amazonaws.mchp.awsprovisionkit.model.DeviceModel;

/**
 * 常量定义
 */
public final class MyConfig {

	static {
		S_NEW_LINE = System.getProperty("line.separator", "\n");
	}

	/**
	 * 换行符号
	 */
	public static final String S_NEW_LINE;

	/**
	 * 空白符号
	 */
	public static final String S_EMPTY = "-";

	/**
	 * APP-HTTP 端口，用于OTAU
	 */
	public static final int APP_HTTP_PORT = 8890; // 预留 8890

	/**
	 * 插座 AP 模式 IP地址
	 */
	public static final String PLUG_AP_IP = "192.168.1.1";

	/**
	 * 插座监听的 TCP 端口
	 */
	public static final int PLUG_TCP_PORT = 8899;

	/**
	 * 插座 监听 的 UDP 端口
	 */
	public static final int PLUG_UDP_PORT = 8898;

	/**
	 * APP监听的 UDP 端口
	 */
	public static final int APP_UDP_PORT = 8897;

	/**
	 * Log.write TAG 日志TAG名称
	 */
	public static final String TAG = "atmel.sp";

	/**
	 * 是否启用CRC校验， 默认={@value}
	 */
	public static final Boolean EnableCrcCheck = true;

	/**
	 * 是否启用加密, 默认={@value}
	 */
	public static final Boolean EnableEncrypt = true;

	/**
	 * 进入模拟模式
	 */
	public static final Boolean InMock = false;

	/**
	 * 连接Device失败（添加设备时）
	 */
	public static final String ERR_ConnectDevFail = "Connect device fail, please try again.";

	/**
	 * Send Provision Data to Device Fail
	 */
	public static final String ERR_SendProvDataFail = "Send Provision data to device get fail, please try again.";

	public static final String ERR_ScanAPFail = "Fail to Scan target AP";
	/**
	 * Execution Success
	 */
	public static final String Success_ConnectDev = " Connect Device Success";
	public static final String Success_SendProvData = " Send Provision Data Success";
	public static final String Success = "SUCCESS";

	/**
	 * Execution Success
	 */
	public static final String Fail = "Fail";

	/**
	 * 新添加的设备
	 */
	public static DeviceModel NewPlug = null;
	
	/**
	 * 是否挂起UDP监听
	 */
	public static Boolean UDP_PENDING = false;
}
