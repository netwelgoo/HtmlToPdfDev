package pdf.html;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HtmlCrawled {
	
	public static final String SUCCESS = "0000";
	
	private boolean FINISH= true;
	
	Map<String, String> imageResult = new TreeMap<String, String>();
	Map<String, String> cssResult 	= new TreeMap<String, String>();
	Map<String, String> imageErrors = new TreeMap<String, String>();
	Map<String, String> cssErrors 	= new TreeMap<String, String>();
	
	private final AtomicInteger imageErrorCount = new AtomicInteger(0);

	private final AtomicInteger cssErrorCount = new AtomicInteger(0);
	
	private String resultCode=SUCCESS;
	
	private String resultMessage = "";
	
	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public int getImageErrorCount() {
		return imageErrorCount.get();
	}

	public int getCssErrorCount() {
		return cssErrorCount.get();
	}

	public Map<String, String> getImageResult() {
		return imageResult;
	}
	
	public void addImageResult(String key, String value) {
		imageResult.put(key, value);
	}
	
	public void setImageResult(Map<String, String> imageResult) {
		this.imageResult = imageResult;
	}

	public Map<String, String> getCssResult() {
		return cssResult;
	}

	public void setCssResult(Map<String, String> cssResult) {
		this.cssResult = cssResult;
	}

	public Map<String, String> getImageErrors() {
		return imageErrors;
	}

	public void setImageErrors(Map<String, String> imageErrors) {
		this.imageErrors = imageErrors;
		imageErrorCount.set(this.imageErrors.size());
	}
	
	public void addImageErrors(String key, String value) {
		imageErrors.put(key, value);
		imageErrorCount.getAndIncrement();
	}

	public Map<String, String> getCssErrors() {
		return cssErrors;
	}

	public void setCssErrors(Map<String, String> cssErrors) {
		this.cssErrors = cssErrors;
		cssErrorCount.set(this.cssErrors.size());
	}
	
	public void addCssErrors(String key, String value) {
		cssErrors.put(key, value);
		cssErrorCount.getAndIncrement();
	}

	public String resultCode() {
		return resultCode;
	}

	public void resultCode(String result) {
		this.resultCode = result;
	}
	
	@Override
	public String toString() {
		return "HtmlCrawled [FINISH=" + FINISH + ", imageResult=" + imageResult + ", cssResult=" + cssResult
				+ ", imageErrors=" + imageErrors + ", cssErrors=" + cssErrors + ", imageErrorCount=" + imageErrorCount
				+ ", cssErrorCount=" + cssErrorCount + ", resultCode=" + resultCode + "]";
	}
}
