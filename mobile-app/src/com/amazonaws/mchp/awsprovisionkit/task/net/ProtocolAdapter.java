package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * 数据协议适配器， 注意： 1. 数字高位在前，低位在后， 2.字符串从向后
 *
 */
public class ProtocolAdapter {
	/**
	 * 尝试启用加密 并 记录SessionKey
	 * 
	 * @param exPubKey64
	 * @param random
	 */
	public void trySaveSessionKey(byte[] exPubKey64, byte[] random) {
		if (this.m_enableAES)
			return;
		//this.m_sessionKey = ECCrypto.single().getSessionKey(exPubKey64, random);
		if (null != this.m_sessionKey && this.m_sessionKey.length > 0) {
			this.setEnableAES(true);
		}
	}

	/**
	 * 创建完整数据包
	 * 
	 * @param cmdId
	 *            命令ID
	 * @param parameters
	 *            参数数据
	 * @return 完整数据包
	 */
	byte[] getPackedData(byte cmdId, byte[] parameters) {
		byte[] data = new byte[SF.fiParamStart + parameters.length + SF.lenCRC];
		data[SF.fiSOf] = SF.SOF;
		data[SF.fiCmdID] = cmdId;
		int len = (data.length - SF.fiDataLenH - 1 - SF.lenCRC);
		byte[] bb = MyHelper.intToByte(len);
		data[SF.fiDataLenL] = bb[0];
		data[SF.fiDataLenH] = bb[1];
		data[SF.fiSequence] = this.getSequence();

		for (int i = 0; i < parameters.length; i++) {
			data[SF.fiParamStart + i] = parameters[i];
		}

		// 对数据包做预处理
		data = ProtocolHelper.tryPackage(data, this);
		return data;
	}

	/**
	 * 解析数据包
	 * 
	 * @param data
	 *            要解析的数据包
	 * @return 返回解析的结果对象
	 */
	public static MsgData tryParse(byte[] data, byte[] random, ProtocolAdapter protocol) {
		MsgData item = null;
		try {
			data = ProtocolHelper.tryUnPack(data, random, protocol);
			item = new MsgData(data);
			item.tryParse();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			item = new MsgData(data);
			item.setError(e.getMessage());
		}

		return item;
	}

	/**
	 * 构建发现设备的数据包
	 * 
	 * @return 完成数据包
	 * @throws UnsupportedEncodingException
	 */
	public byte[] makeDiscoveryReq() {

		byte[] parms = MyHelper.asciiToBytes("Atmel_WiFi_Discovery");
		byte[] data = getPackedData(SF.cmdDiscovery, parms);

		return data;
	}

	/**
	 * 获取是否启用AES加密
	 * 
	 * @return
	 */
	public Boolean getEnableAES() {
		return this.m_enableAES;
		// return false;
	}

	/**
	 * 设置是否启用AES加密
	 * 
	 * @param enableAES
	 */
	public void setEnableAES(Boolean enableAES) {
		this.m_enableAES = enableAES;
	}

	/**
	 * 获取会话 key
	 * 
	 * @return
	 */
	public byte[] getSessionKey() {
		return this.m_sessionKey;
	}

	/**
	 * 获取IV - 加密向量
	 * 
	 * @return
	 */
	public byte[] getIV() {
		return this.m_IV;
	}

	/**
	 * 通讯序号，用于匹配数据包
	 */
	private byte m_sequence = 0;
	/**
	 * 是否启用AES加密，true=发送数据包启用AES
	 */
	private Boolean m_enableAES = false;

	/**
	 * 会话KEY，用于AES加密
	 */
	private byte[] m_sessionKey = null;

	/**
	 * 加密向量IV
	 */
	private byte[] m_IV = null;

	/**
	 * 查看当前Sequence
	 * 
	 * @return
	 */
	public byte peekSequence() {
		return this.m_sequence;
	}

	/**
	 * 获取通讯序号，从1开始，自动加1
	 * 
	 * @return 序号
	 */
	public byte getSequence() {
		return ++m_sequence;
	}

	/**
	 * 构建发现设备的数据包
	 * 
	 * @return 完成数据包
	 * @throws UnsupportedEncodingException
	 */
	public byte[] makeDiscoveryATC(String wifiSsid, String password, WifiSEC securityType, String uuid) {
		String ssid = wifiSsid; // "mytest81";
		String pwd = password; // "11111111";
		String devName = "kitchen";	// To Do: Hardcord the device name as "light" for test
		byte secType = securityType.getValue();

		byte[] parms = MyHelper.asciiToBytes("CONFIG=0" + ssid + "0" + pwd + "0" + "0" + uuid + "0" + devName);
		int ii = 7;
		int ssidLen = ssid.length();
		parms[ii] = (byte) ssidLen;
		parms[ii + 1 + ssidLen] = (byte) pwd.length();
		parms[ii + 1 + ssidLen + 1 + pwd.length()] = secType;
		parms[ii + 1 + ssidLen + 1 + pwd.length() + 1 ] = (byte)uuid.length();
		parms[ii + 1 + ssidLen + 1 + pwd.length() + 1 + uuid.length() + 1] = (byte)devName.length();

		byte[] data = getPackedData(SF.cmdDiscovery, parms);

		return data;
	}

	/**
	 * 构建发现设备的数据包
	 * 
	 * @return 完成数据包
	 */
	public byte[] makeDiscoveryATZ() {

		byte[] parms = MyHelper.asciiToBytes("CONDONE");
		byte[] data = getPackedData(SF.cmdDiscovery, parms);

		return data;
	}

	public ProtocolAdapter() {
		this.m_IV = MyHelper.genRandom(16);
	}

	/**
	 * 查询MAC地址
	 * 
	 * @return 完成数据包
	 */
	public byte[] makeQueryMAC() {
		byte[] cid = MyHelper.intToByte(SF.cidMAC);
		// CID=0x00fd, cluster index=0, attribute=
		byte[] parms = new byte[] { cid[0], cid[1], 0, 2 };
		byte[] data = getPackedData(SF.cmdQueryAttr, parms);

		return data;
	}


	/**
	 * 查询设备温度 Device Temperature 0x0003, Query Cluster
	 * 
	 * @return 数据包
	 */
	public byte[] makeQueryTemp() {
		/*
		 * format = CID=0x0003, cluster index=0, attribute1=[1] attributes
		 */
		byte[] cid = MyHelper.intToByte(SF.cidDevTemp);
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

}
