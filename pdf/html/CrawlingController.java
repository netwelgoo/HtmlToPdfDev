package pdf.html;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XmlSerializer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

import pdf.common.SysProperty;
import pdf.convert.ConverterHtmlToPdf;
import pdf.util.FileUtilProxy;
import pdf.util.HttpsContentWithoutValidation;
import pdf.util.StringUtil;
import pluto.lang.Tracer;

/**
 * By crawling the URL and save it to local repository   
 * 
 * @author pioneer(2016. 3. 22.)
 * @since 1.0
 */
enum RemoveTag{
	Tags("script,noscript");
	
	private final String tag;
	
	private RemoveTag(String tag){
		this.tag = tag;
	}
	
	public String value(){
		return this.tag;
	}
}
public class CrawlingController{
//	private static final Logger log = LoggerFactory.getLogger(CrawlingController.class);
	public static final boolean DEBUG				= ("Y".equalsIgnoreCase(SysProperty.PdfTestDebug.value()));
	public static final String DEFAULT_PATH			= SysProperty.PdfLocalHtmlPath.value();
	public static final String BACKUP_PATH			= "bck/";
	public static final String WEB_FILE_SUFFIX 		= ".web";
	public static final String HTML_FILE_EXTENSION 	= ".html";
	public static final String PDF_FILE_EXTENSION 	= ".pdf";
	private final String INTERPOSE_DIR		;
	
	private final HtmlInfo pdf;
	
	private HtmlCrawled result = new HtmlCrawled();
	private String imagePath;
	private String cssPath;
	private String jsPath;
	private String statePath;
	private String htmlHost;
	private Document document;
	
	public CrawlingController(HtmlInfo pdfInfo){
		INTERPOSE_DIR = pdfInfo.getTempDirectory();
		this.pdf = pdfInfo;
	}
	
	public HtmlCrawled result(){
		return result;
	}
	
	public String interposeDir(){
		return INTERPOSE_DIR;
	}
	
	public void createCrawlingFolder() throws Exception{
		statePath 	= DEFAULT_PATH+INTERPOSE_DIR+this.pdf.getPostId();
		Tracer.info("pdf create crawling directory["+statePath+"]");
		System.out.println("pdf create crawling directory["+statePath+"]");
		imagePath 	= statePath +"/img/";
		cssPath 	= statePath +"/css/";
		jsPath 		= statePath +"/js/";
    	
    	FileUtilProxy.createFolder(imagePath);
    	FileUtilProxy.createFolder(cssPath);
    	FileUtilProxy.createFolder(jsPath);
    }
	
	private String makeHtmlFile(){
		statePath 	= DEFAULT_PATH+INTERPOSE_DIR+this.pdf.getPostId();
		return statePath+"/"
			   + this.pdf.getPostId()
			   + WEB_FILE_SUFFIX
			   + HTML_FILE_EXTENSION;
	}
	
	private String convertLocalFileName(String fileSuffix){
		statePath 	= DEFAULT_PATH+INTERPOSE_DIR+this.pdf.getPostId();
		return statePath+"/"
				+this.pdf.getPostId()
				+fileSuffix;				
	}
	
	/**
	 * url에서 Web site를 crawling 하고 
	 * Local server에 기록한다.
	 * 
	 * @param urlFile
	 * @param destinationFile 목적 파일 명
	 * @throws IOException
	 * @throws Exception
	 */
	public String loadUrl(String urlFile, String destinationFile) throws IOException,Exception {
		String lowUrlFile = urlFile.toLowerCase();
		
		
		if(!lowUrlFile.startsWith("http")){
			FileUtils.copyFile(new File(urlFile), new File(destinationFile));
			return destinationFile;
			
		}else if(lowUrlFile.startsWith("https")){
			new HttpsContentWithoutValidation(urlFile).writeFile(destinationFile);
			return destinationFile;
		}
		URL url = new URL(urlFile);
//		URL url = new URL(URLEncoder.encode(urlFile));
		
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);
		try {
			byte[] b = new byte[2048];
			int length;
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
		return destinationFile;	
		} catch (Exception e) {
			Tracer.error("error load url["+urlFile+"] destination local file["+destinationFile+"] Exception "+e);
			e.printStackTrace();
			throw e;
			//이미지 저장 오류
		}finally{
			is.close();
			os.close();
		}
	}
	
	public String hostUrl(String url){
		String[] sep = StringUtil.urlSeparate(url);
		if(sep == null) return url;
		else{
			return sep[1]+"://"+sep[2]+sep[3];
		}
	}
	
	public boolean containTextCSS(String href){
	   	return href.toLowerCase().indexOf(".css") > 0 ;
	}
	
	public static String controlUrl(String urlFile){
		if(urlFile.startsWith("//")){
			return "http:"+urlFile;
		}
		return urlFile;
	}
	public String loadUrl(String urlFile, boolean encode) throws IOException,Exception {
    	urlFile = controlUrl(urlFile); 
		String saveFileName = urlFile.substring(urlFile.lastIndexOf("/")+1);
    	
    	if(containTextCSS(saveFileName)){
    		saveFileName = saveFileName.replace("?", "_"); // if css or image is add(?) then replace(_)
    	}
		return loadUrl((encode)?urlEncode(urlFile):urlFile, statePath + saveFileName);
	}
	
	public String urlEncode(String url){
		String[] sep = StringUtil.urlSeparate(url);
		if(sep == null) return url;
		else{
			try {
//				url = url.replace(sep[5], URLEncoder.encode(sep[5], "UTF-8"));
				url = url.replace(sep[7], URLEncoder.encode(sep[7], this.pdf.getCharset() ));
//				url = url.replace(sep[9], URLEncoder.encode(sep[9], "UTF-8"));s
				return url;
			} catch (Exception e) {
				Tracer.error("fail url encoding so return orginal data url["+url+"] Exception " +e);
				return url;
			}
		}
	}
	
	public boolean isIcon(String site, String icon){
		if("icon".equals(icon) || "shortcut icon".equals(icon)){
    		return containTextIcon(site);
    	}
    	return false;
	}
	
	public boolean containTextIcon(String href){
    	return href.toLowerCase().indexOf(".ico") > 0 ;
    }
	
	public boolean isJs(String js){
		return js.toLowerCase().indexOf(".js") > 0 ;
	}
	
    public boolean isCss(String href, String rel){
    	if("stylesheet".equals(rel)){
    		return containTextCSS(href);
    	}
    	return false;
    }
	
	public void crawlingWeb(String url, boolean local) throws Exception{
		Document doc;
		try {
			if(local){
				doc = Jsoup.parse(new File(url), this.pdf.getCharset(), htmlHost);
				this.document = doc;
			}
			else      doc = Jsoup.connect(url).get();
		} catch (Exception e) {
			Tracer.error("html parsing(Jsoup) "
							+ "failed url["+url+"] "
							+ "htmlHost["+htmlHost+"] Exception "+e);
			throw e;
		}
		Elements media 		= doc.select("[src]");
        Elements imports	= doc.select("link[href]");
        Elements removeTag 	= doc.select(RemoveTag.Tags.value());
        for (Element e : removeTag){e.remove();}  //script tag remove
        
        for (Element src : media) {
        	String site = src.attr("abs:src");
//        	Tracer.debug("html crawling tag src is ["+src.tagName()+"] site["+site+"]");

        	if("iframe".equals(src.tagName())){
    			if(site.length() == 0 ) continue;
    			Tracer.debug("itrame crawling reflash ["+src.tagName()+"] site["+site+"]");
    			crawlingWeb(site, false);
    		}
        	if (src.tagName().equals("img") && site.length() > 0){
        		try {
        			statePath = imagePath;
        			String replacedPath = loadUrl(site, true);
        			src.attr("src", replacedPath);  //replace path

    			} catch (Exception e) {
    				result.addImageErrors(site, e.toString());
    				Tracer.error("don't crawling load image[{}] Error[{}]", site, e);
    			}
        	}else{
        		if(isJs(site)){
        			statePath = jsPath;
        			Tracer.info("it's javascript tag site=" + site);
        			src.attr("src", loadUrl(site, false));
        		}else if(isCss(site, src.attr("rel"))){ //css 파일인지 참고 한다.
            		try {
            			statePath = cssPath;
                    	src.attr("src", loadUrl(site, false));
                    	crawlingWeb(site, false);
					} catch (Exception e) {
						result.addCssErrors(site, e.toString());
						Tracer.error("don't load css[{}] Error[{}]", site, e);
					}
                }
            }
        }
        
/** Call css =======================================*/        
        for (Element link : imports) {
            String site = link.attr("abs:href");
//if icon crawling
            if(isIcon(site, link.attr("rel"))){
            	statePath = imagePath;
            	String replacedPath = loadUrl(site, true);
            	link.attr("src", replacedPath);
            }
//if CSS crawling
            if(isCss(site, link.attr("rel"))){
            	try {
            		statePath = cssPath;
                	if(!link.attr("href").isEmpty()){
                		link.attr("href", loadUrl(site, false));
                	}else{
                		link.attr("src", loadUrl(site, false));
                	}
                	crawlingWeb(site, false);
				} catch (Exception e) {
					result.addCssErrors(site, e.toString());
					Tracer.error("don't load css[{}] Error[{}]", site, e);
				}
            }
        }
	}
	
	/**
	* It corrects a grammatical error in the HTML. 
	*  
	* @param htmlContent
	* @return
	*/
	public static String cleanHtml(String htmlContent){
		final HtmlCleaner cleaner = new HtmlCleaner();
        try {
        	final TagNode rootTagNode = cleaner.clean(htmlContent);

        	// set up properties for the serializer (optional, see online docs)
        	final CleanerProperties cleanerProperties = cleaner.getProperties();
        	//cleanerProperties.setOmitComments(true);  //HTML comments removed
        	cleanerProperties.setOmitXmlDeclaration(true);
        	cleanerProperties.setUseCdataForScriptAndStyle(false);
        	cleanerProperties.setUseEmptyElementTags(false);
        	cleanerProperties.setIgnoreQuestAndExclam(true);

        	// use the getAsString method on an XmlSerializer class
        	final XmlSerializer xmlSerializer = new PrettyXmlSerializer(cleanerProperties);
        	return xmlSerializer.getAsString(rootTagNode);
        } catch (Exception e) {
            e.printStackTrace();
            Tracer.error("fail clean html so return orginal Html content");
            return htmlContent;
        }  
	}
	
	public void execute(){
//1. set default path		
		try {
			createCrawlingFolder();  
		} catch (Exception e) {
			Tracer.error("pdf[9000] don't create crawling directory Exception "+e);
			result.resultCode("9000"); 
			return;
		}
		String postHtmlFile = makeHtmlFile();
		
//2. Receiving the URL information to be stored as File (Html).
//   File rule : postId.web.html [original]
		try {
			loadUrl(pdf.getUrl(), postHtmlFile);
		} catch (Exception e) {
			// TODO: handle exception
			Tracer.error("pdf[9100] don't load url["+pdf.getUrl()+"] Exception "+e);
			result.resultCode("9100");
			return;
		}
//3. crawling from local HTML(postId.web.html)
		try {
			htmlHost = hostUrl(pdf.getUrl());
			crawlingWeb(postHtmlFile, true);
		} catch (Exception e) {
			System.out.println("pdf[9200] error local Crawling htmlFile["+postHtmlFile+"] Exception "+e);
			Tracer.error("pdf[9200] error local Crawling htmlFile["+postHtmlFile+"] Exception "+ e);
			result.resultCode("9200");
			return;
		}
//4. Local Path로 설정된 html content를 file로 저장한다.
		String localHtml = "";
		String generatedHtml = "";
		try {
			localHtml = convertLocalFileName(HTML_FILE_EXTENSION);
			document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
			document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
			generatedHtml = bookmarkTagControl(cleanHtml(document.toString())); 
			
			if(DEBUG){
				Tracer.debug("crawled html start >> ");
				Tracer.debug(generatedHtml);
				Tracer.debug("<< crawled html end ");
			}
			
			FileUtilProxy.createFile(generatedHtml.getBytes(pdf.getCharset()), localHtml);
			this.pdf.setLocalHtmlFileName(localHtml);
			
		} catch (Exception e) {
			Tracer.error("pdf[9300] error save generated local File["+localHtml+"] Exception "+ e);
			result.resultCode("9300");
			return;
		}
//5. validation check for PDF converting 
		validatorCheck(localHtml);
	}
	
	public void validatorCheck(String localHtml){
		try {
			htmlValidatior(localHtml);
	        cssValidator();
		} catch (Exception e) {
			result.resultCode("9630");
		}
	}

	private void cssValidator() {
		File [] cssFileList = new File(cssPath).listFiles();
		for (File cssFile : cssFileList) {
			if(cssFile.isFile()) {
				if(! cssValidatorSuccess(cssFile)){
					result.resultCode("9620");
					return;
				}
			}
		}
	}

	private void htmlValidatior(String localHtml) throws Exception {
		Document doc = Jsoup.parse(new File(localHtml), this.pdf.getCharset(), htmlHost);
		Elements htmlTag  = doc.select(CrawlingBlockTag.html.value());
		
		if(!validatorSuccess(htmlTag, CrawlingBlockTag.html.value())) return;
	}
	
	public boolean cssValidatorSuccess(File cssFile){
		InputStream stream = null;
		boolean success = true;
		try {
			stream = new BufferedInputStream(new FileInputStream(cssFile));
			InputSource source = new InputSource(new InputStreamReader(stream));
			CSSOMParser parser = new CSSOMParser();
            CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, null);
            CSSRuleList ruleList = stylesheet.getCssRules();
            
            String[] cssProperty = CrawlingBlockTag.css.value().split(",");
            
            for (int i = 0; i < ruleList.getLength(); i++) 
            {
              CSSRule rule = ruleList.item(i);
              if (rule instanceof CSSStyleRule) 
              {
                  CSSStyleRule styleRule=(CSSStyleRule)rule;
                  CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                  for (int j = 0; j < styleDeclaration.getLength(); j++) 
                  {
                       String property = styleDeclaration.item(j).toLowerCase();
                       String value = styleDeclaration.getPropertyCSSValue(property).getCssText().toLowerCase();
                       for (int k = 0; k < cssProperty.length; k++) {
                    	   if(cssProperty[k].indexOf(":") > 0 ){
                    		   String[] vProperty = cssProperty[k].split(":");
                    		   if(vProperty[0].equals(property) && vProperty[1].equals(value)){
                    			   result.addCssErrors(property, value);
                    			   result.setResultMessage("CSS validator failed this property["+property+"] value["+value+"] checked ");
                    			   Tracer.error("CSS validator failed this property["+property+"] value["+value+"] checked ");
                    			   result.setResultCode("9690");
                    			   success = false;
                    		   }
                    	   }else{
                    		   if(cssProperty[k].equals(property)){
                    			   result.addCssErrors(property, value);
                    			   result.setResultMessage("CSS validator failed this property["+property+"] checked");
                    			   Tracer.error("CSS validator failed this property["+property+"] checked");
                    			   result.setResultCode("9690");
                    			   success =false;
                    		   }
                    	   }
                        }
                    }
                }
             }
             return success;
		} catch (Exception e) {
			Tracer.error("CSS Validator checking Exception " +e);
			result.resultCode("9691");
			return false;
		}finally{
			try {
				if(stream != null) stream.close();
			} catch (Exception e2) {
				Tracer.error("[se] Css file stream closing failed Exception "+ e2);
			}
		}
		
	}
	
	public String inlineStyles(String html, File cssFile, boolean removeClasses) throws IOException, FileNotFoundException {
	    Document document = Jsoup.parse(html);
	    CSSOMParser parser = new CSSOMParser();
	    InputSource source = new InputSource(new FileReader(cssFile));
	    CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, null);

	    CSSRuleList ruleList = stylesheet.getCssRules();
	    Map<Element, Map<String, String>> allElementsStyles = new HashMap();
	    for (int ruleIndex = 0; ruleIndex < ruleList.getLength(); ruleIndex++) {
	        CSSRule item = ruleList.item(ruleIndex);
	        if (item instanceof CSSStyleRule) {
	            CSSStyleRule styleRule = (CSSStyleRule) item;
	            String cssSelector = styleRule.getSelectorText();
	            Elements elements = document.select(cssSelector);
	            for (int elementIndex = 0; elementIndex < elements.size(); elementIndex++) {
	                Element element = elements.get(elementIndex);
	                Map<String, String> elementStyles = allElementsStyles.get(element);
	                if (elementStyles == null) {
	                    elementStyles = new LinkedHashMap<String, String>();
	                    allElementsStyles.put(element, elementStyles);
	                }
	                CSSStyleDeclaration style = styleRule.getStyle();
	                for (int propertyIndex = 0; propertyIndex < style.getLength(); propertyIndex++) {
	                    String propertyName = style.item(propertyIndex);
	                    String propertyValue = style.getPropertyValue(propertyName);
	                    elementStyles.put(propertyName, propertyValue);
	                }
	            }
	        }
	    }

	    for (Map.Entry<Element, Map<String, String>> elementEntry : allElementsStyles.entrySet()) {
	        Element element = elementEntry.getKey();
	        StringBuilder builder = new StringBuilder();
	        for (Map.Entry<String, String> styleEntry : elementEntry.getValue().entrySet()) {
	            builder.append(styleEntry.getKey()).append(":").append(styleEntry.getValue()).append(";");
	        }
	        builder.append(element.attr("STYLE_ATTR"));
	        element.attr("STYLE_ATTR", builder.toString());
	        if (removeClasses) {
	            element.removeAttr("CLASS_ATTR");
	        }
	    }

	    return document.html();
	}
	
	
	public boolean validatorSuccess(Elements tags, String checkTags){
		boolean success = true;
		StringBuffer errorTagsName = new StringBuffer();
		try {
			for (Element tag : tags) {
				String tagName = tag.tagName().toLowerCase();
				if(checkTags.indexOf(tagName) >=0 ){
					result.addCssErrors(tagName, "");
					Tracer.error("crawling html validator failed to tag["+tagName+"] checked ");
					result.setResultCode("9610");
					errorTagsName.append(tagName);
					errorTagsName.append(",");
					success = false;
				}
			}
			if(!success) 
			result.setResultMessage("crawling html validator failed. "
										+ "this tags["+errorTagsName.toString()+"] checked ");
			
			return success;
		} catch (Exception e) {
			Tracer.error("crawling html validator Exception "+e);
			result.resultCode("9611");
			return false;
		}
	}
	
	/**
	* bookmark Tag handling feature </br>
	* 
	* HtmlClean open source bug 
	* or HtmlClean has no this feature 
	* @param html
	* @return
	*/
	public static String bookmarkTagControl(String html){
		try {
			html = html.replaceAll("</bookmark>", "");
			int bookmarkIndex = html.indexOf("<bookmark");
			String[] htmls = html.split("\n");
			Map<String, String> bookmarkLines = new ConcurrentHashMap<String, String>();
			for (int i = 0; i < htmls.length; i++) {
				if(htmls[i].indexOf("<bookmark") >=0 ){
					String complete = htmls[i].replace(">", "/>");
					bookmarkLines.put(htmls[i], complete);
				}
			}
			Iterator iter = bookmarkLines.keySet().iterator();
			while(iter.hasNext()){
				String orgBookmarkText = iter.next().toString();
				html = html.replace(orgBookmarkText, bookmarkLines.get(orgBookmarkText));
			}
			return html;
		} catch (Exception e) {
			Tracer.error("bookmark tag control error org html return Exception  "+e );
			return html;
		}
	}
	
	
/**  crawling html file로  PDF File(post_id.pdf)을 생성한다.  **/
	public void createPdf(ByteArrayOutputStream pdfStream) {
		String localPdf = "";
		try {
			localPdf 		= convertLocalFileName(PDF_FILE_EXTENSION);
			ConverterHtmlToPdf chtp = new ConverterHtmlToPdf(this.pdf.getPostId());
			chtp.createPdf( new File(this.pdf.getLocalHtmlFileName()), 
							new File(localPdf), 
							pdfStream
						  );
		} catch (Exception e) {
			e.printStackTrace();
			Tracer.error("pdf[9400] fail create PDF File["+localPdf+"] Exception "+ e);
			result.resultCode("9400");
		}
	}
	
//	public static void main(String[] args) throws Exception{
//		String html = PdfWorker.htmlContent("C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/infos/20160317_4/20160317_4.web.html");
//		System.out.println(bookmarkTagControl(cleanHtml(html)));
//	}
	
	public static void main(String[] args) throws Exception{
		String url="http://localhost/shinhan.html";  //Crawling URL 
//		String url="C:/project/PDF/sample/craw/p_530.7164920971356/p_530.7164920971356.html";  //Crawling URL 
		String postId="humuson"; 					  //POST_ID	
		String stateCode="00"; //00=initial, 40=saved  //FIX value = 00 
		HtmlInfo pdf = new HtmlInfo(url, postId);
		try {
//Html Crawling =====================================================================
			HtmlCrawler crawler = CrawlingStateFactory.INSTANCE.instance(stateCode);
			crawler.htmlLoad(pdf);  //HTML Crawling 실행
			HtmlCrawled crawlerResult = crawler.result();
			
			System.out.println("result code["+crawlerResult.getResultCode() +"] "
							 + "result message="+crawlerResult.getResultMessage());
			
// PDF converter  ==========================================================================			
// 1. first way =========================================================			
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			crawler.createPdf(stream);  //pdf 변환

// 2. second way ==========================================================
//			String htmlContent = ConverterHtmlToPdf.htmlContent("C:/project/PDF/sample/craw/"+postId+"/"+postId+".html");
//			ConverterHtmlToPdf converter = new ConverterHtmlToPdf(postId);
//			String pdfFileName = "C:/project/PDF/sample/craw/"+postId+"/"+postId+".pdf";
//			converter.createPdf(htmlContent, new File(pdfFileName), stream); //1.File write  2. Stream write 
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}