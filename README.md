# converter4byteStr:emoji等四字节字符转换器

Emoji表情字符现在在APP已经广泛支持了。但是Mysql的UTF8编码对Emoji字符的支持却不是那么好。所以我们经常会遇到这样的异常：

    Incorrect string value: '\xF0\x90\x8D\x83...' for column
原因是Mysql里UTF8编码最多只能支持3个字节，而Emoji表情字符使用的UTF8编码，很多都是4个字节，有些甚至是6个字节。

比如：🍎💰📱👮👲👳👷

还有一些汉字，比如：𤋮

**解决的方案有两种：**


1. 使用 **utf8mb4** 的 **mysql** 编码来容纳这些字符。
2. 过滤掉这些特殊的表情字符。

第一种方法，网上很多，随便搜一下基本都是第一种方法。

但是我自己在使用过程中，感觉代价太大了，需要改动很多地方，有时候只是一个表的一个字段，这样的改动比较危险，不划算。

所有写了这个简单的工具，来对**四字节字符**进行编码，使其变成普通字符串存到数据库，用的时候再进行解码。

## 使用 ##
只有两个对外开放的方法，一个编码（**encoder**）、一个解码（**decoder**）

1. **encoder**
        
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
    

2. **decoder**
    
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
   
只需要在入库的时候，调用 **encoder** 编码字符串，查询出库后调用 **decoder** 解码字符串。

## 问题 ##
1. **过滤**

	这里过滤的范围是在网上查的：
   
	` Pattern pattern = Pattern.compile ("[\ud800\udc00-\udbff\udfff\ud800-\udfff]",Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE ) ;`
	
	需要过滤其他的可以修改这个正则表达式。

2. **字符串长度**

	编码后字符串会比原本的长，数据库字段设置了长度的需要注意一下。