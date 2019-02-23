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






	public Boolean NonsupportSysStatus = false;
}
