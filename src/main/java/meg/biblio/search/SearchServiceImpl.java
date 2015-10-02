package meg.biblio.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Coalesce;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.BookIdentifier;
import meg.biblio.catalog.BookMemberService;
import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookDetailRepository;
import meg.biblio.catalog.db.FoundWordsDao;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.web.model.Pager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchServiceImpl implements SearchService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	BookMemberService bMemberService;

	@Autowired
	private BookDetailRepository bookDetailRepo;

	public ArtistDao findArtistMatchingName(ArtistDao tomatch) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ArtistDao> c = cb.createQuery(ArtistDao.class);
		Root<ArtistDao> exp = c.from(ArtistDao.class);
		c.select(exp);

		if (tomatch != null) {
			// get where clause
			List<Predicate> whereclause = new ArrayList<Predicate>();
			// lastname
			if (tomatch.hasLastname()) {
				Expression<String> path = exp.get("lastname");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.equal(lower, tomatch.getLastname()
						.toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("lastname")));
			}
			// middlename
			if (tomatch.hasMiddlename()) {
				Expression<String> path = exp.get("middlename");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.equal(lower, tomatch.getMiddlename()
						.toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("middlename")));
			}
			// firstname
			if (tomatch.hasFirstname()) {
				Expression<String> path = exp.get("firstname");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.equal(lower, tomatch.getFirstname()
						.toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("firstname")));
			}

			// creating the query
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			TypedQuery<ArtistDao> q = entityManager.createQuery(c);

			List<ArtistDao> results = q.getResultList();
			if (results != null && results.size() > 0) {
				return results.get(0);
			} else {
				return null;
			}

		}

		return null;
	}

	public List<Long> findBookIdByClientId(String clientbookid) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> c = cb.createQuery(Long.class);
		Root<BookDao> exp = c.from(BookDao.class);
		c.select(exp.<Long> get("id"));

		if (clientbookid != null) {
			// get where clause
			List<Predicate> whereclause = new ArrayList<Predicate>();

			Expression<String> path = exp.get("clientbookid");
			Expression<String> trim = cb.trim(path);
			Predicate predicate = cb.equal(trim, clientbookid.trim());
			whereclause.add(predicate);

			// creating the query
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			TypedQuery<Long> q = entityManager.createQuery(c);

			List<Long> results = q.getResultList();
			return results;
		}

		return null;

	}

	public List<BookDao> findBooksWithoutDetails(int max, ClientDao client) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<BookDao> c = cb.createQuery(BookDao.class);
		Root<BookDao> exp = c.from(BookDao.class);
		Join<BookDao, BookDetailDao> bookdetail = exp.join("bookdetail");
		c.select(exp);

		// get where clause
		List<Predicate> whereclause = new ArrayList<Predicate>();

		Expression<Long> path = bookdetail.get("detailstatus");
		Predicate predicate = cb.equal(path, new Long(
				CatalogService.DetailStatus.NODETAIL));
		whereclause.add(predicate);

		Expression<Long> path2 = exp.get("clientid");
		Predicate predicate2 = cb.equal(path2, client.getId());
		whereclause.add(predicate2);

		// creating the query
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
		TypedQuery<BookDao> q = entityManager.createQuery(c).setMaxResults(max);

		List<BookDao> results = q.getResultList();
		return results;

	}

	@Override
	public Long getBookCount(Long clientid) {
		// put together query
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> c = cb.createQuery(Long.class);
		Root<BookDao> exp = c.from(BookDao.class);
		Expression countExpression = cb.count(exp.<Number> get("id"));
		c.select(countExpression);

		List<Predicate> whereclause = new ArrayList<Predicate>();

		// making space for parameters
		// always add client id
		ParameterExpression<Long> clientparam = cb.parameter(Long.class,
				"clientid");
		whereclause.add(cb.equal(exp.<Long> get("clientid"), clientparam));

		// adding the whereclause
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating the query
		TypedQuery<Long> q = entityManager.createQuery(c);

		// setting the parameters
		// always add clientid
		q.setParameter("clientid", clientid);

		List<Long> results = q.getResultList();
		if (results != null && results.size() > 0) {
			return results.get(0);
		}
		return 0L;
	}

	@Override
	public Long getBookCountForCriteria(BookSearchCriteria criteria,
			Long clientid) {
		if (criteria != null) {
			if (criteria.hasKeyword()) {
				return getBookCountWithKeyword(criteria, clientid);
			}
			return getBookCountNoKeyword(criteria, clientid);
		}
		return 0L;
	}

	private Long getBookCountNoKeyword(BookSearchCriteria criteria,
			Long clientid) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> c = cb.createQuery(Long.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		Join<BookDao, BookDetailDao> bookdetail = bookroot.join("bookdetail");
		Expression countExpression = cb.count(bookroot.<Number> get("id"));
		c.select(countExpression);

		if (criteria != null) {

			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,
					cb, bookroot, bookdetail, sortexpr);

			// adding where clause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

			// creating the query
			TypedQuery<Long> q = entityManager.createQuery(c);

			// setting the parameters
			setParametersInQuery(criteria, q, clientid);

			List<Long> results = q.getResultList();
			if (results != null && results.size() > 0) {
				return results.get(0);
			}
			return 0L;

		}

		return 0L;
	}

	private Long getBookCountWithKeyword(BookSearchCriteria criteria,
			Long clientid) {

		// process keywords
		List<String> keywordlist = null;
		double listsize = 1.0;
		String keywords = criteria.getKeyword().toLowerCase().trim();
		if (keywords.contains(" ")) {
			keywordlist = new ArrayList<String>();
			// we have more than one word.
			// split keyword into array
			// use "in" (like be damned)
			String[] wordarray = keywords.split(" ");
			for (int i = 0; i < wordarray.length; i++) {
				keywordlist.add(wordarray[i]);
			}
			listsize = new Double(keywordlist.size()).doubleValue();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> c = cb.createQuery(Long.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		Join<BookDao, BookDetailDao> bookdetail = bookroot.join("bookdetail");
		Join<BookDetailDao, FoundWordsDao> foundwords = bookdetail
				.join("foundwords");
		Expression countExpression = cb.countDistinct(bookroot.<Number> get("id"));
		c.select(countExpression);

		if (criteria != null) {
			// get where clause
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,
					cb, bookroot, bookdetail, null);

			// add keyword(s) to where clause
			if (keywordlist != null) {
				Expression<String> exp = foundwords.get("word");
				Predicate predicate = exp.in(keywordlist);
				whereclause.add(predicate);
			} else {
				ParameterExpression<String> param = cb.parameter(String.class,
						"keyword");
				whereclause.add(cb.like(
						cb.lower(foundwords.<String> get("word")), param));
			}

			// adding the whereclause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

			// creating the query
			TypedQuery<Long> q = entityManager.createQuery(c);

			// setting the parameters
			setParametersInQuery(criteria, q, clientid);

			// add keyword(s) parameters
			if (keywordlist == null) {
				q.setParameter("keyword", "%" + keywords + "%");
			}

			List<Long> results = q.getResultList();
			if (results != null && results.size() > 0) {
				return results.get(0);
			}
			return 0L;

		}

		return 0L;
	}

	@Override
	public HashMap<Long, Long> breakoutByBookField(long bookkey, Long clientid) {

		// determine field string
		String fieldstring = "status";
		if (bookkey == SearchService.Breakoutfield.DETAILSTATUS) {
			fieldstring = "detailstatus";
		}
		// put together query
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createQuery(Tuple.class);
		Root<BookDao> exp = c.from(BookDao.class);
		Expression countExpression = cb.count(exp.<Number> get("id"));
		c.multiselect(exp.get(fieldstring), countExpression).groupBy(
				exp.get(fieldstring));

		List<Predicate> whereclause = new ArrayList<Predicate>();

		// making space for parameters
		// always add client id
		ParameterExpression<Long> clientparam = cb.parameter(Long.class,
				"clientid");
		whereclause.add(cb.equal(exp.<Long> get("clientid"), clientparam));

		// adding the whereclause
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating the query
		TypedQuery<Tuple> q = entityManager.createQuery(c);

		// setting the parameters
		// always add clientid
		q.setParameter("clientid", clientid);

		List<Tuple> results = q.getResultList();
		HashMap<Long, Long> toreturn = new HashMap<Long, Long>();

		for (Tuple t : results) {
			Long key = (Long) t.get(0);
			Long value = (Long) t.get(1);
			toreturn.put(key, value);
		}

		return toreturn;
	}

	@Transactional
	@Override
	public List<BookDao> findBooksForCriteria(BookSearchCriteria criteria,
			Pager pager, Long clientid) {

		if (criteria != null) {
			if (criteria.hasKeyword()) {
				return findBooksWithKeyword(criteria, pager, clientid);
			}
		}
		return findBooksNoKeyword(criteria, pager, clientid);
	}

	@Override
	public BookDetailDao findBooksForIdentifier(BookIdentifier bi) {
		if (bi != null) {
			// ean , isbn
			String ean = bi.getEan();
			String isbn = bi.getIsbn();

			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<BookDetailDao> c = cb
					.createQuery(BookDetailDao.class);
			Root<BookDetailDao> bookroot = c.from(BookDetailDao.class);
			c.select(bookroot);

			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = new ArrayList<Predicate>();
			boolean hasean = false;
			// ean
			if (ean != null) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"ean");
				whereclause.add(cb.like(
						cb.lower(bookroot.<String> get("isbn13")), param));
				hasean = true;
			}
			if (isbn != null) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"isbn");
				whereclause.add(cb.like(
						cb.lower(bookroot.<String> get("isbn10")), param));
			}

			// adding where clause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

			// creating the query
			TypedQuery<BookDetailDao> q = entityManager.createQuery(c);

			// setting the parameters
			// title
			if (ean != null) {
				q.setParameter("ean", ean.trim());
			}
			if (isbn != null) {
				q.setParameter("isbn", isbn.trim());
			}

			List<BookDetailDao> results = q.getResultList();
			if (results != null && results.size() > 0) {
				return results.get(0);
			}
		}
		return null;
	}

	private List<BookDao> findBooksNoKeyword(BookSearchCriteria criteria,
			Pager pager, Long clientid) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<BookDao> c = cb.createQuery(BookDao.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		Join<BookDao, BookDetailDao> bookdetail = bookroot.join("bookdetail");
		c.select(bookroot);

		if (criteria != null) {

			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,
					cb, bookroot, bookdetail, sortexpr);

			// adding where clause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

			// adding orderby
			long orderdir = criteria.getOrderbydir();
			List<Expression> exprlist = new ArrayList<Expression>();
			if (sortexpr == null) {
				if (criteria.getOrderby() == BookSearchCriteria.OrderBy.AUTHOR) {
					Join<BookDao, ArtistDao> authorjoin = bookdetail
							.join("authors");
					sortexpr = authorjoin.get("lastname");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.DATEADDED) {
					sortexpr = bookroot.get("createdon");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.TITLE) {
					sortexpr = bookdetail.get("title");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.SHELFCLASS) {
					sortexpr = bookroot.get("clientshelfcode");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.BOOKID) {
					exprlist.add(bookroot.get("clientbookidsort"));
					sortexpr = bookroot.get("clientbookid");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.BOOKTYPE) {
					Coalesce<Long> cexp = cb.coalesce();
					cexp.value(bookroot.<Long> get("clientbooktype"));
					cexp.value(bookdetail.<Long> get("listedtype"));

					sortexpr = cexp;
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.STATUS) {
					sortexpr = bookroot.get("status");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.DETAILSTATUS) {
					sortexpr = bookdetail.get("detailstatus");
				} else {
					// default
					sortexpr = bookdetail.get("title");
				}
			}
			exprlist.add(sortexpr);
			List<Order> orderlist = new ArrayList<Order>();
			if (orderdir == BookSearchCriteria.OrderByDir.ASC) {
				for (Expression order : exprlist) {
					orderlist.add(cb.asc(order));
				}
			} else {
				for (Expression order : exprlist) {
					orderlist.add(cb.desc(order));
				}
			}
			c.orderBy(orderlist);
			// creating the query
			int firstresult =  0;
			int maxresult = 0;

			if (pager!=null) {
				if (pager.getResultcount()>0) {
					firstresult =  pager.getFirstResult(); 	
				} 
				maxresult=pager.getMaxResults() ;
			}
			TypedQuery<BookDao> q = entityManager.createQuery(c)
					.setFirstResult(firstresult).setMaxResults(maxresult);

			// setting the parameters
			setParametersInQuery(criteria, q, clientid);

			return q.getResultList();

		}

		return null;
	}

	private List<BookDao> findBooksWithKeyword(BookSearchCriteria criteria,
			Pager pager, Long clientid) {

		// process keywords
		List<String> keywordlist = null;
		double listsize = 1.0;
		String keywords = criteria.getKeyword().toLowerCase().trim();
		if (keywords.contains(" ")) {
			keywordlist = new ArrayList<String>();
			// we have more than one word.
			// split keyword into array
			// use "in" (like be damned)
			String[] wordarray = keywords.split(" ");
			for (int i = 0; i < wordarray.length; i++) {
				keywordlist.add(wordarray[i]);
			}
			listsize = new Double(keywordlist.size()).doubleValue();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createQuery(Tuple.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		Join<BookDao, BookDetailDao> bookdetail = bookroot.join("bookdetail");
		Join<BookDetailDao, FoundWordsDao> foundwords = bookdetail
				.join("foundwords");
		Expression sumExpression = cb.sum(foundwords
				.<Number> get("countintext"));
		Expression countExpression = cb.count(foundwords.<Number> get("word"));
		Expression factorExp = cb.quot(countExpression, listsize);
		Expression pertExp = cb.prod(factorExp, sumExpression);
		c.multiselect(bookroot, bookdetail, sumExpression, countExpression,
				pertExp).groupBy(bookroot, bookdetail);

		if (criteria != null) {

			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,
					cb, bookroot, bookdetail, sortexpr);

			// add keyword(s) to where clause
			if (keywordlist != null) {
				Expression<String> exp = foundwords.get("word");
				Predicate predicate = exp.in(keywordlist);
				whereclause.add(predicate);
			} else {
				ParameterExpression<String> param = cb.parameter(String.class,
						"keyword");
				whereclause.add(cb.like(
						cb.lower(foundwords.<String> get("word")), param));
			}

			// adding the whereclause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

			// adding orderby
			long orderdir = criteria.getOrderbydir();
			List<Expression> orderlist = new ArrayList<Expression>();
			if (sortexpr == null) {
				if (criteria.getOrderby() == BookSearchCriteria.OrderBy.AUTHOR) {
					Join<BookDao, ArtistDao> authorjoin = bookroot
							.join("authors");
					sortexpr = authorjoin.get("lastname");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.DATEADDED) {
					sortexpr = bookroot.get("createdon");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.TITLE) {
					sortexpr = bookdetail.get("title");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.SHELFCLASS) {
					sortexpr = bookroot.get("shelfclass");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.BOOKID) {
					orderlist.add(bookroot.get("clientbookidsort"));
					sortexpr = bookroot.get("clientbookid");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.BOOKTYPE) {
					sortexpr = bookroot.get("type");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.STATUS) {
					sortexpr = bookroot.get("status");
				} else if (criteria.getOrderby() == BookSearchCriteria.OrderBy.DETAILSTATUS) {
					sortexpr = bookroot.get("detailstatus");
				} else {
					// default
					sortexpr = bookdetail.get("title");
				}
			}
			orderlist.add(sortexpr);
			if (orderdir == BookSearchCriteria.OrderByDir.ASC) {
				for (Expression order : orderlist) {
					c.orderBy(cb.asc(order));
				}
			} else {
				for (Expression order : orderlist) {
					c.orderBy(cb.desc(order));
				}
			}

			// creating the query
			// creating the query
			int firstresult =  0;
			int maxresult = 0;

			if (pager!=null) {
				if (pager.getResultcount()>0) {
					firstresult = pager.getFirstResult(); 	
				} 
				maxresult=pager.getMaxResults() ;
			}
			TypedQuery<Tuple> q = entityManager.createQuery(c)
					.setFirstResult(firstresult).setMaxResults(maxresult);

			// setting the parameters
			setParametersInQuery(criteria, q, clientid);

			// add keyword(s) parameters
			if (keywordlist == null) {
				q.setParameter("keyword", "%" + keywords + "%");
			}

			List<Tuple> results = q.getResultList();
			List<BookDao> toreturn = new ArrayList<BookDao>();

			for (Tuple t : results) {
				BookDao book = (BookDao) t.get(0);
				toreturn.add(book);
			}

			return toreturn;

		}

		return null;
	}

	private void setParametersInQuery(BookSearchCriteria criteria,
			TypedQuery q, Long clientid) {
		// always add clientid
		q.setParameter("clientid", clientid);

		// title
		if (criteria.hasTitle()) {
			q.setParameter("title", "%"
					+ criteria.getTitle().toLowerCase().trim() + "%");
		}
		// clientbookid
		if (criteria.hasClientbookid()) {
			q.setParameter("clientbookid", criteria.getClientbookid()
					.toLowerCase().trim());
		}

		// isbn10
		if (criteria.getIsbn10() != null) {
			q.setParameter("isbn10", criteria.getIsbn10().trim());
		}
		// isbn13
		if (criteria.getIsbn13() != null) {
			q.setParameter("isbn13", criteria.getIsbn13().trim());
		}

		// clientspecific
		if (criteria.hasClientspecific()) {
			q.setParameter("clientspecific", criteria.getClientspecific());
		}
		// shelf class
		if (criteria.hasShelfclasskey()) {
			q.setParameter("shelfcode", criteria.getShelfclasskey());
		}
		// status
		if (criteria.hasSingleStatus()) {
			q.setParameter("status", criteria.getSingleStatus());
		}
		// status
		if (criteria.hasBooktype()) {
			q.setParameter("type", criteria.getBooktype());
		}
		// detail status
		if (criteria.hasDetailstatus()) {
			q.setParameter("detailstatus", criteria.getDetailstatus());
		}
		// author
		if (criteria.hasAuthor()) {
			ArtistDao tomatch = bMemberService.textToArtistName(criteria
					.getAuthor());
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
		// publisher
		if (criteria.hasPublisher()) {
			q.setParameter("publisher", "%"
					+ criteria.getPublisher().toLowerCase().trim() + "%");
		}

	}

	private List<Predicate> getPredicatesForCriteria(
			BookSearchCriteria criteria, CriteriaBuilder cb,
			Root<BookDao> bookroot, Join<BookDao, BookDetailDao> bookdetail,
			Expression sortexpr) {
		// put together where clause
		List<Predicate> whereclause = new ArrayList<Predicate>();

		// making space for parameters
		// always add client id
		ParameterExpression<Long> clientparam = cb.parameter(Long.class,
				"clientid");
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"), clientparam));

		// title
		if (criteria.hasTitle()) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"title");
			whereclause.add(cb.like(cb.lower(bookdetail.<String> get("title")),
					param));

		}
		if (criteria.hasClientbookid()) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"clientbookid");
			whereclause.add(cb.equal(
					cb.lower(bookroot.<String> get("clientbookid")), param));

		}
		// isbn10
		if (criteria.getIsbn10() != null) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"isbn10");
			whereclause.add(cb.equal(
					cb.lower(bookdetail.<String> get("isbn10")), param));

		}
		// isbn13
		if (criteria.getIsbn13() != null) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"isbn13");
			whereclause.add(cb.equal(
					cb.lower(bookdetail.<String> get("isbn13")), param));

		}
		// clientspecific
		if (criteria.hasClientspecific()) {
			ParameterExpression<Boolean> param = cb.parameter(Boolean.class,
					"clientspecific");
			whereclause.add(cb.equal(
					bookdetail.<Boolean> get("clientspecific"), param));
		}
		// shelfclass
		if (criteria.hasShelfclasskey()) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"shelfcode");
			whereclause.add(cb.equal(bookroot.<Long> get("clientshelfcode"),
					param));

		}

		// status
		if (criteria.hasSingleStatus()) {
			ParameterExpression<Long> param = cb
					.parameter(Long.class, "status");
			whereclause.add(cb.equal(bookroot.<Long> get("status"), param));

		}
		
		// statuslist
		if (criteria.hasStatusList()) {
			if (criteria.getInstatuslist()!=null && criteria.getInstatuslist().booleanValue()) {
				whereclause.add(bookroot.<Long> get("status").in(criteria.getStatuslist()));	
			} else {
				whereclause.add(cb.not(bookroot.<Long> get("status").in(criteria.getStatuslist())));
			}
			
		}		

		// type
		if (criteria.hasBooktype()) {
			Coalesce<Long> cexp = cb.coalesce();
			cexp.value(bookroot.<Long> get("clientbooktype"));
			cexp.value(bookdetail.<Long> get("listedtype"));

			ParameterExpression<Long> param = cb.parameter(Long.class, "type");
			whereclause.add(cb.equal(cexp, param));

		}

		// detail status
		if (criteria.hasDetailstatus()) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"detailstatus");
			whereclause
					.add(cb.equal(bookroot.<Long> get("detailstatus"), param));

		}

		// author
		if (criteria.hasAuthor()) {
			Join<BookDetailDao, ArtistDao> authorjoin = bookdetail
					.join("authors");
			ArtistDao tomatch = bMemberService.textToArtistName(criteria
					.getAuthor());
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

			// check sort
			if (criteria.getOrderby() == BookSearchCriteria.OrderBy.AUTHOR) {
				sortexpr = authorjoin.get("lastname");
			}
		}
		// publisher
		if (criteria.hasPublisher()) {
			Join<BookDetailDao, PublisherDao> publishjoin = bookdetail
					.join("publisher");

			ParameterExpression<String> param = cb.parameter(String.class,
					"publisher");
			Expression<String> path = publishjoin.get("name");
			Expression<String> lower = cb.lower(path);
			Predicate predicate = cb.like(lower, param);
			whereclause.add(predicate);
		}

		return whereclause;
	}

}
