package pdf.convert.test;

import java.io.File;
import java.util.concurrent.Callable;

import pdf.convert.ConverterHtmlToPdf;
import pdf.convert.PdfAudience;
import pdf.convert.PdfConvertContainer;
import pdf.db.DefaultDatabaseExecutor;
import pluto.lang.Tracer;

public class PdfWorker2 implements Runnable{
	private final PdfAudience audience ;

	public PdfWorker2(PdfAudience audience){
		this.audience = audience;
	}
	
	public PdfAudience audience(){
		return audience;
	}
	
	@Override
	public void run() {
		try {
			ConverterHtmlToPdf converter 
								= PdfConvertContainer.INSTANCE.converter(audience.getPostId());
			
			converter.createPdf(audience.getHtmlContent(), 
								audience.getPassword(), 
								new File(audience.getOutPdfFileName())
								);
			
		} catch (Exception e) {
			audience.setSuccess(false);
			if(!removeFile()){
				Tracer.error("[se] don't delete or rename File["+audience.getOutPdfFileName()+"]");
			}
		}
		// TODO LIST TABLE DB UPDATE 작업 처리
		// PDF첨부 파일 정보  Table에 업데이트 해준다
		// updateAttachedFile(audience);
		audience.setFinished(true);
	}
	
	/**
	* Html to PDF error 발생한 파일이라서 존재하면 삭제 하고
	* 삭제가 안되면 rename 해준다.
	* @return
	 */
	public boolean removeFile(){
		File pdfFile = new File(audience.getOutPdfFileName());
		if(pdfFile.exists()){
			if(!pdfFile.delete()){
				return pdfFile.renameTo(new File(audience.getOutPdfFileName()+".error"));
			}
			return true;
		}else return true;
	}
	
	public void updateAttachedFile(PdfAudience audience){
		try {
			//audience --> Map 변환;
			new DefaultDatabaseExecutor("AUTO_UPDATE", "PDF_ATTACH_FILE_UPDATE").dmlExecute(audience);
		} catch (Exception e) {
			Tracer.error("fail pdf attach update Exception " + e);
			audience.setSuccess(false);
		}
	}


}
