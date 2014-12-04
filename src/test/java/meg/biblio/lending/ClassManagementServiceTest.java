package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import meg.biblio.lending.db.SchoolGroupRepository;
import meg.biblio.lending.db.StudentRepository;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ClassManagementServiceTest {

	@Autowired
	ClassManagementService lendingService;
	
	@Autowired
	SchoolGroupRepository sgroupRepo;
	
	@Autowired
	StudentRepository studentRepo;	
	
	Long artistid;
	Long pubtestid;
Long testclassid;


	
	@Before
	public void inittest() {
		// make class
		// make new class model
		ClassModel classmod = new ClassModel();
		// set "Alba Rodrieguez" as teacherentry
		classmod.setTeachername("Mrs. Watson");
		// set "alba@free.fr" as email entry
		classmod.setTeacheremail("watson@free.fr");
		classmod.fillInTeacherFromEntry();

		// service call
		ClassModel result = lendingService.createClassFromClassModel(classmod,
				1L);	
		testclassid = result.getClassid();
	}
	
	@Test
	public void testCreateClassFromClassModel() {
		// make new class model
		ClassModel classmod = new ClassModel();
		// set "Alba Rodrieguez" as teacherentry
		classmod.setTeachername("Alba Rodrieguez");
		// set "alba@free.fr" as email entry
		classmod.setTeacheremail("alba@free.fr");
		classmod.fillInTeacherFromEntry();

		// service call
		ClassModel result = lendingService.createClassFromClassModel(classmod,
				1L);
		// assert - teacher created with id and client, class created with id
		// and client
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getSchoolGroup());
		Assert.assertNotNull(result.getTeacher());
		SchoolGroupDao schoolgroup = result.getSchoolGroup();

		// also assert that school year is correct
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		if (month < Calendar.JULY) {
			year--;
		}
		Assert.assertEquals(new Integer(year), schoolgroup.getSchoolyearbegin());
		Assert.assertEquals(new Integer(year + 1),
				schoolgroup.getSchoolyearend());

	}

	@Test
	public void testNewAddStudentToClass() {
		// make dummy class
		SchoolGroupDao sgroup = new SchoolGroupDao();
		ClassModel model = new ClassModel(sgroup);
		model.setTeachername("willy wonka");
		model.fillInTeacherFromEntry();
		model = lendingService.createClassFromClassModel(model, 1L);

		// service call
		ClassModel test = lendingService.addNewStudentToClass("keli skalicky",
				1L, model.getSchoolGroup(), 1L);

		// load classmodel
		test = lendingService.loadClassModelById(model.getClassid());

		// ensure that: model not null, at least one student in class
		Assert.assertNotNull(test);
		Assert.assertNotNull(test.getStudents());
		Assert.assertTrue(test.getStudents().size() > 0);

		// find student
		for (StudentDao student : test.getStudents()) {
			if (student.getFirstname().equals("keli")
					&& student.getLastname().equals("keli")) {
				// ensure that student first name and last name are correct by
				// being here! :-)
				// ensure that client and section are set
				Assert.assertNotNull(student.getClient());
				Assert.assertNotNull(student.getSchoolgroup()); // may need to
																// change this
																// to db
																// grab....
			}
		}

	}

	@Test
	public void removeStudentsFromClass() {
		// make dummy class
		SchoolGroupDao sgroup = new SchoolGroupDao();
		ClassModel model = new ClassModel(sgroup);
		model.setTeachername("willy wonka");
		model.fillInTeacherFromEntry();
		model = lendingService.createClassFromClassModel(model, 1L);
		Long classid = model.getClassid();
		ClassModel test = lendingService.addNewStudentToClass("keli skalicky",
				1L, model.getSchoolGroup(), 1L);
		test = lendingService.addNewStudentToClass("björn straube", 1L,
				model.getSchoolGroup(), 1L);
		test = lendingService.addNewStudentToClass("allison noinvite", 1L,
				model.getSchoolGroup(), 1L);

		// load test, and make list of ids to be removed(2)
		// first two will be removed
		test = lendingService.loadClassModelById(test.getClassid());
		List<Long> toremove = new ArrayList<Long>();
		List<StudentDao> students = test.getStudents();
		toremove.add(students.get(0).getId());
		toremove.add(students.get(1).getId());

		// service call
		test = lendingService.removeStudentsFromClass(toremove,
				test.getSchoolGroup(), 1L);
		// reload class
		ClassModel results = lendingService.loadClassModelById(classid);
		// assertions - classmodel not null, students not null, students size 1
		Assert.assertNotNull(results);
		Assert.assertNotNull(results.getStudents());
		Assert.assertTrue(results.getStudents().size() == 1);
		// hashmap - ids to StudentDao
		HashMap<Long, StudentDao> map = new HashMap<Long, StudentDao>();
		for (StudentDao student : results.getStudents()) {
			map.put(student.getId(), student);
		}
		// assert toremove ids aren't in hashmap
		for (Long removeid : toremove) {
			Assert.assertTrue(!map.containsKey(removeid));
		}

	}

	@Test
	public void tempTextGetSchoolYearBegin() {
		// this test is only for development, because it will be private in the
		// end service
		// marked as public for test only

		// Calendar for tests
		Calendar cal = Calendar.getInstance();

		// test for september 1970 - result should be 1970
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
		Integer beginyear = lendingService.getSchoolYearBeginForDate(cal
				.getTime());
		Assert.assertEquals(new Integer(1970), beginyear);

		// test for december 1970 - result should be 1970
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		beginyear = lendingService.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1970), beginyear);

		// test for july 1970 - result should be 1970
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JULY);
		beginyear = lendingService.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1970), beginyear);

		// test for july 1970 - result should be 1969
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.MARCH);
		beginyear = lendingService.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1969), beginyear);

	}

	@Test
	public void testEditStudent() {
		SchoolGroupDao sgroup = sgroupRepo.findOne(testclassid);
		// create student for class
		ClassModel createstudent = lendingService.addNewStudentToClass("george booth", 1L, sgroup, sgroup.getClient().getId());
		List<StudentDao> students = createstudent.getStudents();
		StudentDao student = students.get(0);
		long studentid = student.getId();
		// service call
		lendingService.editStudent(sgroup.getClient().getId(),student.getId(),"charles","schulz",student.getClient().getId());
		
		// reload student
		StudentDao result = studentRepo.findOne(studentid);
		
		// Assert  - not null, name charles, last name schulz, section 2
		Assert.assertNotNull(result);
		Assert.assertEquals("charles",result.getFirstname());
		Assert.assertEquals("schulz",result.getLastname());
		Assert.assertEquals(new Long(2),result.getSectionkey());
		Assert.assertNotNull(result.getClient());
		Assert.assertNotNull(result.getSchoolgroup());
		
		
	}
	
	@Test
	public void testAddExistingStudent() {
		// dummy class
				SchoolGroupDao dummyone = sgroupRepo.findOne(testclassid);
				Long dummyoneclient = dummyone.getClient().getId();
				// add students to class 
				ClassModel test = lendingService.addNewStudentToClass("keli skalicky",
								1L, dummyone, dummyoneclient);
						test = lendingService.addNewStudentToClass("björn straube", dummyoneclient,
								dummyone, 1L);
						test = lendingService.addNewStudentToClass("allison noinvite", dummyoneclient,
								dummyone, 1L);

				// get ids
				List<StudentDao> students = test.getStudents();
				List<Long> studentids = new ArrayList<Long>();
				for (StudentDao student:students) {
					studentids.add(student.getId());
				}
				// remove students from class
				test = lendingService.removeStudentsFromClass(studentids, dummyone, dummyoneclient);

				// now, add these students to another dummy class
				SchoolGroupDao sgroup = new SchoolGroupDao();
				ClassModel model = new ClassModel(sgroup);
				model.setTeachername("pete repeat");
				model.fillInTeacherFromEntry();
				model = lendingService.createClassFromClassModel(model, 1L);				
				
				// service call
				ClassModel resultclass = lendingService.assignStudentsToClass(studentids, model.getSchoolGroup(), model.getSchoolGroup().getClient().getId());
				
				
				// repo call to get students for another dummy class
				List<StudentDao> results = studentRepo.findActiveStudentsForClass(model.getSchoolGroup(), model.getSchoolGroup().getClient());
				
				// should be three students
				Assert.assertNotNull(results);
				Assert.assertTrue(results.size()==3);
				
				
	}
}