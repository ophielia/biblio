package meg.biblio.common.report;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tablevalue")
public class TableValue {

	String value;
	Integer position;
	
	@XmlElement
	public String getValue() {
		return value;
	}
	
	@XmlElement
	public Integer getPos() {
		return position;
	}
}
