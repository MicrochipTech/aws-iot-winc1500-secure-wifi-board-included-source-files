package com.amazonaws.mchp.awsprovisionkit.utils;

import android.util.Log;

/**
 * 日志工具类
 * 
 * @author ShinSoft
 * 
 */
public class LogUtils {
	private static String TAG = "XL";
	public static int logLevel = Log.VERBOSE;
	public static boolean isDebug = true;

	public LogUtils() {
	}

	public LogUtils(String tag) {
		TAG = tag;
	}

	public void setTag(String tag) {
		TAG = tag;
	}

	private static String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();

		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			//Log.i(TAG, st.getClassName()+"=="+LogUtils.class.getClass().getName()+"=="+Thread.class.getName()+"==="+LogUtils.class.getSimpleName());
			if (st.getClassName().equals(LogUtils.class.getClass().getName())) {
				continue;
			}
			
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			
//			if (st.getClassName().equals(MyLog.class.getClass().getName())) {
//				continue;
//			}
			
			if (st.getClassName().indexOf(LogUtils.class.getSimpleName())!=-1) {
				continue;
			}

			return "[" + Thread.currentThread().getName() + "("
					+ Thread.currentThread().getId() + "): " + st.getFileName()
					+ ":" + st.getLineNumber() + "]";
		}

		return null;
	}

	public static void info(Object str) {
		if (logLevel <= Log.INFO) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.i(TAG, ls);
		}
	}

	public static void i(Object str) {
		if (isDebug) {
			info(str);
		}
	}

	public static void verbose(Object str) {
		if (logLevel <= Log.VERBOSE) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.v(TAG, ls);
		}
	}

	public static void v(Object str) {
		if (isDebug) {
			verbose(str);
		}
	}

	public static void warn(Object str) {
		if (logLevel <= Log.WARN) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.w(TAG, ls);
		}
	}

	public static void w(Object str) {
		if (isDebug) {
			warn(str);
		}
	}

	public static void error(Object str) {
		if (logLevel <= Log.ERROR) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.e(TAG, ls);
		}
	}

	public static void error(Exception ex) {
		if (logLevel <= Log.ERROR) {
			StringBuffer sb = new StringBuffer();
			String name = getFunctionName();
			StackTraceElement[] sts = ex.getStackTrace();

			if (name != null) {
				sb.append(name + " - " + ex + "\r\n");
			} else {
				sb.append(ex + "\r\n");
			}

			if (sts != null && sts.length > 0) {
				for (StackTraceElement st : sts) {
					if (st != null) {
						sb.append("[ " + st.getFileName() + ":"
								+ st.getLineNumber() + " ]\r\n");
					}
				}
			}

			Log.e(TAG, sb.toString());
		}
	}

	public static void e(Object str) {
		if (isDebug) {
			error(str);
		}
	}

	public static void e(Exception ex) {
		if (isDebug) {
			error(ex);
		}
	}

	public static void debug(Object str) {
		if (logLevel <= Log.DEBUG) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.d(TAG, ls);
		}
	}

	public static void d(Object str) {
		if (isDebug) {
			debug(str);
		}
	}
}
