package pdf;

import java.io.File;
import java.util.Properties;

import pdf.common.SysProperty;
import pdf.db.DefaultDatabaseExecutor;
import pdf.html.CrawlingController;
import pdf.html.CrawlingStateFactory;
import pdf.html.HtmlCrawled;
import pdf.html.HtmlCrawler;
import pdf.html.HtmlInfo;
import pluto.config.SqlManager;
import pluto.db.ConnectionPool;
import pluto.db.eMsConnection;
import pluto.db.eMsResultSet;
import pluto.db.eMsStatement;
import pluto.lang.Tracer;

/**
 * html content crawling
 * ${base}/pdf/infos/post_id/
 * 
 * @author pioneer(2016. 3. 14.)
 * @since 1.0
 */
public class PdfCrawlingTask extends pluto.schedule.Task {
	
	private static eMsConnection CONNECTION	= null;
	
	public static String SELECT_PDF_URL_P0;

	private static String UPDATE_SCHD_JOB_STATUS;

	private static String UPDATE_PDF_CRAWLING_JOB_END;
	
	public PdfCrawlingTask(){
		super(TYPE_INTERVAL,2);
		this.setName("PdfCrawlingTask");
		this.setTaskID("PdfCrawlingTask" + System.currentTimeMillis());
	}
	
	static{
		SELECT_PDF_URL_P0 = SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "SELECT_PDF_URL_P0");

		UPDATE_SCHD_JOB_STATUS = SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "UPDATE_SCHD_JOB_STATUS");

		UPDATE_PDF_CRAWLING_JOB_END = SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "UPDATE_PDF_CRAWLING_JOB_END");
	}
	
	public boolean getPdfContent(Properties prop){
		eMsStatement SELECT_PDF_STATEMENT = null;
		eMsResultSet rs = null;
		try {
			SELECT_PDF_STATEMENT = CONNECTION.createStatement();
			rs = SELECT_PDF_STATEMENT.executeQuery(SELECT_PDF_URL_P0, new Properties(), "${", "}");
			
			boolean work = rs.next();
			if(work) rs.putToMap(prop, false);
			return work;
			
		} catch (Exception e) {
			Tracer.error("pdf[P0] select Exception " + e);
			return false;
		}finally{
			try {
				if(rs !=null) rs.close();
				if(SELECT_PDF_STATEMENT !=null) SELECT_PDF_STATEMENT.close();
			} catch (Exception e2) {
				// TODO: handle exception
				Tracer.error("[se] fail close() statment or resultSet Exception " + e2);
			}
		}
	}
	
	private void failConvertPdfJob(Properties prop) throws Exception{
		prop.setProperty("NEW_JOB_STATUS", "41");
		updateScheJobStatus(UPDATE_SCHD_JOB_STATUS, prop);
	}
	
	private void updateScheJobStatus(String sql, Properties prop) throws Exception{
		new DefaultDatabaseExecutor(sql).dmlExecute(prop);
	}
	
	private void startCrawling(Properties prop) throws Exception{
		prop.setProperty("NEW_JOB_STATUS", "P1");
		updateScheJobStatus(UPDATE_SCHD_JOB_STATUS, prop);
		prop.setProperty("JOB_STATUS", "P1");
	}
	
	@Override
	public void execute() throws Exception {
		this.CONNECTION 	= ConnectionPool.getConnection();
		Properties pdfInfo  = new Properties();
		
		if(!getPdfContent(pdfInfo)) return;
		
		startCrawling(pdfInfo);
		String postId = pdfInfo.getProperty("POST_ID");
		Tracer.info("pdf crawling start post_id["+postId+"]");
		boolean success = true;
		try {
			if(!HtmlCrawled.SUCCESS.equals(crawling(pdfInfo))){ 
				success = false;
				return;
			}
			
			pdfInfo.setProperty("PDF_CONTENT", htmlFileForTransferPdf(pdfInfo, postId));
			
			Tracer.info("pdf crawling end post_id["+postId+"]"
						+ "content update from "
						+ "org content["+pdfInfo.getProperty("CONTENT")+"] "
						+ "to pdf content["+pdfInfo.getProperty("PDF_CONTENT")+"]"
						);
			if(!finishPdfContent(pdfInfo)){
				failConvertPdfJob(pdfInfo);
			}
		} catch (Exception e) {
			Tracer.error("error pdf Content crawling Job Exception "+ e);
			success = false;
		}finally{
			try {
				if(!success){
					Tracer.error("pdf crawling error post_id["+postId+"] so update fail(41) Update");
					failConvertPdfJob(pdfInfo);
				}
			} catch (Exception e2) {
				Tracer.error("[se]pdf crawling fail update error post_id["+postId+"] Exception " +e2);
			}
		}
	}

	/**
	* 임의로 HTML 디자인을 변경할 필요가 있을 경우(ex:Freemarker) 
	* HTML을 파일명을 변경해서 저장 하고 저장된 HTML 파일을 불러온다.
	* TODO !POST_ID(workday_seqno)별로 디자인되는 기능이 적용되어 
	*      이 부분에 대한 개선이 필요하다.
	*      즉, 자동메일 종류별로 디자인이 변경 될 경우에 대한 기능이 필요하다. 
	* @param postId
	* @return
	 */
	private String htmlFileForTransferPdf(Properties pdfInfo, String postId)
	throws Exception
	{
		String fullPathHtmlFileName="";
		
		fullPathHtmlFileName = htmlFileName(pdfInfo, postId);

		if(!new File(fullPathHtmlFileName).exists()){
			throw new Exception("not find html file["+fullPathHtmlFileName+"]");
		}

		return fullPathHtmlFileName;
	}

	private String htmlFileName(Properties pdfInfo, String postId) {
		String fullPathHtmlFileName;
		if("Y".equalsIgnoreCase(pdfInfo.getProperty("DESIGN_OPTION", "N"))){
			fullPathHtmlFileName = SysProperty.PdfHtmlDesignPath.value()
									+ getMsgName(pdfInfo.getProperty("MSG_TYPE"), 
												 pdfInfo.getProperty("MSG_TYPE_SEQ"), 
												 false
												 )
									+ System.getProperty("file.separator")
									+ getMsgName(pdfInfo.getProperty("MSG_TYPE"), 
												 pdfInfo.getProperty("MSG_TYPE_SEQ"), 
												 true
												 );
		}else{
			fullPathHtmlFileName = SysProperty.PdfLocalHtmlPath.value()
								  + postId
								  + "/"
								  + postId
								  + CrawlingController.HTML_FILE_EXTENSION;
		}
		return fullPathHtmlFileName;
	}
	
	public String getMsgName(String msgType, String MsgTypeSeq, boolean htmlOption){
		return (htmlOption) 
				? msgType + "_" + MsgTypeSeq + CrawlingController.HTML_FILE_EXTENSION
				: msgType + "_" + MsgTypeSeq; 
	}
	
	public boolean isE(String htmlFile){
		return new File(htmlFile).exists();
	}
	
	
	public boolean finishPdfContent(Properties prop){
		try {
			prop.setProperty("NEW_JOB_STATUS", "P2");
			new DefaultDatabaseExecutor(UPDATE_PDF_CRAWLING_JOB_END).dmlExecute(prop);
			return true;
			
		} catch (Exception e) {
			// TODO: handle exception
			Tracer.error("pdf content["+prop.getProperty("PDF_CONTENT", "")+"] "
						+ "	and JobStatus[00] update error Exception "+e);
			return false;
		}
	}
	
	public String crawling(Properties pdfInfo){
		try {
			HtmlCrawler crawlingState = CrawlingStateFactory.INSTANCE.instance("00");
			
			HtmlInfo htmlInfo = new HtmlInfo(pdfInfo.getProperty("CONTENT"),
											 pdfInfo.getProperty("POST_ID") );
			
			crawlingState.htmlLoad(htmlInfo);
			
			HtmlCrawled crawled = crawlingState.result();
			
			return crawled.resultCode();

		} catch (Exception e) {
			Tracer.error("don't html crawling so resultCode[9900] Exception "+e);
			return "9900";
		}
	}

	@Override
	public void execute_initiateError(Throwable thw) {
		// TODO Auto-generated method stub
	}

	@Override
	public void release_Resource() {
		// TODO Auto-generated method stub
		if(CONNECTION !=null) CONNECTION.recycle();
	}
}
