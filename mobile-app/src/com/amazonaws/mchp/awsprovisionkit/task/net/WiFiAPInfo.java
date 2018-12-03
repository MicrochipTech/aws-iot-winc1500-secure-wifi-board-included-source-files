package com.amazonaws.mchp.awsprovisionkit.task.net;

public class WiFiAPInfo {
	public WiFiAPInfo() {
	}

	private String ssid;
	private String password;

	public int setSSID(String ssid_name)
	{
		ssid = ssid_name;
		return 0;
	}

	public int setPassword(String pwd)
	{
		password = pwd;
		return 0;
	}
	public String getSsid()
	{
		return ssid;
	}
	public String getPassword()
	{
		return password;
	}


}
