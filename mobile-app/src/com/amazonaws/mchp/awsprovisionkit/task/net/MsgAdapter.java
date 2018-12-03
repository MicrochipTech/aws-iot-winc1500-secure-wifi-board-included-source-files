package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Socket 适配器， 执行通讯操作，建立长连接
 *
 */
public class MsgAdapter extends MsgBase {

	/**
	 * 是否温度过高
	 */
	public Boolean IsOverheat = false;

	/**
	 * 开关状态， Ture=开，false=关
	 */
	public Boolean IsPowerOn = false;

	public MsgAdapter(String plugIp) {
		try {
			this.initSocket(InetAddress.getByName(plugIp));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			MyHelper.e(">>>>----8899 Unknow plugIp: " + plugIp);
			e.printStackTrace();
		}
	}

	public MsgAdapter(InetAddress plugIp) {
		this.initSocket(plugIp);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	/**
	 * 关闭链接
	 */
	@Override
	public void close() throws IOException {
		super.close();
	}

	/**
	 * 发现设备，获取MAC地址, WIFI=mytest81, password=11111111, security=WPA
	 * 
	 * @param step
	 *            顺序步骤，1=获取MAC地址，2=发送SSID信息，3=完成
	 * @return 返回接收的数据包
	 */
	public MsgData tryDiscovery(int step) {
		return tryDiscovery(step, null, null, null, null);
	}

	/**
	 * 发现设备，获取MAC地址
	 * 
	 * @param step
	 *            顺序步骤，1=获取MAC地址，2=发送SSID信息，3=完成
	 * @param ssid
	 *            家里WIFI - SSID
	 * @param password
	 *            WIFI密码
	 * @param securityType
	 *            安全类型
	 * @return 返回接收的数据包
	 */
	public MsgData tryDiscovery(int step, String ssid, String password, WifiSEC securityType, String uuid) {
		byte[] data = null;
		switch (step) {
		case 1: // get the mac address from the device
				data = protocol.makeDiscoveryReq();
			break;
		case 2: // send CONFIG=SSID message to configure the network
			if (null == this.getMAC() || this.getMAC().isEmpty()) {
				return buildErrorMsg("The device might be disconnected.");
			}
			data = protocol.makeDiscoveryATC(ssid, password, securityType, uuid);
			break;
		case 3: // send CONDONE message to end the process
			MyHelper.d(">>>>>>>>makeDiscoveryATZ");
			data = protocol.makeDiscoveryATZ();
			break;

		default:
			break;
		}

		MsgData item = doSendAdnRead(data);

		if (step == 3 && !item.hasError()) {
			// 记录第一次的随机数
			String mac = this.getMAC();
			if (mac != null) {
				byte[] token = this.Random;
				MyHelper.saveToken(mac, token, true);
			}
			try {
				Thread.sleep(500); // wait 0.5 sec to close the socket
				this.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (step == 1 && !item.hasError()) {
			// 标记MAC地址，用于后面记录随机数
			this.setMAC(item.MAC);
			if (item.MAC == null)
				item.setError("Read MAC address failed");
		}
		return item;
	}

	/**
	 * 打开设备
	 * 
	 * @return 执行结果数据包信息
	 */
	public MsgData tryOn() {
		byte[] data = protocol.makeSetOnOff(true);
		MsgData item = doSendAdnRead(data);
		if (!item.hasError())
			this.IsPowerOn = true;
		return item;
	}

	/**
	 * 关闭设备
	 * 
	 * @return 执行结果数据包
	 */
	public MsgData tryOff() {
		byte[] data = protocol.makeSetOnOff(false);
		MsgData item = doSendAdnRead(data);
		if (!item.hasError())
			this.IsPowerOn = false;
		return item;
	}

	/**
	 * 获取插座 MAC地址
	 * 
	 * @return 返回Plug-MAC地址
	 */
	public MsgData tryGetMAC() {
		byte[] data = protocol.makeQueryMAC();
		MsgData item = doSendAdnRead(data);
		if (!item.hasError())
			this.m_macAddr = item.MAC;
		return item;
	}

	/**
	 * 获取耗电信息，可以通过MsgData.eOut.... 获取结果
	 * 
	 * @return 返回结果
	 */
	public MsgData tryGetEnergy() {
		byte[] data = protocol.makeQueryEnergy();
		MsgData item = doSendAdnRead(data);
		return item;
	}

	/**
	 * 读取温度 Temperature, 可以通过MsgData.Temp.... 获取结果
	 * 
	 * @return 返回结果
	 */
	public MsgData tryGetTemperature() {
		byte[] data = protocol.makeQueryTemp();
		MsgData item = doSendAdnRead(data);

		if (!item.hasError()) {
			this.IsOverheat = item.TempCurrent >= item.TempHigh;
		}

		return item;
	}

	/**
	 * 读取 插座 类型
	 * 
	 * @return 返回结果
	 */
	public MsgData tryGetStandard() {
		byte[] data = protocol.makeQueryStandard();
		MsgData item = doSendAdnRead(data);
		return item;
	}

	/**
	 * 读取设备时间
	 * 
	 * @return 返回结果
	 */
	public MsgData tryGetTime() {
		byte[] data = protocol.makeQueryTime();
		MsgData item = doSendAdnRead(data);
		return item;
	}

	/**
	 * 读取开关状态
	 * 
	 * @return 返回结果
	 */
	public MsgData tryGetOnOff() {
		byte[] data = protocol.makeQueryOnOff();
		MsgData item = doSendAdnRead(data);
		if (!item.hasError())
			this.IsPowerOn = item.OnOff;
		return item;
	}

	/**
	 * 读取定时信息， 返回1组定时信息， 返回 MsgData.Schedule tDayMask
	 * 
	 * @param scheduleGroupIndex
	 *            定时组索引 [0~9]
	 * @return 返回结果
	 */
	public MsgData tryGetSchedule(byte scheduleGroupIndex) {
		byte[] data = protocol.makeQuerySchedule(scheduleGroupIndex);
		MsgData item = doSendAdnRead(data);
		return item;
	}

	/**
	 * 读取设备信息
	 * 
	 * @return 返回结果
	 */
	public MsgData tryGetProduct() {
		byte[] data = protocol.makeQueryProduct();
		MsgData item = doSendAdnRead(data);
		return item;
	}

	/**
	 * 同步时间信息
	 * 
	 * @return 返回结果
	 */
	public MsgData trySyncTime() {
		byte[] data = protocol.makeSetTime();
		MsgData item = doSendAdnRead(data);
		return item;
	}

	/**
	 * 发送1组定时信息信息
	 * 
	 * @param schedule
	 *            定时计划
	 * @return 处理结果
	 */
	public MsgData trySetTimer(PlugSchedule schedule) {

		byte[] data = protocol.makeSetSchedule(schedule.GroupIndex, schedule.getDayMask(), schedule.Hour,
				schedule.Minute, schedule.OnOff);

		MsgData item = doSendAdnRead(data);
		MyHelper.d("test" + item.CmdId);

		return item;
	}

	/**
	 * 设定温度上下限, low[-200 ~ 200], high[-200 ~ 200]
	 * 
	 * @return 返回结果
	 */
	public MsgData trySetTemperature(int low, int high) {
		byte[] data = protocol.makeSetTemp(low, high);
		MsgData item = doSendAdnRead(data);
		if (!item.hasError()) {
			this.tryGetTemperature();
		}
		return item;
	}

	/**
	 * 更新固件，WIFI 与 MCU同时更新。 备注：由于android禁止使用80端口，而WIFI只有使用80端口更新，所以WIFI更新暂时不做
	 * 
	 * @return 更新结果
	 */
	/*
	public MsgData tryUpgrade(byte[] mcuData) {
		String aOTA_HOST = "OTA_HOST", bCRC_HOST = "CRC_HOST", dRUN_NEWF = "RUN_NEWF";
		// , cOTA_WIFI = "OTA_WIFI";
		// 1. 发送升级命令
		byte[] data = protocol.makeOTAUCmd(aOTA_HOST);
		MsgData r = this.doSendAdnRead(data);
		if (r.hasError())
			return r;

		postMessage(MsgWhat.OTAU_PROGRESS, "1");

		// send mcuData MCU数据包
		int max = 64 * 4, index = 0, start = 0, total = mcuData.length;
		while (true) {
			int len = total - start;
			if (len > max)
				len = max;

			byte[] buffer = new byte[len + 2];
			System.arraycopy(mcuData, start, buffer, 2, len);
			start += len;

			data = protocol.makeOTAUData(index++, buffer);
			r = this.doSendAdnRead(data);
			if (r.hasError())
				return r;

			if (start >= total)
				break;

			if (index % 10 == 0) {
				int percent = (int) (((float) start / (float) total) * 100);
				postMessage(MsgWhat.OTAU_PROGRESS, percent + "");
			}
		}

		postMessage(MsgWhat.OTAU_PROGRESS, "100");

		// 发送MCU_Data_CRC校验和
		data = protocol.makeOTAUCrc(bCRC_HOST, mcuData);
		r = this.doSendAdnRead(data);
		if (r.hasError())
			return r;

		// RUN_NEWF 重启命令
		data = protocol.makeOTAUCmd(dRUN_NEWF);
		r = this.doSendAdnRead(data);
		if (r.hasError())
			return r;

		// 关闭连接
		try {
			String mac2 = this.getMAC();
			MsgMulticast.single().removeAdapterByMAC(mac2);
		} catch (Exception e) {
			MyHelper.e(e.getMessage());
		}
		return r;
	}
	*/

	/**
	 * v222, 校验签名
	 * 
	 * @return
	 */
	/*
	MsgData tryACK() {
		byte[] data = protocol.makeAUTHREQ();
		MsgData item = doSendAdnRead(data);

		if (item.hasError())
			return item;

		// Verify Signer Cert - ECDSA(sha2-Pub-s, Sig-s, Pub-root)
		Boolean verify1 = ECCrypto.single().ecdsaVerify(ECCrypto.RootPubKey, item.cpSigS, item.cpPubS);
		if (!verify1) {
			item.setError("ecdsa 1");
			return item;
		}

		// Verify Device Cert - ECDSA(sha2-Pub-d, Sig-d, Pub-s)
		Boolean verify2 = ECCrypto.single().ecdsaVerify(item.cpPubS, item.cpSigD, item.cpPubD);
		if (!verify2) {
			item.setError("ecdsa 2");
			return item;
		}

		// ramchal
		byte[] randnum = this.Random;
		data = protocol.makeRAMCHAL(randnum);
		MsgData item2 = doSendAdnRead(data);

		if (item2.hasError())
			return item2;

		// Verify Device - ECDSA(sha2-randNum1, authSig, Pub-d)
		Boolean verify3 = ECCrypto.single().ecdsaVerify(item.cpPubD, item2.cpAuthSig, randnum);
		if (!verify3) {
			item2.setError("ecdsa 3");
			return item2;
		}

		byte[] pubKey = ECCrypto.single().DevicePubKey;
		printLog("session key=", pubKey);

		data = protocol.makePublicKey(pubKey);
		MsgData item3 = doSendAdnRead(data);
		if (item3.hasError() || !item3.Message.startsWith("+ok")) {
			item3.setError(item3.getError() + ", or not +ok");
		}

		return item3;
	}
	*/

	/**
	 * 验证建立的TCP-IP连接，验证失败返回 false
	 * 
	 * @return
	 */
	/*
	public boolean tryAuthenticity() {
		// 1.send wifi discovery
		try {
			byte[] data = protocol.makeDiscoveryReq();
			MsgData item = this.doSendAdnRead(data);
			if (item.hasError()) {
				MyHelper.e(">>>>----104 Auth discovery error: " + item.getError());
				return false;
			}

			this.Random = item.cpRandnum;
			this.setMAC(item.MAC);
			if (item.MAC == null || item.MAC.isEmpty()) {
				MyHelper.e(">>>>----103 Auth MAC address is empty");
				return false;
			}
			Object[] th = MyHelper.readToken(item.MAC);
			if (th == null || th.length != 2)
				return false;

			this.Token = (byte[]) th[0];
			this.IsHost = (Boolean) th[1];

			byte[] sig = ECCrypto.single().generateSignature(ECCrypto.single().DeviceKPA, item.cpRandnum);
			// ASN1 转 Raw
			byte[] rawSig = ECCrypto.single().decodeSig(sig);

			MyHelper.printHex("Auth token=", this.Token);
			data = protocol.makeRAMCHAL82(rawSig, this.Token);
			item = this.doSendAdnRead(data);
			if (item.hasError())
				return false;

			return true;
		} catch (Exception e) {
			return false;
		}
	}
	*/

	/**
	 * 准备分享数据
	 * 
	 * @return 分享数据的穿
	 */
	/*
	public MsgData prepareShareData() {
		byte[] random = MyHelper.genRandom(32);
		MyHelper.printHex("shareRandom", random);

		byte[] data = protocol.makeShareRAMSHARE02(random);
		MsgData md = this.doSendAdnRead(data);

		if (md.hasError()) {
			return buildErrorMsg("Consult share random failed, " + md.getError());
		}
		byte[] rawSig = null;
		try {
			byte[] sig = ECCrypto.single().generateSignature(ECCrypto.single().DeviceKPA, random);
			// ASN1 转 Raw
			rawSig = ECCrypto.single().decodeSig(sig);
		} catch (Exception e) {
			e.printStackTrace();
			return buildErrorMsg("Signature share random failed, " + e.getMessage());
		}

		MyHelper.printHex("shareSig", rawSig);

		// 前面32字节， 后面64字节, IP地址
		String s = MyHelper.bytesToHexText(random) + MyHelper.bytesToHexText(rawSig)
				+ MyHelper.bytesToHexText(this.getPlugIp().getAddress());
		md.Message = s;

		return md;
	}
	*/
	/**
	 * 尝试连接分享的Plug
	 * 
	 * @param shareData
	 * @return false=验证失败，true=验证成功
	 */
	/*
	public MsgData tryAuthShare(byte[] hostRandom, byte[] hostSig) {
		try {
			byte[] data = protocol.makeDiscoveryReq();
			MsgData item = this.doSendAdnRead(data);
			if (item.hasError())
				return item;

			this.Random = item.cpRandnum;
			this.setMAC(item.MAC);
			if (item.MAC == null || item.MAC.isEmpty()) {
				return buildErrorMsg("MAC address is empty");
			}
			if (HisHelper.isExisingDevice(item.MAC)) {
				return buildErrorMsg("The device already in your list, please delete it first.");
			}

			this.Token = hostRandom;
			byte[] sig = ECCrypto.single().generateSignature(ECCrypto.single().DeviceKPA, item.cpRandnum);
			// ASN1 转 Raw
			byte[] rawSig = ECCrypto.single().decodeSig(sig);

			// 验证分享 和 签名，成功返回 deviceId + mac
			data = protocol.makeShareACCREQ02(hostRandom, hostSig, ECCrypto.single().DevicePubKey, rawSig);
			item = this.doSendAdnRead(data);
			if (item.hasError())
				return item;

			if (null == item.MAC || item.MAC.isEmpty())
				return buildErrorMsg("Can't find MAC address");

			if (!item.MAC.equals(this.getMAC()))
				return buildErrorMsg("MAC address is not match");

			MyHelper.saveToken(this.getMAC(), this.Token, false);

			MsgData stand = this.tryGetStandard();
			if (!stand.hasError()) {
				item.PlugStandard = stand.PlugStandard;
			}

			return item;
		} catch (Exception e) {
			e.printStackTrace();
			return this.buildErrorMsg(e.getMessage());
		}
	}
*/
	public MsgData tryDeleteShare(String token) {
		if (token == null || token.length() < 64)
			return buildErrorMsg("Remove failed,unknow token");
		byte[] random = MyHelper.hexTextToBytes(token.substring(0, 64));
		byte[] data = protocol.makeShareDelete(random);

		MsgData item = this.doSendAdnRead(data);

		return item;
	}

	/**
	 * 获取历史记录
	 * 
	 * @return
	 */
	public List<HisItem> tryQueryHistory() {
		int startYear = -1;
		MsgData md = this.tryGetTime();
		if (md.hasError()) {
			MyHelper.e("get time error: " + md.getError());
		} else {
			startYear = md.cStartYear;
		}

		List<HisItem> his = new ArrayList<HisItem>();
		// ON/ OFF
		byte[] data = protocol.makeQueryHistory((byte) 0);
		md = this.doSendAdnRead(data);
		if (!md.hasError()) {
			his.addAll(md.hisLogs);
		}

		// temperature 禁用此功能
		// data = protocol.makeQueryHistory((byte) 1);
		// md = this.doSendAdnRead(data);
		// if (!md.hasError()) {
		// his.addAll(md.hisLogs);
		// }

		// energy
		data = protocol.makeQueryHistory((byte) 2);
		md = this.doSendAdnRead(data);
		if (!md.hasError()) {
			his.addAll(md.hisLogs);
			// his.add(HisItem.mockEnergy(220, -1, 0, 0));
			// his.add(HisItem.mockEnergy(330, -2, 0, 0));
			// his.add(HisItem.mockEnergy(350, -3, 0, 0));
			// his.add(HisItem.mockEnergy(200, -6, 2, 3));
			// his.add(HisItem.mockEnergy(100, -9, 3, 6));
			// his.add(HisItem.mockEnergy(100, -9, 4, 6));
			// his.add(HisItem.mockEnergy(100, -9, 5, 6));
			// his.add(HisItem.mockEnergy(150, -8, 0, -2));
			// his.add(HisItem.mockEnergy(258, -10, 0, -5));
			// his.add(HisItem.mockEnergy(560, 0, 0, -8));
			// his.add(HisItem.mockEnergy(300, -11, 0, 0));
			// his.add(HisItem.mockEnergy(450, -12, 0, 0)); // 超过12个月无效
			// his.add(HisItem.mockEnergy(400, -13, 0, 0)); // 超过12个月无效
		}



		return his;
	}

	/**
	 * 查询系统状态
	 */
	public MsgData tryQueryStatus() {
		if (this.NonsupportSysStatus)
			return buildErrorMsg("Nonsupport");
		byte[] data = protocol.makeQueryStatus();

		MsgData item = this.doSendAdnRead(data);
		if (item.hasError() && "81".equals(item.ErrorCode)) {
			NonsupportSysStatus = true;
		}

		return item;
	}

	public Boolean NonsupportSysStatus = false;
}
