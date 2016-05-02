package pdf.convert.test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import jupiter.mass.log.update.ConcurrentInitialCode;
import jupiter.mass.log.update.sms.SmsResultWorker;
import pdf.convert.PdfAudience;
import pluto.lang.Tracer;

/**
 * @Link{PdfWorker} container (singleton)
 *  
 * @author pioneer(2016. 2. 23.)
 *
 */
public enum PdfWorkerContainer2 {
	
	INSTANCE;
	
	private static final int workers;
	private static final BlockingQueue<PdfWorker2> blockingQueue; 
	
	private static ReentrantLock lock = new ReentrantLock();
	private static Map<String, AtomicInteger> workingAudiencePosts 
							= new ConcurrentHashMap<String, AtomicInteger>();
	
	static{
		int N_CPU = Runtime.getRuntime().availableProcessors(); 
		workers = (N_CPU <= 3) ? N_CPU : N_CPU * 7/10; //CPU<=3(Thread 3), CPU > 3 : 70% Active(thread) 
		
		long heapSizeMB  = Runtime.getRuntime().totalMemory() / (1024*1024);
		int  queueSize 	 = (int) (heapSizeMB/workers);
		
		blockingQueue = new ArrayBlockingQueue<PdfWorker2>(workers * queueSize);
		
		for (int i = 0; i < workers; i++) {
			PdfWorkerHandler workerHandler = new PdfWorkerHandler(workers, blockingQueue);
			new Thread(workerHandler).start();
		}
	}
	
	/**
	* 메일 컨텐츠(postId)가 Working 중인지 판단해서 알려준다. 
	* @param postId
	* @return
	*/
	public boolean running(String postId){
		if(!workingAudiencePosts.containsKey(postId)) return false;
		
		return (workingAudiencePosts.get(postId).get()<=0);
	}
	
	/**
	* 잠깐 동안 정지 한다.
	* Web 에서 PDF 저장 시에 실제 저장이 되어 있다면 
	* @param millesecond
	*/
	public void shortStop(int millesecond){
		wating();
		try {
			Thread.sleep(millesecond);
		} catch (Exception e) {
			// TODO: handle exception
			Tracer.error("[se] thread sleep error so continue..  Exception "+e);
		}finally{
			resumeWork();
		}
	}
	
	/**
	 * locking 시 작업 하는 프로세스는 계속 작업하고 
	 * 추가 일거리를 받지 않고 lock() 상태를 유지한다.
	 *   
	 * (!중요)해당 메소드는 반듯이 finally에서 
	 * resumeWork() 메소드를 호출 하여야 한다.
	 * 안하면 일 안하고 계속 wating 상태로 머물러 있다. 
	 * ex) shortStop() Method 참조
	 */
	public void wating(){
		lock.lock();

	}
	
	public void resumeWork(){
		lock.unlock();
	}
	
	public void convert(PdfAudience audience) throws InterruptedException{
		addWorkingCount(audience);
//Local Server ===============================================		
		blockingQueue.put(new PdfWorker2(audience));
//Remote Server ==============================================
		
//=============================================================		
	}

	private void addWorkingCount(PdfAudience audience) {
		while(lock.isLocked()){
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String postId=audience.getPostId();
		//next time
		if(workingAudiencePosts.containsKey(postId)){
			AtomicInteger count = workingAudiencePosts.get(postId);
			count.set(count.incrementAndGet());
		}else{ //first time..
			workingAudiencePosts.put(audience.getPostId(), new AtomicInteger(1));
		}
	}
	
	
}
