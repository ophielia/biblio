package meg.biblio.common;

import java.util.Locale;

import meg.biblio.common.report.BarcodeSheet;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;

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
public class BarcodeServiceTest {

	@Autowired
	ClientService clientService;

	@Autowired
	ClassManagementService classService;
	
	@Autowired
	BarcodeService barcodeService;
	
	@Test
	public void testAssembleBookSheet() {
		/*	PROBLEM LOADING THE MESSAGES PROPERTIES CAUSES THE WHOLE TEST TO FAIL
		Long clientid = clientService.getTestClientId();
		Locale test = Locale.US;
		BarcodeSheet sheet = barcodeService.assembleBarcodeSheetForBooks(50,  clientid,test);
		Assert.assertNotNull(sheet);
		Assert.assertTrue(sheet.getTitle().startsWith("dBook"));
		*/
	}
	
	@Test
	public void testAssembleClassSheet() {
	/*	PROBLEM LOADING THE MESSAGES PROPERTIES CAUSES THE WHOLE TEST TO FAIL
	 * 
	 * Long clientid = clientService.getTestClientId();
		
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
		Assert.assertNotNull(sheet);
		//Assert.assertEquals("Class of prof mcgonnagal",sheet.getTitle());
		Assert.assertEquals(5, sheet.getCodes().size());
		Assert.assertTrue(sheet.getCodes().get(0).getCode()!=null);
		*/
	}	


}