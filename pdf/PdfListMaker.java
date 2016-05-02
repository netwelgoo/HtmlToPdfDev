package pdf;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mysql.jdbc.StringUtils;

import freemarker20.ext.beans.StaticModels;
import freemarker20.template.SimpleHash;
import mercury.contents.common.basic.ContentInfo;
import mercury.contents.common.basic.ContentInfoManager;
import pdf.common.SysProperty;
import pdf.convert.ContentConverter;
import pdf.convert.PdfAudience;
import pdf.convert.PdfWorker;
import pdf.convert.PdfWorkerContainer;
import pdf.db.DefaultDatabaseExecutor;
import pdf.util.FileUtilProxy;
import pluto.config.SqlManager;
import pluto.db.ConnectionPool;
import pluto.db.eMsConnection;
import pluto.db.eMsResultSet;
import pluto.db.eMsStatement;
import pluto.io.FileElement;
import pluto.lang.Tracer;
import pluto.lang.eMsLocale;
import pluto.util.StringConvertUtil;
import pluto.util.convert.TrackingInfoConvertor;
import venus.spool.common.basic.SpoolInfo;
import venus.spool.common.basic.SpoolInfoManager;
import venus.spool.common.handler.LongMappingSpoolReader;
import venus.spool.common.parser.SpoolHashParser;

/**
 * List Table[TMS_AUTO_EMAIL_LIST_XX] read and write to PDF File
 * PDF File name rule is POST_ID(workday_seqno) + MEMBER_ID + MEMBER_ID_SEQ + .pdf
 * 
 * @author pioneer(2016. 3. 28.)
 * @since 1.2
 * 
 * @comment 1.1 : OutofMemory @Link{PdfWorkerContainer} handling customizer 
 * @comment 1.2 : Concurrent feature applied ( Inheritance to Thread )
 */
public class PdfListMaker extends Thread{
	
	private final String postId;
	
	private final Properties arguments;
	
	private static eMsConnection CONNECTION	= null;
	
	protected SpoolHashParser AUTO_KEY_VALUE_PARSER = null;
	
	public static final String EMPTY = "";
	
	private static String SELECT_PDF_LIST_TABLE;
	
	private static String UPDATE_SCHD_JOB_STATUS;

	private static String SELECT_PDF_LIST_CNT;
	
	private ContentConverter<String, String>
			converter = new ContentConverter<String, String>(){
							@Override
							public String convert(String i) 
							throws Exception {
								return i;
							}
							@Override
							public ContentConverter setNext(ContentConverter cc) {
								// TODO Auto-generated method stub
								return null;
							}
						};
	
	static{
		SELECT_PDF_LIST_TABLE 	= SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "SELECT_PDF_LIST_TABLE");
		UPDATE_SCHD_JOB_STATUS 	= SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "UPDATE_SCHD_JOB_STATUS");
		SELECT_PDF_LIST_CNT 	= SqlManager.getQuery("MAIN_SCHEDULE_CHECK", "SELECT_PDF_LIST_CNT");
	}
	
	public PdfListMaker(String postId, Properties prop){
		this.postId = postId;
		this.arguments = prop;
		this.AUTO_KEY_VALUE_PARSER 	= new SpoolHashParser( "|" );
	}
	
	/**
	* HTML Content converter
    * If there is a need to change the PDF before converting HTML Content
    * Set the instance that implements the ContentConverter.
	* 
	* (ex : watermark, etc ..)
	* 
	* @param converter
	 */
	public PdfListMaker setContentConverter(ContentConverter<String, String> converter){
		this.converter = converter;
		return this;
	}
	
	private void finishConvertPdfJob(Properties prop) throws Exception{
		try {
			prop.setProperty("NEW_JOB_STATUS", "20");
			updateScheJobStatus(UPDATE_SCHD_JOB_STATUS, prop);
		} catch (Exception e2) {
			Tracer.error("[se] don't update pdf job_status(20) so again update job(41) Exception "+ e2);
			failConvertPdfJob(prop);
		}
	}
	
	private void failConvertPdfJob(Properties prop) throws Exception{
		prop.setProperty("NEW_JOB_STATUS", "41");
		updateScheJobStatus(UPDATE_SCHD_JOB_STATUS, prop);
	}
	
	private void updateScheJobStatus(String sql, Properties prop) throws Exception{
		new DefaultDatabaseExecutor(sql).dmlExecute(prop);
	}
	
	public void run(){
		try {
			Tracer.info("PDF Job start post_id["+postId+"]");
			
			readerLineConvert();
			
			finishConvertPdfJob(this.arguments);
			
			Tracer.info("PDF Job end post_id["+postId+"]");
		} catch (Exception e) {
			Tracer.error("PDF Job fail post_id["+postId+"] so Schd Table update(41) Exception " +e);
			try {
				failConvertPdfJob(this.arguments);
			} catch (Exception e2) {
				Tracer.error("fail Update(41) Exception "+e2);
			}
		}
	}
	
	private eMsResultSet getPdfLists(eMsStatement SELECT_PDF_STATEMENT, Properties arguments) 
	throws Exception {
		this.CONNECTION = ConnectionPool.getConnection();

		SELECT_PDF_STATEMENT = CONNECTION.createStatement();
		SELECT_PDF_STATEMENT.setFetchSize(50);
		return SELECT_PDF_STATEMENT.executeQuery(SELECT_PDF_LIST_TABLE, arguments, "${", "}");
	}
	
	/**
	* List Table을 한 라인 씩 읽어 Content를 개인별 Mapping 하고
	* PDF Work Container[@Link(PdfWorkerContainer)]에 변환을 요청 한다.   
	* 
	* @param postId
	* @param prop
	* @throws Exception
	*/
	public void readerLineConvert() throws Exception {
		ContentInfo sendContentInfo = ContentInfoManager.getContentInfo(postId);
		SpoolInfo sendSpoolInfo 	= SpoolInfoManager.getSpoolInfo(postId);
		
		eMsResultSet rs = null;
		eMsStatement SELECT_PDF_STATEMENT = null;
		try {
			rs = getPdfLists(SELECT_PDF_STATEMENT, arguments);
		} catch (Exception e) {
			Tracer.error("error select pdf list table Exception " + e);
			if(rs != null) rs.close();
			if(SELECT_PDF_STATEMENT != null) SELECT_PDF_STATEMENT.close();
			CONNECTION.recycle();
			return;
		}
		String passKey = arguments.getProperty("SECURE_PWD", "");  //PDF PASSWORD KEY 
		
		Properties pdfInfo 			= new Properties();
		SimpleHash _MEMBER_HASH_ 	= new SimpleHash();
		int rsCount = 0;
		List<PdfWorker> workers = new ArrayList<PdfWorker>();
		
//TODO pioneer(2016.03.30) 건수가 많을 경우 ===========	//
//		pdf file directory를 분할 할 수 있는 기능 추가. ==   	// 
//		                                          	// 
//		createDirectoryForPdf(arguments);         	//
//================================================	//		
		try {
			StringBuffer mappedHtmlContent = new StringBuffer(2048);
			while(rs.next()){
				rsCount++;
				try {
					pdfInfo.putAll(arguments);
					try {
						rs.putToMap(pdfInfo, false);
						_MEMBER_HASH_ 	= mapping(pdfInfo, sendContentInfo,  sendSpoolInfo);
						mappedHtmlContent.append(sendContentInfo.getSpoolPrev(_MEMBER_HASH_, sendSpoolInfo.getDefaultMapping()));
					} catch (Exception e) {
						Tracer.error("parsing error html Content "
										+ "postId["+postId+"] "
										+ "email["+pdfInfo.getProperty("EMS_M_EMAIL", "")+"] "
										+ "member Info["+_MEMBER_HASH_.toString()+"]"
										+ "data clear and continue... Exception "+ e);
						continue;
					}
					
					PdfAudience audience = new PdfAudience(postId, 
										 				   pdfInfo.getProperty("EMS_M_EMAIL"), 
										 				   converter.convert(mappedHtmlContent.toString()),
														   setPassword(passKey, _MEMBER_HASH_));
					
					PdfWorkerContainer.INSTANCE.invoke(audience, workers);
				} catch (Exception e) {
					Tracer.error("fail convert html to pdf "
								+ "	postId["+postId+"] "
								+ "email["+pdfInfo.getProperty("EMS_M_EMAIL", "")+"]   "
								+ "so continue...  "
								+ "	Exception "+ e);
					continue;
				}finally{
					mappedHtmlContent.setLength(0);
					_MEMBER_HASH_.clear();
					pdfInfo.clear();
				}
			} //while close(resultSet)
			
			//Last invoke for remainder
			PdfWorkerContainer.INSTANCE.invokeAll(workers);
			
		} catch (Exception e) {
			Tracer.error("pdf convert list while error so fail update Exception "+ e);
			throw e;
		}finally{
			try {
				Tracer.info("htmlToPdf file write finish postId["+this.postId+"] rs count["+rsCount+"]");
				if(rs != null) rs.close();
				if(SELECT_PDF_STATEMENT != null) SELECT_PDF_STATEMENT.close();
				CONNECTION.recycle();
			} catch (Exception e2) {
				Tracer.error("[se] clear and close Exception -> selected Result Data "+ e2);
				throw e2;
			}
		}
	}
	
	/**
	*  분산 directory로 pdf file을 저장하기 위해서 
	*  전체 count 개수를 기준으로 pdf file directory를 생성한다.
	*/
	private boolean createDirectoryForPdf(Properties prop){
		eMsConnection connection = null;
		eMsStatement SELECT_PDF_STATEMENT = null;
		eMsResultSet rs = null;
		try {
			connection = ConnectionPool.getConnection();
			SELECT_PDF_STATEMENT = connection.createStatement();
			
			rs = SELECT_PDF_STATEMENT.executeQuery(SELECT_PDF_LIST_CNT, arguments, "${", "}");
			rs.next();
			int listCount = rs.getInt("CNT");
			
			return FileUtilProxy.createFolder(prop.getProperty("POST_ID"), 
												listCount, 
												Integer.parseInt(SysProperty.PdfFileCountUnit.value()) 
											  );
			
		} catch (Exception e) {
			Tracer.error("Pdf Directory creating failed Exception " +e );
			return false;
		}finally{
			try {
				if(rs != null) rs.close();
				if(SELECT_PDF_STATEMENT != null) SELECT_PDF_STATEMENT.close();
			} catch (Exception e2) {
				Tracer.error("[se] ResultSet or Statement closing failed Exception " +e2 );
			}
			connection.recycle();
		}
	}
	
//TODO pioneer pass value가 없을 경우 처리 방법
// 1. 메일을 보내지 않는다.
// 2. 보안을 해제 해서 메일을 보낸다.
// 3. 임시로 임의 보안 문자를 넣는다.	
	private String setPassword(String passKey, SimpleHash _MEMBER_HASH_){
		try {
			if(StringUtils.isNullOrEmpty(passKey)) return "";
			
			return _MEMBER_HASH_.get(passKey).toString();
			
		} catch (Exception e) {
			Tracer.error("secure pwd["+passKey+"] find failed so Encryption is don't Exception is "+e);
			return "";
		}
	}
	
	/**
	* properties argument를 이용하여 
	* _MEMBER_HASH_에 Mapping 값을 넣는다.
    *
	* @param pdfInfo  
	* @param _MEMBER_HASH_
	* @throws Exception
	 */
	private synchronized SimpleHash mapping(Properties pdfInfo, 
								ContentInfo sendContentInfo, 
								SpoolInfo sendSpoolInfo )throws Exception{
		
		SimpleHash _MEMBER_HASH_ 	= new SimpleHash();
		try {
			String mappingData 		= memberMapping(pdfInfo.getProperty("MAPPING"));

			//매핑의 종류에 따라서 파싱하는 방법이 다르게 되므로 추가...
			if( mappingData.startsWith( "#P#:" ) ){
				// 매핑이 key value 로 들어 왔을 때
				StringConvertUtil.ConvertStringToSimpleHash( _MEMBER_HASH_ , mappingData );
			}
			else{
				AUTO_KEY_VALUE_PARSER.setDelim( sendContentInfo.getScheduleInfo().getProperty("DELIMIT","|" ) );
				AUTO_KEY_VALUE_PARSER.parse(  _MEMBER_HASH_ , 
												mappingData , 
												sendSpoolInfo.getHEADER_KEY_MAP( "|" ), 
												sendSpoolInfo.getDefaultSimpleHashMapping() 
											);
			}
			
			addArgument(pdfInfo, _MEMBER_HASH_);
			
		} catch (Exception e) {
			Tracer.error("Mapping failed Data Properties.MAPPING["+pdfInfo.getProperty("MAPPING")+"] Exception "+e );
			throw e;
		}
		return _MEMBER_HASH_;
	}
	
	
	private void addArgument(Properties prop, SimpleHash _MEMBER_HASH_){
		
		_MEMBER_HASH_.put( "m_id" , 	prop.getProperty("MEMBER_ID"));
		_MEMBER_HASH_.put( "m_email" , 	prop.getProperty("TO_EMAIL"));
		//_MEMBER_HASH_.put( "m_name" , "name" );
		_MEMBER_HASH_.put( "m_name" ,   prop.getProperty("MEMBER_NAME") );
		_MEMBER_HASH_.put( "p_id" , 	prop.getProperty("POST_ID"));
		
		_MEMBER_HASH_.put( "EMS_M_ID" , 	prop.getProperty("MEMBER_ID") );
		_MEMBER_HASH_.put( "EMS_M_EMAIL" , 	prop.getProperty("TO_EMAIL") );
		//_MEMBER_HASH_.put( "EMS_M_NAME" , "name" );
		_MEMBER_HASH_.put( "EMS_M_NAME" , prop.getProperty("MEMBER_NAME") );

		//트래킹 맴버아이디
		_MEMBER_HASH_.put( "enc_mid" , TrackingInfoConvertor.enc_MEMBER_ID( "preView_1"  )  );
		//트래킹 맴버메일아이디
		_MEMBER_HASH_.put( "enc_p_id" , TrackingInfoConvertor.enc_MAIL_ID( prop.getProperty("POST_ID")  )  );
		//트래킹 마감일
		_MEMBER_HASH_.put( "enc_t_close" , TrackingInfoConvertor.enc_CLOSE( "19000101" )  );
		
		//함수변환
		_MEMBER_HASH_.put("statics", StaticModels.INSTANCE);
		
		//add mapped argument
//		_MEMBER_HASH_.putAll(mappingArgument);
	}
	
	/**
	* Long Mapping Data 정보를 가져온다.
	* List Table.[mapping_header] column Info 
	* @param MEMBER_MAPPING
	* @return
	* @throws Exception
	* @throws UnsupportedEncodingException
	 */
	private String memberMapping(String MEMBER_MAPPING) throws Exception, UnsupportedEncodingException {
		ByteArrayOutputStream INNER_BYTE_ARRAY = new ByteArrayOutputStream( 1024 );
		
		if( MEMBER_MAPPING.startsWith( "isam://" ) ) {
			synchronized( INNER_BYTE_ARRAY ){
				INNER_BYTE_ARRAY.reset();
				
				int idx_of_comma = MEMBER_MAPPING.indexOf( "," , 7 );
				int idx_of_at = MEMBER_MAPPING.indexOf( "@" , idx_of_comma );
				long start = Long.parseLong( MEMBER_MAPPING.substring( 7 , idx_of_comma ) );
				long end = Long.parseLong( MEMBER_MAPPING.substring( idx_of_comma + 1 , idx_of_at ) );
				
				LongMappingSpoolReader reader = LongMappingSpoolReader.getInstance( MEMBER_MAPPING.substring( idx_of_at + 1 ).replace( '\\' , '/' ) );
				
				reader.inner_get( start , end , INNER_BYTE_ARRAY );
				
				MEMBER_MAPPING = INNER_BYTE_ARRAY.toString( eMsLocale.FILE_SYSTEM_IN_CHAR_SET );
			}
		}
		else if( MEMBER_MAPPING.startsWith( "file://" ) ) {
			synchronized( INNER_BYTE_ARRAY ){
				INNER_BYTE_ARRAY.reset();
				
				FileElement.putFileBodyToStream( MEMBER_MAPPING.substring( 7 ) , INNER_BYTE_ARRAY );
				
				MEMBER_MAPPING = INNER_BYTE_ARRAY.toString( eMsLocale.FILE_SYSTEM_IN_CHAR_SET );
			}
		}
		return MEMBER_MAPPING;
	}
	
	
	/**
	* pdf 파일 명을 조합하여 만든다. 
	* PDF FILE NAME = workday_seqno_(member_id)_(member_id_seq).pdf
	* @param prop
	* @return
	 */
	private String makePdfFileName(Properties prop) throws Exception{
		return FileUtilProxy.pdfAttachFileName(
										prop.getProperty("POST_ID"), 
										prop.getProperty("MEMBER_ID"), 
										prop.getProperty("MEMBER_ID_SEQ"));
		
	}

}
