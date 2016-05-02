package pdf.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pdf.convert.ConverterHtmlToPdf;
import pdf.convert.PdfConvertContainer;
import pdf.convert.PdfInfomation;
import pdf.itext.sample.ParseHtml;
import pluto.lang.Tracer;

public class ConcurrentTest {
//	private static final Logger log = LoggerFactory.getLogger(ConcurrentTest.class);
	
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
		
		//concurrent thread
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		CompletionService<String> taskCompletionService 
								  = new ExecutorCompletionService<String>(executorService);
		List<String> workers = new ArrayList<String>();
		
		int COUNT = 1000;
		for (int i = 0; i < COUNT; i++) {
			workers.add("worker["+i+"]");
		}
		long s = System.currentTimeMillis();
		try {
			String htmlContent = PdfWorker.htmlContent("C:/project/PDF/sample/in/shinhan2.html");
			for (String worker : workers ) {
				taskCompletionService.submit(new PdfWorker(worker, htmlContent));
			}

			for (int i = 0; i < workers.size(); i++) {
				 Future<String> result = taskCompletionService.take(); 
				 String autoChannel = result.get();
			}
		s = ParseHtml.log(s, COUNT+"개 변환 시간");
		
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
