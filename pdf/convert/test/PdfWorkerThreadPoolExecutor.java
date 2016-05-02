package pdf.convert.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PdfWorkerThreadPoolExecutor extends ThreadPoolExecutor{
	
	public PdfWorkerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			LinkedBlockingQueue<Runnable> blockingQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, blockingQueue);
	}
	
	@Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
/** module play before Thread executor  **/

	}
 
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
//          System.out.println("Perform exception handler logic");
        }
/** module play after Thread executor  **/
        
    }

}
