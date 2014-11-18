package meg.biblio.catalog;

import meg.biblio.catalog.CatalogService.BookType;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;

public class MareschaleClassifier implements Classifier {

	public static final class ClassKey {
		public static final Long math = 1L;
		public static final Long peoplesactivities = 2L;
		public static final Long art = 3L;
		public static final Long humanbody = 4L;
		public static final Long englishbooks = 5L;
		public static final Long italianbooks = 6L;
		public static final Long germanbooks = 7L;
		public static final Long dictionaries = 8L;
		public static final Long philosophy = 9L;
		public static final Long fairytales = 10L;
		public static final Long birds = 11L;
		public static final Long insects = 12L;
		public static final Long mammals = 13L;
		public static final Long sea = 14L;
		public static final Long riveranimals = 15L;
		public static final Long differentanimals = 16L;
		public static final Long farmanimals = 17L;
		public static final Long history = 18L;
		public static final Long geography = 19L;
		public static final Long treesandplants = 20L;
		public static final Long waterair = 21L;
		public static final Long earth = 22L;
		public static final Long fict_ab = 23L;
		public static final Long fict_cd = 24L;
		public static final Long fict_efg = 25L;
		public static final Long fict_hijkl = 26L;
		public static final Long fict_mnop = 27L;
		public static final Long fict_qrst = 28L;
		public static final Long fict_uvwxyz = 29L;
		public static final Long fict_boardbooks = 30L;
		public static final Long fict_comicbooks = 31L;

	}

	@Override
	public BookDao classifyBook(BookDao book) {
		// only a classification guess. will need to be verified
		// unless a book is marked as non-fiction, it's classified as
		// fiction, since that's the only classification that can really
		// be done automatically. If it's non-fiction, the classification is
		// left blank
		Long classification = null;

		// don't classify if it has a shelfclass which has been verified
		boolean dontprocess = book.getShelfclassverified() != null
				&& book.getShelfclassverified();
		if (!dontprocess) {

			// get book type
			Long booktype = book.getType();
			if (booktype != null) {
				if (book.getLanguage() != null
						&& !book.getLanguage().equals("fr")) {
					String language = book.getLanguage();
					// english to english
					if (language.equals("en")) {
						classification = ClassKey.englishbooks;
						book.setType(new Long(BookType.FOREIGNLANGUAGE));
					} else if (language.equals("de")) {
						classification = ClassKey.germanbooks;
						book.setType(new Long(BookType.FOREIGNLANGUAGE));
					} else if (language.equals("it")) {
						classification = ClassKey.italianbooks;
						book.setType(new Long(BookType.FOREIGNLANGUAGE));
					}
				} else

				// if not non-fiction - classify according to author's last name
				if (booktype != CatalogService.BookType.NONFICTION
						&& booktype != CatalogService.BookType.REFERENCE) {
					// catalog as fiction
					ArtistDao author = book.getAuthors() != null
							&& book.getAuthors().size() > 0 ? book.getAuthors()
							.get(0) : null;
					if (author != null && author.getLastname() != null) {
						String lastname = author.getLastname();
						String firstinit = lastname.substring(0, 1).trim()
								.toLowerCase();
						if (firstinit.equals("a") || firstinit.equals("b")) {
							classification = ClassKey.fict_ab;
						} else if (firstinit.equals("c")
								|| firstinit.equals("d")) {
							classification = ClassKey.fict_cd;
						} else if (firstinit.equals("e")
								|| firstinit.equals("f")
								|| firstinit.equals("g")) {
							classification = ClassKey.fict_efg;
						} else if (firstinit.equals("h")
								|| firstinit.equals("i")
								|| firstinit.equals("j")) {
							classification = ClassKey.fict_hijkl;
						} else if (firstinit.equals("k")
								|| firstinit.equals("l")) {
							classification = ClassKey.fict_hijkl;
						} else if (firstinit.equals("m")
								|| firstinit.equals("n")) {
							classification = ClassKey.fict_mnop;
						} else if (firstinit.equals("o")
								|| firstinit.equals("p")) {
							classification = ClassKey.fict_mnop;
						} else if (firstinit.equals("q")
								|| firstinit.equals("r")) {
							classification = ClassKey.fict_qrst;
						} else if (firstinit.equals("s")
								|| firstinit.equals("t")) {
							classification = ClassKey.fict_qrst;
						} else if (firstinit.equals("u")
								|| firstinit.equals("v")) {
							classification = ClassKey.fict_uvwxyz;
						} else if (firstinit.equals("w")
								|| firstinit.equals("x")) {
							classification = ClassKey.fict_uvwxyz;
						} else if (firstinit.equals("y")
								|| firstinit.equals("z")) {
							classification = ClassKey.fict_uvwxyz;
						}
					}
				}
				book.setShelfclass(classification);
			}
		}
		return book;
	}
}
