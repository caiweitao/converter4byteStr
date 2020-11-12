package caiweitao.converter.tools;

/**
* @author caiweitao
* @Date 2020年11月12日
* @Description 
*/
public class Test {
	public static void main(String[] args) {
		String str = "aaa假按🍎揭啊🍎&#🍎&#1777777777771⏰🍁;";
		System.out.println("原字符串："+str);
		String converter = Converter.encoder(str);
		System.out.println("编码后："+converter);
		String decoder = Converter.decoder(converter);
		System.out.println("解码后："+decoder);

	}
}
