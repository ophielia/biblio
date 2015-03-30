package meg.biblio.catalog;

import java.util.List;

import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;

public interface BookMemberService {

	public ArtistDao textToArtistName(String text);

	PublisherDao findPublisherForName(String text);

	SubjectDao findSubjectForString(String text);

	BookDetailDao insertAuthorsIntoBookDetail(List<String> artists,
			BookDetailDao bookdetail);

	BookDetailDao addArtistToAuthors(String artist, BookDetailDao bookdetail);

	BookDetailDao addArtistToIllustrators(String artist,
			BookDetailDao bookdetail);

	BookDetailDao insertSubjectsIntoBookDetail(List<String> subjectstrings,
			BookDetailDao bookdetail);

	String normalizeArtistName(String artist);

	List<ArtistDao> stringListToArtists(List<String> artiststrings);
	
	List<SubjectDao> stringListToSubjects(List<String> subjectstrings);




}
