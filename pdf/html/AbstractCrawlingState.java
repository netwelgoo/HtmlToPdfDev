package pdf.html;

import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.commons.io.FileUtils;
import pdf.common.SysProperty;
import pdf.convert.exception.PdfDirectoryExistException;

public abstract class AbstractCrawlingState implements HtmlCrawler{
	protected CrawlingController crawler;
	protected String tempPostIdDirectory;
	protected String realPostIdDirectory;
	
	@Override
	public void htmlLoad(HtmlInfo pdf) {
		crawler = new CrawlingController(pdf);
		crawler.execute();
		System.out.println("## 결과 정보 #################");
		System.out.println(crawler.result().toString());
	}
	
	public boolean existDirectory(String postId){
		tempPostIdDirectory = SysProperty.PdfLocalHtmlPath.value()
										+ crawler.interposeDir()
										+ postId;
		return new File(tempPostIdDirectory).exists();
	}
	
	public boolean changeRealDirectory(HtmlInfo pdf) throws Exception{
		if(!existDirectory(pdf.getPostId())){ //기존 파일이 존재하지 않다면 htmlLoad 한 다음 수행한다. 
			htmlLoad(pdf);
		}
		File destDir = new File(SysProperty.PdfLocalHtmlPath.value() +pdf.getPostId());
		if(destDir.exists()){  //TODO 처음 등록할 경우에 기존 Directory가 존재할 경우 어떻게 할 것인지 고려 할 것.  
			throw new PdfDirectoryExistException("postId["+pdf.getPostId()+"]");
		}else{
			FileUtils.moveDirectoryToDirectory(new File(tempPostIdDirectory), destDir, true);
			return true;
		}
	}
	
	public HtmlCrawled result(){
		return crawler.result();
	}
	
	@Override
	public void createPdf(ByteArrayOutputStream pdfStream) {
		crawler.createPdf(pdfStream);
	}
	
}
