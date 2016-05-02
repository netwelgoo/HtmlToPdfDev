package pdf.util;

import java.net.URL;

public abstract class HttpsHandler {
	
	protected final String httpsUrl ; 
	
	protected final URL url;
	
	public HttpsHandler(String httpsUrl) throws Exception{

		this.httpsUrl= httpsUrl;
		url = new URL(httpsUrl);
	}
	public abstract String getHttpsContent() throws Exception;
	
}
