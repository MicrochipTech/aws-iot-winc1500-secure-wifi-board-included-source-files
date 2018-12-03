package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.io.Serializable;

/**
 * 定时信息类，包含所属分组、定时时间、dayMask、开关动作
 *
 */
public class PlugSchedule implements Serializable {
	/**
	 * 定时分组 [0~9]
	 */
	public byte GroupIndex = 0;

	/**
	 * 定时 小时
	 */
	public byte Hour = 0;

	/**
	 * 定时 分钟
	 */
	public byte Minute = 0;

	/**
	 * 周7 是否启用
	 */
	public Boolean Day7 = false;

	/**
	 * 周6 是否启用
	 */
	public Boolean Day6 = false;

	/**
	 * 周5 是否启用
	 */
	public Boolean Day5 = false;

	/**
	 * 周4 是否启用
	 */
	public Boolean Day4 = false;

	/**
	 * 周3 是否启用
	 */
	public Boolean Day3 = false;

	/**
	 * 周2 是否启用
	 */
	public Boolean Day2 = false;

	/**
	 * 周1 是否启用
	 */
	public Boolean Day1 = false;
	/**
	 * 开关动作，true=开，false=关
	 */
	public Boolean OnOff = false;

	/**
	 * 根据数据包解析数据 CID_2b + C-Index_1b + Target{ CID_2b + C-Index_1b + AttrID_1b +
	 * DayMask_1b + time_2b + AttrValue_Xb }
	 * 
	 * @param data
	 *            完整数据包，包含SOF...CRC32
	 */
	public void init(byte[] data) {
		int ciStart = SF.fiParamStart + 2;
		this.GroupIndex = data[ciStart];
		this.Hour = data[ciStart + 7];
		this.Minute = data[ciStart + 6];
		this.OnOff = data[ciStart + 8] == 1;
		byte dayMask = data[ciStart + 5];

		this.Day1 = (dayMask & 2) == 2;
		this.Day2 = (dayMask & 4) == 4;
		this.Day3 = (dayMask & 8) == 8;
		this.Day4 = (dayMask & 0x10) == 0x10;
		this.Day5 = (dayMask & 0x20) == 0x20;
		this.Day6 = (dayMask & 0x40) == 0x40;
		this.Day7 = (dayMask & 0x80) == 0x80;
		if (!(this.Day1 || this.Day2 || this.Day3 || this.Day4 || this.Day5 || this.Day6 || this.Day7))
			this.OnOff = false;
	}

	/**
	 * 获取周xx掩码
	 * 
	 * @return 返回周xx掩码
	 */
	public byte getDayMask() {
		byte dayMask = 0;
		if (Day1)
			dayMask |= 2;
		if (Day2)
			dayMask |= 4;
		if (Day3)
			dayMask |= 8;
		if (Day4)
			dayMask |= 0x10;
		if (Day5)
			dayMask |= 0x20;
		if (Day6)
			dayMask |= 0x40;
		if (Day7)
			dayMask |= 0x80;
		return dayMask;
	}
}
