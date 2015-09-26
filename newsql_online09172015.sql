/**  lending key **/
insert into select_key (id, lookup,version) values (nextval('hibernate_sequence'),'lendtypeselect',0);

insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'All',1,'1',true,'en',0,(select id from select_key where lookup='lendtypeselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'Checked out',2,'2',true,'en',0,(select id from select_key where lookup='lendtypeselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'Overdue',3,'3',true,'en',0,(select id from select_key where lookup='lendtypeselect'));

insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'Toutes',1,'1',true,'fr',0,(select id from select_key where lookup='lendtypeselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'Emprunté',2,'2',true,'fr',0,(select id from select_key where lookup='lendtypeselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'En retard',3,'3',true,'fr',0,(select id from select_key where lookup='lendtypeselect'));


/** class select */
insert into select_key (id, lookup,version) values (nextval('hibernate_sequence'),'classselect',0);

insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'All',1,'1',true,'en',0,(select id from select_key where lookup='classselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'Toutes',1,'1',true,'fr',0,(select id from select_key where lookup='classselect'));


/** time period select */
insert into select_key (id, lookup,version) values (nextval('hibernate_sequence'),'timeperiodselect',0);

insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'This Week',1,'1',true,'en',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'This Month',2,'2',true,'en',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'Last Three Months',3,'3',true,'en',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'School Year',4,'4',true,'en',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'All',5,'5',true,'en',0,(select id from select_key where lookup='timeperiodselect'));

insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'cette semaine',1,'1',true,'fr',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'ce mois',2,'2',true,'fr',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'trois derniers mois',3,'3',true,'fr',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'année Scolaire',4,'4',true,'fr',0,(select id from select_key where lookup='timeperiodselect'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'tous',5,'5',true,'fr',0,(select id from select_key where lookup='timeperiodselect'));

/** time period select */
insert into select_key (id, lookup,version) values (nextval('hibernate_sequence'),'schoolyear',0);

insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'School Year',1,'1',true,'en',0,(select id from select_key where lookup='schoolyear'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'année Scolaire',1,'1',true,'fr',0,(select id from select_key where lookup='schoolyear'));


/** new view **/
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
    tch.psn_type::text = 'TeacherDao'::text AS isteacher
   FROM loanrecord r
   JOIN book b ON r.book = b.id
   JOIN bookdetail bd ON bd.id = b.bookdetail
   JOIN person p ON p.id = r.borrower
   LEFT JOIN person tch ON r.teacherid = tch.id AND tch.psn_type::text = 'TeacherDao'::text
   LEFT JOIN book_author ba ON ba.bookdetail_id = bd.id AND ba.authororder = 0
   LEFT JOIN artist auth ON auth.id = ba.artist_id;


/** correction select_key / select_value **/
update select_value set display ='lost by borrower' where id = (select id from select_value where keyid=(select id from select_key where lookup='bookstatus')
and display='lost by lender' and languagekey='en');
update select_value set display ='perdu par l''emprunteur' where id = (select id from select_value where keyid=(select id from select_key where lookup='bookstatus')
and display='lost by lender' and languagekey='fr');
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'In Repair',7,'7',true,'en',0,(select id from select_key where lookup='bookstatus'));
insert into select_value (id, display,disporder,value,active,languagekey,version,keyid) values (nextval('hibernate_sequence'),'dans la réparation',7,'7',true,'fr',0,(select id from select_key where lookup='bookstatus'));

/** correction - non-fiction categories to non-fiction  book type **/
update  book set clientbooktype = 2 where clientshelfcode in (1,2,4,9,11,12,13,14,15,16,17,18,19,20,21,22,22) and clientbooktype <> 2 and clientid = 68;
update  book set clientbooktype = 3 where clientshelfcode in (8) and clientbooktype <> 2 and clientid = 68;

/** adding norwegian flag **/
INSERT INTO classification( id, clientid, description, version, imagedisplay, key, language, textdisplay) VALUES (nextval('hibernate_sequence'), (select id from client where name like '%Mareschale%'), 'norwegian books', 0, '/images/mareschale/classification/norwegianflag.gif', 32, 'en','norwegian flag' );