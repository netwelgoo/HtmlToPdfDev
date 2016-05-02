package pdf.convert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum PdfConvertContainer {
	
	INSTANCE;

	public Map<String, ConverterHtmlToPdf> converter = new ConcurrentHashMap<String, ConverterHtmlToPdf>();
	
	public ConverterHtmlToPdf converter(String postId){
		if(!converter.containsKey(postId)){
			registConverter(new ConverterHtmlToPdf(new PdfInfomation(postId)));
		}
		return converter.get(postId);
	}
	
	public void registConverter(ConverterHtmlToPdf chtp){
		converter.put(chtp.postId(), chtp);
	}
}
