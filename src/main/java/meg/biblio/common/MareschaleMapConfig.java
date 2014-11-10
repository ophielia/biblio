package meg.biblio.common;

import java.util.ArrayList;
import java.util.List;

import meg.tools.imp.MapConfig;
import meg.tools.imp.utils.FieldMapping;




public class MareschaleMapConfig implements MapConfig {

	public String getDestinationClassName() {
		return "meg.biblio.common.db.dao.ImportBookDao";
	}

	public List<FieldMapping> getMappings() {
		List<FieldMapping> mappings = new ArrayList<FieldMapping>();

		// map a couple of fields here - just a test for now

		FieldMapping map = new FieldMapping();
		map.setFromFieldTag("field1");
		map.setSetterMethod("setClientbookid");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field2");
		map.setSetterMethod("setTitle");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field3");
		map.setSetterMethod("setAuthor");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field4");
		map.setSetterMethod("setIllustrator");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field5");
		map.setSetterMethod("setPublisher");
		mappings.add(map);
		
		return mappings;
	}

	public String getHelperClassName() {
		return "meg.bank.bus.imp.SocGenMappingHelper";
	}

}
