package pdf.test;

import java.util.Map;
import java.util.TreeMap;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CleanerTransformations;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagTransformation;
import org.htmlcleaner.XmlSerializer;

import pluto.lang.Tracer;

public class HtmlCleanTest {

	public static void main(String[] args) {
		
		final HtmlCleaner cleaner = new HtmlCleaner();
        try {
        	String htmlContent = PdfWorker.htmlContent("C:/project/PDF/sample/craw/humuson/humuson.web.html");
        	
        	final CleanerProperties cleanerProperties = cleaner.getProperties();
        	//cleanerProperties.setOmitComments(true);  //HTML comments removed
        	cleanerProperties.setOmitXmlDeclaration(true);
        	cleanerProperties.setUseCdataForScriptAndStyle(false);
        	cleanerProperties.setUseEmptyElementTags(false);
        	cleanerProperties.setIgnoreQuestAndExclam(true);
        	
//        	CleanerTransformations transformations = new CleanerTransformations();
//        	TagTransformation freemarkTag = new TagTransformation("list", "list");
//        	transformations.addTransformation(freemarkTag);
//        	freemarkTag.addAttributeTransformation("targetAttName"); 
//        	cleaner.getProperties().setCleanerTransformations(transformations);
        	
        	final TagNode rootTagNode = cleaner.clean(htmlContent);
        	
        	// use the getAsString method on an XmlSerializer class
        	final XmlSerializer xmlSerializer = new PrettyXmlSerializer(cleanerProperties);
        	System.out.println(xmlSerializer.getAsString(rootTagNode));

        } catch (Exception e) {
            e.printStackTrace();
            Tracer.error("fail clean html so return orginal Html content");
        } 
	}
}
