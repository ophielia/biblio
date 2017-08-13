package meg.biblio.catalog;

import meg.biblio.catalog.db.BookDetailRepository;
import meg.biblio.catalog.db.BookRepository;
import meg.biblio.catalog.db.FoundDetailsRepository;
import meg.biblio.catalog.db.dao.*;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
@EnableScheduling
public class DetailSearchServiceImpl implements DetailSearchService {

    @Autowired
    AppSettingService settingService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    CatalogService catalogService;

    @Autowired
    SearchService searchService;

    @Autowired
    FoundDetailsRepository foundRepo;

    @Autowired
    ClientService clientService;

    @Autowired
    BookRepository bookRepo;

    @Autowired
    BookDetailRepository bookDetailRepo;

    @Autowired
    GeneralClassifier generalClassifier;

    @Autowired
    GoogleDetailFinder googleFinder;

    @Autowired
    InternalDetailFinder internalFinder;

    @Autowired
    AmazonDetailFinder amazonFinder;

    @Autowired
    BNFCatalogFinder bnfFinder;

    /* Get actual class name to be printed on */
    static Logger log = Logger.getLogger(DetailSearchServiceImpl.class
            .getName());

    public List<FoundDetailsDao> getFoundDetailsForBook(Long id) {
        if (id != null) {
            BookDao book = bookRepo.findOne(id);
            if (book != null) {
                Long detailid = book.getBookdetail().getId();
                // query db for founddetails
                List<FoundDetailsDao> details = foundRepo
                        .findDetailsForBook(detailid);
                // return founddetails
                return details;
            }
        }
        return null;
    }


    @Override
    public BookModel fillInDetailsForBook(BookModel model, ClientDao client) {
        // get ready for search - determine is search is made with isbn,
        // complete for client
        BookDetailDao detail = model.getBook().getBookdetail();

        long clientcomplete = client.getDetailCompleteCode();

        // prepare FinderObject
        FinderObject findobj = new FinderObject(detail, client);

        // get finderchain
        DetailFinder finderchain = createFinderChain();

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
        detail.setFinderlog(findobj.getCurrentFinderLog());
        model.setBookdetail(detail);
        if (detail.getDetailstatus() == CatalogService.DetailStatus.DETAILFOUND) {
            // check for any remaining founddetail objects, and delete
            if (detail.getId() != null) {
                List<FoundDetailsDao> todelete = getFoundDetailsForBook(model
                        .getBookid());
                if (todelete != null && todelete.size() > 0) {
                    foundRepo.delete(todelete);
                }
            }
            try {
                classifyBook(client.getId(), model.getBook());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // check for addl codes
            checkAndSaveAdditionalCodes(findobj);
            // check for image
            String imagelink = findobj.getBookdetail().getImagelink();
            if (imagelink != null) {
                // we have an image. Now, this book may have been
                // saved already with a different isbn without an
                // image. Here, we're going to check for books with matching
                // titles, and author/publisher without images. If any are
                // found, the image will be set in these objects
                List<BookDetailDao> matcheswoimage = findMatchingBooksWithoutImages(findobj
                        .getBookdetail());
                if (matcheswoimage != null) {
                    for (BookDetailDao match : matcheswoimage) {
                        match.setImagelink(imagelink);
                        bookDetailRepo.save(match);
                    }
                }
            }
            // copy (overwrite) classification info with existing book (if any)
            if (model.hasIsbn()) {
                BookSearchCriteria criteria = new BookSearchCriteria();
                String isbn = model.getIsbn13();
                if (isbn == null) {
                    isbn = model.getIsbn10();
                    criteria.setIsbn10(isbn);
                } else {
                    criteria.setIsbn13(isbn);
                }
                List<BookDao> found = searchService.findBooksForCriteria(
                        criteria, null, client.getId());
                if (found != null && found.size() > 0) {
                    // found a book with the same isbn belonging to this client
                    // copy the classification from found book to new book
                    BookDao copyfrom = found.get(0);
                    model.setType(copyfrom.getClientbooktype());
                    model.setShelfcode(copyfrom.getClientshelfcode());
                    model.setShelfclass(copyfrom.getClientshelfclass());
                }
            }
        } else if (detail.getDetailstatus() == CatalogService.DetailStatus.MULTIDETAILSFOUND) {
            // put found details into bookmodel
            List<FoundDetailsDao> founddetails = findobj.getMultiresults();
            model.setFounddetails(founddetails);
        }

        // return book model
        return model;
    }

    private List<BookDetailDao> findMatchingBooksWithoutImages(
            BookDetailDao detail) {
        // gather params
        String title = detail.getTitle().toLowerCase().trim();
        List<ArtistDao> authors = detail.getAuthors();
        ArtistDao tomatch = authors != null && authors.size() > 0 ? authors
                .get(0) : null;
        String author = tomatch != null ? tomatch.getDisplayName().toLowerCase().trim() : null;
        PublisherDao tomatchpub = detail.getPublisher();
        String publisher = tomatchpub != null ? tomatchpub.getName().trim() : null;

        // only continue if title, and at least one of author or publisher exist
        if (title != null && (author != null || publisher != null)) {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<BookDetailDao> c = cb.createQuery(BookDetailDao.class);
            Root<BookDetailDao> bookroot = c.from(BookDetailDao.class);
            c.select(bookroot);

            // get where clause
            List<Predicate> whereclause = new ArrayList<Predicate>();
            // imagelink null
            Expression<String> image = bookroot.get("imagelink");
            Predicate pisnull = cb.isNull(image);
            Predicate pisempty = cb.equal(image, "");
            Predicate imagetest = cb.or(pisnull, pisempty);
            whereclause.add(imagetest);

            // title
            if (title != null) {
                ParameterExpression<String> param = cb.parameter(String.class,
                        "title");
                whereclause.add(cb.equal(cb.lower(cb.trim(bookroot.<String>get("title"))),
                        param));
            }

            // author
            if (author != null) {
                Join<BookDetailDao, ArtistDao> authorjoin = bookroot
                        .join("authors");
                // where firstname = firstname and middlename = middlename and
                // lastname = lastname
                // together with likes and to lower
                // lastname
                if (tomatch.hasLastname()) {
                    ParameterExpression<String> param = cb.parameter(String.class,
                            "alastname");
                    Expression<String> path = authorjoin.get("lastname");
                    Expression<String> lower = cb.lower(path);
                    Predicate predicate = cb.equal(lower, param);
                    whereclause.add(predicate);
                }
                // middlename
                if (tomatch.hasMiddlename()) {
                    ParameterExpression<String> param = cb.parameter(String.class,
                            "amiddlename");
                    Expression<String> path = authorjoin.get("middlename");
                    Expression<String> lower = cb.lower(path);
                    Predicate predicate = cb.equal(lower, param);
                    whereclause.add(predicate);
                }
                // firstname
                if (tomatch.hasFirstname()) {
                    ParameterExpression<String> param = cb.parameter(String.class,
                            "afirstname");
                    Expression<String> path = authorjoin.get("firstname");
                    Expression<String> lower = cb.lower(path);
                    Predicate predicate = cb.equal(lower, param);
                    whereclause.add(predicate);
                }
            } else if (publisher != null) {
                Join<BookDetailDao, PublisherDao> publishjoin = bookroot
                        .join("publisher");

                ParameterExpression<String> param = cb.parameter(String.class,
                        "publisher");
                Expression<String> path = publishjoin.get("name");
                Expression<String> lower = cb.lower(cb.trim(path));
                Predicate predicate = cb.equal(lower, param);
                whereclause.add(predicate);
            }

            // adding where clause
            c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

            // creating the query
            TypedQuery<BookDetailDao> q = entityManager.createQuery(c);

            // setting the parameters
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
                    q.setParameter("alastname", tomatch.getLastname().toLowerCase().trim());
                }
                // middlename
                if (tomatch.hasMiddlename()) {
                    q.setParameter("amiddlename", tomatch.getMiddlename().toLowerCase().trim());
                }
                // firstname
                if (tomatch.hasFirstname()) {
                    q.setParameter("afirstname", tomatch.getFirstname().toLowerCase().trim());

                }
            } else if (publisher != null) {
                q.setParameter("publisher", publisher.toLowerCase().trim());
            }

            List<BookDetailDao> results = q.getResultList();
            return results;
        }
        return null;
    }

    private void checkAndSaveAdditionalCodes(FinderObject findobj) {
        BookDetailDao detail = findobj.getBookdetail();
        // check for additional details
        if (findobj.getAddlcodes() != null && !findobj.getAddlcodes().isEmpty()) {
            // go ahead and save these other codes as additional book details,
            // (even though it could be that the original book isn't saved - we
            // have
            // more info for later on....)
            for (BookIdentifier bi : findobj.getAddlcodes()) {
                BookDetailDao newdetail = searchService
                        .findBooksForIdentifier(bi);
                if (newdetail != null)
                    continue;
                newdetail = new BookDetailDao();

                newdetail.copyFrom(detail);
                if (bi.getEan() != null) {
                    newdetail.setIsbn13(bi.getEan());
                }
                if (bi.getIsbn() != null) {
                    newdetail.setIsbn10(bi.getIsbn());
                }
                if (bi.getPublishyear() != null) {
                    newdetail.setPublishyear(bi.getPublishyear());
                }
                catalogService.saveBookDetail(newdetail);
            }
        }

    }

    @Override
    public List<BookModel> fillInDetailsForBookList(List<BookModel> models,
                                                    ClientDao client) {
        if (models != null) {
            // get ready for search - clientcompletecode
            long clientcomplete = client.getDetailCompleteCode();
            Integer batchsearchmax = settingService
                    .getSettingAsInteger("biblio.google.batchsearchmax");

            // get finderchain
            DetailFinder finderchain = createOnlineFinderChain();

            // make list of finderobjects (using batch maximum)
            HashMap<Long, BookModel> puzzlehash = new HashMap<Long, BookModel>();
            int maximum = batchsearchmax > models.size() ? models.size()
                    : batchsearchmax;
            List<FinderObject> forsearch = new ArrayList<FinderObject>();
            long i = 1;
            for (BookModel model : models) {
                if (model != null && model.getBook() != null) {
                    BookDetailDao bd = model.getBook().getBookdetail();
                    if (bd.getFinderlog() != null && bd.getFinderlog() > 1) {
                        continue;
                    }
                    FinderObject obj = new FinderObject(bd, client);
                    i++;
                    obj.setTempIdent(new Long(i));
                    forsearch.add(obj);
                    puzzlehash.put(new Long(i), model);
                    if (i > maximum) {
                        break;
                    }
                }

            }

            // run chain
            try {
                forsearch = finderchain.findDetailsForList(forsearch,
                        clientcomplete, batchsearchmax);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            List<BookModel> toreturn = new ArrayList<BookModel>();
            for (FinderObject findobj : forsearch) {
                // make finderobject
                Long ident = findobj.getTempIdent();
                BookModel bmodel = puzzlehash.get(ident);
                BookDetailDao detail = findobj.getBookdetail();
                // set detailsearchstatus in bookdetail
                detail.setDetailstatus(findobj.getSearchStatus());
                detail.setFinderlog(findobj.getCurrentFinderLog());
                bmodel.setBookdetail(detail);
                // put results of finder object in model
                toreturn.add(bmodel);
                // check for addlcodes
                checkAndSaveAdditionalCodes(findobj);
            }

            return toreturn;
        }
        return null;
    }

    @Override
    public List<BookModel> doOfflineSearchForBookList(List<BookModel> models,
                                                      ClientDao client) {
        if (models != null) {
            // get ready for search - clientcompletecode
            long clientcomplete = client.getDetailCompleteCode();
            Integer batchsearchmax = 99999;

            // get finderchain
            DetailFinder offlinefinderchain = createOfflineFinderChain();

            // make list of finderobjects (using batch maximum)
            HashMap<Long, BookModel> puzzlehash = new HashMap<Long, BookModel>();
            List<FinderObject> forsearch = new ArrayList<FinderObject>();
            long i = 1;
            for (BookModel model : models) {
                if (model != null && model.getBook() != null) {
                    BookDetailDao bd = model.getBook().getBookdetail();
                    FinderObject obj = new FinderObject(bd, client);
                    i++;
                    obj.setTempIdent(new Long(i));
                    forsearch.add(obj);
                    puzzlehash.put(new Long(i), model);
                }
            }

            // run chain
            try {
                forsearch = offlinefinderchain.findDetailsForList(forsearch,
                        clientcomplete, batchsearchmax);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            List<BookModel> toreturn = new ArrayList<BookModel>();
            for (FinderObject findobj : forsearch) {
                Long ident = findobj.getTempIdent();
                if (findobj.getSearchStatus() == null
                        || findobj.getSearchStatus() == CatalogService.DetailStatus.NODETAIL
                        || findobj.getSearchStatus() == CatalogService.DetailStatus.DETAILNOTFOUND) {
                    // nothing found - just add to return array
                    BookModel bmodel = puzzlehash.get(ident);
                    toreturn.add(bmodel);
                } else {
                    BookModel bmodel = puzzlehash.get(ident);
                    BookDetailDao detail = findobj.getBookdetail();

                    // set detailsearchstatus in bookdetail
                    detail.setDetailstatus(findobj.getSearchStatus());
                    detail.setFinderlog(findobj.getCurrentFinderLog());
                    bmodel.setBookdetail(detail);
                    // put results of finder object in model
                    toreturn.add(bmodel);
                }
            }

            return toreturn;
        }
        return null;
    }

    // @Scheduled(fixedRate = 60000)
    private void scheduledFillInDetails() {
        Integer batchsearchmax = settingService
                .getSettingAsInteger("biblio.google.batchsearchmax");
        Boolean progressivefillenabled = settingService
                .getSettingAsBoolean("biblio.progressivefill.turnedon");
        if (progressivefillenabled) {
            // get list of clients
            List<ClientDao> clients = clientService.getAllClients();

            for (ClientDao client : clients) {

                // get list of books without details - max batchsearchmax
                List<BookDao> nodetails = searchService
                        .findBooksWithoutDetails(batchsearchmax, client);

                // put books in book model
                if (nodetails != null) {
                    List<BookModel> adddetails = new ArrayList<BookModel>();
                    for (BookDao book : nodetails) {
                        adddetails.add(new BookModel(book));
                    }
                    // service call to fill in details
                    fillInDetailsForBookList(adddetails, client);

                }

            }

            // end

        }
    }

    private DetailFinder createFinderChain() {
        amazonFinder.setNext(bnfFinder);
        googleFinder.setNext(amazonFinder);
        internalFinder.setNext(googleFinder);
        return internalFinder;
    }

    private DetailFinder createOfflineFinderChain() {
        return internalFinder;
    }

    private DetailFinder createOnlineFinderChain() {
        amazonFinder.setNext(bnfFinder);
        googleFinder.setNext(amazonFinder);
        return googleFinder;
    }

    private void classifyBook(Long clientkey, BookDao book) throws Exception {
        if (book != null) {
            if (book.getBookdetail().getDetailstatus()
                    .equals(CatalogService.DetailStatus.DETAILFOUND)) {
                // get general classifier
                book = generalClassifier.classifyBook(book);

                if (book.getClientid() != null) {
                    Classifier classifier = clientService
                            .getClassifierForClient(clientkey);
                    if (classifier != null) {
                        book = classifier.classifyBook(book);
                    }

                }
            }
        }
    }

    @Override
    public BookModel assignDetailToBook(BookModel bookModel,
                                        FoundDetailsDao fd, ClientDao client) throws Exception {
        // reset finderlog, finder fills in details, rerun normal search chain
        BookDetailDao bd = bookModel.getBook().getBookdetail();
        // set tracking to false
        bd.setTrackchange(false);
        // reset finder log, search status
        bd.setFinderlog(1L);
        bd.setDetailstatus(null);

        // to finderobject
        FinderObject findobj = new FinderObject(bd, true);

        // call chain to assign the details
        DetailFinder finderchain = createFinderChain();

        findobj = finderchain.assignDetailToBook(findobj, fd);

        // replace bookdetail in bookmodel
        bd = findobj.getBookdetail();
        bd.setDetailstatus(findobj.getSearchStatus());
        bd.setFinderlog(findobj.getCurrentFinderLog());
        bookModel.setBookdetail(bd);

        // once assigned (from finder which found the details), now rerun the
        // search
        bookModel = fillInDetailsForBook(bookModel, client);
        return bookModel;
    }


}
