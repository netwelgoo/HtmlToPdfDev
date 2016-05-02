package pdf.html;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import pdf.common.SysProperty;
import pdf.util.FileUtilProxy;

/**
 * 기존 PostID가 등록된 경우 불러오기 및 등록 작업
 * @author pioneer(2016. 2. 23.)
 * 
 * TODO changeRealDirectory() @Link{NothingState}와 중복됨 리팩토링 필요
 */
public class RegistState extends AbstractCrawlingState{

	@Override
	public boolean regist(HtmlInfo pdf) {
		try {
			if(backup(pdf.getPostId())){  //백업 완료 되었는지 확인
				changeRealDirectory(pdf);
			}else{ //TODO 백업이 되지 않으면 어떻게 해야 할까?
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
		try {
			//3. DB 저장된 Post Id를 찾아서 사용 불가 Update 시킨다.
			//4. 새로 등록 한다.
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
	
	public boolean backup(String postId) throws Exception{
		realPostIdDirectory = SysProperty.PdfLocalHtmlPath.value() + postId;
		
		String bckDirectory = SysProperty.PdfLocalHtmlPath.value()
								+ CrawlingController.BACKUP_PATH
								+ new SimpleDateFormat("yyMMddhhmmss").format(new Date()) +"/"
								+ postId;
		try {
			FileUtilProxy.createFolder(bckDirectory);
			File realDirectory = new File(realPostIdDirectory);
			if(!realDirectory.exists()){  //존재하지 않으면 옮길 필요도 없다. //TODO 로그만 남기자
				return true;
			}else{
				FileUtils.moveDirectoryToDirectory(realDirectory, new File(bckDirectory), true);
				FileUtils.deleteDirectory(realDirectory);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
	}
	
//	public boolean changeRealDirectory(HtmlInfo pdf) throws Exception{
//		if(!existDirectory(pdf.getPostId())){ //기존 파일이 존재하지 않다면 htmlLoad 한 다음 수행한다. 
//			htmlLoad(pdf);
//		}
//		File destDir = new File(CrawlingController.DEFAULT_PATH+pdf.getPostId());
//		if(destDir.exists()){  //TODO 처음 등록할 경우에 기존 Directory가 존재할 경우(이러면 안되지만) 어떻게 할 것인지 고려 할 것.  
//			throw new PdfDirectoryExistException("postId["+pdf.getPostId()+"]");
//		}else{
//			FileUtils.moveDirectoryToDirectory(new File(tempPostIdDirectory), destDir, true);
//			return true;
//		}
//	}
//
//	public boolean existDirectory(String postId){
//		tempPostIdDirectory = CrawlingController.DEFAULT_PATH
//										+ CrawlingController.PREFIX_VIEW
//										+ postId;
//		return new File(tempPostIdDirectory).exists();
//	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleDateFormat("yyMMddhhmmss").format(new Date()));
	}


}
