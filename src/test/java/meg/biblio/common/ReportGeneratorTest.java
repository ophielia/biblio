package meg.biblio.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import meg.biblio.common.report.BarcodeSheet;
import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.DailySummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ReportGeneratorTest {
	

	@Autowired
	ClassManagementService classService;
	
	@Autowired
	BarcodeService barcodeService;
	
	@Autowired
	LendingService lendingService;
	
	@Autowired
	ClientService clientService;	
	
	private FopFactory fopFactory = FopFactory.newInstance();
	private TransformerFactory tFactory = TransformerFactory.newInstance();

	@Test
	public void testMakeAnXml() throws JAXBException {
		Long clientid = clientService.getTestClientId();
		ClassSummaryReport list = lendingService.assembleClassSummaryReport( new Long(33724), new Date(), clientid);
//assembleOverdueBookReport
		JAXBContext context = JAXBContext.newInstance(ClassSummaryReport.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		m.marshal(list, new File("C:/Temp/csr.xml"));
		
		}
	
	@Test
	public void testMakeAClassXml() throws JAXBException {
Long clientid = clientService.getTestClientId();
		
		// create dummy class, and three students
		SchoolGroupDao sgroup = new SchoolGroupDao();
		ClassModel model = new ClassModel(sgroup);
		model.setTeachername("prof mcgonnagal");
		model.fillInTeacherFromEntry();
		model = classService.createClassFromClassModel(model, clientid);

		// add students to class -
		StudentDao hermione = classService.addNewStudentToClass("hermione granger",
				1L, model.getSchoolGroup(), clientid);
		StudentDao ron = classService.addNewStudentToClass("ron weasley",
				1L, model.getSchoolGroup(), clientid);
		StudentDao draco = classService.addNewStudentToClass("draco malfoy", 2L,
				model.getSchoolGroup(), clientid);
		StudentDao neville = classService.addNewStudentToClass(
				"neville longbottom", 3L, model.getSchoolGroup(), clientid);
		model = classService.loadClassModelById(model.getClassid());
		
		
		BarcodeSheet sheet = barcodeService.assembleBarcodeSheetForClass(model.getSchoolGroup().getId(),  clientid, null);
		
		JAXBContext context = JAXBContext.newInstance(BarcodeSheet.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		m.marshal(sheet, new File("C:/Temp/bcs.xml"));
		}	
	
	@Test
	public void testHelloWorld() throws IOException, TransformerException {

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/myfile2.pdf")));

		try {


		  //Setup FOP
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

			//Setup Transformer
			Source xsltSrc = new StreamSource(new File("C:/Temp/csr-en.xsl"));
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			//Make sure the XSL transformation's result is piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			//Setup input
			Source src = new StreamSource(new File("C:/Temp/csr.xml"));


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

	
/*
	@Autowired
	ReportGenerator rGen;
	
	@Autowired
	LendingService lendingService;

	@Test
	public void testGetCurrentClientKeyDefault() {
		try {
			rGen.testHelloWorld();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testMakeXml() throws JAXBException {
		rGen.testMakeAnClassSummaryXml(1L);

	}
	
	
	@Test
	public void testdevelopXSL() throws JAXBException, IOException, TransformerException {
		rGen.developXSL();

	}
	@Test
	public void testTogether() throws JAXBException, FOPException, TransformerException, IOException {
		OverdueBookReport obr = lendingService.assembleOverdueBookReport(1L);
		String transformpath = "c:/Users/Margaret/Documents/workspace/biblio/src/main/resources/META-INF/web-resources/transform/table-en.xsl";
		String outputpath = "C:/Temp/";
		rGen.generateOverdueNoticeReport(transformpath, outputpath, obr,1L);
		Assert.assertEquals(1L,1L);
	}
	

	public static final class Transform {
		public static final String OVERDUE="overduenotices"; 
		
	}

	@Autowired
	LendingService lendingService;

	@Autowired
	ClientService clientService;
	
	

	
	private FopFactory fopFactory = FopFactory.newInstance();
	private TransformerFactory tFactory = TransformerFactory.newInstance();

	
	public String generateOverdueNoticeReport(String xslfilename,String outputpath, OverdueBookReport obr, Long clientid) throws FOPException, JAXBException, TransformerException, IOException {
		long millis = new Date().getTime();
		String reportname = clientid + "overdue" + millis + ".pdf";
		
		String outputfilename = outputpath + reportname;
		
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
		return reportwebdir + reportname;
	}
	
	public String generateClassSummaryReport(String xslfilename,String outputpath, ClassSummaryReport csr, Long clientid) throws FOPException, JAXBException, TransformerException, IOException {
		long millis = new Date().getTime();
		String reportname = clientid + "classsummary" + millis + ".pdf";
		
		String outputfilename = outputpath + reportname;
		
		//OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/togetherfile.pdf")));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outputfilename)));
		try {
		
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
		JAXBContext context = JAXBContext.newInstance(OverdueBookReport.class);
		JAXBSource source = new JAXBSource(context,csr);
		
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

public void testMakeAnClassSummaryXml(Long clientid) throws JAXBException {
	Calendar cal = Calendar.getInstance();
	cal.set(Calendar.MONTH,Calendar.DECEMBER);
	cal.set(Calendar.DAY_OF_MONTH, 1);
	
	ClassSummaryReport summary = lendingService.assembleClassSummaryReport(21486L, new Date(), 1L);
	
	JAXBContext context = JAXBContext.newInstance(ClassSummaryReport.class);
	Marshaller m = context.createMarshaller();
	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	
	m.marshal(summary, new File("C:/Temp/csr.xml"));
		
	}

public void developXSL() throws IOException, TransformerException {


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
	*/

}