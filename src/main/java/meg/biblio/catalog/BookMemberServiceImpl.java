package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.AmazonDetailFinder.NameMatchType;
import meg.biblio.catalog.db.PublisherRepository;
import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.catalog.db.dao.SubjectDao;
import meg.biblio.search.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookMemberServiceImpl implements BookMemberService {

	@Autowired
	SubjectRepository subjectRepo;

	@Autowired
	PublisherRepository pubRepo;

	@Autowired
	SearchService searchService;

	@Override
	public PublisherDao findPublisherForName(String text) {
		if (text != null && text.trim().length()>0) {
			// clean up text
			text = text.trim();
			// query db
			List<PublisherDao> foundlist = pubRepo.findPublisherByName(text
					.toLowerCase());
			if (foundlist != null && foundlist.size() > 0) {
				return foundlist.get(0);
			} else {
				// if nothing found, make new PublisherDao
				PublisherDao pub = new PublisherDao();
				pub.setName(text);
				return pub;
			}
		}
		return null;
	}

	@Override
	public SubjectDao findSubjectForString(String text) {
		if (text != null) {
			// clean up text
			text = text.trim();
			// query db
			List<SubjectDao> foundlist = subjectRepo.findSubjectByText(text
					.toLowerCase());
			if (foundlist != null && foundlist.size() > 0) {
				return foundlist.get(0);
			} else {
				// if nothing found, make new PublisherDao
				SubjectDao pub = new SubjectDao();
				pub.setListing(text);
				return pub;
			}
		}
		return null;
	}

	public void reconcileArtists(ArtistDao newartist,
			List<ArtistDao> targetlist, List<ArtistDao> checklist) {
		reconcileArtists(newartist, targetlist, checklist, false);
	}

	public void reconcileArtists(ArtistDao newartist,
			List<ArtistDao> targetlist, List<ArtistDao> checklist,
			boolean replaceintarget) {
		List<ArtistDao> newtargetlist = new ArrayList<ArtistDao>();
		if (newartist != null) {
			boolean targetexist = false;
			boolean checkexist = false;
			// exists in targetlist?
			if (targetlist != null) {

				for (ArtistDao existart : targetlist) {
					if (targetexist) {
						// just filling in the new list
						newtargetlist.add(existart);
					} else {
						targetexist = artistsMatch(existart, newartist);
						if (targetexist) {
							// check completion - fill in
							ArtistDao artist = checkArtistInfo(existart,
									newartist);
							newtargetlist.add(artist);
						} else {
							// just filling in the new list
							newtargetlist.add(existart);
						}
					}
				}
				// replace target list if change made
				if (targetexist) {
					targetlist.clear();
					targetlist.addAll(newtargetlist);
				}
			}

			// exists in checkexist?
			if (!targetexist || replaceintarget) {
				boolean replacechecklist = false; // to signal delete was
													// made....
				if (checklist != null) {
					List<ArtistDao> newchecklist = new ArrayList<ArtistDao>();
					for (ArtistDao existart : checklist) {
						if (checkexist) {
							// just filling in the new list
							newchecklist.add(existart);
						} else {
							checkexist = artistsMatch(existart, newartist);
							if (checkexist) {
								// in "wrong" list....
								replacechecklist = true;
								// check completion - fill in
								ArtistDao artist = checkArtistInfo(existart,
										newartist);

								if (replaceintarget) {
									// if hadn't been found in targetlist, add
									// to
									// targetlist, and delete from checklist
									// if had been found, delete from checklist,
									// don't add to targetlist
									// note - deleting by not adding to
									// newchecklist
									if (!targetexist) {
										targetlist.add(artist);
									}
								} else {
									newchecklist.add(artist);
								}
							}
						}
					}
					// replace target list if change made
					if (replacechecklist) {
						checklist.clear();
						checklist.addAll(newchecklist);
					}
				}
			}

			// if not present - add to target list
			if (!targetexist && !checkexist) {
				newartist = checkArtistInfo(null, newartist);
				targetlist.add(newartist);
			}
		}
	}

	@Override
	public BookDetailDao insertAuthorsIntoBookDetail(List<String> artists,
			BookDetailDao bookdetail) {
		// split authors into authors and illustrators (somewhat arbitrary, but
		// this is how we'll do it)
		// convert strings to ArtistDaos....
		List<ArtistDao> authors = new ArrayList<ArtistDao>();
		List<ArtistDao> illustrators = new ArrayList<ArtistDao>();
		if (artists != null && artists.size() > 0) {
			if (artists.size() > 1) {
				// last artist goes to illustrators
				String rawname = artists.get(artists.size() - 1);
				ArtistDao ill = textToArtistName(rawname);
				illustrators.add(ill);
				// all others to to author
				artists = artists.subList(0, artists.size() - 1);
				for (String name : artists) {
					ArtistDao auth = textToArtistName(name);
					authors.add(auth);
				}
			} else {
				// one artist listed - put in author list
				String rawname = artists.get(0);
				ArtistDao author = textToArtistName(rawname);
				authors.add(author);
			}

			// get existing authors and illustrators
			List<ArtistDao> existauth = bookdetail.getAuthors();
			List<ArtistDao> existillus = bookdetail.getIllustrators();

			// loop through all authors, calling reconcileArtists
			for (ArtistDao newauthor : authors) {
				reconcileArtists(newauthor, existauth, existillus);
			}
			// loop through all illustrators, calling reconcileArtists
			for (ArtistDao newillus : illustrators) {
				reconcileArtists(newillus, existillus, existauth);
			}

			// set lists in book detail
			bookdetail.setAuthors(existauth);
			bookdetail.setIllustrators(existillus);
		}

		// return book detail
		return bookdetail;
	}

	@Override
	public BookDetailDao addArtistToAuthors(String artist,
			BookDetailDao bookdetail) {
		// split authors into authors and illustrators (somewhat arbitrary, but
		// this is how we'll do it)
		// convert strings to ArtistDaos....
		if (artist != null) {

			ArtistDao newauthor = textToArtistName(artist.trim());

			// get existing authors and illustrators
			List<ArtistDao> existauth = bookdetail.getAuthors();
			List<ArtistDao> existillus = bookdetail.getIllustrators();

			// reconcileArtists - with option to replace if found in "wrong"
			// list
			reconcileArtists(newauthor, existauth, existillus, true);

			// set lists in book detail
			bookdetail.setAuthors(existauth);
			bookdetail.setIllustrators(existillus);
		}

		// return book detail
		return bookdetail;
	}

	@Override
	public BookDetailDao addArtistToIllustrators(String artist,
			BookDetailDao bookdetail) {
		if (artist != null) {
			ArtistDao newauthor = textToArtistName(artist.trim());

			// get existing authors and illustrators
			List<ArtistDao> existauth = bookdetail.getAuthors();
			List<ArtistDao> existillus = bookdetail.getIllustrators();

			// reconcileArtists - with option to replace if found in "wrong"
			// list
			reconcileArtists(newauthor, existillus, existauth, true);

			// set lists in book detail
			bookdetail.setAuthors(existauth);
			bookdetail.setIllustrators(existillus);
		}

		// return book detail
		return bookdetail;
	}

	/**
	 * Transforms format of author "lastname, firstname" into
	 * "firstname lastname"
	 * 
	 * @param artist
	 * @return
	 */
	@Override
	public String normalizeArtistName(String artist) {
		if (artist != null) {
			int commaloc = artist.indexOf(",");
			if (commaloc > 0) {
				String beforecomma = artist.substring(0, commaloc);
				String aftercomma = artist.substring(commaloc + 1);
				return aftercomma.trim() + " " + beforecomma.trim();
			}
		}
		return artist;
	}

	@Override
	public BookDetailDao insertSubjectsIntoBookDetail(
			List<String> subjectstrings, BookDetailDao bookdetail) {
		if (subjectstrings != null && subjectstrings.size() > 0) {

			// get existing list of subjects
			List<SubjectDao> subjects = bookdetail.getSubjects();
			if (subjects == null)
				subjects = new ArrayList<SubjectDao>();

			// set up list of existing subjects - string of subject as key
			List<String> existing = new ArrayList<String>();
			for (SubjectDao subject : subjects) {
				existing.add(subject.getListing().toLowerCase().trim());
			}

			// go through subject strings
			for (String newsubject : subjectstrings) {
				if (!existing.contains(newsubject.toLowerCase().trim())) {
					// if new string doesn't exist in the hash, add it
					// to add it, call findSubjectForString - to pull existing
					// subject by name from db
					SubjectDao newdao = findSubjectForString(newsubject);
					subjects.add(newdao);
				}

			}

			// set subjects in bookdetail
			bookdetail.setSubjects(subjects);
		}
		return bookdetail;

	}

	@Override
	public List<ArtistDao> stringListToArtists(List<String> artiststrings) {
		if (artiststrings!=null) {
			List<ArtistDao> artistlist = new ArrayList<ArtistDao>();
			for (String artist:artiststrings) {
				ArtistDao artistobj = textToArtistName(artist);
				if (artistobj!=null) {
					// see if this is in the db
					ArtistDao dbfound = searchService.findArtistMatchingName(artistobj);
					// if found in db, set db artist in list
					if (dbfound != null) {
						artistlist.add(dbfound);
					} else {
						// if not found in db, set artistobj in list
						artistlist.add(artistobj);
					}
				}
			}
			return artistlist;
		}
		
		return null;
	}
	
	@Override
	public List<SubjectDao> stringListToSubjects(List<String> subjectstrings) {
		if (subjectstrings!=null) {
			List<SubjectDao> subjectlist = new ArrayList<SubjectDao>();
			for (String subject:subjectstrings) {
				SubjectDao subjectobj = findSubjectForString(subject);
				if (subjectobj!=null) {
					subjectlist.add(subjectobj);
				}
			}
			return subjectlist;
		}
		
		return null;
	}
	
	@Override
	public ArtistDao textToArtistName(String text) {
		ArtistDao name = new ArtistDao();
		boolean nonempty = text != null && text.trim().length() > 0;
		if (nonempty) {
			if (text.contains(",")) {
				// break text by comma
				String[] tokens = text.trim().split(",");
				List<String> tknlist = arrayToList(tokens);
				// first member goes to last name
				String lastname = tknlist.remove(0);
				name.setLastname(lastname);
				if (tknlist.size() > 0) {
					// break remaining by space
					String remaining = tknlist.get(0);
					tokens = remaining.trim().split(" ");
					tknlist = arrayToList(tokens);
					// first member goes to first name
					String firstname = tknlist.remove(0);
					name.setFirstname(firstname);
					// any remaining members go to middle name
					if (tknlist.size() > 0) {
						String middlename = tknlist.remove(0);
						name.setMiddlename(middlename);
					}
				}
			} else {
				// break name into list
				String[] tokens = text.trim().split(" ");
				List<String> tknlist = arrayToList(tokens);
				// last member of list is last name
				String lastname = tknlist.remove(tknlist.size() - 1);
				name.setLastname(lastname);
				// if members remaining, first member is firstname
				if (tknlist.size() > 0) {
					String firstname = tknlist.remove(0);
					name.setFirstname(firstname);
					if (tknlist.size() > 0) {
						// all remaining go to middlename
						String middlename = tknlist.remove(0);
						name.setMiddlename(middlename);
					}
				}
			}
			// return name
			return name;
		}
		// return name
		return null;
	}

	private ArtistDao checkArtistInfo(ArtistDao existart, ArtistDao newart) {
		boolean existmorecomplete = false;
		if (existart != null) {
	
			existmorecomplete = (existart.hasFirstname() && !newart
					.hasFirstname());
			existmorecomplete |= (existart.hasMiddlename() && !newart
					.hasMiddlename());
			existmorecomplete |= (existart.hasFirstname()
					&& newart.hasFirstname() && newart.getFirstname().length() < existart
					.getFirstname().length());
		}
		// if more complete, set in new list (author or
		// illustrator)
		if (!existmorecomplete) {
			// see if this is in the db
			ArtistDao dbfound = searchService.findArtistMatchingName(newart);
			// if found in db, set db artist in list
			if (dbfound != null) {
				return dbfound;
			} else {
				// if not found in db, return foundart
				return newart;
			}
		}
		return existart;
	}

	private boolean artistsMatch(ArtistDao existart, ArtistDao newartist) {
		boolean targetexist = false;
		// check if lastnames match
		if (namesMatch(NameMatchType.LASTNAME, existart, newartist)) {
			// if last names match, check that first initials
			// match
			if (namesMatch(NameMatchType.FIRSTINITIAL, existart, newartist)) {
				// first initial and lastname equals enough to
				// match
				targetexist = true;
			} else if (!existart.hasFirstname() || !newartist.hasFirstname()) {
				// either book or found doesn't have first name
				// - will still
				// match
				targetexist = true;
			}
		}
		return targetexist;
	}

	private boolean namesMatch(long matchtype, ArtistDao bookauth,
			ArtistDao foundauthor) {
		String bookval = null;
		String compareval = null;
	
		if (matchtype == NameMatchType.LASTNAME) {
			bookval = bookauth.getLastname() != null ? bookauth.getLastname()
					: "";
			compareval = foundauthor.getLastname() != null ? foundauthor
					.getLastname() : "";
		} else if (matchtype == NameMatchType.FIRSTINITIAL) {
			bookval = bookauth.getFirstname() != null ? bookauth.getFirstname()
					: "";
			compareval = foundauthor.getFirstname() != null ? foundauthor
					.getFirstname() : "";
		}
		bookval = bookval.trim().toLowerCase();
		compareval = compareval.trim().toLowerCase();
	
		// make comparison
		if (matchtype == NameMatchType.LASTNAME) {
			return (bookval.equals(compareval));
		} else if (matchtype == NameMatchType.FIRSTINITIAL) {
			bookval = bookval.length() > 0 ? bookval.substring(0, 1) : "";
			compareval = compareval.length() > 0 ? compareval.substring(0, 1)
					: "";
			return (bookval.equals(compareval));
		}
		return false;
	}

	private List<String> arrayToList(String[] tokens) {
		List<String> list = new ArrayList<String>();
		if (tokens != null) {
			for (int i = 0; i < tokens.length; i++) {
				list.add(tokens[i]);
			}
		}
		return list;
	}
}
