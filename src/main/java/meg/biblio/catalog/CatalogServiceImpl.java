package meg.biblio.catalog;

import meg.biblio.catalog.db.*;
import meg.biblio.catalog.db.dao.*;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.*;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class CatalogServiceImpl implements CatalogService {

    /* Get actual class name to be printed on */
    static Logger log = Logger.getLogger(CatalogServiceImpl.class.getName());

    @Autowired
    SelectKeyService keyService;

    @Autowired
    ScalarFunction<Boolean> scalarDb;

    @Autowired
    ScalarFunction<BigInteger> scalarLongDb;


    @Autowired
    BookMemberService bmemberService;

    @Autowired
    SearchService searchService;

    @Autowired
    ClientService clientService;

    @Autowired
    BookRepository bookRepo;

    @Autowired
    PublisherRepository pubRepo;

    @Autowired
    BookDetailRepository bookDetailRepo;

    @Autowired
    FoundDetailsRepository foundRepo;

    @Autowired
    FoundWordsRepository indexRepo;

    @Autowired
    IgnoredWordsRepository ignoredRepo;

    @Autowired
    ArtistRepository artistRepo;


    @Autowired
    SubjectRepository subjectRepo;

    @Autowired
    ClassificationRepository classRepo;

    @Autowired
    AppSettingService settingService;

    @Autowired
    BarcodeService barcodeService;

    /**
     * Assumes validated BookModel. Saves a book to the database for the first
     * time. Usually, only minimal entries are made here - details are filled in
     * later. Book is entered with corresponding clientkey.
     */
    @Override
    public BookModel createCatalogEntryFromBookModel(Long clientkey,
                                                     BookModel model) {
        BookDao book = createBookFromBookModel(clientkey, model);
        Long bookid = book.getId();

        // reload book in bookmodel
        BookModel result = loadBookModel(bookid);

        // return bookmodel
        return result;
    }

    @Override
    public List<BookModel> createCatalogEntriesFromList(Long clientkey,
                                                        List<BookModel> toimport) {
        List<BookModel> createdobjects = new ArrayList<BookModel>();

        // go through list of BookModels, persisting them
        for (BookModel imported : toimport) {

            BookDao saved = createBookFromBookModel(clientkey, imported);
            Long bookid = saved.getId();

            // reload book in bookmodel
            BookModel result = loadBookModel(bookid);

            // add info to lists
            createdobjects.add(result);
        }

        return createdobjects;

    }

    @Override
    public BookModel loadBookModel(Long id) {
        // get book from repo
        BookDao book = bookRepo.findOne(id);
        BookDetailDao bookdetail = book.getBookdetail();

        if (bookdetail != null) {
            // get authors, subjects , illustrators and foundwords
            List<ArtistDao> authors = bookdetail.getAuthors();
            bookdetail.setAuthors(authors);
            List<ArtistDao> illustrators = bookdetail.getIllustrators();
            bookdetail.setIllustrators(illustrators);
            List<SubjectDao> subjects = bookdetail.getSubjects();
            bookdetail.setSubjects(subjects);
            PublisherDao publisher = bookdetail.getPublisher();
            bookdetail.setPublisher(publisher);
            List<FoundWordsDao> foundwords = bookdetail.getFoundwords();
            bookdetail.setFoundwords(foundwords);

            // set book in model
            book.setBookdetail(bookdetail);
            BookModel model = new BookModel(book);

            // return model
            return model;

        }
        // return model
        return new BookModel();
    }

    public BookDao saveBook(BookDao book) {
        BookDetailDao bookdetail = book.getBookdetail();
        boolean bookchange = bookdetail.getTextchange();

        bookdetail = saveBookDetail(bookdetail);
        book.setBookdetail(bookdetail);
        BookDao saved = bookRepo.save(book);
        if (bookchange) {
            indexBooktext(saved);
            bookdetail.setTextchange(false);
        }

        return saved;
    }

    private void indexBooktext(BookDao savebook) {
        BookDetailDao saved = savebook.getBookdetail();
        log.debug("indexing for bookdetailid:" + saved.getId()
                + "; description:" + saved.getDescription());

        List<FoundWordsDao> todelete = indexRepo.findWordsForBookDetail(saved);
        indexRepo.delete(todelete);

        // make counting hash
        HashMap<String, Integer> wordcounts = new HashMap<String, Integer>();
        List<FoundWordsDao> foundwords = new ArrayList<FoundWordsDao>();

        // mash up all text fields together - title, description, subjects,
        // authors, illustrators
        StringBuffer charles = new StringBuffer(saved.getTitle());
        if (saved.getDescription() != null) {
            charles.append(" ").append(saved.getDescription());
        }
        if (saved.getSubjects() != null) {
            for (SubjectDao subject : saved.getSubjects()) {
                charles.append(" ").append(subject.getListing());
            }
        }
        if (saved.getAuthors() != null) {
            for (ArtistDao author : saved.getAuthors()) {
                charles.append(" ").append(author.getDisplayName());
            }
        }
        if (saved.getIllustrators() != null) {
            for (ArtistDao author : saved.getIllustrators()) {
                charles.append(" ").append(author.getDisplayName());
            }
        }

        // split all text into words
        String mashup = charles.toString();
        // remove punctuation
        String cleanmashup = mashup.replaceAll("[^a-zA-Z'éèàùâêîôûëïç ]", " ");
        // split by space
        String[] words = cleanmashup.split(" ");

        // go through all words, counting each
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            word = word.toLowerCase().trim();
            if (word.length() > 0) {
                // count this word....
                if (wordcounts.containsKey(word)) {
                    // add to count
                    Integer count = wordcounts.get(word);
                    count++;
                    wordcounts.put(word, count);
                } else {
                    // make new key
                    wordcounts.put(word, new Integer(1));
                }
            }
        }

        // double all words with apostrophes
        HashMap<String, Integer> noapostrophes = new HashMap<String, Integer>();
        for (String word : wordcounts.keySet()) {
            // go through all keys in wordcounts
            // if word contains apostrophe
            if (word.contains("'")) {
                // get count
                Integer count = wordcounts.get(word);

                // replace apostrophe with space
                String noapos = word.replaceAll("'", " ");
                // split on space
                String[] noaposparts = noapos.split(" ");
                // add each part to repeat hash with same count
                for (int i = 0; i < noaposparts.length; i++) {
                    String newword = noaposparts[i].trim();
                    noapostrophes.put(newword, count);
                }
            }
        }
        // add all repeat hash values and keys to wordcount
        for (String word : noapostrophes.keySet()) {
            Integer count = noapostrophes.get(word);
            wordcounts.put(word, count);
        }

        // get ignoredwords
        List<String> toignore = new ArrayList<String>();
        List<IgnoredWordsDao> ignoredlist = ignoredRepo.findAll();
        for (IgnoredWordsDao ignore : ignoredlist) {
            toignore.add(ignore.getWord());
        }

        // now, save counted words - book, word, countintext
        for (String word : wordcounts.keySet()) {
            if (word.length() > 1) {
                if (!toignore.contains(word)) {
                    Integer count = wordcounts.get(word);
                    FoundWordsDao wordcount = new FoundWordsDao();
                    wordcount.setBookdetail(saved);
                    wordcount.setCountintext(count);
                    wordcount.setWord(word);
                    foundwords.add(wordcount);
                }
            }
        }
        // persist
        if (foundwords.size() > 0) {
            indexRepo.save(foundwords);
        }

    }


    public List<FoundDetailsDao> getFoundDetailsForBook(Long detailid) {
        if (detailid != null) {
            // query db for founddetails
            List<FoundDetailsDao> details = foundRepo
                    .findDetailsForBook(detailid);
            // return founddetails
            return details;
        }
        return null;
    }


    @Override
    public HashMap<Long, ClassificationDao> getShelfClassHash(Long clientkey,
                                                              String lang) {
        lang = getShortLangCode(lang);
        HashMap<Long, ClassificationDao> resulthash = new HashMap<Long, ClassificationDao>();
        List<ClassificationDao> shelfclasses = classRepo
                .findByClientidAndLanguage(clientkey, lang);
        if (shelfclasses != null) {
            for (ClassificationDao shelfclass : shelfclasses) {
                resulthash.put(shelfclass.getKey(), shelfclass);
            }
        }
        return resulthash;
    }

    @Override
    public List<ClassificationDao> getShelfClassList(Long clientkey, String lang) {
        lang = getShortLangCode(lang);
        List<ClassificationDao> shelfclasses = classRepo
                .findByClientidAndLanguage(clientkey, lang);
        return shelfclasses;
    }

    private String getShortLangCode(String lang) {
        if (lang == null) {
            return null;
        }
        if (lang.startsWith("en"))
            return "en";
        if (lang.startsWith("fr"))
            return "fr";
        return "en";
    }

    private BookDao createBookFromBookModel(Long clientkey, BookModel model) {
        // get configuration for barcodes
        ClientDao client = clientService.getClientForKey(clientkey);

        // get book
        BookDao book = model.getBook();

        // get book detail
        BookDetailDao bookdetail = book.getBookdetail();

        // get founddetails
        List<FoundDetailsDao> founddetails = model.getFounddetails();

        // add clientkey
        book.setClientid(clientkey);

        // make new clientbookid if needed
        if (book.getClientbookid() == null
                || book.getClientbookid().length() == 0) {
            // no client book number was entered - get the next one
            // get max bookid
            Long maxbookid = getMaxLastBookNrForClient(clientkey);
            maxbookid++;
            // set max bookid in book
            book.setClientbookid(maxbookid.toString());
        }

        // create barcode from clientbookid
        String barcode = barcodeService.getBookBarcodeForClientid(client,
                book.getClientbookid());
        // set in bookdetail
        if (barcode != null) {
            book.setBarcodeid(barcode);
        }


        // add default entries for status, detail status, type
        if (book.getStatus() == null) {
            book.setStatus(Status.SHELVED);
        }
        book.setCreatedon(new Date());
        if (bookdetail.getDetailstatus() == null) {
            bookdetail.setDetailstatus(CatalogService.DetailStatus.NODETAIL);
        }

        // handle authors and illustrators by
        // retrieving any existing artists from db
        if (bookdetail.getAuthors() != null) {
            List<ArtistDao> newauthors = new ArrayList<ArtistDao>();
            for (ArtistDao author : bookdetail.getAuthors()) {
                if (author.getId() != null) {
                    // we have an existing author here - add it to the list
                    newauthors.add(author);
                } else {
                    // new author - check for match in db
                    ArtistDao dbfound = searchService
                            .findArtistMatchingName(author);
                    if (dbfound != null) {
                        newauthors.add(dbfound);
                    } else {
                        newauthors.add(author);
                    }
                }
            }
            bookdetail.setAuthors(newauthors);
        }
        if (bookdetail.getIllustrators() != null) {
            List<ArtistDao> newillustrators = new ArrayList<ArtistDao>();
            for (ArtistDao illustrator : bookdetail.getIllustrators()) {
                if (illustrator.getId() != null) {
                    // we have an existing illustrator here - add it to the list
                    newillustrators.add(illustrator);
                } else {
                    // new illustrator - check for match in db
                    ArtistDao dbfound = searchService
                            .findArtistMatchingName(illustrator);
                    if (dbfound != null) {
                        newillustrators.add(dbfound);
                    } else {
                        newillustrators.add(illustrator);
                    }
                }
            }
            bookdetail.setIllustrators(newillustrators);
        }
        if (bookdetail.getPublisher() != null) {
            PublisherDao pub = bmemberService.findPublisherForName(bookdetail.getPublisher()
                    .getName());
            bookdetail.setPublisher(pub);
        }

        if (bookdetail.hasIsbn()
                && (bookdetail.getTitle() == null || bookdetail.getTitle()
                .trim().length() == 0)) {
            bookdetail.setTitle(CatalogService.titledefault);
        }

        // persist book
        book.setBookdetail(bookdetail);
        BookDao saved = saveBook(book);

        // save found details here, if there
        bookdetail = book.getBookdetail();
        if (founddetails != null) {
            // set bookdetail in FoundDetail and save
            List<FoundDetailsDao> details = model.getFounddetails();
            for (FoundDetailsDao det : details) {
                det.setBookdetailid(bookdetail.getId());
                foundRepo.save(det);
            }
        }
        return saved;

    }

    private Long getMaxLastBookNrForClient(Long clientkey) {
        String sql = "select max(clientbookidsort) from book where clientid="
                + clientkey;
        BigInteger maxbook = scalarLongDb.singleResult(sql);

        return maxbook != null ? maxbook.longValue() : 0L;
    }

    @Override
    public void assignShelfClassToBooks(Long shelfclassUpdate,
                                        List<Long> toupdate) {
        List<BookDao> books = bookRepo.findAll(toupdate);
        List<BookDao> tosave = new ArrayList<BookDao>();
        if (books != null) {
            for (BookDao book : books) {
                book.setClientshelfcode(shelfclassUpdate);
                tosave.add(book);
            }
        }
        if (tosave.size() > 0) {
            bookRepo.save(tosave);
        }

    }

    @Override
    public void assignStatusToBooks(Long statusUpdate, List<Long> toupdate) {
        List<BookDao> books = bookRepo.findAll(toupdate);
        List<BookDao> tosave = new ArrayList<BookDao>();
        if (books != null) {
            for (BookDao book : books) {
                book.setStatus(statusUpdate);
                tosave.add(book);
            }
        }
        if (tosave.size() > 0) {
            bookRepo.save(tosave);
        }

    }

    @Override
    public BookModel updateCatalogEntryFromBookModel(Long clientkey,
                                                     BookModel model, Boolean fillindetails)
            throws GeneralSecurityException, IOException {
        // get book
        BookDao book = model.getBook();
        book.setClientid(clientkey);

        // if book is not null, save it
        if (book != null) {
            book = saveBook(book);
        }

        BookModel toreturn = loadBookModel(book.getId());
        return toreturn;
    }

    @Override
    public BookDao findBookById(Long bookid) {
        if (bookid != null) {
            return bookRepo.findOne(bookid);
        }
        return null;
    }

    @Override
    public BookDao findBookByClientBookId(String bookid, ClientDao client) {
        // call repository
        List<BookDao> books = bookRepo.findBookByClientAssignedId(
                bookid.trim(), client.getId());
        // return results
        if (books != null && books.size() > 0) {
            return books.get(0);
        }
        return null;
    }

    @Override
    public BookDao updateBookStatus(Long bookid, long status) {
        // get book
        BookDao book = bookRepo.findOne(bookid);

        // set status
        book.setStatus(new Long(status));

        // save book
        book = bookRepo.save(book);

        // return book
        return book;
    }

    @Override
    public void assignCodeToBook(String code, Long bookid) {
        if (bookid != null && code != null) {
            // get book
            BookDao book = bookRepo.findOne(bookid);
            if (book != null) {
                // put code in slot
                book.setBarcodeid(code);
                // save book
                saveBook(book);
            }
        }
    }

    @Override
    public BookDao findBookByBarcode(String barcode) {
        BookDao book = bookRepo.findBookByBarcode(barcode);
        return book;
    }

    @Override
    public BookDetailDao saveBookDetail(BookDetailDao newdetail) {
        boolean newobject = false;
        if (newdetail != null) {

            // deal with client specific change.
            // if the newdetail changes have been made on the main object (not
            // client specific)
            // than the newdetail changes should be made on it's own object. It
            // won't be saved under the
            // newdetail id, but as a new object, getting a new id
            // get from db if already persisted
            if (newdetail.getId() != null) { // only applies if this has been
                // saved to the db

                String sql = "select clientspecific from bookdetail where id = "
                        + newdetail.getId();
                Boolean dbclientsp = scalarDb.singleResult(sql);

                // so, if we're here, we know that a database version is
                // available which shouldn't be overwritten with client specific
                // changes.
                // only save if no client specific changes
                if (dbclientsp != null && !dbclientsp) {
                    if (newdetail.getClientspecific()) {
                        // save into new client specific detail and return
                        BookDetailDao newclientspecific = new BookDetailDao();
                        // copy from newdetail
                        newclientspecific.copyFrom(newdetail);
                        // save and return
                        newdetail = newclientspecific;
                        newdetail.setTrackchange(true);
                        newdetail.setClientspecific(true);
                        newdetail.setTrackchange(false);
                        newdetail.setFoundwords(null);
                    }
                }
            } else {
                newobject = true;
            }

            // refresh objects in bookdetail
            Boolean origcs = newdetail.getClientspecific();
            // refresh authors (avoid detached object problem)
            List<ArtistDao> authors = new ArrayList<ArtistDao>();
            if (newdetail.getAuthors() != null) {
                for (ArtistDao detailauth : newdetail.getAuthors()) {
                    if (detailauth.getId() != null) {
                        // get from db
                        ArtistDao dbauth = artistRepo.findOne(detailauth
                                .getId());
                        // copy into db from bookdetail
                        dbauth.copyFrom(detailauth);
                        // add to persist list
                        authors.add(dbauth);
                    } else {
                        authors.add(detailauth);
                    }
                }
                newdetail.setAuthors(authors);
            }

            // refresh illustrators (avoid detached object problem)
            List<ArtistDao> illustrators = new ArrayList<ArtistDao>();
            if (newdetail.getIllustrators() != null) {
                for (ArtistDao detailillus : newdetail.getIllustrators()) {
                    if (detailillus.getId() != null) {
                        // get from db
                        ArtistDao dbauth = artistRepo.findOne(detailillus
                                .getId());
                        // copy into db from bookdetail
                        dbauth.copyFrom(detailillus);
                        // add to persist list
                        illustrators.add(dbauth);
                    } else {
                        illustrators.add(detailillus);
                    }
                }
                newdetail.setIllustrators(illustrators);
            }

            // refresh subjects
            List<SubjectDao> subjects = new ArrayList<SubjectDao>();
            if (newdetail.getSubjects() != null) {
                for (SubjectDao sbjt : newdetail.getSubjects()) {
                    if (sbjt.getId() != null) {
                        // get from db
                        SubjectDao dbsbjt = subjectRepo.findOne(sbjt.getId());
                        // copy changes into db object
                        dbsbjt.copyFrom(sbjt);
                        // add to persist list
                        subjects.add(dbsbjt);
                    } else {
                        // add to persist list
                        subjects.add(sbjt);
                    }
                }
                newdetail.setSubjects(subjects);
            }

            // refresh found words
/* 			if (newdetail.getId() != null) {
                List<FoundWordsDao> foundwords = new ArrayList<FoundWordsDao>();
				if (newdetail.getFoundwords() != null
						&& newdetail.getFoundwords().size() > 0) {
					for (FoundWordsDao fwords : newdetail.getFoundwords()) {
						if (fwords.getId() != null) {
							// get from db
							FoundWordsDao dbfw = indexRepo.findOne(fwords
									.getId());
							// copy changes into db object
							dbfw.copyFrom(fwords);
							// add to persist list
							foundwords.add(dbfw);
						} else {
							// add to persist list
							foundwords.add(fwords);
						}
					}
					newdetail.setFoundwords(foundwords);
				}
			}*/

            // publisher
            if (newdetail.getPublisher() != null) {
                if (newdetail.getPublisher().getId() != null) {
                    // pull publisher from db
                    PublisherDao dbpub = pubRepo.findOne(newdetail
                            .getPublisher().getId());
                    if (dbpub != null) {
                        dbpub.copyFrom(newdetail.getPublisher());
                        newdetail.setPublisher(dbpub);
                    }

                }
            }

            // swap out NODETAILFOUNDWISBN - shouldn't be saved to db
            if (newdetail.getDetailstatus() != null
                    && newdetail.getDetailstatus() == CatalogService.DetailStatus.DETAILNOTFOUNDWISBN) {
                newdetail
                        .setDetailstatus(CatalogService.DetailStatus.DETAILNOTFOUND);
            }

            if (newobject) {
                // this detail hasn't yet been saved. Save it
                // as non-client specific, so it can be used as a base
                // for further searches.
                newdetail.setClientspecific(false);

            }

            // save and return
            newdetail = bookDetailRepo.save(newdetail);
            return newdetail;

        }
        return null;
    }


    @Override
    public void changeClientBookNr(String newclientbookid, Long bookid,
                                   ClientDao client) {
        // check for filled in newclientbookid
        if (newclientbookid != null) {
            newclientbookid = newclientbookid.trim();
            if (newclientbookid.length() > 0) {
                // verify no book exists with new clientbookid
                List<BookDao> matchingbooks = bookRepo
                        .findBookByClientAssignedId(newclientbookid,
                                client.getId());
                if (matchingbooks == null || matchingbooks.size() == 0) {
                    // retrieve book
                    BookDao tochange = bookRepo.findOne(bookid);
                    // set clientbookid in book
                    tochange.setClientbookid(newclientbookid);
                    // make barcode and save in book
                    String barcode = barcodeService.getBookBarcodeForClientid(
                            client, newclientbookid);
                    // set in bookdetail
                    if (barcode != null) {
                        tochange.setBarcodeid(barcode);
                    }

                    // save book
                    saveBook(tochange);
                }
            }
        }
    }


}
