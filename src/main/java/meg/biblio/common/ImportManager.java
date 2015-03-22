package meg.biblio.common;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.DetailSearchService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.db.ImportBookRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.db.dao.ImportBookDao;
import meg.biblio.search.SearchService;
import meg.tools.imp.FileConfig;
import meg.tools.imp.Importer;
import meg.tools.imp.ImporterFactory;
import meg.tools.imp.MapConfig;
import meg.tools.imp.Mapper;
import meg.tools.imp.MapperFactory;
import meg.tools.imp.utils.Placeholder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportManager {

	public static final String ClientKeyLkup = "clientkey";

	public final class Results {
		public static final String listsize="listsize";
		public static final String importsize="importsize";
		public static final String totalerrorssize="totalerrorssize";
		public static final String duplicatesize="duplicatesize";
		public static final String noidsize="noidsize";
		public static final String notitlesize="notitlesize";
	}

	@Autowired
	ClientService clientService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	SearchService searchService;

	@Autowired
	DetailSearchService detailSearchService;

	@Autowired
	ImportBookRepository importRepo;



	private List<Object> importFile(FileConfig config,MapConfig mapconfig, File file) {
		// Get importer
		Importer importer = ImporterFactory.getImporter(config);

		// parse file into placeholders
		List<Placeholder> placeholders = importer.parseFile(file);

		// map objects
		Mapper mapper = MapperFactory.getMapper(mapconfig);
		List<Object> importedobjects = new ArrayList<Object>();

		for (int i = 0; i < placeholders.size(); i++) {
			Object mapped = mapper.mapObject((Placeholder) placeholders.get(i));
			importedobjects.add(mapped);
		}

		// return lists
		return importedobjects;
	}

	private List<Object> importFile(FileConfig config,MapConfig mapconfig, String string) {
		// Get importer
		Importer importer = ImporterFactory.getImporter(config);

		// parse file into placeholders
		List<Placeholder> placeholders = importer.parseString(string);

		// map objects
		Mapper mapper = MapperFactory.getMapper(mapconfig);
		List<Object> importedobjects = new ArrayList<Object>();

		for (int i = 0; i < placeholders.size(); i++) {
			Object mapped = mapper.mapObject((Placeholder) placeholders.get(i));
			importedobjects.add(mapped);
		}

		// return lists
		return importedobjects;
	}


	public HashMap<String,Integer>  importBookList(Long clientkey, String filestr) throws Exception, IOException {
		// get configs for client
		FileConfig config=null;
		MapConfig mapconfig=null;
		ClientDao client = clientService.getClientForKey(clientkey);
		try {
			config = clientService
					.getFileConfigForClient(clientkey);
			mapconfig = clientService
					.getMapConfigForClient(clientkey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// import file into ImportBookDao
		List<Object> newbooks = importFile(config, mapconfig, filestr);
		Integer listsize = new Integer(newbooks.size());

		// go through all books, creating book models
		// check for duplicates
		List<BookModel> toimport= new ArrayList<BookModel>();
		List<ImportBookDao> errors= new ArrayList<ImportBookDao>();
		int duplicates=0;
		int noid=0;
		int notitle=0;
		for (Object newbookobject:newbooks) {
			ImportBookDao newbook = (ImportBookDao)newbookobject;
			// get clientbookid
			String clientbookid = newbook.getClientbookid();
			// note - only books with clientbookids are imported
			if (clientbookid!=null) {
				// check for existing clientbookid
				List<Long> found = searchService.findBookIdByClientId(clientbookid);
				if (found!=null && found.size()>0) {
					// if exists - put newbook in duplicate list
					errors.add((ImportBookDao)newbookobject);
					duplicates++;
				} else {
					// check for no title
					String title = newbook.getTitle()!=null?newbook.getTitle().trim():"";
					if (title.length()>0) {
						// if doesn't exist and has title- put newbook in toimport list
						BookDao book = new BookDao();
						BookModel model = new BookModel(book);
						model.setClientbookid(newbook.getClientbookid());
						model.setTitle(title);
						if (newbook.getAuthor()!=null && newbook.getAuthor().trim().length()>0 ) {
							List<ArtistDao> authors=new ArrayList<ArtistDao>();
							ArtistDao author = catalogService.textToArtistName(newbook.getAuthor().trim());
							authors.add(author);
							model.setAuthors(authors);
						}
						if (newbook.getIllustrator()!=null && newbook.getIllustrator().trim().length()>0) {
							List<ArtistDao> illustrators=new ArrayList<ArtistDao>();
							ArtistDao illustrator = catalogService.textToArtistName(newbook.getIllustrator().trim());
							illustrators.add(illustrator);
							model.setIllustrators(illustrators);
						}
						if (newbook.getPublisher()!=null && newbook.getPublisher().trim().length()>0) {
							model.setPublisher(newbook.getPublisher().trim());
						}
						if (newbook.getIsbn10()!=null && newbook.getIsbn10().trim().length()>0) {
							model.setIsbn10(newbook.getIsbn10().trim());
						}
						if (newbook.getIsbn13()!=null && newbook.getIsbn13().trim().length()>0) {
							model.setIsbn13(newbook.getIsbn13().trim());
						}
						if (newbook.getBarcode()!=null && newbook.getBarcode().trim().length()>0) {
							model.setBarcode(newbook.getBarcode().trim());
						}
						// reset clientspecific...
						model.getBook().getBookdetail().setClientspecific(false);
						toimport.add(model);

					}else {
						// no title - this is an error
						errors.add(newbook);
						notitle++;
					}

				}
			} else {
				((ImportBookDao)newbookobject).setError("no clientbookid");
				errors.add((ImportBookDao)newbookobject);
				noid++;
			}
		}

		// gather info
		Integer importsize=new Integer(toimport.size());
		Integer errorssize=new Integer(errors.size());

		// do offline search for all books
		toimport = detailSearchService.doOfflineSearchForBookList(toimport, client);

		// persist list of bookmodels
		toimport = catalogService.createCatalogEntriesFromList(clientkey,toimport);

		// search for details for list
		List<BookModel> details = detailSearchService.fillInDetailsForBookList(toimport, client);
		if (details!=null) {
			for (BookModel model:details) {
				catalogService.updateCatalogEntryFromBookModel(clientkey, model, false);
			}
		}

		// persist any duplicates
		importRepo.save(errors);

		// put together resulthash
		HashMap<String,Integer> results = new HashMap<String,Integer>();
		results.put(Results.listsize,listsize);
		results.put(Results.importsize,importsize);
		results.put(Results.totalerrorssize,errorssize);
		results.put(Results.duplicatesize,new Integer(duplicates));
		results.put(Results.noidsize,new Integer(noid));
		results.put(Results.notitlesize,new Integer(notitle));

		return results;
	}




}
