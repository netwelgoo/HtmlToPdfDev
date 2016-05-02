package pdf.test;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import pdf.convert.PdfAudience;
import pdf.convert.test.PdfWorkerContainerBck1;

public class WorkerContainerTest {
	public static final String htmlFileName = "C:/project/PDF/sample/pdf/temp/shinhan/shinhan.html";
	public static final String pdfFileName  = "C:/project/PDF/sample/pdf/temp/shinhan/out/";
	public static String htmlContent;
	
	static{
		try {
			htmlContent = FileUtils.readFileToString(new File(htmlFileName));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void main1(String[] args) {
		Properties in = new Properties();
		in.setProperty("in1", "in1");
		in.setProperty("in2", "in2");
		
		Properties out = new Properties();
		out.setProperty("out1", "out1");
		out.setProperty("out2", "out2");
		out.setProperty("in2", 	"out3");
		
		out.putAll(in);
		System.out.println("in" + in);

		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");

		System.out.println("out" + out);
	}
	
	
	public static void main(String[] args) throws Exception{
		for (int i = 1; i < 100; i++) {
			new Thread(""+i){
				public PdfAudience audience;
				
				public String mapping(String content, String mapping){
					return content.replace("M_A_P", mapping);
				}
				
				public void run(){
					String i = this.currentThread().getName();
					int post = Integer.parseInt(i)%5 ;
					String post_id 		= "1234567890123"+post;
					String emailAddress = "1234567890123"+i+"@humuson.com";
					String password 	= i;
					
					String mappedhtmlContent = mapping(htmlContent, i);
					
					audience = new PdfAudience(post_id, emailAddress, mappedhtmlContent, pdfFileName+i+".pdf", password);
					
					PdfWorkerContainerBck1.INSTANCE.convert(audience);
					
//					if(!post_id.equals(resultAudience.getPostId())){
//						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Exception <<<<<<<<<<<<<<<<<<<<<<<");
//						System.out.println("regist Postid["+post_id+"] result PostId["+resultAudience.getPostId()+"]");
//					}
//					
					int count=0;
					
//					while(!audience.isFinished()){
//						System.out.println("processing....."+ (count++));
//						try {
//							Thread.sleep(50);
//						} catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//						}
//					}
					System.out.println("Thread["+i+"]"+audience);
				}
				
			}.start();
		}
	}
}
