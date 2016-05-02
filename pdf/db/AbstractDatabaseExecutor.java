package pdf.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import pluto.config.SqlManager;
import pluto.db.ConnectInfo;
import pluto.db.ConnectionPool;
import pluto.db.eMsConnection;
import pluto.db.eMsPreparedStatement;
import pluto.lang.Tracer;


/**
 * <pre>
 * database query type 
 * </pre>
 * @author pioneer(2015. 12. 17.)
 * @since 1.0
 */
enum QueryType{
	SELECT("SELECT"), INSERT("INSERT"), UPDATE("UPDATE"), DELETE("DELETE");
	
	private String pre;
	
	QueryType(String pre){
		this.pre = pre;
	}
}

/**
 * <pre>
 * database executor 
 * </pre>
 * @author pioneer(2015. 12. 22.)
 * @since 1.0
 * @param <T>
 */
public abstract class AbstractDatabaseExecutor<T> implements DatabaseExecutor<T> {
	protected final Map<String, QueryType> querys;
	protected eMsConnection connection = null;
	protected int limit = 1000;
	protected DatabaseExecutor<T> nextUploder=null;
	protected Map<String, String> defaultData = new ConcurrentHashMap<String, String>();
	protected Map<String, String> executorResult = new ConcurrentHashMap<String, String>();
	protected boolean isAllUpdateCounters = true;
	
	@Override
	public DatabaseExecutor<T> next(DatabaseExecutor uploader){
		nextUploder = uploader;
		return this;
	}
	
	public void setLimit(int limit){
		this.limit = limit;
	}
	
	public int resultCount(String queryKey){
		return Integer.parseInt(executorResult.get(queryKey));
	}
	
	public boolean isMultiAllUpdater(){
		return  isAllUpdateCounters;
	}
	
	public AbstractDatabaseExecutor(String query) throws Exception{
		this.querys = new TreeMap<String, QueryType>();
		for (QueryType type: QueryType.values()) {
			if(query.toUpperCase().startsWith(type.toString())){
				this.querys.put(query, type);
			}
		}
	}
	public AbstractDatabaseExecutor(String prefix, String[] querykey) throws Exception{
		this.querys = new TreeMap<String, QueryType>();
		
		for (int i = 0; i < querykey.length; i++) {
//			Tracer.debug("Register Query["+query[i]+"]");
			String sql = mapping(prefix, querykey[i].trim()).trim();
			for (QueryType type: QueryType.values()) {
				if(sql.toUpperCase().startsWith(type.toString())){
					this.querys.put(sql, type);
				}
			}
		}
	}

	private String mapping(String prefix, String query) throws Exception{
		return SqlManager.getQuery(prefix, query);
	}
	
	public int dmlExecute(T data, ConnectInfo connectInfo) {
		try {
			this.connection = ConnectionPool.getConnection(connectInfo);
			return dmlExecute(data); 
		}catch (Exception e) {
			Tracer.error("fail connection info["+connectInfo.toString()+"] Exception " +e);
			return -1;
		}
	}
	
	private void connection() throws Exception{
		try {
			if(this.connection==null){
				this.connection = ConnectionPool.getConnection();
			}else if(this.connection.isClosed()){
				this.connection = ConnectionPool.getConnection();
			}
		} catch (Exception e) {
			Tracer.error("[se] db Connection close Exception "+e);
			throw e;
		}
	}
	
	
	@Override
	public int dmlExecute(T data) {
		try {
			connection();
			this.connection.setAutoCommit(false);
			return executor(data);
		}
//		catch(DuplicateException ed){
//			//TODO pioneer(2015.12.22) 
//			//중복 데이터일 경우 어떻게 처리 할 것인가?
//			//현재 프로젝트(pushpia연동)에서는 일반적으로 결과에 대한 Upload 처리(insert,update)라서
//			//문제가 발생되지 않는다. 
//			//하지만 패키지화 하기 위해서는 중복에 대한 처리를 고려 해야 할 듯. 
//			Tracer.error("duplicated Exception", ed);
//			return false;
//		}
		catch (Exception e) {
			Tracer.error("don't query try rollback - Exception ="+e);
			e.printStackTrace();
			try {
				connection.rollback();
				Tracer.error("success rollback transaction ");
			} catch (SQLException e2) {
				Tracer.error("don't query rollback Exception="+e2);
			}
			return -1;
		}finally{
			try {
				connection.setAutoCommit(true);
				connection.recycle();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public int executor(T data) throws SQLException, Exception{
		List<eMsPreparedStatement> pstates = new ArrayList<eMsPreparedStatement>();
		int count=0;
		try {
			for (String query : this.querys.keySet()){
				eMsPreparedStatement pstate = connection.prepareStatement(query,"${","}");
				count = execute(data, query, pstate);
				pstates.add(pstate);
			}
			connection.commit();
		}finally{
			data = null;
			try {
				if(pstates != null) 
				for (eMsPreparedStatement state: pstates) {
					if(state != null) state.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return count;
	}
	
	public DatabaseExecutor addArgument(Map<String, String> data){
		Iterator iter = data.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next().toString();
			this.defaultData.put(key, data.get(key));	
		}
		return this;
	}
	
	/**
	 * <pre>
	 * 중복에 대한 데이터를 검사한다.
	 * CHECK_COUNT_LOOP 건수를 기준으로 중복 검사.
	 * </pre>
	 * @author pioneer(2016. 1. 4.)
	 * @since 1.0
	 */
	protected class DuplicateValidator implements Validator<Map<String, String>>{
		
		private Map<String,String> mdata = new ConcurrentHashMap<String, String>();
		
		private AtomicInteger checkTime = new AtomicInteger(0);
		
		public final int CHECK_COUNT_LOOP;
		
		private DuplicateValidator(int checkCount){
			CHECK_COUNT_LOOP = checkCount;
		}
		
		private boolean isDuplicate(Map<String, String> data){
			if(CHECK_COUNT_LOOP != checkTime.incrementAndGet()) return false;
			
			try {
				Iterator iter = data.keySet().iterator();
				while(iter.hasNext()){
					String key = iter.next().toString();
					String value = data.get(key);
					if(!mdata.containsKey(key)) return false;
					
					if(!value.equals(mdata.get(key))) return false;
				}
				return true;
			}finally{
				checkTime.set(0);
				this.mdata = data; 
			}
		}

		@Override
		public boolean validate(Map<String, String> t) throws Exception {
			return !isDuplicate(t);
		}
	}
	
	abstract protected int execute( T data, 
									 String query, 
									 eMsPreparedStatement pstate
								    ) throws SQLException;

}
