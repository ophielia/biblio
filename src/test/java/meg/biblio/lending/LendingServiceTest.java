package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.ClientRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;
import meg.biblio.search.BookSearchCriteria;
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
	ClientService clientService;
	@Autowired
	CatalogService catalogService;
	@Autowired
	ClassManagementService classService;
	@Autowired
	SearchService searchService;
	@Autowired
	LendingService lendingService;
	@Autowired
	BookRepository bookRepo;
	@Autowired
	ClientRepository clientRepo;
	@Autowired
	LoanRecordRepository lrRepo;

	Long bookid1;
	Long bookid2;
	Long bookid3;
	private Long classid;
	private Long student1id;
	private Long student2id;
	private Long student3id;
	private Long student4id;
	private Long clientid;

	@Before
	public void initData() {
		// create books to checkout and return
		clientid = clientService.getTestClientId();
		BookDao book1 = new BookDao();
		book1.setClientid(clientid);
		book1.getBookdetail().setTitle("Tidings of comfort");
		book1 = bookRepo.save(book1);
		BookDao book2 = new BookDao();
		book2.setClientid(clientid);
		book2.getBookdetail().setTitle("time of the year");
		book2 = bookRepo.save(book2);
		BookDao book3 = new BookDao();
		book3.setClientid(clientid);
		book3.getBookdetail().setTitle("another book");
		book3 = bookRepo.save(book3);

		bookid1 = book1.getId();
		bookid2 = book2.getId();
		bookid3 = book3.getId();

		// create class to do the checking out and returning
		// create dummy class, and three students
		SchoolGroupDao sgroup = new SchoolGroupDao();
		ClassModel model = new ClassModel(sgroup);
		model.setTeachername("Prof MacGonnagal");
		model.fillInTeacherFromEntry();
		model = classService.createClassFromClassModel(model, 1L);

		// add students to class -
		StudentDao luke = classService.addNewStudentToClass("hermione granger",
				1L, model.getSchoolGroup(), 1L);
		student1id = luke.getId();
		StudentDao obiwan = classService.addNewStudentToClass("ron weasley",
				1L, model.getSchoolGroup(), 1L);
		student2id = obiwan.getId();
		StudentDao leia = classService.addNewStudentToClass("draco malfoy", 2L,
				model.getSchoolGroup(), 1L);
		student3id = leia.getId();
		StudentDao hansolo = classService.addNewStudentToClass(
				"neville longbottom", 3L, model.getSchoolGroup(), 1L);
		student4id = hansolo.getId();
		model = classService.loadClassModelById(model.getClassid());
		classid = model.getClassid();
	}

	@Test
	public void testCheckout() {
		testCheckout(1);


	}

	private void testCheckout(int selectnr) {
		// get default client
		ClientDao client = clientService.getClientForKey(1L);
		Long clientid = client.getId();
		// get student
		// get list of classes - go through classes until student
		// is found, make this the student
		Long studentid = 0L;
		List<SchoolGroupDao> sgroups = classService
				.getClassesForClient(clientid);
		int i = 0;
		for (SchoolGroupDao sgroup : sgroups) {
			List<StudentDao> students = sgroup.getStudents();
			if (students != null) {
				for (StudentDao student : students) {
					if (student != null && i == selectnr) {
						studentid = student.getId();
						break;
					}
					i++;
				}
			}

			if (studentid != 0L)
				break;
		}

		// get book
		Long bookid = 0L;
		BookSearchCriteria bscrit = new BookSearchCriteria();
		bscrit.setClientid(clientid);
		List<BookDao> books = searchService.findBooksForCriteria(bscrit,
				clientid);
		i = 0;
		for (BookDao book : books) {
			if (book != null && i == selectnr) {
				bookid = book.getId();
				break;
			}
			i++;
		}

		// service call
		LoanRecordDao lr = lendingService.checkoutBook(bookid, studentid,
				clientid);

		// ensure that loan record exists with studentid, bookid, and checkedout
		// to current date
		Assert.assertNotNull(lr);
		Assert.assertEquals(studentid, lr.getBorrower().getId());
		Assert.assertEquals(bookid, lr.getBook().getId());
		// string comp to today....
	}

	@Test
	public void testReturnBookByBookid() {
		Long clientid = clientService.getTestClientId();
		ClientDao client = clientService.getClientForKey(clientid);
		SchoolGroupDao sgroup = new SchoolGroupDao();
		ClassModel model = new ClassModel(sgroup);
		model.setTeachername("willy wonka");
		model.fillInTeacherFromEntry();
		model = classService.createClassFromClassModel(model, clientid);

		// service call
		StudentDao student = classService.addNewStudentToClass("keli skalicky",
				1L, model.getSchoolGroup(), 1L);
		BookModel newbook = new BookModel();
		newbook.setTitle("hamburgers are good");
		newbook = catalogService.createCatalogEntryFromBookModel(clientid,
				newbook);
		// checkout book
		LoanRecordDao lr = lendingService.checkoutBook(newbook.getBookid(),
				student.getId(), clientid);

		// holdinfo
		// service call
		lendingService.returnBookByBookid(newbook.getBookid(), clientid);

		// ensure - loan record exists, with return date of today, studentid and
		// bookid as in loan record
		LoanRecordDao returned = lrRepo.findOne(lr.getId());
		
		Assert.assertNotNull(returned);
		Assert.assertNotNull(returned.getReturned());
	}

	@Test
	public void testReturn() {
		Long clientid = clientService.getTestClientId();
		// checkout book (book1id) to user (student3id)
		LoanRecordDao lr = lendingService.checkoutBook(bookid1, student3id, clientid);
		
		// service call
		LoanRecordDao lh = lendingService.returnBook(lr.getId(), clientid);

		Assert.assertNotNull(lh);
		Assert.assertEquals(student3id,lh.getBorrower().getId());
		Assert.assertEquals(bookid1,lh.getBook().getId());
		
		// now, check status of book
		BookDao booktest = bookRepo.findOne(bookid1);
		
		Assert.assertNotEquals(new Long(CatalogService.Status.CHECKEDOUT), booktest.getStatus());
	
	}

	@Test
	public void testGetCheckedOutBooksForClass() {
		// checkout 2 books for students
		lendingService.checkoutBook(bookid1, student1id, clientid);
		lendingService.checkoutBook(bookid2, student2id, clientid);

		// service call
		List<LoanRecordDisplay> results = lendingService
				.getCheckedOutBooksForClass(classid, clientid);

		// results not null
		Assert.assertNotNull(results);
		// borrowers to hash, books to hash
		List<Long> borrowerids = new ArrayList<Long>();
		List<Long> bookids = new ArrayList<Long>();
		for (LoanRecordDisplay lr : results) {
			borrowerids.add(lr.getBorrowerid());
			bookids.add(lr.getBookid());
		}
		// assert books and students found in results
		Assert.assertTrue(borrowerids.contains(student1id));
		Assert.assertTrue(borrowerids.contains(student2id));
		Assert.assertTrue(bookids.contains(bookid1));
		Assert.assertTrue(bookids.contains(bookid2));
	}

	@Test
	public void testGetCheckedOutBooksForBorrower() {
		// checkout 2 books for students
		lendingService.checkoutBook(bookid3, student3id, clientid);
		lendingService.checkoutBook(bookid2, student3id, clientid);

		// service call
		List<LoanRecordDisplay> results = lendingService
				.getCheckedOutBooksForUser(student3id, clientid);

		// results not null
		Assert.assertNotNull(results);
		Assert.assertEquals(2, results.size());
		// borrowers to hash, books to hash
		List<Long> borrowerids = new ArrayList<Long>();
		List<Long> bookids = new ArrayList<Long>();
		for (LoanRecordDisplay lr : results) {
			borrowerids.add(lr.getBorrowerid());
			bookids.add(lr.getBookid());
		}
		// assert books and students found in results
		Assert.assertTrue(borrowerids.contains(student3id));
		Assert.assertTrue(bookids.contains(bookid3));
		Assert.assertTrue(bookids.contains(bookid2));
	}

	@Test
	public void testLendingLimit() {
		// set lending limits for client
		ClientDao client = clientService.getClientForKey(clientid);
		client.setStudentCOLimit(1);
		client.setTeacherCOLimit(50);
		clientRepo.save(client);
		// get teacher for schoolgroup
		ClassModel cmodel = classService.loadClassModelById(classid);
		TeacherDao teacher = cmodel.getTeacher();
		// get student for schoolgroup
		List<StudentDao> students = cmodel.getStudents();
		StudentDao student = students.get(0);

		// service call - teacher
		int limit = lendingService.getLendLimitForBorrower(teacher.getId(),
				clientid);

		// should be 50
		Assert.assertEquals(50, limit);

		// service call - student
		limit = lendingService.getLendLimitForBorrower(student.getId(),
				clientid);

		// should be 1
		Assert.assertEquals(1, limit);
	}

	@Test
	public void testGetOverdue() {
		// make one overdue book - checkout book and then fiddle with db.
		LoanRecordDao makeoverdue = lendingService.checkoutBook(bookid3,
				student3id, clientid);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		Date fiddledate = cal.getTime();
		makeoverdue.setDuedate(fiddledate);
		makeoverdue.setCheckoutdate(fiddledate);
		lrRepo.save(makeoverdue);
		Long lrid = makeoverdue.getId();

		// get overdue list
		List<LoanRecordDisplay> results = lendingService
				.getOverdueBooksForClient(clientid);

		// ensure that size >0 and student/ book appear at leas once in list
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean lrfound = false;
		for (LoanRecordDisplay lr : results) {
			if (lr.getLoanrecordid().longValue() == lrid.longValue()) {
				lrfound = true;
				break;
			}
		}
		Assert.assertTrue(lrfound);
	}

	@Test
	public void testGetCheckedOutForClient() {
		// make one overdue book - checkout book and then fiddle with db.
		LoanRecordDao makeoverdue = lendingService.checkoutBook(bookid1,
				student4id, clientid);
		Long lrid = makeoverdue.getId();

		// get overdue list
		List<LoanRecordDisplay> results = lendingService
				.getCheckedOutBooksForClient(clientid);

		// ensure that size >0 and student/ book appear at leas once in list
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean lrfound = false;
		for (LoanRecordDisplay lr : results) {
			if (lr.getLoanrecordid().longValue() == lrid.longValue()) {
				lrfound = true;
				break;
			}
		}
		Assert.assertTrue(lrfound);
	}

}