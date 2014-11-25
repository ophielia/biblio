package meg.biblio.search;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.FoundWordsDao;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {


    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private CatalogService catalogService;

    

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
				Expression<String> lower =cb.lower(path);
				Predicate predicate = cb.equal(lower,tomatch.getLastname().toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("lastname")));
			}
			// middlename
			if (tomatch.hasMiddlename()) {
				Expression<String> path = exp.get("middlename");
				Expression<String> lower =cb.lower(path);
				Predicate predicate = cb.equal(lower,tomatch.getMiddlename().toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("middlename")));
			}
			// firstname
			if (tomatch.hasFirstname()) {
				Expression<String> path = exp.get("firstname");
				Expression<String> lower =cb.lower(path);
				Predicate predicate = cb.equal(lower,tomatch.getFirstname().toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("firstname")));
			}
			
			// creating the query
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			TypedQuery<ArtistDao> q = entityManager.createQuery(c);

			
			List<ArtistDao> results = q.getResultList();
			if (results!=null && results.size()>0) {
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
		c.select(exp.<Long>get("id"));		
		
		if (clientbookid != null) {
			// get where clause
			List<Predicate> whereclause = new ArrayList<Predicate>();
			
			Expression<String> path = exp.get("clientbookid");
			Expression<String> trim =cb.trim(path);
			Predicate predicate = cb.equal(trim,clientbookid.trim());
			whereclause.add(predicate);
			
			// creating the query
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			TypedQuery<Long> q = entityManager.createQuery(c);

			
			List<Long> results = q.getResultList();
			return results;
		}

		return null;

	}
	
	public List<BookDao> findBooksWithoutDetails(int max) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<BookDao> c = cb.createQuery(BookDao.class);
		Root<BookDao> exp = c.from(BookDao.class);
		c.select(exp);		
		
			// get where clause
			List<Predicate> whereclause = new ArrayList<Predicate>();
			
			Expression<Long> path = exp.get("detailstatus");
			Predicate predicate = cb.equal(path,new Long(CatalogService.DetailStatus.NODETAIL));
			whereclause.add(predicate);
			
			// creating the query
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			TypedQuery<BookDao> q = entityManager.createQuery(c).setMaxResults(max);

			
			List<BookDao> results = q.getResultList();
			return results;

	}
	
	
	@Override
	public List<BookDao> findBooksForCriteria(BookSearchCriteria criteria) {

		if (criteria!=null) {
			if (criteria.getKeyword()!=null) {
				return findBooksWithKeyword(criteria);
			}
		}
		return findBooksNoKeyword(criteria);
	}
	
	
	private List<BookDao> findBooksNoKeyword(BookSearchCriteria criteria) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<BookDao> c = cb.createQuery(BookDao.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		c.select(bookroot);

		
		
		/*
		 * 
		 * 		private static final long PERTINANCE=1;
		private static final long DATEADDED=2;
		private static final long TITLE=3;
		private static final long AUTHOR=4;
		private static final long SHELFCLASS=5;
		 */
		
		if (criteria != null) {
			
			
			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,cb,bookroot,sortexpr);
			
			// adding where clause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			
			// adding orderby
			long orderdir=criteria.getOrderbydir();
			if (sortexpr==null) {
				if (criteria.getOrderby()==BookSearchCriteria.OrderBy.AUTHOR) {
					Join<BookDao, ArtistDao> authorjoin = bookroot.join("authors");
					sortexpr=authorjoin.get("lastname");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.DATEADDED) {
					sortexpr=bookroot.get("createdon");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.TITLE) {
					sortexpr=bookroot.get("title");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.SHELFCLASS) {
					sortexpr=bookroot.get("shelfclass");
				} else  {
					// default
					sortexpr=bookroot.get("title");
				}  
			}
			if (orderdir==BookSearchCriteria.OrderByDir.ASC) {
				c.orderBy(cb.asc(sortexpr));
			} else {
				c.orderBy(cb.desc(sortexpr));
			}
			
			// creating the query
			TypedQuery<BookDao> q = entityManager.createQuery(c);

			// setting the parameters
			setParametersInQuery(criteria,q);
			
			return q.getResultList();

		}

		return null;
	}	

	private List<BookDao> findBooksWithKeyword(BookSearchCriteria criteria) {

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
			for (int i=0;i<wordarray.length;i++) {
				keywordlist.add(wordarray[i]);
			}
			listsize = new Double(keywordlist.size()).doubleValue();
		} 
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createQuery(Tuple.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		Join<BookDao,FoundWordsDao> foundwords = bookroot.join("foundwords"); 
		Expression sumExpression = cb.sum(foundwords.<Number>get("countintext"));
		Expression countExpression = cb.count(foundwords.<Number>get("word"));
		Expression factorExp = cb.quot(countExpression,listsize);
		Expression pertExp = cb.prod(factorExp,sumExpression);
		c.multiselect(bookroot, sumExpression,countExpression,pertExp)
		.groupBy(bookroot);
		
		if (criteria != null) {

			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,cb,bookroot,sortexpr);
			
			// add keyword(s) to where clause
			if (keywordlist!=null) {
				Expression<String> exp = foundwords.get("word");
				Predicate predicate = exp.in(keywordlist);
				whereclause.add(predicate);
			}	else {
				ParameterExpression<String> param = cb.parameter(String.class,
						"keyword");
				whereclause
				.add(cb.like(cb.lower(foundwords.<String> get("word")), param));
			}


			// adding the whereclause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			
			// adding orderby
			long orderdir=criteria.getOrderbydir();
			if (sortexpr==null) {
				if (criteria.getOrderby()==BookSearchCriteria.OrderBy.AUTHOR) {
					sortexpr=bookroot.get("authors").get("lastname");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.DATEADDED) {
					sortexpr=bookroot.get("createdon");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.TITLE) {
					sortexpr=bookroot.get("title");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.SHELFCLASS) {
					sortexpr=bookroot.get("shelfclass");
				}else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.PERTINANCE) {
					sortexpr=pertExp;
				} else  {
					// default
					sortexpr=bookroot.get("title");
				}  
			}
			if (orderdir==BookSearchCriteria.OrderByDir.ASC) {
				c.orderBy(cb.asc(sortexpr));
			} else {
				c.orderBy(cb.desc(sortexpr));
			}
			
			// creating the query
			TypedQuery<Tuple> q = entityManager.createQuery(c);

			// setting the parameters
			setParametersInQuery(criteria,q);
			
			// add keyword(s) parameters
			if (keywordlist==null) {
				q.setParameter("keyword", "%"
						+ keywords + "%");
			}			
			
			List<Tuple> results = q.getResultList(); 
			List<BookDao> toreturn = new ArrayList<BookDao>();
			
			for (Tuple t: results) {
				BookDao book = (BookDao) t.get(0);
				toreturn.add(book);
			}
			
			return toreturn;

		}

		return null;
	}

	
	private void setParametersInQuery(BookSearchCriteria criteria, TypedQuery q) {
		// always add clientid
		q.setParameter("clientid", criteria.getClientid());

		// title
		if (criteria.getTitle() != null) {
			q.setParameter("title", "%"
					+ criteria.getTitle().toLowerCase().trim() + "%");
		}
		// shelf class
		if (criteria.getShelfclasskey() != null) {
			q.setParameter("shelfclass", criteria.getShelfclasskey());
		}
		
		// author
		if (criteria.getAuthor() != null) {
			ArtistDao tomatch = catalogService.textToArtistName(criteria
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
		if (criteria.getPublisherentry()!=null) {
			q.setParameter("publisher",  "%"
						+ criteria.getPublisherentry().toLowerCase().trim() + "%");	
		}

	}

	private List<Predicate> getPredicatesForCriteria(
			BookSearchCriteria criteria, CriteriaBuilder cb, Root<BookDao> bookroot, Expression sortexpr) {
		// put together where clause
		List<Predicate> whereclause = new ArrayList<Predicate>();

		// making space for parameters
		// always add client id
		ParameterExpression<Long> clientparam = cb.parameter(Long.class, "clientid");
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"), clientparam));

		// title
		if (criteria.getTitle() != null) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"title");
			whereclause
					.add(cb.like(cb.lower(bookroot.<String> get("title")), param));

		}
		// shelfclass
		if (criteria.getShelfclasskey() != null) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"shelfclass");
			whereclause.add(cb.equal(bookroot.<Long> get("shelfclass"), param));

		}
		
		// author
		if (criteria.getAuthor() != null) {
			Join<BookDao, ArtistDao> authorjoin = bookroot.join("authors");
			ArtistDao tomatch = catalogService.textToArtistName(criteria
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
			if (criteria.getOrderby()==BookSearchCriteria.OrderBy.AUTHOR) {
				sortexpr = authorjoin.get("lastname");
			}
		}
		// publisher
		if (criteria.getPublisherentry() != null) {
			Join<BookDao, PublisherDao> publishjoin = bookroot
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

	private void setOrderBy(BookSearchCriteria criteria, CriteriaBuilder cb,
			CriteriaQuery<BookDao> query, Root<BookDao> root) {
		
/*		List<String> sortstrings=new ArrayList<String>();
		// set sort string
		if (criteria.getSorttype() != null) {
			if (criteria.getSorttype().equals(BookSearchCriteria.SortType.Amount)) {
				sortstrings.add("displayamount");
			} else if (criteria.getSorttype().equals(
					BookSearchCriteria.SortType.Category)) {
				sortstrings.add("catName");
			} else if (criteria.getSorttype().equals(
					BookSearchCriteria.SortType.Date)) {
				sortstrings.add("transdate");
				sortstrings.add("transid");
			} else if (criteria.getSorttype().equals(
					BookSearchCriteria.SortType.Detail)) {
				sortstrings.add("detail");
			}
		} else {
			sortstrings.add("transdate");
			sortstrings.add("transid");
		}
		
		// now, put into list of order objects with direction
		List<Order> orderstatements = new ArrayList<Order>();
		for (String sortparam:sortstrings) {
			if (criteria.getSortdir()!=null && criteria.getSortdir().longValue()==BookSearchCriteria.SortDirection.Asc) {
				 Order neworder = cb.asc(root.get(sortparam));
				 orderstatements.add(neworder);
			} else {
				Order neworder = cb.desc(root.get(sortparam));
				 orderstatements.add(neworder);
			}
		}
		query.orderBy(orderstatements);
		
		*/
	}
	
	
	
	
	
}

