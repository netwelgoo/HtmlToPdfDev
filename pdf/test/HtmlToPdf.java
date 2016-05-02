package pdf.test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import pdf.convert.ConverterHtmlToPdf;

public class HtmlToPdf {
	public static void main(String[] args) {
		try {
			// 2. second way ========================================================
			String postId="123456789";
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			
//			String htmlContent = ConverterHtmlToPdf.htmlContent("C:/project/PDF/sample/craw/p_530.7164920971356/shinhan/shinhan.html");
//			String htmlContent = ConverterHtmlToPdf.htmlContent("C:/project/PDF/sample/craw/jsoup/jsoup.html");
			String htmlContent = ConverterHtmlToPdf.htmlContent("C:/web/Apache2.2/htdocs/test.html");
		
			ConverterHtmlToPdf converter = new ConverterHtmlToPdf(postId);
//			String pdfFileName = "C:/project/PDF/sample/craw/jsoup/jsoup.pdf";
			String pdfFileName = "C:/web/Apache2.2/htdocs/test.pdf";
			converter.createPdf(htmlContent, new File(pdfFileName), stream); //1.File write  2. Stream write 
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
