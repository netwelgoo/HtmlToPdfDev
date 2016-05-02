package pdf.itext.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;

import org.junit.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
 
public class EncryptionPdf {
    /** User password. */
    public static byte[] USER = "Hello".getBytes();
    /** Owner password. */
    public static byte[] OWNER = "World".getBytes();
 
    /** The resulting PDF file. */
    public static final String RESULT1  = "C:/tmp/nio_new.pdf";
    /** The resulting PDF file. */
    public static final String RESULT2  = "C:/tmp/nio1.pdf";
    /** The resulting PDF file. */
    public static final String RESULT3  = "C:/tmp/nio_en.pdf";
    
    public boolean htmlToPdf(URL url, String pdfFile) throws IOException{
    	String urlText = url.getContent().toString();
    	
    	return htmlToPdf(urlText, pdfFile);
    }
    
    
    @Test
    public void htmlCreate(String htmlContent) throws Exception {
        // TODO Auto-generated method stub
        String fontname = "c:/tmp/GulimChe.ttf";
        String filename = "c:/sample/_filename.pdf";
         
        FontFactory.register(fontname);
        StringBuffer sBuff = new StringBuffer("<html>");
        sBuff.append("<head></head>");
        sBuff.append("<body>");
        sBuff.append("<table border=1>");
        sBuff.append("<tr><td>Test worker <b>가</b> 안녕하세요.</td><td>11<b>1</b>11</td></tr>");
        sBuff.append("</table>");
        sBuff.append("</body>");
        sBuff.append("</html>");
        StringReader stringReader = new StringReader(htmlContent);
 
        Document document = new Document();
        StyleSheet st = new StyleSheet();
        st.loadTagStyle("body", "face", ""); 
        st.loadTagStyle("body", "encoding", "Identity-H"); 
        st.loadTagStyle("body", "leading", "12,0"); 
        HTMLWorker worker = new HTMLWorker(document);
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        java.util.List<Element> p = HTMLWorker.parseToList(stringReader, st);
        for (int k = 0; k < p.size(); ++k)
            document.add((Element)p.get(k));
        
        document.close();
    }

    
    
    public boolean textToPdf(String content, String createFileName, boolean fileAppend) 
    throws Exception{
    	OutputStream file = new FileOutputStream(new File(createFileName), fileAppend);
        Document document = new Document();
        PdfWriter.getInstance(document, file);
        document.open();
        document.add(new Paragraph(content));
        document.close();
        file.close();
        return true;
    }
    
    public boolean htmlToPdf(String htmlContent, String pdfFile){
    	 try {
             OutputStream file = new FileOutputStream(new File(pdfFile));

             Document document = new Document(PageSize.A4.rotate());
             PdfWriter pdfWriter = PdfWriter.getInstance(document, file);
             document.open();
//           document.addAuthor("Real Gagnon");
//             document.addCreator("Real's HowTo");
//             document.addSubject("Thanks for your support");
//             document.addCreationDate();
//             document.addTitle("Please read this");

//             XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
//             worker.parseXHtml(pdfWriter, document, new StringReader(htmlContent));
           HTMLWorker htmlWorker = new HTMLWorker(document);
	         htmlWorker.parse(new StringReader(htmlContent));

             document.close();
             file.close();
             
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
    }
    
    /**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws DocumentException 
     * @throws IOException 
     */
    public void createPdf(String filename) throws IOException, DocumentException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
//        writer.setEncryption(USER, OWNER, PdfWriter.ALLOW_PRINTING, PdfWriter.STANDARD_ENCRYPTION_128);
        writer.setEncryption(USER, OWNER, PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
        writer.createXmpMetadata();
        // step 3
        document.open();
        // step 4
        document.add(new Paragraph("Hello World"));
        // step 5
        document.close();
    }
 
    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     */
    public void decryptPdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src, OWNER);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        stamper.close();
        reader.close();
    }
 
    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     */
    public void encryptPdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader 	= new PdfReader(src);
        PdfStamper stamper 	= new PdfStamper(reader, new FileOutputStream(dest));
        stamper.setEncryption(USER, OWNER, PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
        stamper.close();
        reader.close();
    }
 
    /**
     * Main method.
     *
     * @param  args no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main1(String[] args) throws Exception {
        EncryptionPdf metadata = new EncryptionPdf();
//      metadata.createPdf(RESULT1);
        BufferedReader reader = new BufferedReader(new FileReader("C:/sample/letter3.html"));
        
        String line;
        StringBuffer buffer = new StringBuffer();
        
        while( (line = reader.readLine()) != null){
        	buffer.append(line+"\n");
        }
        System.out.println(buffer.toString());
        
//        metadata.htmlToPdf(buffer.toString(), "C:/sample/letter2.pdf");
          metadata.htmlCreate(buffer.toString());
//        metadata.decryptPdf(RESULT1, RESULT2);
          metadata.encryptPdf(RESULT2, RESULT3);  // pdf --> pdf:�뷀샇��
    }
    
    
    public static void main(String[] args)  throws Exception {
        EncryptionPdf metadata = new EncryptionPdf();
        metadata.encryptPdf(RESULT2, RESULT3);  // pdf --> pdf:�뷀샇��
	}
    
}
