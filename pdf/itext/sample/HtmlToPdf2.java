package pdf.itext.sample;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class HtmlToPdf2 {
	
	public void htmlToPdf(String text, String i) throws Exception{
		Document document = new Document(PageSize.A4, 50, 50, 50, 50); 

// PdfWriter 생성
		PdfWriter writer = PdfWriter.getInstance(document, 
				new FileOutputStream("C:/project/PDF/workspace/FOP/sample/out/s/everything."+i+".pdf"));

//		writer.setInitialLeading(12.5f);

		String fileName = URLEncoder.encode("한글파일명", "UTF-8"); 

		document.open();

//		XMLWorkerHelper helper = XMLWorkerHelper.getInstance();

//## CSS(html 내에 css 정보를 가져옴)##############################
		CSSResolver cssResolver = new StyleAttrCSSResolver();
//		CssFile cssFile = helper.getCSS(new FileInputStream("C:/project/PDF/sample/css/pdf.css"));
//		cssResolver.addCss(cssFile);
//###########################################################

		// HTML, 폰트 설정
		XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);

		fontProvider.register("C:/project/PDF/sample/tmp/css/malgun.ttf", "MalgunGothic"); // MalgunGothic은 alias, 

		fontProvider.register("C:/project/PDF/sample/font/arial.ttf"); // MalgunGothic은 alias, 
		 
		CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);

		HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);

		htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());

		// Pipelines
		PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);

		HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);

		CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

		XMLWorker worker = new XMLWorker(css, true);

		XMLParser xmlParser = new XMLParser(worker, Charset.forName("UTF-8"));

		// 폰트 설정에서 별칭으로 줬던 "MalgunGothic"을 html 안에 폰트로 지정한다.
		String stext = "<html><head><body style='font-family: MalgunGothic;'>"
	            		+ "<p>PDF 안에 들어갈 내용입니다.</p>"
	            		+ "<h3>한글, English, 漢字.</h3>"
	        			+ "한글 표시 ["+i+"] </body></head></html>";

		StringReader strReader = new StringReader(text);
		xmlParser.parse(strReader);
		document.close();
		writer.close();
	}
	
	public static void multi(String j){
		for (int i = 0; i < 100; i++) {
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
						
						new HtmlToPdf2().htmlToPdf(buffer.toString(), this.currentThread().getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		
		for (int i = 0; i < 300; i++) {
			HtmlToPdf2.multi(""+i);
		}
	}
}
