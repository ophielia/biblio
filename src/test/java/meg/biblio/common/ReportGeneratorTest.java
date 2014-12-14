package meg.biblio.common;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.junit.Assert;
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
	ReportGenerator rGen;

	@Test
	public void testGetCurrentClientKeyDefault() {
		try {
			rGen.HelloWorld();
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
		rGen.makeAnXml(1L);

	}

	@Test
	public void testTogether() throws JAXBException, FOPException, TransformerException, IOException {
		rGen.putThemTogether(1L);

	}
	
	@Test
	public void testWOFO() throws JAXBException, TransformerException {
		String test = rGen.noFo(1L);
		Assert.assertNotNull(test);
	}
	@Test
	public void testGenerateOverdueReport() throws JAXBException {
		rGen.generateOverdueReport(1L);

	}

}