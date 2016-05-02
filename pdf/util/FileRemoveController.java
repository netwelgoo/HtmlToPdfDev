package pdf.util;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pdf.common.SysProperty;
import pdf.convert.PdfAudience;
import pluto.lang.Tracer;

public enum FileRemoveController {
	INSTNACE;
	
	private static final boolean TEST = "Y".equalsIgnoreCase(SysProperty.PdfTestDebug.value());
	private static ExecutorService executorService;
	private static BlockingQueue<String> removeFileQueue 
					= new ArrayBlockingQueue<String>(1000);
	
	private static CompletionService<PdfAudience> taskCompletionService;
	
	static{
//		taskCompletionService = new ExecutorCompletionService<PdfAudience>(executorService);
		
		executorService = Executors.newFixedThreadPool(30);
		executorService.execute(
			new Runnable(){
				@Override
				public void run() {
					long start = 0;
					while(true){
						try {
							if(TEST)
							start = System.currentTimeMillis();
							
							String fileName = removeFileQueue.take();
							File removeFile = new File(fileName);
							if(removeFile.exists()){
								if(!removeFile.delete()){
									Tracer.error("failed remove file["+fileName+"]");
								}
							}
							if(TEST)
							StopWatchUtil.log(start, "File delete StopWatch");
							
						} catch (Exception e) {
							Tracer.error("[se] file delete error continue.. Exception "+e);
						}
					}
				}
			}
		);
		
		if(TEST)
		new Thread(){
			public void run(){
				while(true){
					try {
						Thread.sleep(5000);
						
						if(removeFileQueue.size() > 700)
						Tracer.error("File Delete Queue Size("+removeFileQueue.size()+")");
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
	}
	
	public void remove(String fullPathFileName){
		removeFileQueue.add(fullPathFileName);
	}
	
}
