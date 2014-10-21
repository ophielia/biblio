package meg.biblio.search;

import meg.biblio.catalog.db.dao.ArtistDao;


public interface SearchService {

	public ArtistDao findArtistMatchingName(ArtistDao tomatch);
}