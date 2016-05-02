package pdf.convert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.w3c.tidy.Tidy;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
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

import pdf.font.ItextFontRegister;
import pluto.lang.Tracer;

/**
 * HTML to PDF converter with iText(5.5.5 version)
 * - font is common instance get @Link{XMLWorkerFontProvider}(singleton)
 * - encryption is AES_128
 * 
 * @author pioneer(2016. 4. 4.)
 * @since 1.0
 */
public class ConverterHtmlToPdf {
	
//	private static final Logger log = LoggerFactory.getLogger(ConverterHtmlToPdf.class);
	
	private PdfInfomation pdfInfo;

	public static final String EMPTY="";
	
	private XMLWorkerFontProvider fontProvider;
	
	private boolean success = true;
	
	public ConverterHtmlToPdf(String postId){
//TODO DB 에서 PdfInfomation를 받아온 다음 생성한다.
		this.pdfInfo = new PdfInfomation(postId);
		registPdfElement();
	}
	
	public ConverterHtmlToPdf(PdfInfomation pdfInfo){
		this.pdfInfo = pdfInfo;
		registPdfElement();
	}
	
	public String postId(){
		return pdfInfo.getPostId();
	}
	
	public void registPdfElement(){
		try {
			fontProvider = ItextFontRegister.INSTANCE.fontProvider();
		} catch (Exception e) {
			Tracer.error("fail register font provider Exception "+e);
		}
	}
	
	public byte[] createPdf(String htmlContent){
		return createPdf(htmlContent, "");
	}
	
	public byte[] createPdf(String htmlContent, String password){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			createPdf(htmlContent, password, out);
			return out.toByteArray();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}finally{
			try {
				out.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public void createPdf(File htmlContent, File fileName, ByteArrayOutputStream out){
		String content="";
		try {
			content = FileUtils.readFileToString(htmlContent);
			createPdf(content,  "", fileName, out);
		} catch (Exception e) {
			// TODO: handle exception
			Tracer.error("error create PDF. html content["+content+" Exception="+e);
		}
	}
	
	public static String htmlContent(String htmlFile){
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(htmlFile));
	        String line;
	        
	        while( (line = reader.readLine()) != null){
	        	buffer.append(line+"\n");
	        }
		} catch (Exception e) {
			Tracer.error("file content loading error Exception is "+e);
		}
		return buffer.toString();
	}
	
    public static String htmlClean(String html){
        // Use JTidy to force html to xhtml
        Tidy tidy = new Tidy();
        tidy.setMakeClean(true);
        tidy.setXHTML(true);
        tidy.setXmlTags(true);
        tidy.setBreakBeforeBR(false);
        tidy.setShowWarnings(false);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
        	InputStream is = new ByteArrayInputStream(html.getBytes());
            tidy.parse( is, os );
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
		}
         System.out.println("START CONTENT =======================");
         System.out.println(os.toString());         
         System.out.println("END CONTENT =======================");
       return os.toString();
    }
	
    public void createPdf(String htmlContent, File outputFileName) throws Exception{
    	createPdf(htmlContent, "", outputFileName);
    }
    
    public void createPdf(String htmlContent, String password, File outputFileName)
    throws Exception{
    	FileOutputStream fout = null;
    	try {
    		fout = new FileOutputStream(outputFileName);
    		createPdf(htmlContent, password, fout);
		} catch (Exception e) {
			Tracer.error("don't create PDF so delete pdf file. Exception is "+e);
			removeFailPdfFile(outputFileName);
		}finally{
			try {
				if(fout != null) fout.close();
			} catch (Exception e2) {
				// TODO: handle exception
				Tracer.error("[se] don't stream close]"+e2);
			}
		}
    }
    
    public void removeFailPdfFile(File pdfFile, int count) throws Exception{
    	if(pdfFile.exists()){
			if(!pdfFile.delete()){
				Tracer.error("don't pdf file["+pdfFile.getName()+"] delete so file name rename ");
				if(pdfFile.renameTo(new File(pdfFile.getName() +".error")))
					Tracer.error("rename to["+pdfFile.getName()+".error File name");
				else{
					if(count > 10){
						Tracer.error("[se] pdf file["+pdfFile+"] delete or rename failed");
						throw new Exception("[se] pdf file delete or rename failed");
					}
					count++;
					removeFailPdfFile(pdfFile, count);
				}
			}
		}
    }
    
    public void removeFailPdfFile(File pdfFile) throws Exception{
    	removeFailPdfFile(pdfFile, 0);
	}
    
	public void createPdf(String htmlContent, File fileName, ByteArrayOutputStream out)
	throws Exception{
		createPdf(htmlContent,  "", fileName, out);
	}
	
	public void createPdf(String htmlContent, String password, File outputFileName, ByteArrayOutputStream out)
	throws Exception{
		byte[] bcontent= out.toByteArray();
		FileOutputStream fout = null;
		try {
			createPdf(htmlContent, password, out);
			if(bcontent == null){
				Tracer.error("Exception html to pdf so return don't write file["+outputFileName.getName()+"]");
				return; //error 났을 경우
			}
			fout = new FileOutputStream(outputFileName);
			fout.write(out.toByteArray());
			fout.flush();
		} catch (Exception e) {
			Tracer.error("don't PDF create so pdf file delete. Exception is "+e);
			removeFailPdfFile(outputFileName);
			throw e;
		}finally{
			try {
				bcontent = null;
				if(fout != null) fout.close();
			} catch (Exception e2) {
				Tracer.error("[se] FileoutStream close Exception "+ e2);
			}
		}
	}
	
	public void createPdf(String htmlContent, ByteArrayOutputStream out) throws Exception{
		createPdf(htmlContent, "", out);
	}
	
	public void createPdf(String htmlContent, String password, OutputStream out)
	throws Exception{
		Document document = new Document(pdfInfo.getPageSize(),
											pdfInfo.getTop(), 
											pdfInfo.getBottom(), 
											pdfInfo.getLeft(), 
											pdfInfo.getRight()
							   			);
		
		PdfWriter writer = null;
		try {
			writer = PdfWriter.getInstance(document, out);

			writer.setCompressionLevel(9);  //압축 사이즈 Level 설정
			
			writer.setInitialLeading(pdfInfo.getViewSize());

			writer.setViewerPreferences(
										  PdfWriter.PageModeUseOutlines    
										| PdfWriter.FitWindow 
										| PdfWriter.PageModeUseThumbs
//										| PdfWriter.HideToolbar
//										| PdfWriter.PageModeFullScreen
										);
//			writer.setPdfVersion(PdfWriter.VERSION_1_7); //set PDF Version
			
			encrypt(writer, password);
			document.open();

			XMLWorkerHelper helper = XMLWorkerHelper.getInstance();
			CSSResolver cssResolver = new StyleAttrCSSResolver();
			for(String cssFile: pdfInfo.getCssList()){
//				cssResolver.addCss(helper.getCSS(new FileInputStream(cssFile)));
				cssResolver.addCss(
							helper.getCSS(
									new FileInputStream(
											new File("C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/infos/20160331_4/css/style.css"))));
			}
			CssAppliers cssAppliers 		= new CssAppliersImpl(fontProvider);
			HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
			htmlContext.setAcceptUnknown(true)
					   .autoBookmark(true)
					   .setTagFactory(Tags.getHtmlTagProcessorFactory());
			
			PdfWriterPipeline pdf 	= new PdfWriterPipeline(document, writer);
			HtmlPipeline html 		= new HtmlPipeline(htmlContext, pdf);
			
			CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);
			XMLWorker worker 		= new XMLWorker(css, true);
			XMLParser xmlParser 	= new XMLParser(worker, Charset.forName("UTF-8"));
			
			StringReader strReader  = new StringReader(htmlContent);
			xmlParser.parse(strReader);
			document.close();
			writer.close();
			
		} catch (Exception e) {
			this.success = false;
			Tracer.error("converter html To pdf failed so file delete. "
					    + "content ["+htmlContent+"] failed Exception "+e);
			
			
			throw e;
		}finally{
			try {
				document.close();
				if(writer !=null) writer.close();
			} catch (Exception e2) {
				// TODO: handle exception
				Tracer.error("[se] don't pdf document close & writer so input null Exception "+e2);
				document = null;
				writer = null;
			}
		}
	}
	
	public void encrypt(PdfWriter writer, String password) throws Exception{
		if(password == null) return;
		if(!EMPTY.equals(password)){
			try {
				writer.setEncryption( password.getBytes(), 
						password.getBytes(), 
						PdfWriter.ALLOW_PRINTING, 
						PdfWriter.ENCRYPTION_AES_128
					 );
				writer.createXmpMetadata();	
			} catch (Exception e) {
				Tracer.error("pdf encrypting error Exception "+e);
				throw e;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		try {
			String postId = "1234567890129";
//			String text = ConverterHtmlToPdf.htmlContent("C:/project/PDF/sample/pdf/temp/developerworks/developerworks.html");
//			String text = ConverterHtmlToPdf.htmlContent("C:/project/gsshop/workspace/TV10_GS_ENGINE/engine/src/75_pdf/doc/bookmark.html");
//			String text = ConverterHtmlToPdf.htmlContent("C:/project/gsshop/workspace/TV10_GS_ENGINE/engine/src/75_pdf/doc/Last_success_bookmark.html");
//			String text = ConverterHtmlToPdf.htmlContent("C:/web/Apache2.2/htdocs/shinhanBookmark.html");
			String text = ConverterHtmlToPdf.htmlContent("C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/infos/20160323_1/20160323_1.html");
			ConverterHtmlToPdf converter = new ConverterHtmlToPdf(postId);
//			converter.createPdf(text, new File("C:/project/PDF/sample/pdf/temp/bill_v1.9.pdf"), new ByteArrayOutputStream());	
//			converter.createPdf(text, new File("C:/web/Apache2.2/htdocs/shinhanBookmark.pdf"), new ByteArrayOutputStream());	
			converter.createPdf(text, new File("C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/infos/20160323_1/20160323_1.pdf"), new ByteArrayOutputStream());	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void main1(String[] args) {
		String postId = "1234567890123";
//		FontContainer fc = FontContainer.getInstance();
//		fc.registerFontList("C:/Windows/Fonts/");

		PdfInfomation pdfInfo = new PdfInfomation(postId);
//		pdfInfo.setFont("돋움");
		try {
			ConverterHtmlToPdf converter = new ConverterHtmlToPdf(pdfInfo);
			PdfConvertContainer.INSTANCE.registConverter(converter);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i <1; i++) {
			new Thread("No["+i+"]"){
				String postId = "1234567890123";

				public void run(){
					String im = this.currentThread().getName();
					
//			String text = "<html><head><body style='font-family: 돋움;'>"
//	       		+ "<p>PDF 안에 들어갈 내용입니다.</p>"
//	       		+ "<h3>한글, English, 漢字.</h3>"
//	   			+ "한글 표시["+im+"] </body></head></html>";
					String text = "";
					try {
						text = ConverterHtmlToPdf.htmlContent("C:/project/PDF/sample/in/bookmark.html");	
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					ConverterHtmlToPdf converter = PdfConvertContainer.INSTANCE.converter(postId);
//파일에 기록 : 1000개 / 17~8초 소요(2Page Html)					
					try {
						converter.createPdf(text, new File("C:/project/PDF/sample/test/everything."+im+".pdf"), new ByteArrayOutputStream());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
//					converter.createPdf(text, new ByteArrayOutputStream() );
				}
			}.start();
		}
	}
}
