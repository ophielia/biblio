package meg.biblio.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.web.model.LoanRecordDisplay;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerator {

	public static final class Transform {
		public static final String OVERDUE="overduenotices"; 
		
	}

	@Autowired
	LendingService lendingService;

	@Autowired
	ClientService clientService;
	
	private FopFactory fopFactory = FopFactory.newInstance();
	private TransformerFactory tFactory = TransformerFactory.newInstance();

    @Value("${biblio.report.transformsrc}")
    private String transformdir;
	
	public void testHelloWorld() throws IOException, TransformerException {
		BookDao book = new BookDao();
		book.setTitle("beep");;

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/myfile.pdf")));

		try {


		  //Setup FOP
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

			//Setup Transformer
			Source xsltSrc = new StreamSource(new File("C:/Temp/name2fo.xsl"));
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			//Make sure the XSL transformation's result is piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			//Setup input
			Source src = new StreamSource(new File("C:/Temp/name.xml"));


			//Start the transformation and rendering process
			transformer.transform(src, res);

		} catch (FOPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    //Clean-up
		    out.close();
		}


	}


	public void testMakeAnXml(Long clientid) throws JAXBException {
		List<LoanRecordDisplay> list = lendingService.getOverdueBooksForClient(clientid);

		// get first from list
		LoanRecordDisplay toxml = null;
		if (list!=null) {
			for (LoanRecordDisplay disp:list) {
				toxml = disp;
				break;
			}
		
			JAXBContext context = JAXBContext.newInstance(LoanRecordDisplay.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			m.marshal(toxml, new File("C:/Temp/lrd.xml"));
		}
		
		
		}

public String generateOverdueNoticeReport(String xslfilename,String outputpath, OverdueBookReport obr) throws FOPException, JAXBException, TransformerException, IOException {
	String outputfilename = outputpath + "togetherfile.pdf";
	
	//OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/togetherfile.pdf")));
	OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outputfilename)));
	try {
	
	Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
	JAXBContext context = JAXBContext.newInstance(OverdueBookReport.class);
	JAXBSource source = new JAXBSource(context,obr);
	
	//Setup Transformer
	Source xsltSrc = new StreamSource(new File(xslfilename));
	Transformer transformer = tFactory.newTransformer(xsltSrc);
	
	//Make sure the XSL transformation's result is piped through to FOP
	Result res = new SAXResult(fop.getDefaultHandler());

	//Start the transformation and rendering process
	transformer.transform(source, res);
	} finally {
		   out.close();
	}
	return outputfilename;
}
	
public void testMakeAnClassSummaryXml(Long clientid) throws JAXBException {
	Calendar cal = Calendar.getInstance();
	cal.set(Calendar.MONTH,Calendar.DECEMBER);
	cal.set(Calendar.DAY_OF_MONTH, 1);
	
	ClassSummaryReport summary = lendingService.assembleClassSummaryReport(22042L, cal.getTime(), 1L);
	
	JAXBContext context = JAXBContext.newInstance(ClassSummaryReport.class);
	Marshaller m = context.createMarshaller();
	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	
	m.marshal(summary, new File("C:/Temp/csr.xml"));
		
	}

public void developXSL() throws IOException, TransformerException {
	BookDao book = new BookDao();
	book.setTitle("beep");;

	// Step 2: Set up output stream.
	// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
	OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/myfile.pdf")));

	try {


	  //Setup FOP
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

		//Setup Transformer
		Source xsltSrc = new StreamSource(new File("C:/Temp/csr-en.xsl"));
		Transformer transformer = tFactory.newTransformer(xsltSrc);

		//Make sure the XSL transformation's result is piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());

		//Setup input
		Source src = new StreamSource(new File("C:/Temp/csr-base.xml"));


		//Start the transformation and rendering process
		transformer.transform(src, res);

	} catch (FOPException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (TransformerConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} finally {
	    //Clean-up
	    out.close();
	}


}
	


	
}
