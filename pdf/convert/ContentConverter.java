package pdf.convert;

/**
 * content conversion service 
 * default Implements instance view
 * (ex : HTML content -> markany changing, etc.. )
 * @author pioneer(2016. 4. 4.)
 * @since 1.0
 * @param <I>
 * @param <O>
 */
public interface ContentConverter<I, O> {

	public O convert(I i) throws Exception;
	
	public ContentConverter setNext(ContentConverter cc);
	
}
