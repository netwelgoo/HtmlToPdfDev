package pdf.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;

import pdf.convert.ConverterHtmlToPdf;
import pdf.convert.PdfConvertContainer;

public class PdfWorker implements Callable<String>{

	public final String name ;
	
	public final String content;
	
	public PdfWorker(String name){
		this.name = name;
		this.content = "<html><head><body style='font-family: 돋움;'>"
	    		+ "<p>PDF 안에 들어갈 내용입니다.</p>"
	    		+ "<h3>한글, English, 漢字.</h3>"
				+ "한글 표시["+name+"] </body></head></html>";
	}
	
	public PdfWorker(String name, String content){
		this.name = name;
		this.content = content;
	}
	
//	public void htmlContent(String content){
//		this.content = content;
//	}
	
	public static String htmlContent(String fileName) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        
        StringBuffer buffer = new StringBuffer();
        
        while( (line = reader.readLine()) != null){
        	buffer.append(line+"\n");
        }
//        this.content = buffer.toString();
        return buffer.toString();
	}
	
	@Override
	public String call() throws Exception {
//		htmlContent("C:/project/PDF/sample/in/shinhan2.html");
		ConverterHtmlToPdf converter = PdfConvertContainer.INSTANCE.converter("1234567890123");

// 파일에 기록 : 2Page Html size 
//		  : Thread[10개] => 1000/17782ms (1시간:20만개 | CPU(100%) )	
//		  : Thread[5개]  => 1000/20351ms (1시간:18만개 | CPU(70~85%)
//		converter.createPdf(content, 
//							new File("C:/project/PDF/sample/test/everything."+name+".pdf"), 
//							new ByteArrayOutputStream());

		converter.createPdf(content, 
				new File("C:/project/PDF/sample/test/everything."+name+".pdf") 
				);
		
// byte[]기록 : 2Page Html size
//		: Thread[10]  => 1000/16157ms (1시간:22만개 | CPU(100%)   )		
//		: Thread[5]   => 1000/18558ms (1시간:19만개 | CPU(70~80%) )	

//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		converter.createPdf(content, out);
//		out.close();
		return null;
	}
	
}
