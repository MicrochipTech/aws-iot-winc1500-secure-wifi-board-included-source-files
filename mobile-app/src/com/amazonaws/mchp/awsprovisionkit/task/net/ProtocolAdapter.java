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
	 * 查询电量信息，Query Cluster命令
	 * 
	 * @return 返回查询数据包
	 */
	public byte[] makeQueryEnergy() {
		/*
		 * format = CID=0x6000, cluster index=0, attribute1=[1] attributes:
		 * 1.out type, 2.out voltage, 3.out current, 4.out power, 5.active
		 * energy
		 */
		byte[] cid = MyHelper.intToByte(SF.cidEnergy);
		// CID + Cluster Index
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

	/**
	 * 查询历史数据
	 * 
	 * @param cindex
	 *            0=on/off, 1=TEMP温度, 2 = power info电量信息
	 * @return
	 */
	public byte[] makeQueryHistory(byte cindex) {
		byte[] cid = MyHelper.intToByte(SF.cidHistory);
		byte[] parms = new byte[] { cid[0], cid[1], cindex };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

	/**
	 * 查询产品信息, Query Cluster
	 * 
	 * @return 返回查询数据包
	 */
	public byte[] makeQueryProduct() {
		/*
		 * format = CID=0x00FE, cluster index=0, attribute1=[1] attributes:
		 * 1.node type, 2.manufacture id, 3.firmware version, 4.serial number
		 * length, 5.serial number
		 */
		byte[] cid = MyHelper.intToByte(SF.cidProduct);
		// cid2 + c-index=1
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

	/**
	 * 产设备类型 Plug socket standard, Query cluster
	 * 
	 * @return 数据包
	 */
	public byte[] makeQueryStandard() {
		/*
		 * format = CID=0x6001, cluster index=0, attribute1=[1] attributes
		 */
		byte[] cid = MyHelper.intToByte(SF.cidStandard);
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

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

	/**
	 * 查询开关状态
	 * 
	 * @return 数据包
	 */
	public byte[] makeQueryOnOff() {
		byte[] cid = MyHelper.intToByte(SF.cidOnOff);
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

	/**
	 * 查询设备时间信息, Query cluster
	 * 
	 * @return 数据包
	 */
	public byte[] makeQueryTime() {
		/*
		 * format = CID=0x0004, cluster index=0, attribute1=[1] attributes
		 */
		byte[] cid = MyHelper.intToByte(SF.cidTime);
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

	/**
	 * 查询定时信息, Query cluster
	 * 
	 * @param scheduleGroupIndex
	 *            定时组 [0 ~9]
	 * @return 数据包
	 */
	public byte[] makeQuerySchedule(byte scheduleGroupIndex) {
		if (scheduleGroupIndex > 9)
			scheduleGroupIndex = 9;
		byte[] cid = MyHelper.intToByte(SF.cidSchedule);
		byte[] parms = new byte[] { cid[0], cid[1], scheduleGroupIndex };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}

	/**
	 * 控制开关
	 * 
	 * @param isOn
	 *            开关动作，true=开，false=关
	 * @return
	 */
	public byte[] makeSetOnOff(Boolean isOn) {
		byte[] cid = MyHelper.intToByte(SF.cidOnOff);
		byte onOff = (byte) (isOn ? 0x01 : 0x00);
		// cid2=0x0001, cluster index1=0, attribute1=0x01, value1=onOff
		byte[] parms = new byte[] { cid[0], cid[1], 0, SF.atrOnOff, onOff };
		byte[] data = getPackedData(SF.cmdControlAttr, parms);

		return data;
	}

	/**
	 * 同步时间命令, 数据包中包含当前时间，精确到秒， 使用 Control Cluster 命令
	 * 
	 * @return 返回数据包
	 */
	public byte[] makeSetTime() {
		/*
		 * cid2=0x0004, cluster index1=0, attribute1=1byte, attributes: 1. Start
		 * Year[2], 2.start day[1], 3.Time/Calendar mask[1]=0x3f, 4.
		 * Time/Calendar[4], 5.Running time[4] 同步时间，使用 1_2_4 ATTR,
		 * 4.time/calendar= bit[31...0] [Year: 6bit | Month: 4bit | Date: 5bit |
		 * Hour: 5bit | Minute: 6bit | Second: 6bit]
		 * 
		 * version 2 cid + cluster index + attr values
		 */
		byte[] cid = MyHelper.intToByte(SF.cidTime);
		Calendar c = Calendar.getInstance();
		int y = c.get(Calendar.YEAR), M = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DAY_OF_MONTH),
				h = c.get(Calendar.HOUR_OF_DAY), m = c.get(Calendar.MINUTE), s = c.get(Calendar.SECOND),
				w = c.get(Calendar.DAY_OF_WEEK) - 1;

		byte[] startYear = MyHelper.intToByte(y);
		byte[] startDay = MyHelper.intToByte(w);

		// year offset = 0
		int tc = s | (m << 6) | (h << 12) | (d << 17) | (M << 22);
		byte[] dd = MyHelper.intToByte(tc);

		byte[] parms = new byte[] { cid[0], cid[1], 0x00, // CID + cluster index
				startYear[0], startYear[1], // start year = 2,
				startDay[0], // start day 1
				0x00, // time mask
				dd[0], dd[1], dd[2], dd[3], // time
				0x00, 0x00, 0x00, 0x00 // run
		};

		byte[] data = getPackedData(SF.cmdControlCluster, parms);

		return data;
	}

	/**
	 * 设定温度预警上下限[ -200 ~ 200]
	 * 
	 * @param low
	 *            温度下限
	 * @param high
	 *            温度上限
	 * @return 数据包
	 */
	public byte[] makeSetTemp(int low, int high) {
		byte[] cid = MyHelper.intToByte(SF.cidDevTemp);
		byte[] l = MyHelper.shortToByte((short) low);
		byte[] h = MyHelper.shortToByte((short) high);
		// cid2 + c-index1 + AttrValues：temp2 + low2 + hight2
		byte[] parms = new byte[] { cid[0], cid[1], 0, 0, 0, l[0], l[1], h[0], h[1] };
		byte[] data = getPackedData(SF.cmdControlCluster, parms);

		return data;
	}

	/**
	 * 设定定时
	 * 
	 * @param clusterIndex
	 *            定时组信息 0~9, 共支持10组
	 * 
	 * @param dayMask
	 *            bit[7][6][5][4][3][2][1][0]
	 *            [Sun][Sat][Fri][Thu][Wed][Tue][Mon][Reserve d] 0b00: no
	 *            schedule, 0x01: scheduled
	 * @param hour
	 *            小时
	 * @param minute
	 *            分钟
	 * @param isOn
	 *            开关状态 true=开
	 * @return 数据包
	 */
	public byte[] makeSetSchedule(byte clusterIndex, byte dayMask, byte hour, byte minute, Boolean isOn) {
		byte[] cid = MyHelper.intToByte(SF.cidSchedule);
		byte[] tcid = MyHelper.intToByte(SF.cidOnOff);
		byte onoff = isOn ? SF.atvOn : SF.atvOff;
		/*
		 * CID_2b + C-Index + Target{ CID_2b + C-Index_1b + AttrID_1b +
		 * DayMask_1b + time_2b + AttrValue_Xb }
		 */
		byte[] parms = new byte[] { cid[0], cid[1], clusterIndex, // cid2 +
																	// c-index
				// target { CID_2b + C-Index_1b + AttrID_1b }
				tcid[0], tcid[1], 0, SF.atrOnOff, // target cid=ON/OFF-CID
				// dayMask, time, attribute value= on/off
				dayMask, minute, hour, onoff };
		byte[] data = getPackedData(SF.cmdControlCluster, parms);

		return data;
	}

	/**
	 * 构建OTAU控制命令
	 * 
	 * @param cmd
	 * @return 数据包
	 */
	public byte[] makeOTAUCmd(String cmd) {
		byte[] parms = MyHelper.asciiToBytes(cmd);
		byte[] data = getPackedData(SF.cmdOtauControl, parms);
		return data;
	}

	/**
	 * 构建 MCU Data 数据包
	 * 
	 * @param buffer
	 *            数据包内容
	 * @return 数据包
	 */
	public byte[] makeOTAUData(int index, byte[] buffer) {
		byte[] bb = MyHelper.intToByte(index);
		buffer[0] = bb[0];
		buffer[1] = bb[1];
		byte[] data = getPackedData(SF.cmdOtauData, buffer);
		return data;
	}

	/**
	 * 构建 OTAU校验和数据包
	 * 
	 * @param cmd
	 *            命令前缀
	 * @param mcuData
	 *            MCU 数据
	 * @return 数据包
	 */
	public byte[] makeOTAUCrc(String cmd, byte[] mcuData) {
		byte[] b1 = MyHelper.asciiToBytes(cmd);
		byte[] b2 = ProtocolHelper.calcCRC32(mcuData, 0, mcuData.length);
		byte[] parms = new byte[b1.length + 4];
		System.arraycopy(b1, 0, parms, 0, b1.length);
		System.arraycopy(b2, 0, parms, b1.length, 4);

		byte[] data = getPackedData(SF.cmdOtauControl, parms);
		return data;
	}

	/**
	 * v222, 请求验证
	 * 
	 * @return
	 */
	public byte[] makeAUTHREQ() {
		byte[] parms = MyHelper.asciiToBytes("AUTHREQ");
		byte[] data = getPackedData(SF.cmdAuthReq, parms);

		return data;
	}

	/**
	 * v222, 生成32字节随机数
	 * 
	 * @return
	 */
	public byte[] makeRAMCHAL(byte[] rand) {
		byte[] bpre = MyHelper.asciiToBytes("RAMCHAL");
		byte[] parms = new byte[bpre.length + rand.length];

		System.arraycopy(rand, 0, parms, bpre.length, rand.length);
		System.arraycopy(bpre, 0, parms, 0, bpre.length);

		byte[] data = getPackedData(SF.cmdAuthReq, parms);

		return data;
	}

	/**
	 * v222, 随机数签名结果
	 * 
	 * @return
	 */
	public byte[] makeRAMCHAL82(byte[] randSig, byte[] token) {
		String cmd = "RANCRID=";
		if (null == token) {
			token = new byte[0];
			cmd = "RAMCHAL=";
		}
		byte[] bpre = MyHelper.asciiToBytes(cmd);
		byte[] parms = new byte[bpre.length + randSig.length + token.length];

		System.arraycopy(token, 0, parms, bpre.length + randSig.length, token.length);
		System.arraycopy(randSig, 0, parms, bpre.length, randSig.length);
		System.arraycopy(bpre, 0, parms, 0, bpre.length);

		byte[] data = getPackedData(SF.cmdAuthRep, parms);

		return data;
	}

	/**
	 * v222, 生成APP - Public Key
	 * 
	 * @return
	 */
	public byte[] makePublicKey(byte[] pubKey) {
		byte[] bpre = MyHelper.asciiToBytes("PUBKEY");
		byte[] parms = new byte[bpre.length + pubKey.length];

		System.arraycopy(pubKey, 0, parms, bpre.length, pubKey.length);
		System.arraycopy(bpre, 0, parms, 0, bpre.length);

		byte[] data = getPackedData(SF.cmdAuthReq, parms);

		return data;
	}

	/**
	 * 拼RAMSHARE数据包，把分享用的随机数发给 Plug
	 * 
	 * @param randnum
	 * @return
	 */
	public byte[] makeShareRAMSHARE02(byte[] randnum) {
		byte[] bpre = MyHelper.asciiToBytes("RAMSHARE");
		byte[] parms = new byte[bpre.length + randnum.length];

		System.arraycopy(randnum, 0, parms, bpre.length, randnum.length);
		System.arraycopy(bpre, 0, parms, 0, bpre.length);

		byte[] data = getPackedData(SF.cmdAuthReq, parms);

		return data;
	}

	public byte[] makeShareACCREQ02(byte[] shareRandom, byte[] shareSig, byte[] myPubKey, byte[] mySig) {
		byte[] bpre = MyHelper.asciiToBytes("ACCREQ");
		byte[] parms = new byte[bpre.length + shareRandom.length + shareSig.length + myPubKey.length + mySig.length];

		System.arraycopy(bpre, 0, parms, 0, bpre.length);
		System.arraycopy(shareRandom, 0, parms, bpre.length, shareRandom.length);
		System.arraycopy(shareSig, 0, parms, bpre.length + shareRandom.length, shareSig.length);
		System.arraycopy(myPubKey, 0, parms, bpre.length + shareRandom.length + shareSig.length, myPubKey.length);
		System.arraycopy(mySig, 0, parms, bpre.length + shareRandom.length + shareSig.length + myPubKey.length,
				mySig.length);

		byte[] data = getPackedData(SF.cmdAuthReq, parms);

		return data;
	}

	public byte[] makeShareDelete(byte[] random) {
		byte[] bpre = MyHelper.asciiToBytes("DELSHARE");
		byte[] parms = new byte[bpre.length + random.length];

		System.arraycopy(bpre, 0, parms, 0, bpre.length);
		System.arraycopy(random, 0, parms, bpre.length, random.length);

		byte[] data = getPackedData(SF.cmdAuthReq, parms);

		return data;
	}

	public byte[] makeQueryStatus() {
		byte[] cid = MyHelper.intToByte(SF.cidSysStatus);
		byte[] parms = new byte[] { cid[0], cid[1], 0 };
		byte[] data = getPackedData(SF.cmdQueryCluster, parms);

		return data;
	}
}
