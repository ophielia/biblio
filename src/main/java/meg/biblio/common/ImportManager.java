package meg.biblio.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.common.db.ImportBookRepository;
import meg.biblio.common.db.dao.ImportBookDao;
import meg.biblio.search.SearchService;
import meg.tools.FileUtils;
import meg.tools.imp.FileConfig;
import meg.tools.imp.Importer;
import meg.tools.imp.ImporterFactory;
import meg.tools.imp.MapConfig;
import meg.tools.imp.Mapper;
import meg.tools.imp.MapperFactory;
import meg.tools.imp.utils.Placeholder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	private ImportConfigManager configman = new ImportConfigManager();

	@Autowired
	CatalogService catalogService;
	
	@Autowired
	SearchService searchService;
	
	@Autowired
	ImportBookRepository importRepo;
	
    @Value("${biblio.import.archivdir}")
    private String archivedir;
	

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

	public HashMap<String,Integer>  importBookList(Long clientkey, String filestr) {
		// get configs for client
		FileConfig config=null;
		MapConfig mapconfig=null;
		try {
			config = configman
					.getFileConfigForClient(clientkey);
			mapconfig = configman
					.getMapConfigForClient(clientkey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		// archive string to file
		Date now = new Date();
		String filename = getArchiveDir() + now.getTime() + "_"+clientkey+"_imp.txt";

		// import file into ImportBookDao
		FileUtils.writeStringToFile(filename, filestr);
		File file = new File(filename);
		List<Object> newbooks = importFile(config, mapconfig, file);
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
				List<BookDao> found = searchService.findBookByClientId(clientbookid); 
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
						book.setClientbookid(newbook.getClientbookid());
						book.setTitle(title);
						if (newbook.getAuthor()!=null) {
							List<ArtistDao> authors=new ArrayList<ArtistDao>();
							ArtistDao author = catalogService.textToArtistName(newbook.getAuthor().trim());
							book.setAuthors(authors);
						}
						if (newbook.getIllustrator()!=null) {
							List<ArtistDao> illustrators=new ArrayList<ArtistDao>();
							ArtistDao illustrator = catalogService.textToArtistName(newbook.getIllustrator().trim());
							book.setIllustrators(illustrators);
						}
						if (newbook.getPublisher()!=null) {
							PublisherDao publisher=catalogService.findPublisherForName(newbook.getPublisher());
							book.setPublisher(publisher);
						}						
						BookModel model = new BookModel(book);
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
		
		// persist list of bookmodels
		catalogService.createCatalogEntriesFromList(clientkey,toimport);

		// persist any duplicates
		importRepo.save(errors);
		
		// put together resulthash
		HashMap<String,Integer> results = new HashMap<String,Integer>();
		results.put(Results.listsize,listsize);
		results.put(Results.importsize,importsize);
		results.put(Results.totalerrorssize,errorssize);
		results.put(Results.duplicatesize,new Integer(duplicates));
		results.put(Results.noidsize,new Integer(noid));
		results.put(Results.notitlesize,new Integer(noid));
		
		return results;
	}


	public String getArchiveDir() {
		return this.archivedir;
	}

	public void setArchiveDir(String archivedir) {
		this.archivedir = archivedir;
	}


}
