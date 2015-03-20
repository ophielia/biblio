package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.common.AppSettingService;
import meg.biblio.search.SearchService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InternalDetailFinder extends BaseDetailFinder {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	AppSettingService settingService;

	@Autowired
	SearchService searchService;

	@Autowired
	PublisherRepository pubRepo;

	/* Get actual class name to be printed on */
	static Logger log = Logger.getLogger(InternalDetailFinder.class.getName());

	Boolean lookupinternally;
	Long identifier = 11L;

	protected boolean isEnabled() throws Exception {
		if (lookupinternally == null) {
			lookupinternally = settingService
					.getSettingAsBoolean("biblio.internalfiner.turnedon");
		}
		return lookupinternally;
	}

	protected boolean isEligible(FinderObject findobj) throws Exception {
		// Eligible to be run if has isbn or has author and title
		BookDetailDao detail = findobj.getBookdetail();
		boolean titleauthor = false;
		boolean isbn = false;
		if (detail.hasAuthor() && detail.getTitle() != null) {
			titleauthor = true;
		}
		if (detail.getIsbn10() != null || detail.getIsbn13() != null) {
			isbn = true;
		}

		if (!(titleauthor || isbn)) {
			return false;
		}
		boolean searchrun = findobj.getCurrentFinderLog() % getIdentifier() == 0;

		return !searchrun;
	}

	protected Long getIdentifier() throws Exception {
		return identifier;
	}

	protected FinderObject searchLogic(FinderObject findobj) throws Exception {
		boolean isbnsearch = false;
		// determine search type - title/author or isbn
		BookDetailDao detail = findobj.getBookdetail();
		List<BookDetailDao> results = new ArrayList<BookDetailDao>();
		if (detail.getIsbn10() != null || detail.getIsbn13() != null) {
			// do isbn search
			results = doIsbnSearch(detail);
			isbnsearch = true;
		} else if (detail.hasAuthor() && detail.getTitle() != null) {
			// do titleauthor search
			results = doTitleAuthorSearch(detail);
		}

		// process results, if any
		if (results != null && results.size() > 0) {
			// set detailstatus
			findobj.setSearchStatus(CatalogService.DetailStatus.DETAILFOUND);
			// put first result into findobj
			BookDetailDao found = results.get(0);
			findobj.setBookdetail(found);
		} else {
			// set detailstatus to not found in book
			Long searchstatus = isbnsearch?CatalogService.DetailStatus.DETAILNOTFOUNDWISBN:CatalogService.DetailStatus.DETAILNOTFOUND;
			findobj.setSearchStatus(searchstatus);
		}

		// return
		return findobj;
	}

	private List<BookDetailDao> doTitleAuthorSearch(BookDetailDao detail) {
		// gather params
		String title = detail.getTitle().toLowerCase().trim();
		List<ArtistDao> authors = detail.getAuthors();
		ArtistDao tomatch = authors!=null && authors.size()>0?authors.get(0):null;
		String author = tomatch.getDisplayName().toLowerCase().trim();
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<BookDetailDao> c = cb.createQuery(BookDetailDao.class);
		Root<BookDetailDao> bookroot = c.from(BookDetailDao.class);
		c.select(bookroot);

		// get where clause
		List<Predicate> whereclause = new ArrayList<Predicate>();
		// always add clientspecific
		ParameterExpression<Boolean> clientparam = cb.parameter(Boolean.class, "clientspecific");
		whereclause.add(cb.equal(bookroot.<Boolean> get("clientspecific"), clientparam));
		// title
		if (title!=null) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"title");
			whereclause
					.add(cb.like(cb.lower(bookroot.<String> get("title")), param));
		}

		// author
		if (author!=null) {
			Join<BookDetailDao, ArtistDao> authorjoin = bookroot.join("authors");
			// where firstname = firstname and middlename = middlename and
			// lastname = lastname
			// together with likes and to lower
			// lastname
			if (tomatch.hasLastname()) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"alastname");
				Expression<String> path = authorjoin.get("lastname");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.like(lower, param);
				whereclause.add(predicate);
			}
			// middlename
			if (tomatch.hasMiddlename()) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"amiddlename");
				Expression<String> path = authorjoin.get("middlename");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.like(lower, param);
				whereclause.add(predicate);
			}
			// firstname
			if (tomatch.hasFirstname()) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"afirstname");
				Expression<String> path = authorjoin.get("firstname");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.like(lower, param);
				whereclause.add(predicate);
			}
		}
		
		// adding where clause
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating the query
		TypedQuery<BookDetailDao> q = entityManager.createQuery(c);

		// setting the parameters
		q.setParameter("clientspecific", new Boolean(false));
		// title
		if (title != null) {
			q.setParameter("title", title.trim());
		}
		if (author != null) {
			// author
				// where firstname = firstname and middlename = middlename and
				// lastname = lastname
				// together with likes and to lower
				// lastname
				if (tomatch.hasLastname()) {
					q.setParameter("alastname", "%"
							+ tomatch.getLastname().toLowerCase().trim() + "%");
				}
				// middlename
				if (tomatch.hasMiddlename()) {
					q.setParameter("amiddlename", "%"
							+ tomatch.getMiddlename().toLowerCase().trim() + "%");				
				}
				// firstname
				if (tomatch.hasFirstname()) {
					q.setParameter("afirstname", "%"
							+ tomatch.getFirstname().toLowerCase().trim() + "%");				

			}	
		}

		List<BookDetailDao> results = q.getResultList();
		return results;

	}

	private List<BookDetailDao> doIsbnSearch(BookDetailDao detail) {
		// gather params
		String isbn = detail.getIsbn10();
		String ean = detail.getIsbn13();

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<BookDetailDao> c = cb.createQuery(BookDetailDao.class);
		Root<BookDetailDao> bookroot = c.from(BookDetailDao.class);
		c.select(bookroot);

		// get where clause
		Expression sortexpr = null;
		List<Predicate> whereclause = new ArrayList<Predicate>();
		// always add clientspecific
		ParameterExpression<Boolean> clientparam = cb.parameter(Boolean.class, "clientspecific");
		whereclause.add(cb.equal(bookroot.<Boolean> get("clientspecific"), clientparam));
		// ean
		if (ean != null) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"ean");
			whereclause.add(cb.like(cb.lower(bookroot.<String> get("isbn13")),
					param));
		}
		if (isbn != null) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"isbn");
			whereclause.add(cb.like(cb.lower(bookroot.<String> get("isbn10")),
					param));
		}

		// adding where clause
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating the query
		TypedQuery<BookDetailDao> q = entityManager.createQuery(c);

		// setting the parameters
		q.setParameter("clientspecific", new Boolean(false));
		// title
		if (ean != null) {
			q.setParameter("ean", ean.trim());
		}
		if (isbn != null) {
			q.setParameter("isbn", isbn.trim());
		}

		List<BookDetailDao> results = q.getResultList();
		return results;
	}

	@Override
	public List<FinderObject> findDetailsForList(List<FinderObject> objects,
			long clientcomplete, Integer batchsearchmax) throws Exception {
		// check enabled
		if (isEnabled()) {

			// go through list
			for (FinderObject findobj : objects) {
				// check eligibility for object (eligible and not complete)
				if (isEligible(findobj)
						&& !resultsComplete(findobj, clientcomplete)) {
					// do search
					findobj = searchLogic(findobj);
					// log, process search
					findobj.logFinderRun(getIdentifier());
				}
			} // end list loop
		}
		// pass to next in chain, or return
		if (getNext() != null) {
			objects = getNext().findDetailsForList(objects, clientcomplete,
					batchsearchmax);
		}

		return objects;
	}

}
