package com.itextpdf.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.w3c.tidy.Tidy;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * @author pioneer
 */
public class ParseHtml {
 
    public static final String HTML = "C:/project/htmlToPdf/in/long1.html";

    public static final String HTML1 ="C:/project/htmlToPdf/in/short.html";
        
    public static final String OUT  = "C:/project/htmlToPdf/out/";
    
    public static final boolean ENCRYPT		= true;

    public static final String USER_PASS	="aaa";
    
    public static final String OWNER_PASS	="aaa";
    
    public static int LIMIT_UNDER = 0;
    
    public static final int LIMIT = 100;
    
    public static long start;
    
    public static boolean END = false;
    
    public static synchronized void end(String i){
    	LIMIT_UNDER ++;
    	System.out.println(i);
    	if(LIMIT == LIMIT_UNDER) ParseHtml.END = true;
    }
    
    
    public static long log(long start, String message){
		 long e = System.currentTimeMillis();
	     
		 System.out.println(message+ " : " + (e-start));
	     
		 return e;
	}
    
    /**
     * Creates a PDF with the words "Hello World"
     * @param pdfFileName
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdf(String pdfFileName, int j) throws IOException, DocumentException {
        long s = System.currentTimeMillis();
 
    	Document document = new Document(PageSize.A4, 50,50,50,50);
    	
    	s = log(s, "create document time :");
    	
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
        
// page 설정 ========================================================
//      PdfPageEvent event = new PdfPageEvent();
//      writer.setBoxSize("crop", new Rectangle(50, 54, 559, 788));
//      writer.setPageEvent(event);
//=======================================================================
        
        s = log(s, "PdfWriter getInstance :");
        
        if(ENCRYPT){
        	writer.setEncryption(USER_PASS.getBytes(), OWNER_PASS.getBytes(),
        	       PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
        }
        document.open();
        
        s = log(s, "document.open()");
//        document.add(new Chunk("")); 
        
        // XMLWorkerHelper.getInstance() singleton class - don't multiprocess
        XMLWorkerHelper.getInstance().parseXHtml(
					        					writer, 
					        					document, 
					        					new FileInputStream(getHtmlData(j)), 
					        					Charset.forName("UTF-8"));
        
        s = log(s, "=== XMLWorker.parseXHtml==================================================== :");
                
        document.close();
    }
    
    public static String getHtmlData(int j){
    	if(j == 0) return HTML;
    	if(j % 2 == 0 ) return HTML;
    	else return HTML1;
    }
    
    public static void jtidyClean(String html) throws Exception{
        // Use JTidy to force html to xhtml
        Tidy tidy = new Tidy();
        tidy.setMakeClean(true);
        tidy.setXHTML(true);
        tidy.setBreakBeforeBR(false);
        tidy.setShowWarnings(false);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        tidy.parse( is, os ); 
        String fieldValue = os.toString();
    }
    
    
    public static void main(String[] args) throws IOException, DocumentException {
    	ParseHtml.start = System.currentTimeMillis();
        
//    	new Thread(){
//        	public void run(){
//        		while(!ParseHtml.END){
//        			try {
//        				Thread.sleep(100);
//					} catch (Exception e) {
//						// TODO: handle exception
//					}
//        		}
//        		System.out.println((System.currentTimeMillis() - ParseHtml.start)+"msec");
//        	}
//        }.start();
    	
    	for (int i = 0; i < LIMIT; i++) {
        	Thread t = new Thread(""+i){
        		public void run(){
        			String prcessName = this.currentThread().getName();
        			for (int j = 0; j < 10; j++) {
        				try {
        					File file = new File(OUT+prcessName+"/"+j+".pdf");
        		            file.getParentFile().mkdirs();
        		            new ParseHtml().createPdf(OUT+prcessName+"/"+j+".pdf", j);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
        			ParseHtml.end(prcessName);
        		}
        	};
        	t.start();
		}
    }
    
//    public static void main(String[] args) {
//    	String prcessName = "";
//		long s = System.currentTimeMillis();
//    	for (int j = 0; j < 10; j++) {
//			try {
//				File file = new File(OUT+prcessName+"/"+j+".pdf");
//	            file.getParentFile().mkdirs();
//	            new ParseHtml().createPdf(OUT+prcessName+"/"+j+".pdf");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		ParseHtml.end(prcessName);
//		s = log(s, "10건 생성 시간 ");
//	} 
}
