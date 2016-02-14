package meg.biblio.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import meg.biblio.common.report.TableReport;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.LendingService;

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
	public void markerMethod() {

	}

	@Test
	public void testMakeABarcodeXML() throws JAXBException {
		Long clientid = clientService.getTestClientId();
		Locale locale = Locale.FRANCE;

		BarcodeSheet sheet = barcodeService.assembleBarcodeSheetForBooks(65,0,
				0, 68L, locale);

		JAXBContext context = JAXBContext.newInstance(BarcodeSheet.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		m.marshal(sheet, new File("C:/Temp/bcsmareschale.xml"));
	}
	
	@Test
	public void testMakeAReportXML() throws JAXBException {
		Long clientid = clientService.getTestClientId();
		Locale locale = Locale.FRANCE;

		TableReport tr = new TableReport("title");
		tr.addColHeader("header1");
		tr.addColHeader("header2");
		tr.addColHeader("header3");
		tr.addValue("one");
		tr.addValue("two");
		tr.addValue("three");
		tr.addValue("four");
		tr.addValue("five");
		tr.addValue("six");
		
		JAXBContext context = JAXBContext.newInstance(TableReport.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		m.marshal(tr, new File("C:/Temp/reportmareschale.xml"));
	}	
	
	@Test
	public void testHelloWorld() throws IOException, TransformerException {

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/barcodes.pdf")));

		try {


		  //Setup FOP
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

			//Setup Transformer
			Source xsltSrc = new StreamSource(new File("C:/Temp/sample.xsl"));
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			//Make sure the XSL transformation's result is piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			//Setup input
			Source src = new StreamSource(new File("C:/Temp/sample_en.xml"));


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

	
	@Test
	public void testReport() throws IOException, TransformerException {

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/report.pdf")));

		try {


		  //Setup FOP
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

			//Setup Transformer
			Source xsltSrc = new StreamSource(new File("C:/Temp/tablereportformat.xsl"));
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			//Make sure the XSL transformation's result is piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			//Setup input
			Source src = new StreamSource(new File("C:/Temp/reportmareschale.xml"));


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
 * 
 * 
 
 
 

	@Test
	public void testMakeAnXml() throws JAXBException {
		Long clientid = clientService.getTestClientId();
		ClassSummaryReport list = lendingService.assembleClassSummaryReport( new Long(33724), new Date(), clientid);

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
	public void testMakeABarcodeXML() throws JAXBException {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		Locale locale = Locale.FRANCE;

		BarcodeSheet sheet = barcodeService.assembleBarcodeSheetForBooks(65,0,
				clientid, locale);

		JAXBContext context = JAXBContext.newInstance(BarcodeSheet.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		m.marshal(sheet, new File("C:/Temp/bcs.xml"));
	}

	@Test
	public void testHelloWorld() throws IOException, TransformerException {

		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("C:/Temp/barcodes.pdf")));

		try {


		  //Setup FOP
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

			//Setup Transformer
			Source xsltSrc = new StreamSource(new File("C:/Temp/bcs.xsl"));
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			//Make sure the XSL transformation's result is piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			//Setup input
			Source src = new StreamSource(new File("C:/Temp/bcs.xml"));


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