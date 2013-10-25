BEGIN;
CREATE TABLE queues (
		id		serial primary key
);

CREATE TABLE clients (
		id 		serial primary key,
        can_send boolean,
        can_receive boolean
);

CREATE TABLE messages (
		mid 	serial primary key,
		qid		integer,
		msg		varchar(2000),
		pri		smallint not null,
		type	char(1) not null,
		sender	integer,
		receiver integer,
		tstamp	timestamp not null
);	
COMMIT;
