/**  app setting **/
INSERT INTO appsetting(id, key, value, version) VALUES (nextval('hibernate_sequence'), 'biblio.testclient', '1', 0);
INSERT INTO appsetting(id, key, value, version) VALUES (nextval('hibernate_sequence'), 'biblio.inventory.showtoreconcile', '15', 0);

/** new select keys / values **/
insert into select_key (id, lookup,version) values (nextval('hibernate_sequence'),'reconcilestatus',0);
insert into select_value (id, active,display,disporder,value,version,keyid,languagekey) 
select nextval('hibernate_sequence'), active,display,disporder,value,version,(select id from select_key where lookup='reconcilestatus'),languagekey
from select_value where keyid = (select id from select_key where lookup = 'bookstatus') and value in ('3','4','5')


/** new column in view **/
CREATE OR REPLACE VIEW loanrecorddisplay AS 
 SELECT r.returned IS NOT NULL AND r.returned > r.duedate AS returnedlate,
    r.returned IS NULL AND now() > r.duedate AS currentlyoverdue,
    r.returned IS NULL AS currentlycheckedout,
    r.returned IS NOT NULL AND r.returned > r.duedate OR r.returned IS NULL AND now() > r.duedate AS overdue,
    r.id AS loanrecordid,
    r.borrower AS borrowerid,
    r.client AS clientid,
    r.book AS bookid,
    p.schoolgroup AS classid,
    p.firstname AS borrowerfn,
    p.lastname AS borrowerln,
    r.borrowersection as borrowersection,
    bd.title AS booktitle,
    b.clientbookid AS bookclientid,
    b.clientbookidsort AS bookclientidsort,
    concat(concat(auth.firstname, ' '), auth.lastname) AS author,
    b.clientshelfcode AS shelfclass,
    r.checkoutdate AS checkedout,
    r.returned,
    r.duedate,
    tch.firstname AS teacherfirstname,
    tch.lastname AS teacherlastname,
    p.psn_type::text = 'TeacherDao'::text AS isteacher
   FROM loanrecord r
   JOIN book b ON r.book = b.id
   JOIN bookdetail bd ON bd.id = b.bookdetail
   JOIN person p ON p.id = r.borrower
   LEFT JOIN person tch ON r.teacherid = tch.id AND tch.psn_type::text = 'TeacherDao'::text
   LEFT JOIN book_author ba ON ba.bookdetail_id = bd.id AND ba.authororder = 0
   LEFT JOIN artist auth ON auth.id = ba.artist_id;

ALTER TABLE loanrecorddisplay
  OWNER TO postgres;
GRANT ALL ON TABLE loanrecorddisplay TO postgres;
GRANT ALL ON TABLE loanrecorddisplay TO appuser_bookcatalog;
