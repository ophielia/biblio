package meg.biblio.catalog.db;

import java.util.List;

import meg.biblio.catalog.db.dao.SubjectDao;

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
public class SubjectRepositoryTest {

	@Autowired
	SubjectRepository subjectRepo;

		
	@Before
	public void setup() {
		SubjectDao pub = new SubjectDao();
		pub.setListing("TestPub");
		subjectRepo.save(pub);
	}
	
	@Test
	public void testFindPublisherByName() {
		List<SubjectDao> results = subjectRepo.findSubjectByText("tteessddtt");
		Assert.assertNotNull(results);
		Assert.assertEquals(0,results.size());
		
		// now, for the one we know is there....
		results = subjectRepo.findSubjectByText("testpub");
		Assert.assertNotNull(results);
		Assert.assertEquals(1,results.size());
		
	}
}