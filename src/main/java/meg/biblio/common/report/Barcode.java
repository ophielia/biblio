package meg.biblio.common.report;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "barcode")
public class Barcode {

	String code;

	String label;

	int position;

	public Barcode(String code, String label) {
		super();
		this.code = code;
		this.label = label;
	}
	
	

	public Barcode() {
		super();
	}



	@XmlElement(name = "msg")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement(name = "description")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlElement(name = "pos")
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
