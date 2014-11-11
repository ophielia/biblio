package meg.biblio.catalog.web.model;

public class BookImportModel {

	private String contentType;
	private Integer importListSize;
	private Integer importedSize;
	private Integer TotalErrorsSize;
	private Integer DuplicatesSize;
	private Integer noIdSize;
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
this.contentType=contentType;
		
	}

	public Integer getImportListSize() {
		return importListSize;
	}

	public Integer getImportedSize() {
		return importedSize;
	}

	public void setImportListSize(Integer integer) {
		this.importListSize = integer;
	}

	public void setImportedSize(Integer integer) {
		this.importedSize = integer;
		
	}

	public Integer getTotalErrorsSize() {
		return TotalErrorsSize;
	}

	public void setTotalErrorsSize(Integer integer) {
		this.TotalErrorsSize = integer;
		
	}

	public Integer getDuplicatesSize() {
		return DuplicatesSize;
	}

	public void setDuplicatesSize(Integer integer) {
		this.DuplicatesSize = integer;
		
	}

	public Integer getNoIdSize() {
		return noIdSize;
	}

	public void setNoIdSize(Integer integer) {
		this.noIdSize = integer;
		
	}

}
