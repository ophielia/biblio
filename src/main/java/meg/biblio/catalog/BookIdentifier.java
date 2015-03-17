package meg.biblio.catalog;

public class BookIdentifier {

	
	String isbn;
	String ean;
	Long publishyear;
	private String ark;
	
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getEan() {
		return ean;
	}
	
	public String getArk() {
		return ark;
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
	public void setArk(String ark) {
		this.ark = ark;
	}
	
	
}
