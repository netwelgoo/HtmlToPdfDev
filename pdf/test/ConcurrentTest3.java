package pdf.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pdf.convert.ConverterHtmlToPdf;
import pdf.convert.PdfConvertContainer;
import pdf.convert.PdfInfomation;
import pdf.itext.sample.ParseHtml;
import pluto.lang.Tracer;

public class ConcurrentTest3 {
//	private static final Logger log = LoggerFactory.getLogger(ConcurrentTest.class);
	
	//concurrent thread
	public static final ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public static void invoke(String name, String content, List<PdfWorker> workers){
		if(workers.size() > 1000){
			invokeAll(workers);
		}
		else{
			workers.add(new PdfWorker(name, content));
		}
	}
	
	public static void invokeAll(List<PdfWorker> workers){
		long s = System.currentTimeMillis();
		try {
			List<Future<String>> resultFutures = executorService.invokeAll(workers);
			for (Future<String> future :resultFutures) {
				future.get();
			}
		} catch (InterruptedException e1) {
			Tracer.error("invokeAll Exception "+e1);
		} catch (Exception e){
			Tracer.error("future get Exception "+e);
		}
		s = ParseHtml.log(s, workers.size()+"개 변환 시간");
	}
	
	public static void main(String[] args) {
		String postId = "1234567890123";
		PdfInfomation pdfInfo = new PdfInfomation(postId);
		try {
			ConverterHtmlToPdf converter = new ConverterHtmlToPdf(pdfInfo);
			PdfConvertContainer.INSTANCE.registConverter(converter);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		try{
			List<PdfWorker> workers = new ArrayList<PdfWorker>();
			String htmlContent = PdfWorker.htmlContent("C:/web/Apache2.2/htdocs/shinhan.html");
			
			for (int i = 0; i < 100000; i++) {
				ConcurrentTest3.invoke("name["+i+"]", htmlContent, workers);
			}
		} catch (InterruptedException e) {
			Tracer.error("[5202] error Msg ="+e.toString());
            e.printStackTrace();
        } catch (ExecutionException e) {
        	Tracer.error("[5203] error Msg ="+e.toString());
            e.printStackTrace();
        } catch (Exception e){
        	Tracer.error("[5204] error Msg ="+e.toString());
            e.printStackTrace();
        }
        executorService.shutdown();
	}
}
