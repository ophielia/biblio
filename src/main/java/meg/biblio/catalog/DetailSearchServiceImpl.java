package meg.biblio.catalog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundDetailsRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DetailSearchServiceImpl implements DetailSearchService {

	@Autowired
	AppSettingService settingService;


	@Autowired
	FoundDetailsRepository foundRepo;

	@Autowired
	ClientService clientService;
	
	@Autowired
	BookRepository bookRepo;

	@Autowired
	CatalogService catService;
	
	DetailFinder finderchain;

	  /* Get actual class name to be printed on */
	  static Logger log = Logger.getLogger(
			  DetailSearchServiceImpl.class.getName());


	public List<FoundDetailsDao> getFoundDetailsForBook(Long id) {
		
		if (id != null) {
			BookDao book = bookRepo.findOne(id);
			if (book!=null) {
				Long detailid = book.getBookdetail().getId();
				// query db for founddetails
				List<FoundDetailsDao> details = foundRepo.findDetailsForBook(detailid);
				// return founddetails
				return details;
			}
		}
		return null;
	}


	public void assignDetailToBook(Long detailid, Long bookid)
			throws GeneralSecurityException, IOException {
		// get found detail

		// copy found detail into bookdetail

		// update detailstatus of bookdetail

		// delete found details for book detail
	}


	@Override
	public BookModel fillInDetailsForBook(BookModel model, ClientDao client) {
		// get ready for search - determine is search is made with isbn,
		// complete for client
		BookDetailDao detail = model.getBook().getBookdetail();
		//  initialize search status
		detail.setDetailstatus(CatalogService.DetailStatus.NODETAIL);
		long clientcomplete = client.getDetailCompleteCode();

		// prepare FinderObject
		FinderObject findobj = new FinderObject(detail);

		// get finderchain
		if (finderchain==null) {
			finderchain = createFinderChain();
		}
		
		// run chain
		
		try {
			findobj = finderchain.findDetails(findobj, clientcomplete);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// process results 
		// reconstruct book model
		// put detail into bookmodel
		detail = findobj.getBookdetail();
		// set detailsearchstatus in bookdetail
		detail.setDetailstatus(findobj.getSearchStatus());
		model.setBookdetail(detail);
		if (detail.getDetailstatus()==CatalogService.DetailStatus.DETAILFOUND) {
			// check for any remaining founddetail objects, and delete
			if (detail.getId()!=null) {
				List<FoundDetailsDao> todelete = getFoundDetailsForBook(model.getBookid());
				if (todelete!=null && todelete.size()>0) {
					foundRepo.delete(todelete);
				}
			}
			try {
				classifyBook(client.getId(),model.getBook());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (detail.getDetailstatus()==CatalogService.DetailStatus.MULTIDETAILSFOUND) {
			// put found details into bookmodel
			List<FoundDetailsDao> founddetails = findobj.getMultiresults();
			model.setFounddetails(founddetails);
		}
		
		
		
		// return book model
		return model;
	}


	@Override
	public List<BookModel> fillInDetailsForBookList(List<BookModel> models,
			ClientDao client) {
		// get ready for search - clientcompletecode
		long clientcomplete = client.getDetailCompleteCode();
		Integer batchsearchmax = settingService.getSettingAsInteger("biblio.google.batchsearchmax");
		
		// get finderchain
		if (finderchain==null) {
			finderchain = createFinderChain();
		}
		
		// run chain
		try {
			models = finderchain.findDetailsForList(models, clientcomplete,batchsearchmax);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// persist found details
		List<BookModel> toreturn = new ArrayList<BookModel>();
		for (BookModel model:models) {
			if (model.getDetailstatus()==CatalogService.DetailStatus.DETAILFOUND || 
					model.getDetailstatus()==CatalogService.DetailStatus.MULTIDETAILSFOUND ||
					   model.getDetailstatus()==CatalogService.DetailStatus.DETAILNOTFOUND) {
				model = catService.createCatalogEntryFromBookModel(client.getId(), model);
			} 
			toreturn.add(model);
		}
		
		return toreturn;
	}

	private DetailFinder createFinderChain() {
		return null;
	}

	private void classifyBook(Long clientkey,BookDao book) throws Exception {
		if (book != null) {
			if (book.getBookdetail().getDetailstatus().equals(CatalogService.DetailStatus.DETAILFOUND)) {
				if (book.getClientid() != null) {
					Classifier classifier = clientService
							.getClassifierForClient(clientkey);
					book = classifier.classifyBook(book);
				}
			}
		}
	}
}
