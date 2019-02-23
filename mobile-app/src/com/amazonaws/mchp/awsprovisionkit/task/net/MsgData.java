package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.net.InetAddress;
import java.util.List;

/**
 * 数据包信息
 */
public class MsgData {
	public MsgData() {
	}

	public MsgData(byte[] rawData) {
		this.m_data = rawData;
	}

	/**
	 * 原始数据
	 */
	private byte[] m_data = null;
	/**
	 * 是否有错误信息
	 */
	private Boolean m_hasErr = false;
	/**
	 * 错误信息
	 */
	private String m_error = null;

	/**
	 * 插座设备的IP地址
	 */
	private InetAddress m_plugIp = null;

	/**
	 * 获取插座的IP地址
	 * 
	 * @return 插座IP
	 */
	public InetAddress getPlugIp() {
		return m_plugIp;
	}

	/**
	 * 记录插座的IP地址
	 * 
	 * @param ip
	 *            IP地址
	 */
	public void setPlugIp(InetAddress ip) {
		this.m_plugIp = ip;
	}

	/**
	 * 设置错误信息
	 * 
	 * @param error
	 *            错误信息
	 */
	public void setError(String error) {
		this.m_error = error;
		this.m_hasErr = (error != null && error.length() > 0);
	}

	/**
	 * 获取错误信息
	 * 
	 * @return 返回错误信息
	 */
	public String getError() {
		return this.m_error;
	}

	/**
	 * 是否出现错误
	 * 
	 * @return true=出错了，false=未出错
	 */
	public Boolean hasError() {
		return m_hasErr;
	}

	/**
	 * 获取原始数据包
	 */
	public byte[] getData() {
		return m_data;
	}

	/**
	 * =DataLength 数据包长度，不包含SOF、DataLength、CRC
	 */
	public int DataLength = 0;

	/**
	 * 命令ID
	 */
	public byte CmdId = 0;

	/**
	 * 设备类型ID，0x两个字节
	 */
	public String DevId = null;

	/**
	 * MAC地址，0x6个字节
	 */
	public String MAC = null;

	public String ThingID = null;

	/**
	 * 文本消息（Parameters），应用场景：入网激活，OTAU升级
	 */
	public String Message = null;

	/**
	 * 错误代码
	 */
	public String ErrorCode = null;

	/**
	 * CID, Cluster Identifier, 命令识别码
	 */
	public int CID = 0;

	/**
	 * 开关状态
	 */
	public Boolean OnOff = null;

	/**
	 * 设备当前温度 [-200 ~ 200]
	 */
	public int TempCurrent = 0;
	/**
	 * 设备预警-最低温度 [-200 ~ 200]
	 */
	public int TempLow = 0;
	/**
	 * 设备预警-最高温度 [-200 ~ 200]
	 */
	public int TempHigh = 0;

	/**
	 * 插座标准 美标0x01: US， 欧标0x02: EU， 中国0x03: China
	 */
	public String PlugStandard = null;

	/**
	 * Product NodeType, 设备节点类型
	 */
	public int pNodeType = 0;

	/**
	 * Product ManufacturerId, 设备ManufacturerID
	 */
	public int pManufacturerId = 0;

	/**
	 * Product Firmware Version 主版本号, 设备 MCU版本号
	 */
	public byte pVersionMain = 0;

	/**
	 * Product Firmware Version 子版本号, 设备 MCU版本号
	 */
	public byte pVersionSub = 0;

	/**
	 * 产品序列号
	 */
	public String pSerialNo = null;

	/**
	 * 供电输出类型 = AC/DC , AC：交流，DC：直流
	 */
	public String eOutputType = null;

	/**
	 * 输出电压, AC单位V，DC单位 1/10 V
	 */
	public int eOutputVoltage = 0;

	/**
	 * 输出电流,单位 mA
	 */
	public int eOutputCurrent = 0;

	/**
	 * 瞬时耗电 = 当前输出功率，单位 1/10
	 */
	public float eOutputPower = 0;

	/**
	 * 累计耗电量（每次读取后，设备都会重新累计耗电）
	 */
	public float eActiveEnergy = 0;

	/**
	 * Plug时间 - 开始年份
	 */
	public int cStartYear = 0;

	/**
	 * 继电器粘连错误
	 */
	public boolean statusOnOffERR = false;

	/**
	 * 尝试解析内部的数据包，解析失败时记录错误信息，可以通过getError方法获取到错误信息
	 * 
	 * @return true = success to parse, false = fail to parse
	 */
	public Boolean tryParse() {
		byte[] bb = this.m_data;
		this.CmdId = bb[SF.fiCmdID];
		this.DataLength = MyHelper.byteToInt(new byte[] { bb[SF.fiDataLenL], bb[SF.fiDataLenH], 0, 0x01, 0x00 });
		int parametersLen = bb.length - SF.fiParamStart - SF.lenCRC;

		MyHelper.d("tryParse: CmdId="+ this.CmdId);
		switch (this.CmdId) {
		case SF.cmdDiscoveryR: // response of discovery command
			MyHelper.d("tryParse: log1");
			if (this.DataLength < 13) {
				MyHelper.d("tryParse: log2");
				this.Message = MyHelper.bytesToAscii(bb, SF.fiParamStart, 3);
				if (!this.Message.equals("+ok")) {
					this.setError("返回结果错误");
					MyHelper.d("tryParse: log3");
				}
				break;
			} else {
				//this.DevId = MyHelper.bytesToHexText(bb, SF.fiParamStart, 2);
				this.MAC = MyHelper.bytesToHexText(bb, SF.fiParamStart, 6);
				this.ThingID = MyHelper.bytesToAscii(bb, SF.fiParamStart+6, 40);
				MyHelper.d("MAC=="+bb[9]+" "+bb[10]+" "+bb[11]+" "+bb[12]+" "+bb[13]);


			}
			break;



		case SF.cmdReportErrCode: // error happen
			this.ErrorCode = MyHelper.bytesToHexText(bb, SF.fiParamStart, parametersLen);
			this.setError("Erroed code is " + this.ErrorCode);
			return false;
		case SF.cmdOtauControlRe:
		case SF.cmdOtauDataRe:
			this.Message = MyHelper.bytesToAscii(bb, SF.fiParamStart, 3);
			if (!this.Message.equals("+ok"))
				this.setError("Need [+ok] response");
			break;

		default:
			this.setError("Unknow command:" + this.CmdId);
			return false;
		}

		return true;
	}


	/**
	 * Pub-d= Device Public Key
	 */
	public byte[] cpPubD;
	/**
	 * Sig-d= Device Signature by Signer (Priv-s)
	 */
	public byte[] cpSigD;
	/**
	 * Pub-s= Signer Public Key
	 */
	public byte[] cpPubS;
	/**
	 * Sig-s= Signer Signature by Root (Priv-root)
	 */
	public byte[] cpSigS;

	/**
	 * Signature of Random Number used for Authentication
	 */
	public byte[] cpAuthSig;

	/**
	 * 随机数
	 */
	public byte[] cpRandnum;
}
