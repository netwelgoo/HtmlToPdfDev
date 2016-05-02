package pdf.test;

import java.io.FileOutputStream;
import java.io.OutputStream;
 
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
 
 
public class DecryptPDF {
     
    public static void execute(String inputPdfFile,byte[] ownerPassword,String outputPdfFile) throws Exception
    {
        PdfReader pdfReader = new PdfReader(inputPdfFile,ownerPassword);
         
        OutputStream os = new FileOutputStream(outputPdfFile);
         
        PdfStamper pdfStamper = new PdfStamper(pdfReader,os);
         
        pdfStamper.close();
    }
     
    public static void main(String[] args) throws Exception
    {
//        if(args.length!=3)
//        {
//            throw new Exception("Incorrect Using.\nUsage : DecryptPDF <inputPdfFile> <ownerPassword> <outputPdfFile>");
//        }
         
        String inputPdfFile = "C:/project/PDF/PDF_TO_HTML/SKT_201512.pdf";
        byte[] ownerPassword = "520701".getBytes();
        String outputPdfFile = "C:/project/PDF/PDF_TO_HTML/SKT_Sample.pdf";
         
        //System.out.println("inputPdfFile = " + inputPdfFile);
        //System.out.println("ownerPassword = " + new String(ownerPassword));
        //System.out.println("outputPdfFile = " + outputPdfFile);
         
        execute(inputPdfFile,ownerPassword,outputPdfFile);
         
    }
 
}