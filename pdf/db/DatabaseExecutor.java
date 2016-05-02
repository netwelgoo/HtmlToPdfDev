package pdf.db;

import java.util.List;
import java.util.Map;
/**
 * <pre>
 * DB Upload Query (only DML Query)
 * </pre>
 * @author pioneer(2016. 1. 4.)
 * @since 1.0
 * @param <T> response Data
 *            List or Map @Link{GroupDatabaseUploader} or @Link{DefaultDatabaseExecutor} 참조
 */
		
public interface DatabaseExecutor<T> {
	
//	public List select(E argument) throws Exception;
	
	public int dmlExecute(T data);
	
	/** this method is for group(list) Executor **/ 
	public DatabaseExecutor next(DatabaseExecutor uploader);
	
	public DatabaseExecutor addArgument(Map<String, String> data);
	
	public void argumentClear() throws Exception;

//	public T argument();  //return null is call Default Map<String, Object> 
	
}
