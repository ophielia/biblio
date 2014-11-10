package meg.biblio.common;

import java.util.ArrayList;
import java.util.List;

import meg.bank.util.imp.FieldMapping;
import meg.bank.util.imp.MapConfig;



public class MareschaleMapConfig implements MapConfig {

	public String getDestinationClassName() {
		return "meg.bank.bus.dao.BankTADao";
	}

	public List<FieldMapping> getMappings() {
		List<FieldMapping> mappings = new ArrayList<FieldMapping>();

		// map a couple of fields here - just a test for now

		FieldMapping map = new FieldMapping();
		map.setFromFieldTag("field1");
		map.setSetterMethod("setTransdate");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field3");
		map.setSetterMethod("setDetail");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field4");
		map.setSetterMethod("setAmount");
		mappings.add(map);

		map = new FieldMapping();
		map.setFromFieldTag("field2");
		map.setSetterMethod("setDescription");
		mappings.add(map);

		return mappings;
	}

	public String getHelperClassName() {
		return "meg.bank.bus.imp.SocGenMappingHelper";
	}

}
