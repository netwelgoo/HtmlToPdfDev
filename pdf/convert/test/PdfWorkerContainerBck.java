package pdf.convert.test;

import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import pdf.convert.PdfAudience;
import pluto.lang.Tracer;

/**
 * @Link{PdfWorker} container (singleton)
 *  
 * @author pioneer(2016. 2. 23.)
 *
 */
public enum PdfWorkerContainerBck {
	
	INSTANCE;
	private static PdfWorkerThreadPoolExecutor executor;
	private static ReentrantLock lock = new ReentrantLock();
	private static Map<String, AtomicInteger> workingAudiencePosts 
							= new ConcurrentHashMap<String, AtomicInteger>();
	private static final int workers;
	
	static{
		int N_CPU = Runtime.getRuntime().availableProcessors(); 
		workers = (N_CPU <= 3) ? N_CPU : N_CPU * 7/10; //CPU<=3(Thread 3), CPU > 3 : 70% Active(thread) 
		LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>(workers);
		
		executor = new PdfWorkerThreadPoolExecutor(
											workers, // 최소 Thread 개수
											N_CPU,  //최대 Thread 개수 < LinkedBlockingQueue 일경우 사용안함 >
											5000,
											TimeUnit.HOURS,
											blockingQueue
					);
		
		 executor.setRejectedExecutionHandler(
				 	new RejectedExecutionHandler() {
		            @Override
		            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//		                System.out.println("PdfWorker Rejected : " + ((PdfWorker2) r).audience);
		                System.out.println("Waiting for one second !!");
		                try {
		                    Thread.sleep(1000);
		                } catch (InterruptedException e) {
		                    e.printStackTrace();
		                }
//		                System.out.println("retry PdfWorker : " + ((PdfWorker2) r).audience);
		                executor.execute(r);
		            }
	        });
		 
		 executor.prestartAllCoreThreads(); 
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
	
	public void convert(PdfAudience audience){
		addWorkingCount(audience);
//Local Server ===============================================		
		executor.execute(new PdfWorker2(audience));
//Remote Server ==============================================
		
//=============================================================		
	}

	private void addWorkingCount(PdfAudience audience) {
		while(lock.isLocked()){}
		String postId=audience.getPostId();
		//next time
		if(workingAudiencePosts.containsKey(postId)){
			AtomicInteger count = workingAudiencePosts.get(postId);
			count.set(count.incrementAndGet());
		}else{ //first time..
			workingAudiencePosts.put(audience.getPostId(), new AtomicInteger(1));
		}
	}
	
	public void errorSet(PdfAudience audience, String msg){
		audience.setFinished(true);
		audience.setSuccess(false);
		audience.setResultMessage(msg);
	}

}
