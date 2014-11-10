package meg.tools.imp.utils;

import java.util.Hashtable;

public class TextPlaceholder implements Placeholder {

	Hashtable hash = new Hashtable();

	public void setField(int i, Object field) {
		String key = "field" + i;
		setField(key, field);
	}

	public Object getField(int i) {
		String key = "field" + i;
		return getField(key);
	}

	public Object getField(String key) {
		return hash.get(key);
	}

	public void setField(String key, Object field) {
		hash.put(key, field);
	}

}
