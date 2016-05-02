package pdf.common;

import pluto.config.eMsSystem;

/**
 * Engine 과 WEB 서비스가 분리되어 서비스 됨으로 
 * Property interface를 아래와 같이 통일화 함.
 * 
 * @author pioneer(2016. 4. 1.)
 * @since 1.0
 */
public enum SysProperty {
	
	PdfTestDebug(eMsSystem.getProperty("pdf.test.debug", "Y")),
	
	/** crawling local HTML directory **/
	PdfLocalHtmlPath(eMsSystem.getProperty("pdf.local.html.path", "C:/project/PDF/sample/craw/")),
	
	/** Default Font directory assign for HTML to PDF converting **/
	PdfFontFilePath(eMsSystem.getProperty("pdf.font.files.path", "C:/Windows/Fonts/")),
	
	/** HTML charset assign for HTML to PDF converting **/
	HtmlCrawlingCharset(eMsSystem.getProperty("html.crawling.charset", "UTF-8")),
	
	/** created PDF File directory **/
	PdfOutFilePath(eMsSystem.getProperty("pdf.out.file.path", "C:/project/PDF/sample/craw/")),

	/** 개발자/디자이너가 HTML template를 임으로 수정할 경우가 있을 경우 file name에 alias 추가 
	 * ! 모든 첨부 PDF 파일에 적용 되므로 주의해서 사용 할 것.
	 * **/
	PdfContentFileNameOption(eMsSystem.getProperty("pdf.content.file.name.option", "")),

	/** 생성되는 PDF File 갯수별 디렉토리 분류 **/
	PdfFileCountUnit(eMsSystem.getProperty("pdf.file.count.unit", "1000")),  

	/** 개발자/디자이너가 HTML template를 임의로 수정할 경우 해당 디렉토리에 저장 **/
	PdfHtmlDesignPath(eMsSystem.getProperty("pdf.html.design.path", 
											"C:/project/gsshop/workspace/TV10_GS_ENGINE/EXTR/TMS/auto_email/pdf/design/"));  
	
	private String key;
	
	private SysProperty(String key){
		this.key = key;
	}
	
	public String value(){
		return key;
	}
}
