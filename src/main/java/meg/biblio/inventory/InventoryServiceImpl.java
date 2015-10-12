package meg.biblio.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.inventory.db.InventoryHistRepository;
import meg.biblio.inventory.db.InventoryRepository;
import meg.biblio.inventory.db.dao.InvStackDisplay;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.inventory.db.dao.InventoryHistoryDao;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.search.SearchService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

	@Autowired
	SearchService catalogSearch;

	@Autowired
	InventoryRepository invRepo;

	@Autowired
	BookRepository bookRepo;

	@Autowired
	InventoryHistRepository invHistRepo;

	@PersistenceContext
	private EntityManager entityManager;

	/* Get actual class name to be printed on */
	static Logger log = Logger.getLogger(InventoryServiceImpl.class.getName());

	public static final class StackSearchType {
		public static final long UNCOUNTED = 1;
		public static final long STACK = 2;
	}

	/**
	 * Creates a new InventoryDao object for the client, fills it with
	 * information and returns the new client object. Note: only one Inventory
	 * can run at a time for a client. If a client has a current inventory in
	 * progress, null will be returned.
	 *
	 * @param client
	 * @return
	 */
	@Override
	public InventoryDao beginInventory(ClientDao client) {
		// determine if an inventory is currently in progress for the client
		InventoryDao current = getCurrentInventory(client);
		if (current != null) {
			// new inventory cannot be started until the one in progress is
			// finished.
			return null;
		}
		// create new inventory object
		InventoryDao newinv = new InventoryDao();

		// set start date, clientid
		newinv.setStartdate(new Date());
		newinv.setClientid(client.getId());

		// get number of books to be counted (all books for client except those
		// with status lost by borrower, removed from circulation, and not found
		// in inventory.
		List<Long> excludedstatus = new ArrayList<Long>();
		excludedstatus.add(CatalogService.Status.INVNOTFOUND);
		excludedstatus.add(CatalogService.Status.LOSTBYBORROWER);
		excludedstatus.add(CatalogService.Status.REMOVEDFROMCIRC);
		BookSearchCriteria searchcriteria = new BookSearchCriteria();
		searchcriteria.setStatuslist(excludedstatus);
		searchcriteria.setInstatuslist(false);
		Long tobecounted = catalogSearch.getBookCountForCriteria(
				searchcriteria, client.getId());

		// set number of books to be counted in inventory object
		newinv.setTobecounted(tobecounted.intValue());

		// create InventoryDao object by saving
		newinv = invRepo.save(newinv);

		// update all books to be counted - set "tocount" to true, inventoryid
		int updated = prepareInventoryDataInBooks(client.getId());

		// check updated
		if (tobecounted.intValue() != updated) {
			// log error
			log.error("Inventory - books updated doesn't equal preliminary estimate. Books updated:"
					+ updated + ";Estimate:" + tobecounted);
		}

		return newinv;
	}

	/**
	 * This method cancels an inventory in progress. It resets all book counts
	 * to 0, sets the end date in the inventory object, and sets completed in
	 * the inventory object to false.
	 *
	 * @param client
	 *            ClientDao
	 * @return
	 */
	@Override
	public void cancelCurrentInventory(ClientDao client) {
		// get current inventory
		InventoryDao current = getCurrentInventory(client);
		// if current inventory exists, update end date, and completed.
		if (current != null) {
			// get InventoryStatus for inventory, set data in inventory
			InventoryStatus istatus = getInventoryStatus(current, client);

			current.setEnddate(new Date());
			current.setCompleted(false);
			Long countedlong = new Long(istatus.getCountedbooks());
			current.setTotalcounted(new Integer(countedlong.intValue()));
			// persist inventoryDao
			invRepo.save(current);
			// set all bookcounts (tocount in BookDao) to false
			clearInventoryDataInBooks(client.getId());
		}
	}

	/**
	 * Finishes an Inventory for a client. This method tallies up the final
	 * counts ( number counted, number added, number reconciled) for the
	 * inventory, and fills these in in the InventoryDao object. Finally, all
	 * inventory data - counted to count, counted date, counterid, and
	 * reconciled are cleared from the BookDaos.
	 *
	 * @param client
	 *            ClientDao
	 * @return
	 */
	@Override
	public InventoryDao finishInventory(ClientDao client) {
		// get current inventory
		InventoryDao current = getCurrentInventory(client);
		// if no inventory is current, return null
		if (current != null) {
			if (getInventoryIsComplete(client)) {
				// get InventoryStatus for inventory
				InventoryStatus status = getInventoryStatus(current, client);
				// fill in status info in InventoryDao
				// number counted, number added, number reconciled
				Long countedlong = new Long(status.getCountedbooks());
				Long addedlong = new Long(status.getRefoundbooks());
				Long reconciledlong = new Long(status.getReconciledbooks());
				current.setTotalcounted(countedlong.intValue());
				current.setAddedtocount(addedlong.intValue());
				current.setReconciled(reconciledlong.intValue());
				// fill in enddate, and completed as true
				current.setEnddate(new Date());
				current.setCompleted(true);
				// save InventoryDao
				invRepo.save(current);
				// reset inventory information in BookDao.
				clearInventoryDataInBooks(client.getId());
				// return finished inventory
				return current;
			}
		}
		return null;
	}

	/**
	 * Returns the InventoryDao object for the inventory in progress for the
	 * given client. If nothing is in progress, 0 is returned. An InventoryDao
	 * object is in progress when the enddate is null.
	 *
	 * @param client
	 *            ClientDao
	 * @return
	 */
	@Override
	public InventoryDao getCurrentInventory(ClientDao client) {
		// retrieve InventoryDao for client with enddate as null.
		InventoryDao current = invRepo.getCurrentInventoryForClient(client
				.getId());
		return current;
	}

	@Override
	public InventoryDao getInventoryById(Long invid) {
		if (invid != null) {
			return invRepo.findOne(invid);
		}
		return null;
	}

	/**
	 * This method returns an InventoryStatus object - a pojo object which
	 * contains information about the status of the given inventory.
	 *
	 * @param inv
	 *            InventoryDao
	 * @param client
	 *            ClientDao
	 * @return inventorystatus InventoryStatus
	 */
	@Override
	public InventoryStatus getInventoryStatus(InventoryDao inv, ClientDao client) {
		if (inv != null) {
			// determine if this is a current inventory
			boolean iscurrent = false;
			InventoryDao current = getCurrentInventory(client);
			if (current != null
					&& current.getId().longValue() == inv.getId().longValue()) {
				iscurrent = true;
			}

			// make new InventoryStatus object
			InventoryStatus invstatus = new InventoryStatus(inv);

			if (iscurrent) {
				// get breakout by countstatus
				HashMap<Long, Long> counts = catalogSearch
						.breakoutByBookField(
								SearchService.Breakoutfield.COUNTSTATUS,
								client.getId());

				// add counts
				if (counts.containsKey(InventoryService.CountStatus.COUNTED)) {
					invstatus.setCountedBooks(counts
							.get(InventoryService.CountStatus.COUNTED));
				}
				if (counts.containsKey(InventoryService.CountStatus.RECONCILED)) {
					invstatus.setReconciledBooks(counts
							.get(InventoryService.CountStatus.RECONCILED));
				}
				// get count of "refound" books from InventoryHistory
				List<InventoryHistoryDao> refound = invHistRepo
						.getRefoundBooksForInventory(inv);

				if (refound != null) {
					invstatus.setRefoundBooks(refound.size());
				}

			} else {
				// get counted from inventory object
				if (inv.getTotalcounted() != null) {
					invstatus.setCountedBooks(new Long(inv.getTotalcounted()));
				}

				// get reconciled from inventory object
				if (inv.getReconciled() != null) {
					invstatus.setReconciledBooks(new Long(inv.getReconciled()));
				}

				// get added from inventory object
				if (inv.getAddedtocount() != null) {
					invstatus.setRefoundBooks(inv.getAddedtocount());
				}
			}

			// return inventory status
			return invstatus;
		}
		return null;
	}

	@Override
	public List<InventoryDao> getPreviousInventories(ClientDao client) {
		if (client != null) {
			List<InventoryDao> previous = invRepo
					.getPreviousInventoriesForClient(client.getId(), new Sort(
							Sort.Direction.DESC, "id"));
			return previous;
		}
		return null;
	}

	/**
	 * An inventory is deemed complete when all books marked as tocount have
	 * been either counted or reconciled.
	 *
	 * @param inv
	 * @return
	 */
	@Override
	public boolean getInventoryIsComplete(ClientDao client) {
		InventoryDao inv = getCurrentInventory(client);
		if (inv != null) {
			List<InvStackDisplay> uncountedbooks = getUncountedBooks(client);
			return !(uncountedbooks != null && uncountedbooks.size() > 0);
		}
		return false;
	}

	/**
	 * Returns a list of all inventories saved for the client.
	 *
	 * @param client
	 * @return
	 */
	@Override
	public List<InventoryDao> getInventoryList(ClientDao client) {
		// retrieve InventoryDao list for client, ordered by startdate ,
		// enddate descending.
		List<InventoryDao> list = invRepo.getInventoryListForClient(
				client.getId(), new Sort(Sort.Direction.DESC, "startdate"));

		return list;
	}

	@Override
	public List<InventoryHistoryDao> getDetailForInventory(
			InventoryDao inventory, long detailtype) {
		if (inventory != null) {
			if (detailtype == InventoryService.HistoryType.ADDED) {
				return invHistRepo.getRefoundBooksForInventory(inventory);
			} else if (detailtype == InventoryService.HistoryType.RECONCILED) {
				return invHistRepo.getReconciledBooksForInventory(inventory);
			}
		}
		return null;
	}

	/** Counting Operations **/

	/**
	 * During an inventory, books are counted in a stack, which belongs to a
	 * user. This method returns all books belonging to the current stack - in
	 * other words, all books counted with the passed userid in the counterid
	 * slot.
	 *
	 * @param userid
	 * @param client
	 * @return
	 */
	@Override
	public List<InvStackDisplay> getStackForUser(Long userid, ClientDao client) {
		// retrieve all InvStackDisplay objects with the counterid matching the
		// passed userid, for the given client.
		return retrieveInvStackDisplays(StackSearchType.STACK, userid,
				client.getId());
	}

	/**
	 * This method clears the stack for a user, so that another stack can be
	 * counted and verified. All books with the counterid matching the passed
	 * userid have the counterid updated to 0.
	 *
	 * @param userid
	 * @param client
	 */
	@Override
	public void clearStackForUser(Long userid, ClientDao client) {
		// run update for all BookDao objects matching userid and client.
		// set userid to 0.
		// query for all books belonging to client
		// with tocount as true
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<BookDao> q = cb.createCriteriaUpdate(BookDao.class);
		Root<BookDao> bookroot = q.from(BookDao.class);

		// create predicate list
		List<Predicate> whereclause = new ArrayList<Predicate>();
		// add clientid
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"),
				client.getId()));
		whereclause.add(cb.equal(bookroot.<Long> get("userid"), userid));

		// put query together
		Expression<Long> nullLong = cb.nullLiteral(Long.class);
		q.set(bookroot.get("userid"), nullLong);

		q.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
		int result = entityManager.createQuery(q).executeUpdate();
		log.info("clearStackForUser: updated " + result + " books.");

		entityManager.flush();
	}

	/**
	 * This is the method in which books are actually counted. A book is counted
	 * by setting the counted flag to true. If the book has been previously
	 * "lost" - meaning it has a status of not counted in inventory or lost by
	 * borrower, than it is also saved as an InvHistoryDao object.
	 *
	 * @param book
	 * @param userid
	 */
	@Override
	public void countBook(BookDao book, Long userid, ClientDao client,
			Boolean saveinstack) {
		InventoryDao inv = getCurrentInventory(client);
		if (inv != null) {
			if (book != null) {
				// retrieve book
				BookDao countedbook = bookRepo.findOne(book.getId());
				// determine if this is a book to be counted.
				// If so, count and put in stack; if not, add to invhist and put
				// in stack
				if (countedbook.getTocount() != null
						&& countedbook.getTocount()) {
					// set counterid to userid, and counted to true
					// update counted status for book
					countedbook
							.setCountstatus(InventoryService.CountStatus.COUNTED);
					// set userid for book
					if (saveinstack) {
						countedbook.setUserid(userid);
					}
					countedbook.setCounteddate(new Date());
					// save book
					bookRepo.save(countedbook);
					bookRepo.flush();
					entityManager.refresh(countedbook);

				} else {
					// this book was "lost" at the time the inventory was
					// started
					// first, look for a record of the book in invhist
					Long newstatus = CatalogService.Status.SHELVED;
					Long bookstatus = countedbook.getStatus();
					List<InventoryHistoryDao> wasfound = invHistRepo
							.getFoundInInventory(inv, countedbook);
					// if doesn't exist in invhist, create a record there
					if (wasfound == null || wasfound.size() == 0) {

						// if lost book, save as InvHistory object
						InventoryHistoryDao hist = new InventoryHistoryDao();
						hist.setInventory(inv);
						hist.setOriginalstatus(bookstatus);
						hist.setNewstatus(newstatus);
						hist.setFoundbook(true);
						hist.setBook(countedbook);
						hist = invHistRepo.save(hist);
					}

					// now, count the book
					// set userid for book
					if (saveinstack) {
						countedbook.setUserid(userid);
					}
					// set counteddate and countstatus
					countedbook.setStatus(newstatus);
					countedbook
							.setCountstatus(InventoryService.CountStatus.COUNTED);
					countedbook.setCounteddate(new Date());
					// save changes
					countedbook = bookRepo.save(countedbook);
					entityManager.flush();
					entityManager.refresh(countedbook);

				}
			}
		}
	}

	/** Reconciling Operations **/

	/**
	 * Returns a list of InvStackDisplay objects for the inventory which have
	 * not been counted: all BookDao objects with tobecounted true, and
	 * countstatus not in counted (counted, reconciled)
	 *
	 * @param client
	 * @return
	 */
	@Override
	public List<InvStackDisplay> getUncountedBooks(ClientDao client) {

		return retrieveInvStackDisplays(StackSearchType.UNCOUNTED, null,
				client.getId());

	}

	/**
	 * Reconciling a book means marking an uncounted book with an "unfound"
	 * status: either not found in inventory, or lost by borrower. A reconciled
	 * book is marked as reconciled (BookDao reconciled field set to true) and a
	 * record of the reconciled book, with the original status and the final
	 * status is saved in InvHistory.
	 *
	 * @param client
	 * @param bookid
	 * @param updatestatus
	 */
	@Override
	public void reconcileBook(ClientDao client, Long bookid, Long updatestatus,
			String note) {
		// get inventory
		InventoryDao inv = getCurrentInventory(client);

		// only proceed if inventory in progress
		if (inv != null) {
			BookDao book = bookRepo.findOne(bookid);
			reconcileBook(inv, book, updatestatus, note);
		}

	}

	/**
	 * This method performs a reconcileBook operation for an entire book list.
	 *
	 * @param client
	 * @param bookid
	 * @param updatestatus
	 */
	@Override
	public void reconcileBookList(ClientDao client, List<Long> bookidlist,
			Long updatestatus) {
		// get inventory
		InventoryDao inv = getCurrentInventory(client);

		// only proceed if inventory in progress
		if (inv != null) {
			List<BookDao> toreconcile = bookRepo.findAll(bookidlist);
			for (BookDao book : toreconcile) {
				reconcileBook(inv, book, updatestatus, null);
			}
		}

	}

	/** Private Internal Methods **/
	/**
	 * Where the real work is done to reconcile a book. This updates the status
	 * of the book to the passed updatestatus, sets the countstatus to
	 * reconciled, and creates an invhistorydao object for the book.
	 *
	 * @param invinprogress
	 * @param book
	 * @param updatestatus
	 */
	private void reconcileBook(InventoryDao invinprogress, BookDao book,
			Long updatestatus, String note) {
		// create InventoryHistDao
		InventoryHistoryDao invHist = new InventoryHistoryDao();

		// get book
		if (book != null) {
			// set inventory in InventoryHistDao
			invHist.setInventory(invinprogress);
			// set original status, and new status in IHD
			invHist.setOriginalstatus(book.getStatus());
			invHist.setNewstatus(updatestatus);
			invHist.setFoundbook(false);
			// set new status in book
			book.setStatus(updatestatus);

			// if note isn't null, set in book
			if (note != null) {
				book.setNote(note);
			}

			// set countstatus
			book.setCountstatus(InventoryService.CountStatus.RECONCILED);
			book.setCounteddate(new Date());
			// persist the book
			book = bookRepo.save(book);
			// set the book in the IHD and persist
			entityManager.flush();
			entityManager.refresh(book);
			invHist.setBook(book);
			invHistRepo.save(invHist);
		}
	}

	/**
	 * Retrieves a list of InvStackDisplays. Type of search depends upon an
	 * internalsearchclass parameter passed with the displays, which is either
	 * uncounted, reconciled, or stack.
	 *
	 * @return
	 */
	private List<InvStackDisplay> retrieveInvStackDisplays(long searchtype,
			Long userid, Long clientid) {
		// prepare query
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<InvStackDisplay> c = cb
				.createQuery(InvStackDisplay.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		Join<BookDao, BookDetailDao> bookdetail = bookroot.join("bookdetail");

		c.select(cb.construct(InvStackDisplay.class,
				bookroot.get("id").alias("bookid"), bookroot.get("clientid"),
				bookroot.get("clientbookid").alias("clientbooknr"),
				bookroot.get("clientshelfcode"),
				bookroot.get("clientshelfclass"), bookroot.get("status"),
				bookroot.get("note"), bookroot.get("counteddate"),
				bookroot.get("tocount"), bookroot.get("userid"),
				bookroot.get("countstatus"), bookdetail.get("title")));

		// create predicate list
		List<Predicate> whereclause = new ArrayList<Predicate>();
		// add clientid
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"), clientid));
		if (searchtype == StackSearchType.STACK) {
			whereclause.add(cb.equal(bookroot.<Long> get("userid"), userid));
		} else if (searchtype == StackSearchType.UNCOUNTED) {
			Expression<Long> nullLong = cb.nullLiteral(Long.class);
			List<Long> excludedstatus = new ArrayList<Long>();
			excludedstatus.add(InventoryService.CountStatus.COUNTED);
			excludedstatus.add(InventoryService.CountStatus.RECONCILED);

			whereclause.add(cb.isNull(bookroot.<Long> get("countstatus")));
			whereclause.add(cb.equal(bookroot.<Boolean> get("tocount"), true));
		}
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		TypedQuery<InvStackDisplay> q = entityManager.createQuery(c);
		return q.getResultList();
	}

	/**
	 * This method prepares the BookDao objects for the inventory by setting the
	 * tobecounted flag for all eligible books.
	 *
	 * @param clientid
	 * @return
	 */
	private int prepareInventoryDataInBooks(Long clientid) {
		List<Long> excludedstatus = new ArrayList<Long>();
		excludedstatus.add(CatalogService.Status.INVNOTFOUND);
		excludedstatus.add(CatalogService.Status.LOSTBYBORROWER);
		excludedstatus.add(CatalogService.Status.REMOVEDFROMCIRC);

		// query for all books belonging to client
		// with status not in the excluded status list
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<BookDao> q = cb.createCriteriaUpdate(BookDao.class);
		Root<BookDao> bookroot = q.from(BookDao.class);

		// create predicate list
		List<Predicate> whereclause = new ArrayList<Predicate>();
		// add clientid
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"), clientid));
		// exclude status
		whereclause.add(cb
				.not(bookroot.<Long> get("status").in(excludedstatus)));

		// put query together
		q.set(bookroot.get("tocount"), true).where(
				cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
		int result = entityManager.createQuery(q).executeUpdate();
		log.info("prepareInventoryDataInBooks: updated " + result + " books.");

		// flush and clear
		entityManager.flush();
		entityManager.clear();

		// return update count
		return result;
	}

	/**
	 * This method clears all inventory data (tocount, counted, counteddate,
	 * userid and reconciled) from the BookDao for the given client.
	 *
	 * @param clientid
	 * @return
	 */
	private int clearInventoryDataInBooks(Long clientid) {
		// query for all books belonging to client
		// with tocount as true
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaUpdate<BookDao> q = cb.createCriteriaUpdate(BookDao.class);
		Root<BookDao> bookroot = q.from(BookDao.class);

		// create predicate list
		List<Predicate> whereclause = new ArrayList<Predicate>();
		// add clientid
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"), clientid));

		// put query together
		Expression<Date> nullDate = cb.nullLiteral(Date.class);
		Expression<Long> nullLong = cb.nullLiteral(Long.class);
		q.set(bookroot.get("tocount"), false);
		q.set(bookroot.get("countstatus"), nullLong);
		q.set(bookroot.get("counteddate"), nullDate);
		q.set(bookroot.get("userid"), nullLong);

		q.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
		int result = entityManager.createQuery(q).executeUpdate();
		log.info("clearInventoryDataInBooks: updated " + result + " books.");

		// flush and clear
		entityManager.flush();
		entityManager.clear();

		// return update count
		return result;

	}
}
