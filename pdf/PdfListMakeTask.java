package pdf;
import java.util.Properties;

import addon.MarkanyConverter;
import mercury.contents.common.producer.ContentPD;
import pdf.db.DefaultDatabaseExecutor;
import pluto.config.SqlManager;
import pluto.db.ConnectionPool;
import pluto.db.eMsConnection;
import pluto.db.eMsResultSet;
import pluto.db.eMsStatement;
import pluto.lang.Tracer;
import venus.spool.common.parser.SpoolHashParser;

/**
 * HTML to PDF file create and write
 *  
 * @author pioneer(2016. 3. 24.)
 * @since 1.0
 */
public class PdfListMakeTask extends pluto.schedule.Task {
	
	private static final boolean INNER_DEBUG = false;
	
	public static final String EMPTY = "";
	
	public static final String  PDF_EXTENSION=".pdf";
	
	protected SpoolHashParser AUTO_KEY_VALUE_PARSER = null;
	
	protected ContentPD INNER_CONTENT_MAKE_INSTANCE = null;
	
	private static eMsConnection CONNECTION	= null;
	
	private static String SELECT_WORK_PDF_CONTENT;


	private static String UPDATE_SCHD_JOB_STATUS;
	
	static{
		//작업을 요청한 PDF_CONTENT를 가져온다.(1 ROW)
		SELECT_WORK_PDF_CONTENT = SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "SELECT_WORK_PDF_CONTENT");
		UPDATE_SCHD_JOB_STATUS 	= SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "UPDATE_SCHD_JOB_STATUS");
	}
	
	public PdfListMakeTask(){
		super(TYPE_INTERVAL,2);
		this.setName("PdfListMakeTask");
		this.setTaskID("PdfListMakeTask" + System.currentTimeMillis());
		AUTO_KEY_VALUE_PARSER 	= new SpoolHashParser( "|" );
		
	}
	
	public static void init( Object tmp ) throws Exception{
		Properties prop = ( Properties )tmp;
		//INNER_CONTENT_MAKE_INSTANCE = (ContentPD)(Class.forName( prop.getProperty( "instant.content.make.class")).newInstance() );
	}

	@Override
	public void execute() throws Exception {
		try {
			this.execute_init();  //DB 정보 초기화
			
			this.execute_main();  //메인 프로세스
			
			this.execute_finish();//완료 프로세스
			
		} catch (Throwable thw) {
			Tracer.error(this.getClass().getSimpleName(), "CALL execute() ERROR", thw);
			
		}finally {
			// 자원반환.
			this.releaseResource();
		}
	}
	
	/**
	* 등록된(SCHEDULE.JOB_STATUS = P3)상태가 있는지 확인 한다. 
	* 'P3' 상태값일 경우 prop argument에 결과 정보를 넣어 준다. 
	* (! 한 건씩만 받아와서 결과를 처리 한다 ) 
	* @param prop
	* @return
	*/
	public boolean isRegisteredPdfJob(Properties prop){
		eMsStatement SELECT_PDF_STATEMENT = null;
		eMsResultSet rs = null;
		try {
			SELECT_PDF_STATEMENT = CONNECTION.createStatement();
			rs = SELECT_PDF_STATEMENT.executeQuery(SELECT_WORK_PDF_CONTENT, new Properties(), "${", "}");
			boolean work = rs.next();
			if(work) rs.putToMap(prop, false);
			return work;
			
		} catch (Exception e) {
			return false;
		}finally{
			try {
				if(rs !=null) rs.close();
				if(SELECT_PDF_STATEMENT !=null) SELECT_PDF_STATEMENT.close();
			} catch (Exception e2) {
				// TODO: handle exception
				Tracer.error("[se] fail close() statment or resultSet" + e2);
			}
		}
	}
	
	public int startPdfJob(Properties prop) throws Exception{
		prop.setProperty("NEW_JOB_STATUS", "P5");
		int count =  new DefaultDatabaseExecutor(UPDATE_SCHD_JOB_STATUS).dmlExecute(prop);
		prop.setProperty("JOB_STATUS", "P5");
		return count;
	}
	
	public void execute_main() throws Exception{
		Properties prop = new Properties();
		String postId = "";
		try {
			if(!isRegisteredPdfJob(prop)) return;
			
			postId = prop.getProperty( "WORKDAY" )+"_"+prop.getProperty( "SEQNO" );
			
			if(startPdfJob(prop) <= 0){ 
				Tracer.error("PDF Job don't started post_id["+postId+"]");
				return;
			}

			new PdfListMaker(postId, prop).start();
			
//만약 html을 pdf로 변환 하기 전에 content를 변경 시켜야 할 경우가 있을 경우 추가 한다.			
//ex:		new PdfListMaker(postId, prop)
//					.setContentConverter(new MarkanyConverter())
//					.start();
			
		} catch (Exception e) {
			Tracer.error("pdf converting error post_id["+postId+"] so fail update job(41) Exception "+e);
			return;
		}
	}
	
	public void execute_finish() throws Exception{
		CONNECTION.recycle();
	}
	
	@Override
	public void execute_initiateError(Throwable thw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void release_Resource() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 초기화하면서 사용할 자원을 할당한다.
	 * @throws Throwable
	 *         초기화 에러.
	 */
	protected void execute_init() throws Throwable {
		if( INNER_DEBUG ) {
			Tracer.debug(this, "CALL PushToTmsTransfer===> execute_init()", null);
		}
		this.CONNECTION 		= ConnectionPool.getConnection();
	}
	
	protected void releaseResource() {
		if( INNER_DEBUG ) {
			Tracer.debug(this, "CALL PushToTmsTransfer===> releaseResource()", null);
		}
		// TMS 대상 연결을 반환한다.
		if( this.CONNECTION != null ) {
			this.CONNECTION.recycle();
		}
	}
	
}
