package meg.biblio.lending;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.LoanHistoryRepository;
import meg.biblio.lending.db.LoanRecordRepository;
import meg.biblio.lending.db.PersonRepository;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.LoanRecordDisplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LendingServiceImpl implements LendingService {

	@Autowired
	ClientService clientService;
	

	@Autowired
	LendingSearchService lendingSearch;
	
	@Autowired
	BookRepository bookRepo;
	
	@Autowired
	PersonRepository personRepo;
	
	@Autowired
	LoanRecordRepository lrRepo;
	
	@Autowired
	LoanHistoryRepository lhRepo;	
	
	@Override
	public LoanRecordDao checkoutBook(Long bookid, Long borrowerid, Long clientid) {
		// get objects for ids - book, borrower, client
		ClientDao client = clientService.getClientForKey(clientid);
		BookDao book= bookRepo.findOne(bookid);
		PersonDao person = personRepo.findOne(borrowerid);
		
		Integer schoolyear = 0;
		boolean isteacher = true;
		if (person instanceof StudentDao) {
			isteacher=false;
			StudentDao st = (StudentDao)person;
			SchoolGroupDao sg = st.getSchoolgroup();
			schoolyear = sg.getSchoolyearbegin();
		} else if (person instanceof TeacherDao) {
			TeacherDao st = (TeacherDao)person;
			SchoolGroupDao sg = st.getSchoolgroup();
			schoolyear = sg.getSchoolyearbegin();
		}
		
		// make new loan record
		LoanRecordDao loanrec = new LoanRecordDao();
		// insert objects
		loanrec.setBorrower(person);
		loanrec.setBook(book);
		loanrec.setClient(client);
		loanrec.setSchoolyear(schoolyear);
		
		// insert dates - checkedout and due
		Integer checkoutdays = isteacher?client.getTeachercheckouttime():client.getStudentcheckouttime();
		Calendar ddatecal = Calendar.getInstance();
		ddatecal.setTime(new Date());
		ddatecal.add(Calendar.DAY_OF_MONTH, checkoutdays.intValue());
		Date duedate = ddatecal.getTime();
		
		loanrec.setCheckoutdate(new Date());
		loanrec.setDuedate(duedate);
		
		// persist loan record
		loanrec = lrRepo.save(loanrec);
		
		// return loan record
		return loanrec;
	}
	
	@Override
	public LoanHistoryDao returnBook(Long loanrecordid,Long clientid) {
		// get loanrecord, client
		LoanRecordDao lrecord = lrRepo.findOne(loanrecordid);
		ClientDao client = clientService.getClientForKey(clientid);
		
		if (lrecord!=null) {
			// create new loan history record
			LoanHistoryDao lhistory = new LoanHistoryDao();
			
			// fill in from loanrecord
			lhistory.setClient(client);
			lhistory.setBook(lrecord.getBook());
			lhistory.setBorrower(lrecord.getBorrower());
			lhistory.setCheckedout(lrecord.getCheckoutdate());
			lhistory.setDuedate(lrecord.getDuedate());
			lhistory.setSchoolyear(lrecord.getSchoolyear());
			// set teacher if this is a student
			if (lrecord.getBorrower() instanceof StudentDao) {
				// fill in class information (want to know how it was when checkedout / returned - may be different teacher next year)
				StudentDao student = (StudentDao)lrecord.getBorrower();
				SchoolGroupDao sgroup = student.getSchoolgroup();
				TeacherDao teacher = sgroup.getTeacher();
				lhistory.setTeachername(teacher.getFulldisplayname());
				
			}
			
			// fill in return date
			lhistory.setReturned(new Date());
			
			
			// persist loanhistory
			lhistory = lhRepo.save(lhistory);
			
			// delete loanrecord
			lrRepo.delete(lrecord);
			
			// return loanhistory
			return lhistory;
		
		}
return null;
	}

	@Override
	public List<LoanRecordDisplay> getCheckedOutBooksForClass(Long classid, Long clientid) {
		// build criteria
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setSchoolgroup(classid);

		// search for loan records
		List<LoanRecordDisplay> checkedout = lendingSearch.findLoanRecordsByCriteria(criteria, clientid);
		// return list
		return checkedout;
	}

	@Override
	public List<LoanRecordDisplay> getCheckedOutBooksForUser(Long borrowerId,
			Long clientid) {
		// build criteria
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setBorrowerid(borrowerId);

		// search for loan records
		List<LoanRecordDisplay> checkedout = lendingSearch.findLoanRecordsByCriteria(criteria, clientid);
		// return list
		return checkedout;
	}

	@Override
	public int getLendLimitForBorrower(Long borrowerId, Long clientid) {
		// get client
		ClientDao client = clientService.getClientForKey(clientid);
		// get borrower
		PersonDao person = personRepo.findOne(borrowerId);
		boolean isteacher = (person!=null && person instanceof TeacherDao) ;
		
		if (isteacher) {
			return client.getTeacherCOLimit().intValue();
		}
		return client.getStudentCOLimit();
	}

	@Override
	public List<LoanRecordDisplay> getOverdueBooksForClient(Long clientid) {
		// build criteria
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setOverdueOnly(true);
		
		// search for loan records
		List<LoanRecordDisplay> overdue = lendingSearch.findLoanRecordsByCriteria(criteria, clientid);
		// return list
		return overdue;
	}

	@Override
	public List<LoanRecordDisplay> getCheckedOutBooksForClient(Long clientid) {
		// build criteria
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		
		// search for loan records
		List<LoanRecordDisplay> checkedout = lendingSearch.findLoanRecordsByCriteria(criteria, clientid);
		// return list
		return checkedout;
	}


}
