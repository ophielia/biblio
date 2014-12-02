package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import meg.biblio.catalog.db.ArtistRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundWordsDao;
import meg.biblio.catalog.db.FoundWordsRepository;
import meg.biblio.catalog.db.IgnoredWordsDao;
import meg.biblio.catalog.db.IgnoredWordsRepository;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.lending.LendingService;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.search.SearchService;

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
		ClassModel result = lendingService.createClassFromClassModel(classmod, 1L);
		// assert - teacher created with id and client, class created with id
		// and client
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getSchoolGroup());
		Assert.assertNotNull(result.getTeacher());
		SchoolGroupDao schoolgroup = result.getSchoolGroup();
		TeacherDao teacher= result.getTeacher();
		
		// also assert that school year is correct
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		if (month<Calendar.JULY) {
			year--;
		}
		Assert.assertEquals(new Integer(year), schoolgroup.getSchoolyearbegin());
		Assert.assertEquals(new Integer(year+1), schoolgroup.getSchoolyearend());
		
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