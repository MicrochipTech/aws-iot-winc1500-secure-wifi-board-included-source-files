package com.amazonaws.mchp.awsprovisionkit.task.net;

import android.net.wifi.WifiConfiguration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;

import com.amazonaws.mchp.awsprovisionkit.base.MyThreadPool;

/**
 * 广播操作类
 */
public class MsgMulticast extends MsgBase {

	private MsgMulticast() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
		this.tryStopUdp();
	}

	static MsgMulticast m_instance = null;

	/**
	 * 单一实例模式，获取单一实例
	 * 
	 * @return
	 */
	public static MsgMulticast single() {
		if (null == m_instance) {
			m_instance = new MsgMulticast();
		}
		return m_instance;
	}

	/**
	 * UDP监听8897端口
	 */
	DatagramSocket udpListener = null;
	/**
	 * 是否结束监听
	 */
	Boolean bUdpFinish = false;
	/**
	 * UDP已启动
	 */
	Boolean bUdpStarted = false;

	/**
	 * 关闭UDP监听，只能关闭一次
	 */
	public void tryStopUdp() {
		if (bUdpFinish)
			return;

		bUdpFinish = true;
		if (null != udpListener) {
			try {
				udpListener.close();
			} catch (Exception e) {
			}
		}

		udpListener = null;
		bUdpStarted = false;
		isMonitoring = false;
		MyHelper.d("UDP-8897 stoped.");
		clearCachePlugs();
	}

	/**
	 * 启动多线程，监听UDP-8897端口
	 * 
	 * @throws SocketException
	 *             监听失败
	 */
	public final void tryUdpListen() {
		if (bUdpStarted)
			return;
		bUdpStarted = true;
		bUdpFinish = false;

		Thread task = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				doUdpListen();
			}
		});
		task.setPriority(Thread.NORM_PRIORITY);
		task.start();
		MyHelper.d(">>>> UDP-8897 Started");

		monitOnLinePlugs();
	}

	/**
	 * 执行监听操作
	 */
	void doUdpListen() {
		// clearCachePlugs();
		try {
			byte[] buffer = new byte[255];
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			udpListener = new DatagramSocket(MyConfig.APP_UDP_PORT);
			MyHelper.d("UDP-8897 Start Listening..");

			// 循环监听
			while (true) {
				if (bUdpFinish)
					break; // 结束监听
				try {
					// 读取数据
					Thread.sleep(50);
					udpListener.receive(dp);
					InetAddress rip = dp.getAddress();
					if (null == rip || MyConfig.UDP_PENDING) {
						Thread.sleep(2000);
						continue;
					}
					String sip = rip + "";
					if (PlugList.containsKey(sip) || PlugUdpQQ.contains(rip)) {
						continue; // 已建立链接
					} else if (PlugUdpDely.containsKey(sip)) {
						Integer tt = PlugUdpDely.get(sip);
						if (null != tt && tt <= 30) {
							PlugUdpDely.put(sip, ++tt);
							continue;
						}
						MyHelper.d(">>>>----102 UDP Old IP [" + sip + "] Re - Queue" + PlugUdpQQ.size());
					} else
						MyHelper.d(">>>>----101 UDP New IP [" + sip + "] Queue - " + PlugUdpQQ.size());

					PlugUdpDely.put(sip, 0);
					PlugUdpQQ.offer(rip);
					synchronized (syncPlugQueue) {
						syncPlugQueue.notify();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					MyHelper.e("UDP Listen error: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			MyHelper.e("UDP Listen error: " + e.getMessage());
		} finally {
			try {
				if (null != this.udpListener && !this.udpListener.isClosed())
					udpListener.close();
			} catch (Exception e) {
			}
			this.udpListener = null;
			bUdpFinish = true;
			bUdpStarted = false;
		}
	}

	Object syncPlugQueue = new Object();
	Boolean isMonitoring = false;

	/**
	 * 监控UDP队列，并启动UDP处理Task
	 */
	synchronized void monitOnLinePlugs() {
		if (isMonitoring)
			return;
		isMonitoring = true;

		Thread task = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (isMonitoring) {
					processUdpQQ();
				}

				MyHelper.d(">>>> Monitor Started");
			}

			@Override
			protected void finalize() throws Throwable {
				// TODO Auto-generated method stub
				super.finalize();
				isMonitoring = false;
			}
		});

		task.setPriority(Thread.NORM_PRIORITY);
		task.start();
	}

	void processUdpQQ() {
		if (PlugUdpQQ.size() == 0) {
			synchronized (syncPlugQueue) {
				try {
					syncPlugQueue.wait(TIMEOUT);
				} catch (InterruptedException e) {
				}
			}
		} else {
			doAuthPlug();
		}

		if (getCheckingOnlinePlugTask().needCheck())
			MyThreadPool.getExecutor().execute(getCheckingOnlinePlugTask());
	}

	void doAuthPlug() {
		final InetAddress rip = PlugUdpQQ.peek();
		if (null == rip)
			return;

		if (!MyHelper.isLocalIP_OK()) {
			MyHelper.e(">>>>----8899 UDP process delay[" + rip + "] because the WIFI-IP is: " + MyHelper.getLocalIP());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			return;
		}
		/*
		// 建立长连接
		try {
			int cc1 = 5;
			while (cc1-- >= 0 && mUdpTasks >= 5) {
				synchronized (UdpProcessor.class) {
					UdpProcessor.class.wait(TIMEOUT);
				}
				if (cc1 == 0) {
					mUdpTasks = 0;
					MyHelper.e(">>>> #### Background thread too many.....");
				}
			}
			MyThreadPool.getExecutor().execute(this.getUdpProcessor(rip));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			PlugUdpQQ.poll();
		}
		*/
	}
	/*
	UdpProcessor getUdpProcessor(InetAddress ip) {
		mUdpTasks++;
		MyHelper.d(">>>>----401 Udp Processor." + mUdpTasks);
		return new UdpProcessor(ip);
	}

	UdpProcessor mUdpProcessor = null;
	Integer mUdpTasks = 0;

	private class UdpProcessor implements Runnable {
		public UdpProcessor(InetAddress plugIp) {
			this.udpIp = plugIp;
		}

		private InetAddress udpIp = null;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (null == this.udpIp)
				return;
			try {
				final String sip = udpIp + "";
				MsgAdapter ma = new MsgAdapter(udpIp);
				if (!ma.tryAuthenticity()) {
					String mac = ma.getMAC() + "";
					if (null != ma.Token && !ma.IsHost && !SharedCache.containsKey(mac)) {
						SharedCache.put(mac, null);
					}
					if (null != MyHelper.readToken(mac)) {
						PlugUdpDely.remove(sip);
					}
					ma.close();
					MyHelper.d(">>>>----301 UDP AUTH-ERR ## IP[ " + udpIp + ", MAC=" + ma.getMAC() + "]");
					DeviceModel np = MyConfig.NewPlug;
					if (null != np && mac.equals(np.getDeviceMAC())) {
						// New device authenticate failed.
						WifiGlobal.ErrorCode = 401;
						MyHelper.e(">>>>----401 ERROR, New device authenticate failed.");
					}
					return;
				}
				MyHelper.d(">>>>----302  UDP AUTH-Okey ## IP[ " + udpIp + ", MAC=" + ma.getMAC() + "]");
				String mac = ma.getMAC();
				SharedCache.remove(mac);
				MsgData item = ma.tryGetMAC();
				MyHelper.d("MAC=" + ma.getMAC() + ", IP=" + udpIp);
				item = ma.trySyncTime(); // 同步时间给设备
				if (!item.hasError())
					HisHelper.syncHistory(ma);

				ma.tryGetOnOff();// 获取开关状态
				ma.tryGetTemperature(); // 获取温度

				MsgMulticast.this.putAdapter(sip, ma);
				postMessage(MsgWhat.UDP_NEW_PLUG_IN, mac);
				PlugUdpDely.remove(sip);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				synchronized (UdpProcessor.class) {
					mUdpTasks--;
					if (mUdpTasks < 0)
						mUdpTasks = 0;
					UdpProcessor.class.notify();
				}
			}
		}
	}
	*/

	CheckingOnlinePlug taskCheckingOnlinePlug = null;

	CheckingOnlinePlug getCheckingOnlinePlugTask() {
		if (null != taskCheckingOnlinePlug) {
			return taskCheckingOnlinePlug;
		}
		synchronized (CheckingOnlinePlug.class) {
			if (null == taskCheckingOnlinePlug) {
				taskCheckingOnlinePlug = new CheckingOnlinePlug();
			}
		}
		return taskCheckingOnlinePlug;
	}

	/**
	 * 检查插座是否在线，检查插座的温度
	 */
	private class CheckingOnlinePlug implements Runnable {

		public CheckingOnlinePlug() {
			this.nextCheck = Calendar.getInstance();
			this.updateNext();
		}

		public Boolean needCheck() {
			return !(isCheckingPlugTEMP || nextCheck.after(Calendar.getInstance()));
		}

		Boolean isCheckingPlugTEMP = false;
		Calendar nextCheck = null;

		void updateNext() {
			this.nextCheck.add(Calendar.SECOND, 7);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isCheckingPlugTEMP = true;
			try {
				for (Object s1 : PlugList.keySet().toArray()) {
					MsgAdapter b = PlugList.get(s1);
					if (null == b)
						continue;
					Boolean b2 = b.IsOverheat;
					String mac = b.getMAC();
					MsgData md = b.tryGetTemperature();
					if (!md.hasError()) {
						if (b2 != b.IsOverheat) {
							postMessage(MsgWhat.TEMP_WARNING, mac);
						}
					}
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				this.updateNext();
				this.isCheckingPlugTEMP = false;
				// MyHelper.d(">>>>----402 Finish TEMP Checking.");
			}
		}
	}

	/**
	 * 断开当前缓存的设备，这里会等待100毫秒
	 */
	public void clearCachePlugs() {
		for (Object sip : PlugList.keySet().toArray()) {
			MsgAdapter ma = PlugList.get(sip);
			if (null != ma)
				try {
					ma.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		PlugCache.clear();
		PlugUdpDely.clear();
		PlugUdpQQ.clear();

		MyHelper.d(">>>>----0000 ~~~~ runFinalization + GC");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
		System.runFinalization();
		System.gc();
	}

	public void clearDelayCache() {
		PlugUdpDely.clear();
	}

	/**
	 * 用MAC地址 把设备从在线设备列表 中移除
	 * 
	 * @param mac
	 */
	public void removeAdapterByMAC(String mac) {
		for (Object sip : PlugList.keySet().toArray()) {
			MsgAdapter ma = PlugList.get(sip);
			if (null != ma && ma.getMAC() == mac) {
				try {
					ma.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}

	/**
	 * 获取MsgAdapter，设备通讯类
	 * 
	 * @param ip
	 * @return
	 */
	synchronized public MsgAdapter getAdapter(String ipOrMac) {
		if (null == ipOrMac)
			return null;
		MsgAdapter ma = PlugCache.get(ipOrMac);
		if (null == ma)
			ma = PlugList.get(ipOrMac);
		return ma;
	}

	/**
	 * 缓存在线 Plug
	 * 
	 * @param ip
	 *            Plug IP地址
	 * @param ma
	 *            Plug通讯适配器
	 */
	synchronized void putAdapter(String ip, MsgAdapter ma) {
		String mac = ma.getMAC();
		if (null == ip)
			return;

		try {
			if (PlugList.containsKey(ip) || PlugCache.containsKey(mac)) {
				ma.close();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		PlugList.put(ip, ma);
		if (null != mac && !mac.isEmpty()) {
			PlugCache.put(mac, ma);
		}
	}

	/**
	 * 线的 Plug 通讯适配器
	 * 
	 * @param ip
	 * @return
	 */
	synchronized public MsgAdapter popAdapter(String ip) {
		MsgAdapter ma = PlugList.get(ip);
		if (null != ma)
			try {
				ma.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return ma;
	}

	/**
	 * 
	 * @param ma
	 */
	synchronized public void popAdapter(MsgAdapter ma) {
		if (null != ma)
			popAdapter(ma.getPlugIp().toString());
	}

	/**
	 * 发广播，获取局域网内在线设备
	 * 
	 * @return 在线设备的MAC地址数组
	 */
	public final ArrayList<MsgData> tryBroadcase() {
		ArrayList<MsgData> result = new ArrayList<MsgData>();
		try {
			byte[] data = protocol.makeQueryMAC();
			InetAddress ip = MyHelper.getBroadcastAddress();

			DatagramPacket pack = new DatagramPacket(data, data.length, ip, MyConfig.PLUG_UDP_PORT);
			MulticastSocket ms = new MulticastSocket();
			ms.setLoopbackMode(false);
			ms.send(pack);
			ms.setSoTimeout(TIMEOUT);
			int max = 32;
			while (max-- > 0) {
				byte[] rdata = new byte[64];
				DatagramPacket rdp = new DatagramPacket(rdata, rdata.length);
				ms.receive(rdp);
				rdata = rdp.getData();

				InetAddress ip1 = rdp.getAddress();
				MyHelper.d(ip1.toString());
				MsgData md = new MsgData(rdata);
				md.setPlugIp(ip1);
				md.tryParse();
				result.add(md);
			}

			ms.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			MyHelper.e(e.getMessage());
		}

		return result;
	}

	MsgAdapter discovery = null;

	public MsgData tryDiscovery1() {
		MsgData result = null;
		try {
			int cc1 = 8;
			while (cc1-- > 0) {
				String ip = MyHelper.getWifiIpAddress(true);
				MyHelper.d(">> tryDiscovery1 ip = " + ip);
				if (null == ip || ip.isEmpty() || ip.startsWith("0.0.0")) {
					Thread.sleep(500);
					continue;
				}

				if (null != discovery) {
					discovery.close();
					discovery = null;
				}

				discovery = new MsgAdapter(ip);
				result = discovery.tryDiscovery(1);
				if (result.hasError()) {
                    MyHelper.e(">>>>>>tryDiscovery1  result.hasError is ture");
					discovery.close();
					discovery = null;
					Thread.sleep(200);
					continue;
				} else {

					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public MsgData tryDiscovery2(WifiConfiguration apInfo, String password, String uuid) {
		if (null == this.discovery) {
			MsgData md = new MsgData();
			md.setError("The device might be disconnected.");
			return md;
		}

		WifiSEC sec = WifiSEC.OPEN;
		if (apInfo.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
		{
			sec = WifiSEC.WPA;
		}
		else if (apInfo.wepKeys[0] != null)
		{
			sec = WifiSEC.WEP;
		}

		String[] item = apInfo.SSID.split("\"");
		String ssid = item[1];
		MsgData md2 = discovery.tryDiscovery(2, ssid, password, sec, uuid);
		if (null != md2 && !md2.hasError()) {
			MyHelper.d(">>>>>>>>>>>>>tryDiscovery(3)");
			discovery.tryDiscovery(3);// send CONDONE message to gateway to finish the provision
		}
		return md2;
	}

	public MsgData tryDiscovery2(String ssid, String password, WifiSEC wifiSEC, String uuid) {
		if (null == this.discovery) {
			MsgData md = new MsgData();
			md.setError("The device might be disconnected.");
			return md;
		}
		MsgData md2 = discovery.tryDiscovery(2, ssid, password, wifiSEC, uuid);
		if (null != md2 && !md2.hasError()) {
			discovery.tryDiscovery(3);// send CONDONE message to gateway to finish the provision
		}
		return md2;
	}

	/**
	 * 尝试连接被分享的设备
	 * 
	 * @param shareData
	 *            扫描二维码得到的字符串信息
	 * @return
	 */
	/*
	public MsgData tryAuthShare1(String shareData) {
		// 数据长度判断
		int lenRandomSig = 32 * 2 + 64 * 2;
		if (shareData == null || shareData.length() <= lenRandomSig) {
			return this.buildErrorMsg("Unknow shared information");
		}

		// 解析分享数据
		byte[] random = MyHelper.hexTextToBytes(shareData.substring(0, 32 * 2));
		byte[] sig = MyHelper.hexTextToBytes(shareData.substring(32 * 2, 32 * 2 + 64 * 2));
		MyHelper.printHex("shareRandom", random);
		MyHelper.printHex("shareSig", sig);

		byte[] ipAddr = MyHelper.hexTextToBytes(shareData.substring(32 * 2 + 64 * 2));
		InetAddress ip = null;
		try {
			ip = InetAddress.getByAddress(ipAddr);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return this.buildErrorMsg("Unknow IP address");
		}

		MsgAdapter shared = new MsgAdapter(ip);
		MsgData md = shared.tryAuthShare(random, sig);

		try {
			shared.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return md;
	}
	*/
}
