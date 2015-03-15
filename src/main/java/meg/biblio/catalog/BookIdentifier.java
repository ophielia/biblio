package meg.biblio.catalog;

public class BookIdentifier {

	
	String isbn;
	String ean;
	Long publishyear;
	
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getEan() {
		return ean;
	}
	public void setEan(String ean) {
		this.ean = ean;
	}
	public Long getPublishyear() {
		return publishyear;
	}
	public void setPublishyear(Long publishyear) {
		this.publishyear = publishyear;
	}
	
	
}
