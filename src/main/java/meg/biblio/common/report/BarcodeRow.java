package meg.biblio.common.report;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import meg.biblio.common.db.dao.ClientDao;

@XmlRootElement(name = "barcoderow")
public class BarcodeRow {

List<Barcode> members;


public BarcodeRow() {
	super();
	this.members = new ArrayList<Barcode>();
}

public void addBarcode(Barcode barcode) {
	this.members.add(barcode);
}

@XmlElement(name = "barcode")
public List<Barcode> getMembers() {
	return members;
}





}
