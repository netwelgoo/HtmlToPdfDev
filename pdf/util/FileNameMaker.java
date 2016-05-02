package pdf.util;

public interface FileNameMaker<T> {
	
	//T is attach File element 
	public String makeFileName(T t, String addOption) throws Exception;
}
