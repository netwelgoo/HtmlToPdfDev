package pdf.html;

import pdf.common.SysProperty;

public class HtmlInfo {
	
	private final String url;
	
	private final String postId;
	
	private final String tempDirectory;
	
	private String charset = SysProperty.HtmlCrawlingCharset.value() ;

	private String localHtmlFileName ;
	
	public HtmlInfo(String url, String postId){
		this(url, postId, "");
	}
	
	public HtmlInfo(String url, String postId, String inputDirectory){
		this.url=url;
		this.postId = postId;
		this.tempDirectory = inputDirectory;
	}
	
	public HtmlInfo(String url, String postId, String inputDirectory, String charset){
		this.url		=url;
		this.postId 	= postId;
		this.tempDirectory = inputDirectory;
		this.charset 	= charset;
	}
	
	public String getTempDirectory() {
		if("".equals(tempDirectory)) return tempDirectory;
		else{
			if(tempDirectory.endsWith("/"))  return tempDirectory;
			if(tempDirectory.endsWith("\\")) return tempDirectory;
			else return tempDirectory+"/";
		}
	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getUrl() {
		return url;
	}

	public String getPostId() {
		return postId;
	}

	public String getLocalHtmlFileName() {
		return localHtmlFileName;
	}

	public void setLocalHtmlFileName(String localHtml) {
		this.localHtmlFileName = localHtml;
	}

}
