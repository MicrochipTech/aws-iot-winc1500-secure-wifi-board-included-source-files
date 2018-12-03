package com.amazonaws.mchp.awsprovisionkit.task.net;

/**
 * Static Final 常量定义
 */
public final class SF {
	/**
	 * SOF 值 = {@value}
	 */
	public static final byte SOF = 0x5a;

	/**
	 * Format Index SOF, SOF的索引位置
	 */
	public static final byte fiSOf = 0;

	/**
	 * 数据包格式 Data Length 的低位
	 */
	public static final byte fiDataLenL = 1;

	/**
	 * 数据包格式 Data Length 的高位
	 */
	public static final byte fiDataLenH = 2;

	/**
	 * 数据包序列
	 */
	public static final byte fiSequence = 3;

	/**
	 * 数据包格式 Command ID 索引位
	 */
	public static final byte fiCmdID = 4;

	/**
	 * 数据包格式 Parameters 开头位置
	 */
	public static final byte fiParamStart = 9;

	/**
	 * CRC的字节长度
	 */
	public static final byte lenCRC = 4;

	/**
	 * Device Id 字节长度，设备类型
	 */
	public static final byte lenDevId = 2;

	/**
	 * MAC 字节长度
	 */
	public static final byte lenMacAddr = 2;

	/**
	 * 命令，请求 发现设备
	 */
	public static final byte cmdDiscovery = 0x01;

	/**
	 * 命令，发现设备 返回结果
	 */
	public static final byte cmdDiscoveryR = (byte) 0x81;

	/**
	 * 命令，发现设备 返回结果
	 */
	public static final byte cmdReportErrCode = (byte) 0x9c;
	
	/**
	 * 命令，QueryALL - 查询全部, Parameters=0xAA55
	 */
	public static final byte cmdQueryAll = (byte) 0x10;

	/**
	 * 命令，QueryAttr - 查询属性，Parameters=CID + Cluster index (n) + Attribute ID
	 */
	public static final byte cmdQueryAttr = (byte) 0x11;

	/**
	 * 命令，QueryCluster - 查询属性集，CID + Cluster index (n)
	 */
	public static final byte cmdQueryCluster = (byte) 0x12;

	/**
	 * 命令，Report All - 上报所有属性，CID 1 + Number of Clusters + attribute value + ...
	 * CID n + Number of Clusters+ attribute value
	 */
	public static final byte cmdReportAll = (byte) 0x90;

	/**
	 * 命令，Report ATTR - 上报属性，CID + Cluster index (n) + Attribute ID + attribute
	 * value
	 */
	public static final byte cmdReportAttr = (byte) 0x91;

	/**
	 * 命令，Report Cluster - CID + Cluster index (n) + attribute value
	 */
	public static final byte cmdReportCluster = (byte) 0x92;

	/**
	 * 命令，Control ALL - CID = 0xAA55, Reset all attributes to default value
	 */
	public static final byte cmdControlAll = (byte) 0x20;

	/**
	 * 命令，Control Attribute - CID + Cluster index (n) + Attribute ID + attribute
	 * value, Change the specified attribute with received attribute value.
	 */
	public static final byte cmdControlAttr = (byte) 0x21;

	/**
	 * 命令，Control Cluster - CID + Cluster index (n) + attribute values,
	 */
	public static final byte cmdControlCluster = (byte) 0x22;

	/**
	 * 命令，OTAU - 交互 CMD
	 */
	public static final byte cmdOtauControl = (byte) 0x29;
	/**
	 * 命令，OTAU - 数据传输 CMD
	 */
	public static final byte cmdOtauData = (byte) 0x28;

	/**
	 * 命令，OTAU - 数据传输 CMD Response
	 */
	public static final byte cmdOtauControlRe = (byte) 0xA9;
	/**
	 * 命令，OTAU - 数据传输 CMD Response
	 */
	public static final byte cmdOtauDataRe = (byte) 0xA8;

	/**
	 * 开关CID
	 */
	public static final int cidOnOff = 0x0001;
	/**
	 * 设备温度 CID
	 */
	public static final int cidDevTemp = 0x0003;
	/**
	 * 时间信息 CID
	 */
	public static final int cidTime = 0x0004;
	/**
	 * 定时信息 CID
	 */
	public static final int cidSchedule = 0x0005;
	/**
	 * 获取历史 CID
	 */
	public static final int cidHistory = 0x000A;
	/**
	 * MAC地址 CID
	 */
	public static final int cidMAC = 0x00FD;
	/**
	 * 产品信息 CID
	 */
	public static final int cidProduct = 0x00FE;
	/**
	 * 电量信息 CID
	 */
	public static final int cidEnergy = 0x0600;
	/**
	 * 插座标准信息 CID
	 */
	public static final int cidStandard = 0x0601;
	
	/**
	 * 设备状态状态
	 */
	public static final byte cidSysStatus = (byte) 0x0B;


	/**
	 * 开关Attribute = 01
	 */
	public static final byte atrOnOff = 0x01;

	/**
	 * 打开开 Attribute Value= 01
	 */
	public static final byte atvOn = 0x01;

	/**
	 * 关闭 Attribute value = 00
	 */
	public static final byte atvOff = 0x00;
	
	/**
	 * v222, 请求验证
	 */
	public static final byte cmdAuthReq = 0x02;
	
	/**
	 * v222, 验证回复
	 */
	public static final byte cmdAuthRep = (byte)0x82;
}
