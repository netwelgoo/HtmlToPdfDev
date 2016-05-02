package pdf.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	private static long lastAllocatedId = 0;

	public static final void convertString(StringBuffer tmpBuffer, String source, String target, String dest) {
		if (source == null)
			return;

		int idx1 = 0;
		int idx2 = 0;

		while (true) {
			idx1 = source.indexOf(target, idx2);

			if (idx1 < 0)
				break;

			tmpBuffer.append(source.substring(idx2, idx1));
			tmpBuffer.append(dest);

			idx2 = idx1 + target.length();
		}

		tmpBuffer.append(source.substring(idx2));

		return;
	}

	public static String createSequence() {
		long id = System.currentTimeMillis();
		if (id <= lastAllocatedId)
			id = lastAllocatedId + 1;

		lastAllocatedId = id;
		return Long.toString(id, 36).toUpperCase(Locale.ENGLISH);
	}

	/**
	 * URL Pattern 분리
	 *
	 * String[0]=http://www.xxx.com/blog/hwang3337/img/image.jpg?java=io.IOException: 
	 * String[1]=http(s) 
	 * String[2]=www.xxx.com 
	 * String[3]=:8080
	 * String[4]=8080 
	 * String[5]=/blog/hwang3337/img 
	 * String[6]=/img
	 * String[7]=image.jpg 
	 * String[8]=?java=io.IOException:
	 * String[9]=java=io.IOException: 
	 * String[10]=null
	 * 
	 * @param url
	 * @return
	 */
	public static String[] urlSeparate(String url) {
		Pattern urlPattern = Pattern.compile(
				"^(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$");
		Matcher mc = urlPattern.matcher(url);
		if (mc.matches()) {
			String[] result = new String[mc.groupCount()];
			for (int i = 0; i < mc.groupCount(); i++) {
				result[i]=nullToSpace(mc.group(i));
			}
			return result;
		} else {
			System.out.println("not found");
		}
		return null;
	}
	
	public static String nullToSpace(String data){
		if(data == null){
			return "";
		}else{
			return data;
		}
	}

	public static void main(String[] args) {
		String[] data = StringUtil.urlSeparate("http://www.kangcom.com:8080/blog/hwang3337/img/나ㅋㅋ.jpg?java=자바&point=포인트");
//		String[] data = StringUtil.urlSeparate("http://www.kangcom.com:8080/blog");
//		String[] data = StringUtil.urlSeparate("http://www.kangcom.com/blog");
		for (int i = 0; i < data.length; i++) {
			System.out.println("data["+i+"]="+data[i]);
		}
		String result = data[1]+"://"+data[2]+data[3];
		System.out.println(result);
		
		System.out.println("1 bit 1 << 1 is (2 log 1) test="+(1 << 1));
		System.out.println("2 bit 1 << 2 is (2 log 2)  test="+(1 << 2));
		System.out.println("3 bit 1 << 3 is (2 log 3)  test="+(1 << 3));
		
	}

}
