package pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import freemarker20.template.SimpleHash;
import mercury.contents.common.body.SimpleMailBody;
import mercury.contents.common.message.Message;
import mercury.contents.common.util.TrackingInfoConvertor;
import pdf.convert.ConverterHtmlToPdf;
import pdf.util.DateUtil;
import pdf.util.FileNameMaker;
import pdf.util.FileRemoveController;
import pdf.util.FileUtilProxy;
import pluto.config.eMsSystem;
import pluto.io.FileElement;
import pluto.lang.Tracer;
import pluto.lang.eMsLocale;
import pluto.util.Cal;
import pluto.util.StringConvertUtil;
import pluto.util.convert.BASE64;
import venus.spool.common.basic.SpoolInfo;
import venus.spool.common.basic.SpoolInfoManager;

/**
 * pdf content attach mail body
 * 
 * @author pioneer(2016. 4. 5.)
 * @since 1.0 (2016. 03. 14)
 * @since 1.1 sending PDF file remove
 */
public class PdfMailBody extends SimpleMailBody {
	protected static final boolean	INNER_DEBUG					= false;

	protected static String			DEFAULT_ID					= "";

	protected static String			DEFAULT_CODE				= "";

	protected static String			OBJECT_REPLACE				= "####OBJECT####";

	protected static String			DEFAULT_BASE_FILE_NAME		= null;

	protected static String		    PDF_CHARSET					= null;
	
	protected String				PDF_INTRO_TEMPLATE			= null;

	protected static String			PDF_ATTACH_FILE_NAME		= null;
	
	public static synchronized void init(Object tmp) throws Exception {
		Properties prop = (Properties) tmp;

		PDF_ATTACH_FILE_NAME 		= prop.getProperty("pdf.attach.file.name", "message.pdf");
		OBJECT_REPLACE 				= prop.getProperty("pdf.object.index");
		DEFAULT_BASE_FILE_NAME 		= prop.getProperty("default.base.url");
		PDF_CHARSET = prop.getProperty("pdf.charset","euc-kr");
	}
	
	public PdfMailBody() throws Exception {
		super();
	}
	
	protected String fileContentToString(String fullPathFileName, boolean isBase64, boolean remove) 
			throws Exception, UnsupportedEncodingException, IOException {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream(2048);
			out.reset();
			FileElement.putFileBodyToStream(fullPathFileName, out);
			//this.BASE_TEMPLATE = out.toString();
			
//FIXME pioneer(2016.04.05) - PDF file remove			
			if(remove) FileRemoveController.INSTNACE.remove(fullPathFileName);
			
			return (isBase64) ? new BASE64().encode(out.toByteArray()) :
							    new String(out.toByteArray(), eMsLocale.MAIL_BASE_CHAR_SET);
		
		} finally {
			if(out!=null)
			out.close();
			out = null;
		}
	}
	
	protected String getPDFAttachFileName(SimpleHash prop) throws Exception{
		return new FileNameMaker<SimpleHash>(){
			
			@Override	
			public String makeFileName(SimpleHash prop, String option) 
				throws Exception {
					try {
						return "_"+option+".pdf";
					} catch (Exception e) {
						return "default_"+option+".pdf";
					}
				}
			}.makeFileName(prop, DateUtil.getToday("yyyyMMdd"));
	}
	
	public synchronized String getMailBody(Object info1, Object info2, Properties SCHEDULE_INFO) throws Exception {
		SimpleHash __INFO1__ = (SimpleHash) info1;

// 초기 메세지 원문을 암호화 하는 것을 하부 구현 클래스에서 받아온다.
		String body_string = getPdfMailBody(info1, info2, SCHEDULE_INFO);
		
//		String domain = StringUtil.getDomain(__INFO1__.getAsStringWithError("EMS_M_EMAIL"));

		// 작업버퍼를 초기화한다.
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream(2048);
		StringBuffer buffer = new StringBuffer(2048);
		String returnStr = "";
		
		try {

			// add default header
			setHeaderString(info1, info2, SCHEDULE_INFO, byteBuffer);
		
			/* create boundary */
			String boundary = getBoundaryString();

			/* add mime message header */
			byteBuffer.write("Content-Type: multipart/mixed;\r\n".getBytes());
			byteBuffer.write("\tboundary=\"".getBytes());
			byteBuffer.write(boundary.getBytes(eMsLocale.MAIL_BASE_CHAR_SET));

			/* 헤더와 본문의 구분 개행 문자 추가 */
			byteBuffer.write("\"\r\n\r\n\r\n".getBytes());

			/* add mime message */
			byteBuffer.write("This is a multi-part message in MIME format.\r\n\r\n".getBytes());

			
			// intro Html Template
			String INTRO_TEMPLATE = "";
			String EMS_M_SECURE = __INFO1__.getAsString("EMS_M_SECURE", "Y");

			INTRO_TEMPLATE = fileContentToString(SCHEDULE_INFO.getProperty("SECURE_BASE_TEMPLATE"), false, false);
 
			// intro template mapping 
			String introHtmlTemplate = introTemplateMapping(info1, SCHEDULE_INFO, buffer, INTRO_TEMPLATE);
			
			//add boundary 
			byteBuffer.write("\r\n--".getBytes());
			byteBuffer.write(boundary.getBytes());
			byteBuffer.write("\r\n".getBytes());
			byteBuffer.write("Content-Type: text/html;\r\n\tcharset=\"".getBytes());
			byteBuffer.write(eMsLocale.MAIL_MIME_CHAR_SET.getBytes());
			byteBuffer.write("\"\r\nContent-Transfer-Encoding: 8bit\r\n\r\n".getBytes());
			
//			MimeConvertor.putMimeToStream(byteBuffer, introHtmlTemplate, eMsLocale.MAIL_BASE_CHAR_SET, eMsLocale.MAIL_MIME_CHAR_SET, eMsTypes.ENC_BASE64);

			//add intro template
			byteBuffer.write(introHtmlTemplate.getBytes());
			
			byteBuffer.write("\r\n\r\n".getBytes());
			
			byteBuffer.write("\r\n--".getBytes());
			byteBuffer.write(boundary.getBytes());
			byteBuffer.write("\r\n".getBytes());
			byteBuffer.write("Content-Type: text/html;\r\n\tname=\"".getBytes());

			String attach_file_name = "";
			attach_file_name = SCHEDULE_INFO.getProperty("PDF_ATTACH_FILE_NAME", "humuson") 
							   + getPDFAttachFileName(__INFO1__);

			//add attach pdf file name
			byteBuffer.write(  attach_file_name.getBytes() );
			byteBuffer.write("\"\r\nContent-Transfer-Encoding: base64\r\n".getBytes());
			byteBuffer.write("Content-Disposition: attachment;\r\n\tfilename=\"".getBytes());
			byteBuffer.write( attach_file_name.getBytes() );
			byteBuffer.write("\"\r\n\r\n".getBytes());
			
			//add attach pdf file content 
			byteBuffer.write(body_string.getBytes());

// 보안처리된 스트링 붙이기
//			MimeConvertor.putMimeToStream(byteBuffer, body_string, eMsLocale.MAIL_BASE_CHAR_SET, eMsLocale.MAIL_MIME_CHAR_SET, eMsTypes.ENC_BASE64);

			byteBuffer.write("\r\n".getBytes());

			byteBuffer.write("\r\n--".getBytes());
			byteBuffer.write(boundary.getBytes());
			byteBuffer.write("--\r\n\r\n".getBytes());

			returnStr = byteBuffer.toString();
			
		} catch(Exception e) {
			
			throw new Exception(e);
		} finally {

			byteBuffer.close();
			byteBuffer = null;
		}
		
		return returnStr;
	}

	private String introTemplateMapping(Object info1, 
										Properties SCHEDULE_INFO, 
										StringBuffer buffer,
										String INTRO_TEMPLATE) {
		
		addOpenTagInIntroTemplate(info1, SCHEDULE_INFO, buffer, INTRO_TEMPLATE);
		
		StringBuffer introTemplateContent = new StringBuffer();
		
		StringConvertUtil.ConvertString(introTemplateContent, buffer.toString(), info1, "${", "}", false, false);
		
		return introTemplateContent.toString();
	}
	
	/** HTML content add on for open tracking  */
	private void addOpenTagInIntroTemplate(Object info1, 
			Properties SCHEDULE_INFO, 
			StringBuffer buffer,
			String INTRO_TEMPLATE) 
	{
		try {
			StringBuffer TMP_BUFFER = new StringBuffer();
			TMP_BUFFER.setLength(0);
			TMP_BUFFER.append(eMsSystem.getProperty("tracking.url").concat("?${enc_mid}&${enc_s_type}&p_id=${p_id}&m_id=${m_id}&s_tp=${s_type}&"));
			TMP_BUFFER.append(TrackingInfoConvertor.enc_LIST_TABLE(SCHEDULE_INFO.getProperty("LIST_TABLE")));
			TMP_BUFFER.append("&");
			TMP_BUFFER.append(TrackingInfoConvertor.enc_MAIL_ID(SCHEDULE_INFO.getProperty("POST_ID")));
			TMP_BUFFER.append("&");
			TMP_BUFFER.append(TrackingInfoConvertor.enc_SERVER_ID(SCHEDULE_INFO.getProperty("SERVER_ID")));
			TMP_BUFFER.append("&");
			TMP_BUFFER.append(TrackingInfoConvertor.enc_CLOSE(SCHEDULE_INFO.getProperty("CLOSE_DATE", Cal.getAddDayDate(7))));
			
			String head = null;
			String tail = null;
			String output = buffer.toString();
			
			int idxBodyEnd = INTRO_TEMPLATE.toLowerCase().indexOf("</body");

			if( idxBodyEnd > 0 ) {
				head = INTRO_TEMPLATE.substring(0, idxBodyEnd);
				tail = INTRO_TEMPLATE.substring(idxBodyEnd);
			}else {
				head = INTRO_TEMPLATE;
				tail = "";
			}
			buffer.setLength(0);
			buffer.append(head);
			buffer.append("<div style=\"display:none\">");
			buffer.append("<IMG width=0 height=0 src=\"");
			buffer.append(TMP_BUFFER.toString());
			buffer.append("&");
			buffer.append(TrackingInfoConvertor.enc_KIND("O"));
			buffer.append("\"/>");
			buffer.append("</div>");
			buffer.append(tail);

		} catch (Exception e) {
			Tracer.error("Intro open tag add failed so orginal template return Exception "+e);
			buffer.setLength(0);
			buffer.append(INTRO_TEMPLATE);
		}
	}
	
	/**
	 * Itro template value setting(this.PDF_INTRO_TEMPLATE)
	 * and Loading PDF attach file
	 */
	protected synchronized String getPdfMailBody(Object info1, Object info2, Properties SCHEDULE_INFO) 
	throws Exception {
//		 pdfIntroContent(SCHEDULE_INFO);  //intro Template input to this.PDF_INTRO_TEMPLATE
		 
		 SimpleHash __INFO1__ = (SimpleHash) info1;

		 String pdfFullPathFileName =FileUtilProxy.pdfAttachFileName(
													 __INFO1__.get("p_id").toString(), 
													 __INFO1__.get("EMS_M_ID").toString(), 
													 __INFO1__.get("EMS_M_ID_SEQ").toString());
		 
		 //TODO pioneer(2016.03.23) 매핑 기능 재확인 필요
		 /** 개별 재 발송일 경우 첨부 파일이 삭제 되었을 수 있음으로 *
		   * 파일이 없으면 다시 만들어 준다.                **/
		 if(isPrivateResend(__INFO1__)){
			 pdfFullPathFileName = pdfFullPathFileName.replace("^", "-");
			 Tracer.info("make pdf file for resend. pdf email attachFileName["+pdfFullPathFileName+"]");
			 reMakePdfFile(pdfFullPathFileName, __INFO1__, SCHEDULE_INFO);
		 }
		 
		 /** 에러메일 Queue 발송인 경우 PDF 파일이 삭제되어 있음으로 다시 생성해 준다.  */
		 if(isErrorResend(__INFO1__)){
			 Tracer.info("make pdf file for queue(error) send. pdf email attachFileName["+pdfFullPathFileName+"]");
			 reMakePdfFile(pdfFullPathFileName, __INFO1__, SCHEDULE_INFO);
		 }
		 
		 Tracer.info("m_id ["+__INFO1__.get("m_id")+"]");
		 return fileContentToString(pdfFullPathFileName, true, true);
	}
	
	private boolean isErrorResend(SimpleHash __INFO1__){
		return("Y".equals(__INFO1__.getAsString("NEXT_GEN", "")));
	}
	
	private boolean isPrivateResend(SimpleHash __INFO1__){
		return("AUTORESEND".equals(__INFO1__.getAsString("s_type")));
	}

	private void reMakePdfFile(String attachFileName, SimpleHash __INFO1__, Properties SCHEDULE_INFO) 
	throws Exception{
		
		if(new File(attachFileName).exists()) return;
		
		String postId = __INFO1__.get("p_id").toString();
		SpoolInfo sendSpoolInfo 	 = SpoolInfoManager.getSpoolInfo(postId);
		ConverterHtmlToPdf converter = new ConverterHtmlToPdf(postId);
		
		String mappedHtmlContent = getSpoolPrev(__INFO1__, sendSpoolInfo.getDefaultMapping(), SCHEDULE_INFO);
		
		converter.createPdf(mappedHtmlContent, 
							setPassword(SCHEDULE_INFO.getProperty("SECURE_PWD","PDF_SECURE_PWD"), __INFO1__),
							new File(attachFileName)
							);
	}
	
	private String setPassword(String passKey, SimpleHash __INFO1__){
		try {
			return __INFO1__.get(passKey).toString();
		} catch (Exception e) {
			Tracer.error("secure pwd["+passKey+"] find failed so Encryption is don't Exception is "+e);
			return "";
		}
	}
	
	/**
	 * 개인매핑데이터와 기본매핑데이터 둘 다 사용하는 PreView용 메일본문을 생성한다.
	 * 
	 * @param info1
	 *            개인매핑데이터
	 * @param info2
	 *            기본매핑데이터
	 * @throws Exception
	 *             생성에러
	 * @return 최종생성된 메일
	 */
	public String getPreViewMailBody(Object info1, Object info2, Properties prop) throws Exception {
		//return getOriginalMailBody( info1 , info2 , prop );
		//return ((Message) this.myMessages.get(0)).getStringMessage(info1, info2, prop);
		return BASE64.encode(((Message)this.myMessages.get( 0 )).getStringMessage( info1 , info2 , prop ));
	}

}
