package pdf.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

public class CSSParserTest 
{

    protected static CSSParserTest oParser;

    public static void main(String[] args) {
        oParser = new CSSParserTest();
        if (oParser.Parse("C:/project/PDF/sample/craw/p_530.7164920971356/shinhan/css/car_common.css")) {
            System.out.println("Parsing completed OK");
        } else {
            System.out.println("Unable to parse CSS");
        }   
    }

     public boolean Parse(String cssFile) 
     {
         FileOutputStream out = null; 
         PrintStream ps = null; 
         boolean rtn = false;

         try
         {
            // cssfile accessed as a resource, so must be in the pkg (in src dir).
//            InputStream stream = oParser.getClass().getResourceAsStream(cssfile);
            InputStream stream = new BufferedInputStream(new FileInputStream(new File(cssFile)));

             // overwrites and existing file contents
             out = new FileOutputStream("C:/project/PDF/sample/craw/p_530.7164920971356/shinhan/css/log.txt");

             if (out != null)
             {
                 //log file
                 ps = new PrintStream( out );
                 System.setErr(ps); //redirects stderr to the log file as well

             } else {
                 return rtn; 
            }

            InputSource source = new InputSource(new InputStreamReader(stream));
            CSSOMParser parser = new CSSOMParser();
            // parse and create a stylesheet composition
            CSSStyleSheet stylesheet = parser.parseStyleSheet(source, null, null);

            //ANY ERRORS IN THE DOM WILL BE SENT TO STDERR HERE!!
            // now iterate through the dom and inspect.

            CSSRuleList ruleList = stylesheet.getCssRules();

            ps.println("Number of rules: " + ruleList.getLength());


           for (int i = 0; i < ruleList.getLength(); i++) 
           {
             CSSRule rule = ruleList.item(i);
             if (rule instanceof CSSStyleRule) 
             {
                 CSSStyleRule styleRule=(CSSStyleRule)rule;
                 ps.println("selector:" + i + ": " + styleRule.getSelectorText());
                 CSSStyleDeclaration styleDeclaration = styleRule.getStyle();


                 for (int j = 0; j < styleDeclaration.getLength(); j++) 
                 {
                      String property = styleDeclaration.item(j);
                      ps.println("property: " + property);
                      ps.println("value: " + styleDeclaration.getPropertyCSSValue(property).getCssText());
                      ps.println("priority: " + styleDeclaration.getPropertyPriority(property));   
                 }



              }// end of StyleRule instance test
            } // end of ruleList loop

           if (out != null) out.close();
           if (stream != null) stream.close();
           rtn = true;
        }
        catch (IOException ioe)
        {
            System.err.println ("IO Error: " + ioe);
        }
        catch (Exception e)
        {
            System.err.println ("Error: " + e);

        }
        finally
        {
            if (ps != null) ps.close(); 
        }

        return rtn;

    }

}
