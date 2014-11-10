package meg.biblio.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.web.model.BookModel;
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
	CatalogService catalogService;
	
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

	public void importBookList(Long clientkey, String filestr) {
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

		// pass importbookdaos to CatalogService for import
		catalogService.createCatalogEntriesFromList(clientkey,newbooks);

	}


	public String getArchiveDir() {
		return this.archivedir;
	}

	public void setArchiveDir(String archivedir) {
		this.archivedir = archivedir;
	}


}
