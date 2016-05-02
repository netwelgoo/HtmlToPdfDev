package pdf.util;


import pdf.itext.sample.ParseHtml;
import pluto.lang.Tracer;

public class StopWatchUtil {
	
	public static int LIMIT_UNDER = 0;
	
	public static final int LIMIT = 100;
	
    public static synchronized void end(String i){
    	LIMIT_UNDER ++;
    	System.out.println(i);
    	if(LIMIT == LIMIT_UNDER) ParseHtml.END = true;
    }
    
    public static long log(long start, String message){
		 long e = System.currentTimeMillis();
	     
		 System.out.println(message+ " : " + (e-start));
		 Tracer.debug(message+ " : " + (e-start));
		 return e;
	}
    
}
