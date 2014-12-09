package meg.biblio.lending;

import java.util.Date;
import java.util.List;

import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.web.model.ClassModel;
import meg.biblio.lending.web.model.LoanRecordDisplay;

public interface LendingService {

	LoanRecordDao checkoutBook(Long bookid, Long borrowerid, Long clientid);

	LoanHistoryDao returnBook(Long loanrecordid, Long clientid);

	List<LoanRecordDisplay> getCheckedOutBooksForClass(Object classid);


}
