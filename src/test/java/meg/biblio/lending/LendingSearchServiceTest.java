package meg.biblio.lending;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import meg.biblio.common.ClientService;
import meg.biblio.lending.db.LoanHistoryRepository;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.web.model.LoanHistoryDisplay;
import meg.biblio.lending.web.model.LoanRecordDisplay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class LendingSearchServiceTest {

	@Autowired
	ClientService clientService;
	@Autowired
	ClassManagementService classService;
	@Autowired
	LendingSearchService lendingSearchService;
	@Autowired
	LendingService lendingService;
	@Autowired
	LoanRecordRepository lrRepo;
	@Autowired
	LoanHistoryRepository lhRepo;	
	
	@Test
	public void testBlowUp() {
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		Long clientid = 1L;
		
		// service call
		List<LoanRecordDisplay> results = lendingSearchService.findLoanRecordsByCriteria(criteria, clientid);
		
		// found something??
		Assert.assertNotNull(results);
		
		// service call
		List<LoanHistoryDisplay> results2 = lendingSearchService.findLoanHistoryByCriteria(criteria, clientid);
		
		// found something??
		Assert.assertNotNull(results2);		
	}
	
	@Test
	public void testCriteriaMethods() {
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		Long clientid = 1L;
		
		// nothing will be returned - just checking that the sql is generated properly and runnable
		// criteria - checkedouton, forschoolgroup, lenttotype(Student),overdueonly
		criteria.setCheckedouton(new Date());
		criteria.setSchoolgroup(2L);
		criteria.setLentToType(LendingSearchCriteria.LentToType.STUDENT);
		criteria.setOverdueOnly(true);

		// service call
		List<LoanRecordDisplay> results = lendingSearchService.findLoanRecordsByCriteria(criteria, clientid);
		List<LoanHistoryDisplay> histresults = lendingSearchService.findLoanHistoryByCriteria(criteria, clientid);

		// history only - returnedon, overdueonly
		criteria = new LendingSearchCriteria();
		criteria.setReturnedon(new Date());
		criteria.setOverdueOnly(true);
	}
}