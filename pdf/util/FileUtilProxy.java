package pdf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import pdf.common.SysProperty;
import pdf.html.CrawlingController;
import pluto.lang.Tracer;

public class FileUtilProxy {
	
	public static FileUtils fileUtil;
	
    public static boolean createFolder(String folder) throws Exception{
    	try {
    		File crawFolder = new File(folder);
			if(crawFolder.exists()){
				return true;
//				throw new SamePathException("same directory exist");
			}else{
				crawFolder.mkdirs();
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Tracer.error("fail create directory["+folder+"]");
			throw e;
		}
    }
	
    public static boolean createFolder(String folder, int count, int unit) throws Exception{
    	if(count == 0){
    		Tracer.error("create Folder count is zero return false");
    		return false;
    	}
    	
    	int countDigit 	=  Integer.toString(count).length();
    	int unitDigit 	=  Integer.toString(unit).length();
    	
    	if(countDigit <= unitDigit) return true;
    	
//TODO pioneer(2016.03.30) 
//     단위 건수(unit) 이상의 건수가 있을 경우 directory를 생성하는 기준을 잡아야 합.
    	return true;
    }
    
    public static boolean createFile(byte[] content, String fileName){
		OutputStream os = null;
		try {
			os = new FileOutputStream(fileName);
			os.write(content);
			return true;
		}catch(Exception e){
//			log.error("error create file Exception[{}]", e);
			e.printStackTrace();
			return false;
		}finally{
			try {
				if(os !=null){
					os.close();
				}
			}catch(Exception e){}
		}
	}
    
    public static boolean createFile(String content, String fileName){
		return createFile(content.getBytes(), fileName);
	}
    
    public static String pdfAttachFileName(String postId, String memberId, String memberIdSeq) 
    throws Exception{
    	String pdfAttachDirectory = createPdfDirectory(postId, memberIdSeq);
		return (new StringBuffer())
				.append(pdfAttachDirectory)
				.append(System.getProperty("file.separator"))
				.append(postId)
				.append("_")
				.append(memberId)
				.append("_")
				.append(memberIdSeq)
				.append(CrawlingController.PDF_FILE_EXTENSION).toString();
    }
    
    private static String createPdfDirectory(String postId, String seq) throws Exception{
		StringBuffer postIdDirectory = new StringBuffer();
		try {
			postIdDirectory.append(SysProperty.PdfOutFilePath.value())
						   .append(postId);
//						   .append(System.getProperty("file.separator"))
//						   .append(separateDirectory(seq));
			
			if(!new File(postIdDirectory.toString()).exists()){
				new File(postIdDirectory.toString()).mkdirs();
			}
		} catch (Exception e) {
			throw new Exception("don't create Pdf Directory postId["+postId+"] Exception "+e);
		}
		return postIdDirectory.toString();
	}
    
    /** 한 폴더별 파일 개수 XXX 개 생성  **/
    private static String separateDirectory(String seq){
    	int len = seq.length();
    	if(len <= 3){  // 999 자리
    		return "low";
    	}else if(len == 4){  
    		return "midb-"+ Integer.parseInt(seq)%10; 
    	}else if(len == 5){ 
    		return "midt-"+ Integer.parseInt(seq)%100;
    	}else{
    		return "high-"+ Integer.parseInt(seq)%1000;
    	}
    }
    
	public static String fileContent(String fileName) throws Exception{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
	        String line;
	        
	        StringBuffer buffer = new StringBuffer();
	        
	        while( (line = reader.readLine()) != null){
	        	buffer.append(line+"\n");
	        }
	        return buffer.toString();
		} catch (Exception e) {
			Tracer.error("[se] file["+fileName+"] reader Exception "+e);
			throw e;
		}finally{
			if(reader != null) reader.close();
		}
	}
	
	
	public static void main(String[] args) {
		String abc = "abcdefghijklmn";
		
		System.out.println(abc.indexOf("jkl"));
		
	}
}
