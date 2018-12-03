package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.amazonaws.mchp.awsprovisionkit.base.BaseApp;
import android.content.Intent;
import android.os.Bundle;

/**
 * 消息处理基类
 */
public abstract class MsgBase implements Closeable {

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		MyHelper.d("MsgBase finalize");
		this.dispose();
	}

	protected void dispose() {
		try {
			if (null != this.m_socket) {
				this.m_socket.close(); // 关闭连接
				MyHelper.d(">>>>----8899 close socket, ip=" + this.getPlugIp() + ", mac=" + this.getMAC());
			}
		} catch (Exception e) {
		} finally {
			this.m_socket = null;
		}

		try {
			// 从缓存中移除，发送 设备掉线消息
			if (null != this.m_plugIp) {
				String sip = this.m_plugIp.toString();
				PlugList.remove(sip);
				String mac = this.getMAC();

				if (null == mac || mac.isEmpty() || null != MyHelper.readToken(mac)) {
					PlugUdpDely.remove(sip);
				}

				if (mac != null && !mac.isEmpty()) {
					PlugCache.remove(mac);
					this.postMessage(MsgWhat.PLUG_OFF_LINE, mac);
				}
			}
		} catch (Exception e) {
		} finally {
			// this.m_macAddr = null;
			// this.m_plugIp = null;
		}
	}

	/**
	 * 打印接收数据包日志
	 * 
	 * @param data
	 *            数据包
	 */
	protected final void printIn(byte[] data) {
		// MyHelper.d(">>>>----8899 in from [" + this.getPlugIp() + "]" +
		// data.length);
	}

	/**
	 * 打印发送的数据包
	 * 
	 * @param data
	 *            数据包
	 */
	protected final void printOut(byte[] data) {
		// MyHelper.d(">>>>----8899 out to [" + this.getPlugIp() + "]" +
		// data.length);
	}

	/**
	 * 插座对象列表， MAC地址 -> InetAddress
	 */
	protected final static HashMap<String, MsgAdapter> PlugList = new HashMap<String, MsgAdapter>();
	protected final static HashMap<String, MsgAdapter> PlugCache = new HashMap<String, MsgAdapter>();
	protected final static HashMap<String, Object> SharedCache = new HashMap<String, Object>();
	protected final static ConcurrentLinkedQueue<InetAddress> PlugUdpQQ = new ConcurrentLinkedQueue<InetAddress>();
	protected final static HashMap<String, Integer> PlugUdpDely = new HashMap<String, Integer>();

	/**
	 * 是否是分享的设备
	 * 
	 * @return
	 */
	public static Boolean isSharedDevice(String mac) {
		return SharedCache.containsKey(mac);
	}

	/**
	 * 获取通信适配器,请不要直接使用此方法，请使用 DeviceModel.tryGetMsgAdapter 方法
	 * 
	 * @param ip
	 *            ip地址
	 * @return 返回适配器，找不到 返回 NULL
	 */
	public static MsgAdapter getMsgAdapter(String ipOrMac) {
		return MsgMulticast.single().getAdapter(ipOrMac);
	}

	synchronized public static int getCasedAdapterCount() {
		return PlugList.size();
	}

	synchronized public static Collection<MsgAdapter> getCasedAdapters() {
		return PlugList.values();
	}

	@Override
	synchronized public void close() throws IOException {
		MyHelper.d("MsgBase close");
		this.dispose();
	}

	/**
	 * 发送消息给 Handler
	 * 
	 * @param what
	 *            Message.what
	 * @param obj
	 *            Message.obj
	 */
	protected void postMessage(int what, String obj) {
		switch (what) {
		case MsgWhat.UDP_NEW_PLUG_IN:
			sendBroadcast(MsgWhat.ACTION_NEW_PLUG_IN, obj);
			break;
		case MsgWhat.PLUG_OFF_LINE:
			sendBroadcast(MsgWhat.ACTION_PLUG_OFF_LINE, obj);
			break;
		case MsgWhat.PRINT_LOG:
			sendBroadcast(MsgWhat.ACTION_PRINT_LOG, obj);
			break;
		case MsgWhat.OTAU_PROGRESS:
			sendBroadcast(MsgWhat.ACTION_OTAU_PROGRESS, obj);
		case MsgWhat.TEMP_WARNING:
			sendBroadcast(MsgWhat.ACTION_PLUG_TEMP_WARNING, obj);
		default:
			break;
		}
	}

	void sendBroadcast(String action, String message) {
		Intent i = new Intent();
		i.setAction(action);
		Bundle b = new Bundle();
		b.putString("message", message);
		i.putExtras(b);
		BaseApp.getInstance().sendBroadcast(i);
	}

	/**
	 * 打印日志，ADT log， UI-Handler不为NULL时，会发送日志消息到UI窗口
	 * 
	 * @param pre
	 * @param data
	 */
	void printLog(String pre, byte[] data) {
		MyHelper.printHex(pre, data);
		// String log = this.getMAC() + "_" + pre + MyHelper.format2Hex(data);
		// postMessage(MsgWhat.PRINT_LOG, log);
	}

	protected void initSocket(InetAddress plugIp) {
		this.m_plugIp = plugIp;
		try {
			m_socket = new Socket(m_plugIp, MyConfig.PLUG_TCP_PORT);
			m_socket.setKeepAlive(true);
			m_socket.setSoLinger(true, 0);
			m_socket.setTcpNoDelay(true);
		} catch (Exception e) {
			String err = ">>>>----8899 build socket failed,";
			if (null != e)
				err += e.getMessage();
			MyHelper.e(err);
		}
	}

	/**
	 * 当前插座的MAC地址
	 */
	protected String m_macAddr = null;

	/**
	 * 当前插座的IP地址
	 */
	protected InetAddress m_plugIp = null;

	/**
	 * TCP长连接
	 */
	protected Socket m_socket = null;

	/**
	 * 获取设备IP地址
	 * 
	 * @return
	 */
	public InetAddress getPlugIp() {
		return m_plugIp;
	}

	public String getMAC() {
		return this.m_macAddr;
	}

	protected void setMAC(String mac) {
		this.m_macAddr = mac;
	}

	/**
	 * 获取socket对象
	 * 
	 * @return 返回socket对象
	 * @throws UnknownHostException
	 *             创建socket对象失败
	 * @throws IOException
	 *             socket不可用
	 */
	Socket getSocket() throws UnknownHostException, IOException {
		if (null != m_socket) {
			m_socket.setSoTimeout(TIMEOUT);
		}

		return m_socket;
	}

	final int TIMEOUT = 4000;

	/**
	 * send and receive data
	 * 
	 * @param data
	 *            data need to send
	 * @return received data
	 */
	synchronized MsgData doSendAdnRead(byte[] data) {
		String err = null;
		MsgData item = null;
		String ipp = "LOCAL-IP=" + MyHelper.getLocalIP() + ",Plug MAC.IP=" + this.getMAC() + this.getPlugIp();

		try {
			Socket socket = getSocket();
			InputStream in = null;
			OutputStream out = null;
			if (null == socket || socket.isClosed() || !socket.isConnected()) {
				this.close();
				err = "Device Offline";
			} else {
				ipp += ", {LOCAL-PORT=" + socket.getLocalPort() + "}";
				in = socket.getInputStream();
				out = socket.getOutputStream();
				printOut(data);
				out.write(data);
				byte[] ret = new byte[1024];
				int len = in.read(ret);

				if (len > 0) {
					ret = MyHelper.subBytes(ret, 0, len);
					item = ProtocolAdapter.tryParse(ret, this.Random, this.protocol);
					if (null != item) {
						printIn(item.getData());
					}
				} else {
					this.close();
					err = "socket error: read data length = 0";
				}
			}
		} catch (Exception e) {
			err = "Network error, ";
			if (null != e) {
				err += e.getMessage();
				e.printStackTrace();
				MyHelper.e(">>>>-doSendAdnRead Exception err="+err);
			}
			try {
				this.close();
			} catch (Exception e2) {
			}
		} finally {
			if (err != null) {
				item = buildErrorMsg(err);
			}
			if (null == item) {
				item = buildErrorMsg("Unknow error");
			}
			if (item.hasError()) {
				MyHelper.e(">>>>----8899 IO ERR: " + ipp + MyConfig.S_NEW_LINE + item.getError());
			}
		}

		return item;
	}

	public MsgBase() {
		protocol = new ProtocolAdapter();
		Random = MyHelper.genRandom(32);
	}

	/**
	 * 构造错误信息MsgData
	 * 
	 * @param error
	 * @return
	 */
	protected MsgData buildErrorMsg(String error) {
		MsgData md = new MsgData();
		md.setError(error);
		return md;
	}

	/**
	 * 协议处理适配器
	 */
	protected ProtocolAdapter protocol = null;
	protected byte[] Random = null;
	protected byte[] Token = null;
	/**
	 * 是否是主人（通过添加新设备得到的是主人，分享得到的是普通用户）
	 */
	public boolean IsHost = false;
}
