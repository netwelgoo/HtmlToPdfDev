package pdf.convert.test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import pdf.convert.PdfAudience;
import pdf.convert.PdfWorker;
import pluto.lang.Tracer;

/**
 * @Link{PdfWorker} container (singleton)
 *  
 * @author pioneer(2016. 2. 23.)
 *
 */
public enum PdfWorkerContainerBck1 {
	
	INSTANCE;
	private static ExecutorService executorService;
	private static CompletionService<PdfAudience> taskCompletionService;
	private static ReentrantLock lock = new ReentrantLock();
	private static final AtomicInteger errorCount = new AtomicInteger(0);
	private static Map<String, AtomicInteger> workingAudiencePosts 
							= new ConcurrentHashMap<String, AtomicInteger>();
	
	private static final int workers;
	private static boolean running = true;
	
	static{

		int N_CPU = Runtime.getRuntime().availableProcessors(); 
		workers = (N_CPU <= 3) ? N_CPU : N_CPU * 7/10; //CPU<=3(Thread 3), CPU > 3 : 70% Active(thread) 
		
		executorService = Executors.newFixedThreadPool(workers);
		taskCompletionService = new ExecutorCompletionService<PdfAudience>(executorService);
		new Thread(){
			public void run(){
				PdfAudience audience = null;
				while(running){
					String postId = "";
					Future<PdfAudience> result = null;
					try {
						result = taskCompletionService.take();
						audience = result.get();
						postId =  audience.getPostId();
//						System.out.println("Thread finished =>" +audience.toString());
						//TODO Level Log 남길 것 => result.toString();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						if(errorCount.get() > 10){
							try {
								reset();
							} catch (Exception e2) {
								//TODO 중요 Log Error 남길 것.
								e2.printStackTrace();
							}
						}
						errorCount.incrementAndGet();
					}finally{
						try{
							//result가 null일 경우는 없다.
							//만약 null이 발생할 경우 심각한 오류가 발생한 것으로 봐야 함.
							AtomicInteger count = workingAudiencePosts.get(postId);
							count.set(count.decrementAndGet());
						}catch(Exception e){
							Tracer.error("[se] error pdf working postId["+postId+"] count control Exception "+e);
						}
					}
				}
			}
		}.start();
		
		long   heapSizeMB   = Runtime.getRuntime().totalMemory() / (1024*1024);
		
		new Thread(){
			public void run(){
				while(true){
					int totalCount = 0;
					try {
						Iterator iter = workingAudiencePosts.keySet().iterator();
						while(iter.hasNext()){
							String key = iter.next().toString();
							AtomicInteger ai = workingAudiencePosts.get(key);
							if(ai == null) ai.set(0);
							totalCount = totalCount + ai.get();
						}
					} catch (Exception e) {
						Tracer.error("PdfWorker Count Controller error Waiting three second..  Exception "+e);
					}finally{
						try {
							Thread.sleep(5000);
						} catch (Exception e2) {
							Tracer.error("Thread.sleep(5000)  Exception "+e2);
						}
					}
					Tracer.info("PDF Worker Queue Total Count["+totalCount+"]");
					waiting(totalCount);
				}
			}

			private void waiting(int totalCount){
				try {
					while(totalCount > 10000){
						try {
							if(lock.isLocked())	continue;
							Tracer.info("pdf worker queue size 1000 over 10 second waiting.. ");
							Thread.sleep(10000);
						} catch (Exception e) {
							Tracer.error(""+e);
						}finally{
							Thread.sleep(5000);
						}
					}
					Thread.sleep(5000);
				} catch (Exception e) {
					Tracer.error("[se]Waiting Exception "+e);
				}finally{
					if(lock.isLocked()) lock.unlock();
				}
			}
		}.start();
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
	
	public void convert(PdfAudience audience){
		addWorkingCount(audience);
//Local Server ===============================================		
		taskCompletionService.submit(new PdfWorker(audience));
//Remote Server ==============================================
		
//=============================================================		
	}
	private void addWorkingCount(PdfAudience audience) {
//		synchronized(this){
			while(running(audience.getPostId())){
				if( workingAudiencePosts.get(audience.getPostId()).get() > 10000 ) continue;
				try {
					Tracer.info("pdf worker is locked 3 second waiting.. ");
				} catch (Exception e) {
					Tracer.error("pdf worker is locked 3 second waiting.. Exception "+e);
				}finally{
					try {
						Thread.sleep(3000);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
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
//		}
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
