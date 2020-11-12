package caiweitao.converter.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author caiweitao
 * @Date 2020年11月10日
 * @Description 四字节字符转换器
 */
public class Converter {
	private final static String  PREFIX = "&#", SUFFIX = ";";//前缀、后缀

	/**
	 * 编码
	 * @param str
	 * @return 编码后的字符串（四字节字符编码成：前缀+ASCII码+后缀）
	 */
	public static String encoder (String str) {
		if (str == null) 
			return null;
		
		StringBuilder sb = new StringBuilder();
		char[] ach = str.toCharArray();
		for (char c:ach) {
			if (match4byteChar(c)) {//符合四字节字符进行编码（直接转成ASCII）
				sb.append(PREFIX);
				sb.append((int)c);
				sb.append(SUFFIX);
			} else {//否则不变
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 解码
	 * @param str
	 * @return 恢复原本字符串
	 */
	public static String decoder (String str) {
		if (str == null || str.indexOf(PREFIX) < 0) {
			return str;
		}
		StringBuilder sb = new StringBuilder();

		int slither = 0;//字符串滑动窗口下标
		while (slither < str.length()) {
			int prefixIndex = str.indexOf(PREFIX, slither);
			if (prefixIndex >= 0) {//存在前缀字符串（说明可能存在需要解码的字符）
				sb.append(str.substring(slither, prefixIndex));
				//从该前缀往后的字符串中检索字符
				int tempPrefixIndex = str.indexOf(PREFIX, prefixIndex+PREFIX.length());
				int tempSuffixIndex = str.indexOf(SUFFIX, prefixIndex+1);

				if (tempPrefixIndex >= 0 && tempPrefixIndex < tempSuffixIndex) {
					//存在前缀和后缀，并且前缀还在后缀之前（说明刚刚定位的前缀不是编码后得出来的，原本就存在该字符）
					sb.append(str.substring(prefixIndex, tempPrefixIndex));
					slither = tempPrefixIndex;
				} else if (tempSuffixIndex >= 0 && (tempSuffixIndex < tempPrefixIndex || tempPrefixIndex < 0)) {
					//有后缀，并且 后缀在前
					String code = str.substring(prefixIndex+PREFIX.length(), tempSuffixIndex);
					if (isInt(code)) {
						//如果是整形数字
						sb.append((char)Integer.parseInt(code));
					} else {
						//按字符串添加，不需要解码
						sb.append(str.substring(prefixIndex, tempSuffixIndex + 1));
					}
					slither = tempSuffixIndex + 1;
				} else {
					//没有后缀
					sb.append(str.substring(prefixIndex));
					break;
				}
			} else {
				//不存在前缀字符串，不需要解码
				sb.append(str.substring(slither));
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * 判断是否为emoji等四字节字符
	 * @param ch
	 * @return true:包含四字节字符
	 */
	private static boolean match4byteChar (char ch) {
		//网上查的四字节的范围
		Pattern pattern = Pattern.compile ("[\ud800\udc00-\udbff\udfff\ud800-\udfff]",Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE ) ;
		Matcher matcher = pattern.matcher(String.valueOf(ch));
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	private static boolean isInt(String val) {
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
