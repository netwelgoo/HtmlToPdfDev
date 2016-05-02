package pdf.convert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import pdf.convert.test.PdfWorker2;
import pluto.lang.Tracer;

/**
 * @Link{PdfWorker} container (singleton)
 *  
 * @author pioneer(2016. 2. 23.)
 *
 */
public enum PdfWorkerContainer {
	
	INSTANCE;
	private static ExecutorService executorService;
	private static ReentrantLock lock = new ReentrantLock();
	private static Map<String, AtomicInteger> workingAudiencePosts 
							= new ConcurrentHashMap<String, AtomicInteger>();
	private static final Semaphore semaphore;
	private static final int workers;
	private static final int heapSizeMB = (int)(Runtime.getRuntime().totalMemory() / (1024*1024)); 
	private static final int chunkSize = heapSizeMB/2;
	
	private static CompletionService<PdfAudience> taskCompletionService;
	private static boolean running = true;
	private static final AtomicInteger errorCount = new AtomicInteger(0);
	
	static{
		int N_CPU = Runtime.getRuntime().availableProcessors(); 
		workers = (N_CPU <= 3) ? N_CPU : N_CPU * 7/10; //CPU<=3(Thread 3), CPU > 3 : 70% Active(thread) 
		
		semaphore = new Semaphore((int) heapSizeMB);
		Tracer.info("PdfWorkerContainer CPU Workers count["+ workers +"]");
		Tracer.info("PdfWorkerContainer Semaphore acquire count["+semaphore.getQueueLength()+"]");
		
		executorService = Executors.newFixedThreadPool(workers);
	}
	
	/**
	* 메일 컨텐츠(postId)가 Working 중인지 판단해서 알려준다. 
	* @param postId
	* @return
	*/
	public boolean running(String postId){
		if(!workingAudiencePosts.containsKey(postId)) return false;
		Tracer.info("now working Queue Size is "+workingAudiencePosts.get(postId).get());
		return !(workingAudiencePosts.get(postId).get()<=0);
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
	
	public void invoke(PdfAudience audience, List<PdfWorker> workers){
		if(workers.size() > chunkSize){
			workers.add(new PdfWorker(audience));
			invokeAll(workers);
		}
		else{
			workers.add(new PdfWorker(audience));
		}
	}
	
	public void invokeAll(List<PdfWorker> workers){
		try {
			List<Future<PdfAudience>> resultFutures = executorService.invokeAll(workers);
			for (Future<PdfAudience> future :resultFutures) {
				future.get();
			}
		} catch (InterruptedException e1) {
			Tracer.error("invokeAll Exception "+e1);
		} catch (Exception e){
			Tracer.error("future get Exception "+e);
		}finally{
			workers.clear();
		}
	}

//FIXME pioneer(2016.03.24)	
// performance 이슈로 인해 사용 안함.
// contextSwitching lock : 약 100건 처리 이후 2~3초 CPU context switching Idle 발생

	public void convert(PdfAudience audience){
		addWorkingCount(audience);
//Local Server ===============================================		
//		taskCompletionService.submit(new PdfWorker(audience));
		executorService.execute(new PdfWorker2(audience));
		
		
//FIXME pioneer (Remote Server) ===============================
//성능 이슈로 인해 별도 서버에서 변환 작업 수행시 
//Remote Server로 Sending process module 처리		
//=============================================================		
	}
	private void addWorkingCount(PdfAudience audience) {
		try {
			semaphore.acquire();
			postIdCountControl(audience.getPostId());
		} catch (Exception e) {
			Tracer.error("[se] semaphore acquire Exception "+e);
		}
	}
	
	public void postIdCountControl(String postId){
		//next time
		if(workingAudiencePosts.containsKey(postId)){
			AtomicInteger count = workingAudiencePosts.get(postId);
			count.set(count.incrementAndGet());
		}else{ //first time..
			workingAudiencePosts.put(postId, new AtomicInteger(1));
		}
	}
	
	public PdfAudience convert(PdfAudience audience, boolean boo){
		addWorkingCount(audience);
		Future<PdfAudience> result = executorService.submit(new PdfWorker(audience));
		try {
			return (PdfAudience)result.get();
		} catch (InterruptedException interruptException) {
			Tracer.error("audience["+audience.getPostId()+"] interrupted executing " + interruptException);
			errorSet(audience, "[se] interrupted executing  fail");
			return audience;
		} catch (ExecutionException executeException) {
			Tracer.error("audience["+audience.getPostId()+"] executing Exception " + executeException);
			errorSet(audience, "[se] executing  fail");
			return audience;
		} catch (Exception e1){
			Tracer.error("audience["+audience.getPostId()+"] Exception " + e1);
			errorSet(audience, "error working " + e1);
			return audience;
		}finally{
			String postId="";
			try {
				postId = ((PdfAudience)result).getPostId();
				AtomicInteger count = workingAudiencePosts.get(postId);
				count.set(count.decrementAndGet());
			} catch (Exception e) {
				// TODO: handle exception
				Tracer.error("i don't know working or waiting to postid["+postId+"] Exception "+e);
			}
		}
	}
	
	public void errorSet(PdfAudience audience, String msg){
		audience.setFinished(true);
		audience.setSuccess(false);
		audience.setResultMessage(msg);
	}
	
	private static void reset() throws Exception{
		//TODO do lock.
		lock.lock();
		try {
			running = false;
			Thread.sleep(3000); //3초 정도 대기 한다.
			executorService.shutdown();
		} catch (Exception e) {
			//TODO 에러 로그 남길 것.
			e.printStackTrace();
			try{
				executorService.shutdownNow();
				taskCompletionService = null;
			}catch(Exception e1){
				//TODO 중요한 문제 shutdown이 되지 않으면 초기화 수행하기 어렵다.
				e1.printStackTrace();
			}
		}finally{
			lock.unlock();
		}
		executorService 	  = Executors.newFixedThreadPool(workers);
		taskCompletionService = new ExecutorCompletionService<PdfAudience>(executorService);
	}
}
