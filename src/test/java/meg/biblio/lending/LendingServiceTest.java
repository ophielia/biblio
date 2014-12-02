package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
public class LendingServiceTest {

	@Autowired
	LendingService lendingService;

	Long artistid;
	Long pubtestid;

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
	public void testAddStudentToClass() {
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

}