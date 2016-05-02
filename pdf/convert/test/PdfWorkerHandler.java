package pdf.convert.test;

import java.util.concurrent.BlockingQueue;

import pluto.lang.Tracer;

public class PdfWorkerHandler implements Runnable{

	private final BlockingQueue<PdfWorker2> queue;
	
	public PdfWorkerHandler(int workers, BlockingQueue queue){
		this.queue = queue;
	}
	
	@Override
	public void run() {
		while(true){
			String email = "";
			try {
				PdfWorker2 worker = queue.take();
				email = worker.audience().getEmailAddress();
				worker.run();
			} catch (Exception e) {
				Tracer.error("pdf convertor email["+email+"] Exception "+e );
			}finally{
				try{
//TODO pioneer(2016.03.23) ==========
// finish Job 
					Tracer.info("pdf file writed email["+email+"]");	
				}catch(Exception e){
					Tracer.error("[se] error pdf working email["+email+"] count control Exception "+e);
				}
			}
			
		}
	}

}
