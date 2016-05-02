package pdf.test;

import pdf.html.CrawlingStateFactory;
import pdf.html.HtmlCrawler;
import pdf.html.HtmlInfo;

public class HtmlCrawlerTest {
	
	public static void main(String[] args) {
//		String url="C:/project/PDF/sample/craw/p_530.7164920971356/p_530.7164920971356.html";  //Crawling URL 
		String url="http://jsoup.org/download";  //Crawling URL 
//		String url="http://localhost/shinhan.html";  //Crawling URL 
//		String url="http://localhost/shinhan.html";  //Crawling URL 
		String postId="jsoup"; 					  //POST_ID	
		String stateCode="00"; //00=initial, 40=saved  //FIX value = 00 

//FIXME 임시 directory 설정 방법 =====================================//		
//		String tempDirectory=StringUtil.createSequence();		 //
// 		tempDirectory :  DefaultPath/postId/tempDirectory/       //
//		HtmlInfo pdf = new HtmlInfo(url, postId, tempDirectory); //
//===============================================================//
		HtmlInfo pdf = new HtmlInfo(url, postId);
		
		try {
//HTML Crawling =====================================================================
			HtmlCrawler crawler =  CrawlingStateFactory.INSTANCE.instance(stateCode);
			crawler.htmlLoad(pdf);  //HTML Crawling run
			
// PDF convert  =====================================================================		
// 1. first way =========================================================			
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			crawler.createPdf(stream);  //pdf converting
//===================================================================================
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
}
