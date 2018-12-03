package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.util.zip.CRC32;

/**
 * 协议处理协助，执行数据加密、解密，数据校验， 对外公开的方法必须是try开头 协议备注： 1. 数据包长度不包含SOF1_DataLength2_CRC4
 * 2. CRC计算不包含CRC本身，即 从SOF开始，至CRC之前结束， 3.数据包中 结构数据-低位在前、高位在后，字符串数据从前向后；
 */
public class ProtocolHelper {

	/**
	 * CRC校验数据包时的开始位置（从SOF开始）， 默认={@value}
	 */
	static final int _crcStart = 0;

	/**
	 * CRC校验时 不包含的长度（不包含CrcCheckSum=4）， 默认={@value}
	 */
	static final int _crcDeduct = 4;

	public static byte[] tryEncrypt(byte[] data) {
		if (MyConfig.EnableEncrypt) {

		}
		return data;
	}

	/**
	 * 预处理要发送的数据包， 加入CRC校验位，加密数据，用于数据包发送之前的处理
	 * 
	 * @param data
	 * @return 返回加密后的数据
	 */
	public static byte[] tryPackage(byte[] data, ProtocolAdapter protocol) {
		byte[] result = data;
		//CrcAppend(data);
		//result = doEncrypt(data, protocol);

		return result;
	}

	/**
	 * 预处理接收到的数据包， 解密数据 并 校验CRC 是否正确,若CRC校验失败，则抛异常
	 * 
	 * @param data
	 *            要处理的数据
	 * @return 解密后的完整数据包
	 * @throws Exception
	 *             数据解密失败 或 CRC校验未通过
	 */
	public static byte[] tryUnPack(byte[] data, byte[] random, ProtocolAdapter protocol) throws Exception {

		byte[] result = data;
		//result = doDecrypt(data, random, protocol);

		//if (!CrcCheck(result)) {
		//	MyHelper.printHex("校验数据包出错，", result);
		//	throw new Exception("CRC校验失败");
		//}

		return result;
	}

	/**
	 * 用CRC方法校验数据包是否完整
	 * 
	 * @param data
	 *            要校验的数据包
	 * @return true:数据正常，false:数据异常
	 */
	static Boolean CrcCheck(byte[] data) {
		if (!MyConfig.EnableCrcCheck)
			return true; // 未开启CRC校验

		// 长度计算
		int dl = MyHelper.byteToInt(new byte[] { data[SF.fiDataLenL], data[SF.fiDataLenH] });
		if (dl != (data.length - _crcDeduct - SF.fiDataLenH - 1))
			return false;

		byte[] bb = calcCRC32(data, _crcStart, data.length - _crcDeduct);
		int len = data.length - 4;

		for (int i = 0; i < 4; i++) { // 从低位开始校验
			if (bb[i] != data[len + i])
				return false; // CRC结果不同，校验失败返回 false
		}

		return true;
	}

	/**
	 * 计算数据包[0 ~ Length-7]的CRC值，并把CRC结果填充到后四位，用于校验接收到的数据
	 * 
	 * @param data
	 *            要校验的数据包，此数据包必须是完整的数据包，必须包含SOF开始位 和 CRC的4个字节位
	 */
	static void CrcAppend(byte[] data) {
		byte[] b = calcCRC32(data, _crcStart, data.length - _crcDeduct);

		int index = data.length - 4; // 数组结束 = CRC 最低位
		for (int i = 0; i < 4; i++) { // 从CRC低位到高循环赋值
			data[index + i] = b[i];
		}
	}

	/**
	 * 计算校验和， 注意：返回的Byte数组前4个字节有效
	 * 
	 * @param data
	 *            要计算的数据
	 * @param start
	 *            CRC开始位
	 * @param len
	 *            CRC数据长度
	 * @return CRC结果，低位在前
	 */
	public static byte[] calcCRC32(byte[] data, int start, int len) {
		int t1 = data.length - start;
		if (len > t1)
			len = t1;
		CRC32 crc = new CRC32();
		crc.update(data, start, len);
		long v = crc.getValue();
		byte[] b = MyHelper.longToByte(v);
		crc = null;
		return b;
	}

	/**
	 * 加密数据
	 * 
	 * @param data
	 *            要加密的数据
	 * @return 加密之后的数据
	 */
	/*
	static byte[] doEncrypt(byte[] data, ProtocolAdapter protocol) {

		if (!MyConfig.EnableEncrypt)
			return data; // 未启用加密、解密

		if (!protocol.getEnableAES())
			return data;

		// 加密数据
		byte[] sessionKey = protocol.getSessionKey();
		byte[] iv = protocol.getIV();
		//byte[] pubKey = ECCrypto.single().DevicePubKey;
		//byte[] data2 = ECCrypto.single().aesEncrypt(data, sessionKey, iv);

		// 1-5b, 2-len, data-len, 16-IV, 64-PubKey, 4-CRC
		data = new byte[data2.length + 1 + 2 + 16 + 64 + 4];
		byte[] len = MyHelper.intToByte(data.length - _crcDeduct - SF.fiDataLenH - 1);
		data[0] = 0x5b;
		data[SF.fiDataLenL] = len[0];
		data[SF.fiDataLenH] = len[1];
		System.arraycopy(data2, 0, data, SF.fiDataLenH + 1, data2.length);
		System.arraycopy(iv, 0, data, SF.fiDataLenH + 1 + data2.length, iv.length);
		System.arraycopy(pubKey, 0, data, SF.fiDataLenH + 1 + data2.length + iv.length, pubKey.length);
		CrcAppend(data);

		return data;
	}
	*/

	/**
	 * 解密数据
	 * 
	 * @param data
	 *            要解密的数据
	 * @return 解密之后的数据
	 * @throws Exception
	 *             校验 或 解密时的 错误信息
	 */
	/*
	static byte[] doDecrypt(byte[] data, byte[] random, ProtocolAdapter protocol) throws Exception {
		if (!MyConfig.EnableEncrypt)
			return data; // 未启用加密、解密

		if (data[0] == SF.SOF)
			return data; // 非密文数据
		else if (data[0] != 0x5b)
			throw new Exception("Unknow data fromat");

		// check CRC
		int dl = MyHelper.byteToInt(new byte[] { data[SF.fiDataLenL], data[SF.fiDataLenH] });
		if (dl != (data.length - _crcDeduct - SF.fiDataLenH - 1))
			throw new Exception("The Encrypt data length not match");

		byte[] bb = calcCRC32(data, _crcStart, data.length - _crcDeduct);
		int len = data.length - 4;

		for (int i = 0; i < 4; i++) { // 从低位开始校验
			if (bb[i] != data[len + i])
				throw new Exception("The Encrypt data CRC Check fail"); // CRC结果不同，校验失败返回
																		// false
		}

		// 解密数据
		int ivStart = data.length - 4 - 16 - 64; // crc4, iv16, pub64
		byte[] bIV = MyHelper.subBytes(data, ivStart, 16);
		byte[] bPK = MyHelper.subBytes(data, ivStart + 16, 64);
		byte[] bb0 = MyHelper.subBytes(data, 3, data.length - 3 - 16 - 64 - 4);
		byte[] bb1 = ECCrypto.single().aesDecryptSelf(bb0, bPK, bIV, random);
		if (bb1[0] != SF.SOF)
			throw new Exception("the descrypted data[0] is not 5A");

		// tack sessionKey
		protocol.trySaveSessionKey(bPK, random);

		int len2 = MyHelper.byteToInt(new byte[] { bb1[SF.fiDataLenL], bb1[SF.fiDataLenH] });
		byte[] raw = MyHelper.subBytes(bb1, 0, len2 + 4 + SF.fiDataLenH + 1);

		return raw;
	}
	*/
}
