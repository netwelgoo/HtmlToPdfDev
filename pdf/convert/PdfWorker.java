package pdf.convert;

import java.io.File;
import java.util.concurrent.Callable;
import pdf.db.DefaultDatabaseExecutor;
import pluto.lang.Tracer;

public class PdfWorker implements Callable<PdfAudience>{
	public final PdfAudience audience ;

	public PdfWorker(PdfAudience audience){
		this.audience = audience;
	}
	
	@Override
	public PdfAudience call() throws Exception {
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
//Created PDF FileValidator Check
		if(!pdfFileValidatorSuccess()){
			audience.setSuccess(false);
			Tracer.error("this file size is very small so delete. "
						+ "file info["+audience.getOutPdfFileName()+"]");
			removeFile();
		}
		audience.setFinished(true);
		return audience;
	}
	
	private boolean pdfFileValidatorSuccess(){
		File pdfFile = new File(audience.getOutPdfFileName());
		if(pdfFile.exists()){
			return (pdfFile.length() > 2000); 
		}
		return false;
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
