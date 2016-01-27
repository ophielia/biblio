package meg.biblio.common.report;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "barcodes")
public class BarcodeSheet {

	List<Barcode> codes;
	String title;
	int columncount;
	int border=0;
	int nudge=0;
	



	public BarcodeSheet(List<Barcode> codes, String title, int offset) {
		this.codes = codes;
		this.title = title;
		numberCodes(codes,offset);
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


	private void numberCodes(List<Barcode> codes2,int offset) {
		List<Barcode> paddedlist = new ArrayList<Barcode>();
		int i=1;
		for (int j=0;j<offset;j++) {
			Barcode dummy = new Barcode();
			dummy.setCode("dummy");
			dummy.setPosition(i);
			paddedlist.add(dummy);
			i++;
		}
		for (Barcode code:codes2) {
			code.setPosition(i);
			paddedlist.add(code);
			i++;
		}
		this.codes = paddedlist;
	}
	
	@XmlElement
	public int getBorder() {
		return border;
	}

	public void setBorder(int border) {
		this.border = border;
	}

	@XmlElement
	public int getNudge() {
		return nudge;
	}

	public void setNudge(int nudge) {
		this.nudge = nudge;
	}

}
