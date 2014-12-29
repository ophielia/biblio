package meg.biblio.common.report;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "barcodes")
public class BarcodeSheet {

	List<Barcode> codes;
	String title;
	int columncount;

	public BarcodeSheet(List<Barcode> codes, String title) {
		this.codes = codes;
		this.title = title;
		numberCodes(codes);
	}

	public BarcodeSheet() {
		super();
	}

	@XmlElement
	public String getTitle() {
		return this.title;
	}

	@XmlElement
	public List<Barcode> getCodes() {
		return codes;
	}

	public void setCodes(List<Barcode> codes) {
		this.codes = codes;
	}

	private void numberCodes(List<Barcode> codes2) {
		int i=1;
		for (Barcode code:codes2) {
			code.setPosition(i);
			i++;
		}
	}

}
