package pdf.itext.sample;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.charset.Charset;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class HtmlToPdf {
	
	public void htmlToPdf(String text) throws Exception{
		Document document = new Document(PageSize.A4, 50, 50, 50, 50); 
		ByteArrayOutputStream out = new ByteArrayOutputStream();

//		PdfWriter writer = PdfWriter.getInstance(document, 
//				new FileOutputStream("C:/project/PDF/sample/out/shinhan2.pdf"));
        
		PdfWriter writer = PdfWriter.getInstance(document, out);
		
		writer.setInitialLeading(12.5f);  //기본 확대 율

//		String fileName = URLEncoder.encode("korean", "UTF-8"); 

		document.open();

//		XMLWorkerHelper helper = XMLWorkerHelper.getInstance();

//CSS() #####################################################
		CSSResolver cssResolver = new StyleAttrCSSResolver();
//		CssFile cssFile = helper.getCSS(new FileInputStream("C:/project/PDF/sample/css/pdf.css"));
//		cssResolver.addCss(cssFile);
//###########################################################

		XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);

		fontProvider.register("C:/project/PDF/sample/tmp/css/malgun.ttf", "돋움"); 

		fontProvider.register("C:/project/PDF/sample/font/arial.ttf"); 
		 
		CssAppliers cssAppliers 		= new CssAppliersImpl(fontProvider);

		HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);

//		htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
//		htmlContext.autoBookmark(true);
		
		htmlContext.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory());

		// Pipelines
		PdfWriterPipeline pdf 	= new PdfWriterPipeline(document, writer);

		HtmlPipeline html 		= new HtmlPipeline(htmlContext, pdf);

		CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

		XMLWorker worker = new XMLWorker(css, true);

		XMLParser xmlParser = new XMLParser(worker, Charset.forName("UTF-8"));

		String stext ="<html><head><body style='font-family: MalgunGothic;'>"
        		+"<p>PDF 안에 들어갈 내용입니다.</p>"
        		+"<h3>한글, English, 漢字.</h3>" 
				+"<div align='center' style='height:50px'>"
				    +"<H1>A Simple Sample Web Page</H1>"
				    +"<img src='http://sheldonbrown.com/images/scb_eagle_contact.jpeg'/>"
				    +"<H4>By Sheldon Brown</H4>"
				    +"<H2>Demonstrating a few HTML features</H2>"
			    +"</div>"
		        + "한글 표시 </body></head></html>";
		
		StringReader strReader = new StringReader(stext);
		xmlParser.parse(strReader);
		document.close();
		
		byte[] bcontent= out.toByteArray();
		
		FileOutputStream fout = new FileOutputStream("C:/project/PDF/sample/out/shinhanByte.pdf");
		fout.write(bcontent);
		
		writer.close();
	}
	
	public static void multi(String j){
		for (int i = 0; i < 10; i++) {
			new Thread(j+"_"+i){
				public void run(){
					try {
						String htmlText = null;
				//		BufferedReader reader = new BufferedReader(new FileReader("C:/project/PDF/sample/html/letter1.html"));
						BufferedReader reader = new BufferedReader(new FileReader("C:/project/PDF/sample/in/everything.html"));
						/**
						 * if <br> --> "", </br> --> <br />
						 */
				        String line;
				        
				        StringBuffer buffer = new StringBuffer();
				        
				        while( (line = reader.readLine()) != null){
				        	buffer.append(line+"\n");
				        }
				        System.out.println(buffer.toString());
						
//						new HtmlToPdf().htmlToPdf(buffer.toString(), this.currentThread().getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	public static void main(String[] args) throws Exception{
		
//		for (int i = 0; i < 10; i++) {
//			HtmlToPdf.multi(""+i);
//		}
		
		try {
			String htmlText = null;
	//		BufferedReader reader = new BufferedReader(new FileReader("C:/project/PDF/sample/html/letter1.html"));
			BufferedReader reader = new BufferedReader(new FileReader("C:/project/PDF/sample/in/shinhan2.html"));
			/**
			 * if <br> --> "", </br> --> <br />
			 */
	        String line;
	        
	        StringBuffer buffer = new StringBuffer();
	        
	        while( (line = reader.readLine()) != null){
	        	buffer.append(line+"\n");
	        }
	        System.out.println(buffer.toString());
			
			new HtmlToPdf().htmlToPdf(buffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
