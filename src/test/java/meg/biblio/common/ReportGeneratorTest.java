package meg.biblio.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.lending.LendingService;

import org.apache.fop.apps.FOPException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ReportGeneratorTest {

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
		rGen.generateOverdueNoticeReport(transformpath, outputpath, obr);
		Assert.assertEquals(1L,1L);
	}
	




}