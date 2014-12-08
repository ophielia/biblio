package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.LoanHistoryRepository;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.search.SearchService;

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
	ClientService clientService;
	@Autowired
	ClassManagementService classService;
	@Autowired
	SearchService searchService;
	@Autowired
	LendingService lendingService;
	@Autowired
	LoanRecordRepository lrRepo;
	@Autowired
	LoanHistoryRepository lhRepo;	
	
	@Test
	public void testCheckout() {
		testCheckout(1);
		testCheckout(2);
		testCheckout(3);
		
	}
	
	private void testCheckout(int selectnr) {
		// get default client
				ClientDao client = clientService.getClientForKey(1L);
				Long clientid = client.getId();
				// get student
				// get list of classes - go through classes until student 
				// is found, make this the student
				Long studentid = 0L;
				List<SchoolGroupDao> sgroups = classService.getClassesForClient(clientid);
				int i=0;
				for (SchoolGroupDao sgroup:sgroups) {
					List<StudentDao> students = sgroup.getStudents();
					if (students!=null) {
						for (StudentDao student:students) {
							if (student!=null && i==selectnr) {
								studentid = student.getId();
								break;
							}
							i++;
						}
					}

					if (studentid!=0L) break;
				}
				
				// get book
				Long bookid = 0L;
				BookSearchCriteria bscrit = new BookSearchCriteria();
				bscrit.setClientid(clientid);
				List<BookDao> books = searchService.findBooksForCriteria(bscrit,clientid);
				i=0;
				for (BookDao book:books) {
					if (book!=null && i==selectnr) {
						bookid = book.getId();
						break;
					}
					i++;
				}
				
				// service call
				LoanRecordDao lr = lendingService.checkoutBook(bookid, studentid, clientid);
				
				// ensure that loan record exists with studentid, bookid, and checkedout to current date
				Assert.assertNotNull(lr);
				Assert.assertEquals(studentid,lr.getBorrower().getId());
				Assert.assertEquals(bookid,lr.getBook().getId());
				// string comp to today....
	}
	
	@Test
	public void testReturn() {
		// get default client
		ClientDao client = clientService.getClientForKey(1L);
		Long clientid = client.getId();

		// get loanrecords - and select one
		LoanRecordDao selected = null;
		List<LoanRecordDao> checkedout = lrRepo.findForClient(client);
		for (LoanRecordDao lr:checkedout ) {
			if (lr!=null) {
				selected = lr;
			}
		}
		if (selected!=null) {
			// holdinfo
			PersonDao person = selected.getBorrower();
			BookDao book = selected.getBook();
			Long loanrecordid = selected.getId();
			// service call
			LoanHistoryDao lhist = lendingService.returnBook(selected.getId(), clientid);
			
			// ensure - loan record no longer exists
			LoanRecordDao testnull=lrRepo.findOne(loanrecordid);
			Assert.assertNull(testnull);
			// loanhistory record exists, with return date of today, studentid and bookid as in loan record
			boolean found = false;
			List<LoanHistoryDao> returned = lhRepo.findForClient(client);
			for (LoanHistoryDao record:returned) {
				if (record.getId().longValue()==lhist.getId().longValue()) {
					found=true;
					Assert.assertEquals(person.getId(), record.getBorrower().getId());
					Assert.assertEquals(book.getId(), record.getBook().getId());
					// check string here...
					break;
				}
			}
			Assert.assertTrue(found);
		} else {
			// should fail - nothing checkedout, so can't return
			Assert.assertEquals(new Long(3),new Long(4));
		}
			}	
}