package com.amazonaws.mchp.awsprovisionkit.task.net;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * 历史数据信息 Start Year[2], 2.start day[1], 3.Time/Calendar mask[1]=0x3f, 4.
 * Time/Calendar[4], 5.Running time[4] 同步时间，使用 1_2_4 ATTR, 4.time/calendar=
 * bit[31...0] [Year: 6bit | Month: 4bit | Date: 5bit | Hour: 5bit | Minute:
 * 6bit | Second: 6bit]
 */
public class HisItem {
	/**
	 * 解析开关
	 * 
	 * @param data
	 * @return
	 */
	public static HisItem tryParseOnOff(byte[] data) {
		HisItem hi = new HisItem(MyHelper.subBytes(data, 0, 4), 0);
		hi.m_onOff = data[4] == 1;
		return hi;
	}

	/**
	 * 解析温度
	 * 
	 * @param data
	 * @return
	 */
	public static HisItem tryParseTEMP(byte[] data) {
		HisItem hi = new HisItem(MyHelper.subBytes(data, 0, 4), 1);
		hi.m_temperature = MyHelper.byteToInt(MyHelper.subBytes(data, 4, data.length - 4));
		return hi;
	}

	/**
	 * 解析累计耗电
	 * 
	 * @param data
	 * @return
	 */
	public static HisItem tryParseEnergy(byte[] data) {
		HisItem hi = new HisItem(MyHelper.subBytes(data, 0, 4), 2);
		hi.m_energy = MyHelper.byteToInt(MyHelper.subBytes(data, 4, data.length - 4));
		return hi;
	}

	public static HisItem mockEnergy(int energy, int addMonth, int addDay, int addHour) {
		HisItem hi = new HisItem(2);
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, addMonth);
		c.add(Calendar.DAY_OF_MONTH, addDay);
		c.add(Calendar.HOUR_OF_DAY, addHour);
		int day = c.get(Calendar.DAY_OF_MONTH);
		int mon = c.get(Calendar.MONTH) + 1;
		int hour = c.get(Calendar.HOUR_OF_DAY);

		hi.m_startYear = c.get(Calendar.YEAR);
		hi.m_year1 = 0;
		hi.m_month = mon;
		hi.m_day = day;
		hi.m_hour = hour;
		hi.m_minute = 15;
		hi.m_second = 20;
		hi.m_energy = energy;
		return hi;
	}

	public static HisItem mockEnergy2(int energy, int month, int day, int hour, int min, int second) {
		HisItem hi = new HisItem(2);
		hi.m_startYear = 2015;
		hi.m_year1 = 0;
		hi.m_month = month;
		hi.m_day = day;
		hi.m_hour = hour;
		hi.m_minute = min;
		hi.m_second = second;
		hi.m_energy = energy;
		return hi;
	}

	private HisItem(int type) {
		this.m_type = type;
	}

	/**
	 * 用时间数据 构建实例，
	 * 
	 * @param date
	 *            4字节的时间数据
	 */
	private HisItem(byte[] date, int type) {
		/*
		 * bit[31...0] [Year: 6bit | Month: 4bit | Date: 5bit | Hour: 5bit |
		 * Minute: 6bit | Second: 6bit] 0b- 000000.00 00.00000.0 0000.0000
		 * 11.101010
		 */
		this.m_type = type;
		this.m_startYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
		int a = date[0] & 0xff, b = date[1] & 0xff, c = date[2] & 0xff, d = date[3] & 0xff;
		this.m_second = a & 0x3f;
		this.m_minute = ((b << 2) | (a >> 6)) & 0x3f;
		this.m_hour = ((c << 4) | (b >> 4)) & 0x1f;
		this.m_day = (c >> 1) & 0x1f;
		this.m_month = ((d << 2) | (c >> 6)) & 0xf;
		this.m_year1 = (d >> 2) & 0x3f;
	}

	public void setStartYear(int startYear) {
		this.m_startYear = startYear;
	}

	private int m_startYear = 0;
	private int m_year1 = 0;
	private int m_month = 0;
	private int m_day = 0;
	private int m_hour = 0;
	private int m_minute = 0;
	private int m_second = 0;

	private Boolean m_onOff = false; // 开关状态
	private int m_temperature = 0; // 设备温度
	private int m_energy = 0; // 累计耗电

	private int m_type = 0;

	private int getYear() {
		return this.m_startYear + this.m_year1;
	}

	/**
	 * 获取日志类型， 0=On/OFF, 1=温度， 2=累计耗电
	 * 
	 * @return 0=On/OFF, 1=温度， 2=累计耗电
	 */
	public int getType() {
		return this.m_type;
	}

	/**
	 * 获取开关状态
	 * 
	 * @return true=开，false=关
	 */
	public Boolean getOnOff() {
		return this.m_onOff;
	}

	/**
	 * 获取温度
	 * 
	 * @return
	 */
	public int getTemperature() {
		return this.m_temperature;
	}

	/**
	 * 获取累计耗电
	 * 
	 * @return
	 */
	public float getEnergy() {
		float v = (float) this.m_energy / (float) 10;
		return v;
	}

	/**
	 * 获取 Key，可做唯一性判断
	 * 
	 * @return
	 */
	public String getID() {
		return this.getType() + "-" + this.getYear() + "-" + this.m_month + "-" + this.m_day + "-" + this.m_hour + "-" + this.m_minute + "-"
				+ this.m_second;
	}

	public String getDate() {
		DecimalFormat df4 = new DecimalFormat("0000");
		DecimalFormat df2 = new DecimalFormat("00");
		String str = "" + df4.format(this.getYear()) + "-" + df2.format(this.m_month) + "-" + df2.format(this.m_day)
				+ " " + df2.format(this.m_hour) + ":" + df2.format(this.m_minute) + ":" + df2.format(this.m_second);

		return str;
	}

	/**
	 * 是否有效
	 * 
	 * @return true=有效，false=无效
	 */
	public Boolean isValid() {
		return this.m_month != 0;
	}
}
