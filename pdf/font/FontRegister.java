package pdf.font;

import java.util.List;

import com.itextpdf.tool.xml.XMLWorkerFontProvider;

/**
 * 폰트 등록을 위한 
 * @author pioneer(2016.02.11)
 *
 * @param <T> bander Instance <p> example) @Link{ItextFontRegister}
 */
public interface FontRegister<T> {
	
	public void register(T t, String fontName);

	public void registers(T t, List<String> fonts);
	
	public void registerDirectory(T t, String dir);
}
