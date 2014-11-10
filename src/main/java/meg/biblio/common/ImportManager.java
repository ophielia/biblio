package meg.biblio.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

	private ImportConfigManager configman = new ImportConfigManager();

	@Autowired
	SearchService searchService;
	
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

	public List<Object> importTransactions(int clientkey, String filestr) {
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

		// import file into BankTADaos
		FileUtils.writeStringToFile(filename, filestr);
		File file = new File(filename);
		List<Object> newobjects = importFile(config, mapconfig, file);

		// Begin persisting objects
		// ---- search for date of most recently entered banktrans for
		//      comparison
		//Date mostrecent = searchService.getMostRecentTransDate();
		Calendar cal = Calendar.getInstance();
		//cal.setTime(mostrecent);
		// ---- set calendar to latest possible time in date
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);

		// ---- go through object list - create list without duplicates
/*		List<BankTADao> nondups = new ArrayList<BankTADao>();
		for (int i=0;i<newobjects.size();i++) {
			BankTADao trans = (BankTADao) newobjects.get(i);
			if (mostrecent.before(trans.getTransdate())) {
				// ------ if date is after most recent db banktrans, save the trans
				// immediately
				nondups.add(trans);
			} else {
				// ------ if date is on or before the most recent db banktrans date,
				// check for duplicate in db (on amount, desc, and date)
				boolean duplicate = transService.doesDuplicateExist(trans);
				if (!duplicate) nondups.add(trans);
			}
		}

		// ---- persist non duplicates
		for (BankTADao trans: nondups) {
			transService.addTransaction(trans);
		}
		*/
		// returned List is a list of error / log messages (although
		// this isn't yet really implemented - currently just returns the list of new objects
		// TODO: add result object
		return newobjects;

	}


	public String getArchiveDir() {
		return this.archivedir;
	}

	public void setArchiveDir(String archivedir) {
		this.archivedir = archivedir;
	}


}
