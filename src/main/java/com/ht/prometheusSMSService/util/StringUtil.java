package com.ht.prometheusSMSService.util;

import java.io.UnsupportedEncodingException;

/**
 * 字符串处理类
 * 
 * @author tujing
 *
 */
public class StringUtil {

	/**
	 * 把字符串 转成16进制的GBK内码
	 * 
	 * @param str
	 * @return
	 */
	public static String converGBK16(String str) {
		if (str == null) {
			System.out.println("字符串为空");
			return "";
		} else {
			byte[] arrInput;
			try {
				char[] chars = "0123456789ABCDEF".toCharArray();
				arrInput = str.getBytes("GBK");
				StringBuilder sOutput = new StringBuilder(arrInput.length);
				int bit;
				for (int i = 0; i < arrInput.length; i++) {
					bit = (arrInput[i] & 0x0f0) >> 4;
					sOutput.append(chars[bit]);
					bit = arrInput[i] & 0x0f;
					sOutput.append(chars[bit]);
				}
				return sOutput.toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		}

	}
}
