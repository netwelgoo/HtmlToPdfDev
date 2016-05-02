package pdf.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * It is controller for File Name Rule 
 *  
 * @author pioneer(2016. 3. 17)
 * @since 1.0
 */
public enum FileNameMakeOption {

	NOTHING(""),
	YYMMDD_DATE(getDate("yyMMdd", new Date()));
	
	public final String option;
	
	FileNameMakeOption(String option){
		this.option = option;
	}
	
	public String value(){
		if(this.getClass().getSimpleName().endsWith("_DATE"))
			 return new String(new SimpleDateFormat("yyMMdd").format(new Date()));
	
		else return option;
	}
	
	private static String getDate(String format, Date date){
		return new String(new SimpleDateFormat(format).format(date));
	}
}
