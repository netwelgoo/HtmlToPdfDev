package pdf.db;

/**
 * Validate checker
 * 
 * @author pioneer(2016. 3. 23.)
 * @since 1.0
 * @param <T> validate Instance
 */
public interface Validator<T> {
	
	/** if normal is true but etc false or Exception returned **/
	public boolean validate(T t) throws Exception;
	
}
