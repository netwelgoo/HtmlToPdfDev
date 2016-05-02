package pdf.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import pluto.db.eMsPreparedStatement;
import pluto.lang.Tracer;

/**
 * Database DML query executor
 * 
 * @author pioneer(2016. 3. 23.)
 * @since 1.0
 * @param <T>
 */
public class DefaultDatabaseExecutor<T> extends AbstractDatabaseExecutor<Map<String, Object>>{

	public DefaultDatabaseExecutor(String query) throws Exception {
		super(query);
	}
	
	public DefaultDatabaseExecutor(String prefix, String... query) throws Exception {
		super(prefix, query);
	}

	@Override
	protected int execute(Map<String, Object> data, String query, eMsPreparedStatement pstate) 
	throws SQLException{
		
		dataMerge(data);
//FIXME pioneer(2016.03.17) 중복 데이터 검사.
//		if(DuplicateValidator.INS.isDuplicate(defaultData)){
//			throw new DuplicateException("data["+defaultData+"] query["+query+"]");
//		}
		int resultCount;
		resultCount = pstate.executeUpdate(data);
		if(resultCount < 0){
			Tracer.error("dml query resultCount["+resultCount+"] query["+query+"] continue..");
			isAllUpdateCounters = false;
		}else if(resultCount == 0){
			Tracer.info("dml query resultCount is 0(zero) query["+query+"] continue..");
			isAllUpdateCounters = false;
		}
		//muilti query 인 경우 executorResult에 결과 값을 넣어둔다.
		if(this.querys.size() > 1){
			executorResult.put(query, Integer.toString(resultCount));
		}
		return resultCount;
	}

	private void dataMerge(Map<String, Object> data) {
		Iterator iter = defaultData.keySet().iterator();
		
		while(iter.hasNext()){
			String key = iter.next().toString();
			Object t = defaultData.get(key);
			data.put(key, t.toString());
		}
	}

	@Override
	public void argumentClear() throws Exception {
		defaultData.clear();
	}

}
