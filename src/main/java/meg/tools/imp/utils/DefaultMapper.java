package meg.tools.imp.utils;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;

import meg.tools.imp.MapConfig;
import meg.tools.imp.Mapper;

public class DefaultMapper implements Mapper {

	private MapConfig mapconfig;

	public DefaultMapper(MapConfig mapconfig) {
		this.mapconfig = mapconfig;
	}

	public Object mapObject(Placeholder placeholder) {
		// instantiate helper class, object
		MappingHelper helper = getMappingHelperClass();
		Object mapped = helper.instantiateObject(mapconfig
				.getDestinationClassName());

		// do auto mapping
		doAutoMapping(mapped, placeholder);

		// do manual mapping
		helper.doManualMapping(mapped, placeholder);

		// return object
		return mapped;
	}

	private void doAutoMapping(Object mapped, Placeholder placeholder) {
		// read field mappings
		List mappings = mapconfig.getMappings();

		// for each field mapping, set field
		if (mappings != null) {
			// put object methods in map
			Class objectclass = mapped.getClass();
			Method[] methods = objectclass.getMethods();
			Hashtable methodmap = new Hashtable();
			for (int i = 0; i < methods.length; i++) {
				methodmap.put(methods[i].getName(), methods[i]);
			}

			for (int i = 0; i < mappings.size(); i++) {
				FieldMapping field = (FieldMapping) mappings.get(i);
				setFieldInObject(mapped, placeholder, field, methodmap);
			}
		} else {
			// TODO error handling - no field mappings
		}
	}

	/**
	 * Note - this method assumes single parameter setter methods. For setter
	 * methods which have multiple parameters, the manual mapping should be
	 * used.
	 * 
	 * @param mapped
	 * @param placeholder
	 * @param field
	 * @param methods
	 */
	private void setFieldInObject(Object mapped, Placeholder placeholder,
			FieldMapping field, Hashtable methods) {
		// get setter method
		String methodname = field.getSetterMethod();
		Method method = (Method) methods.get(methodname);

		// determine parameter type
		Class[] types = method.getParameterTypes();
		Class methodtype = types[0];

		// retrieve placeholder field
		// TODO add access by field number here
		Object toset = placeholder.getField(field.getFromFieldTag());

		// convert placeholder field to type
		// ---- determine methodtype
		Object arglist[] = new Object[1];
		String typename = methodtype.getName();
		if (typename.endsWith("String")) {
			arglist[0] = ConversionUtils.convertToString(toset);
		} else if (typename.endsWith("Double")) {
			arglist[0] = ConversionUtils.convertToDouble(toset);
		} else if (typename.endsWith("Date")) {
			arglist[0] = ConversionUtils.convertToDate(toset);
		} else if (typename.endsWith("Long")) {
			arglist[0] = ConversionUtils.convertToLong(toset);
		} else {
			// TODO finish implementing all methods here
			// TODO add error handling for unknown method type
		}

		// call setter method
		try {
			method.invoke(mapped, arglist);
		} catch (Exception e) {
			// TODO add error handling

		}
	}

	private MappingHelper getMappingHelperClass() {
		// get class name
		String classname = mapconfig.getHelperClassName();
		if (classname == null) {
			// no helper class declared - use the default
			classname = "meg.tools.imp.utils.DefaultMappingHelper";
		}

		try {
			// get class
			Class cls = Class.forName(classname);

			// create helper class
			MappingHelper helper = (MappingHelper) cls.newInstance();

			// return helper class
			return helper;
		} catch (Throwable e) {
			// TODO add error handling here
		}

		// error - return null
		return null;
	}

}
