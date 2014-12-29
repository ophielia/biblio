package meg.biblio.common;

import java.util.Locale;

import meg.biblio.common.report.BarcodeSheet;
import meg.biblio.lending.db.dao.SchoolGroupDao;



public interface BarcodeService {

	public final static class CodeType {
		public static final String BOOK = "B";
		public static final String PERSON = "A";
	}

	BarcodeSheet assembleBarcodeSheetForClass(SchoolGroupDao schoolgroup,
			Long clientid);

	BarcodeSheet assembleBarcodeSheetForBooks(int barcodecnt, Long clientid,
			Locale locale);
}
