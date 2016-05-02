package pdf.util;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import javax.net.ssl.*;

import pluto.lang.Tracer;


/**
 * 인증절차를 무시하고 Https ssl content를 수신 한다.
 * 
 * @author pioneer
 *
 */
public class HttpsContentWithoutValidation extends HttpsHandler{
	
	public HttpsContentWithoutValidation(String httpsUrl) throws Exception{
		super(httpsUrl);
	}
	
	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType) {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType) {
        }
    } };
	
	@Override
	public String getHttpsContent() throws Exception{
		 try {
		        SSLContext sc = SSLContext.getInstance("SSL");
		        sc.init(null, trustAllCerts, new java.security.SecureRandom());
		        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() 
		        {
                    public boolean verify(String paramString, SSLSession paramSSLSession) {
                        return true;
                    }
		        });
		    } catch (Exception e) {
			    Tracer.error("https connection Exception "+e);
			    throw e;
		    }
		    InputStreamReader isr = null;
		    
		    String sniExtension = System.getProperty("jsse.enableSNIExtension", "null");
		    if(!sniExtension.equals("false"))
		    System.setProperty("jsse.enableSNIExtension", "false");
//Now you can access an https URL without having the certificate in the truststore
		    try {
		        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
		        con.setDoOutput(true);
		        isr = new InputStreamReader(con.getInputStream(), "utf-8");
		        StringBuffer sb = new StringBuffer();
		        
		        int c;
		        while ((c = isr.read()) != -1) {
		            sb.append((char) c);
		        }
		        return sb.toString();
		    } catch (MalformedURLException e) {
		        e.printStackTrace();
		        return "";
		    }catch(Exception e){
		    	e.printStackTrace();
		    	throw e;
		    }finally {
		        try {
		            isr.close();
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
		    }
		}
	
	
	public void writeFile(String FileName) throws Exception{
		OutputStream os = new FileOutputStream(FileName);
		try {
			os.write(getHttpsContent().getBytes());
		} catch (Exception e) {
			Tracer.error("https url content file["+FileName+"] writing failed Exception "+e);
			e.printStackTrace();
			throw e;
		}finally{
			os.close();
		}
	}
	
	public static void main(String[] args) throws Exception{
		HttpsContentWithoutValidation h = new HttpsContentWithoutValidation("https://slipp.net/wiki/pages/viewpage.action?pageId=11632748");
		System.out.println(h.getHttpsContent());
	}
}
	

