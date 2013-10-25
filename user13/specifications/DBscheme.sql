DROP TABLE IF EXISTS client, queue, message;
DROP FUNCTION IF EXISTS createClient(OUT integer);
DROP FUNCTION IF EXISTS createQueue(OUT integer);
DROP FUNCTION IF EXISTS deleteQueue(integer);
DROP FUNCTION IF EXISTS insertMessageOne(integer,integer,integer,integer,integer,character varying);
DROP FUNCTION IF EXISTS insertMessageMany(integer,integer,integer[],integer,integer,character varying);
DROP FUNCTION IF EXISTS readMessage(integer, integer);
DROP FUNCTION IF EXISTS getMessage(integer, integer);
DROP FUNCTION IF EXISTS getMessage(integer, integer, integer);
DROP FUNCTION IF EXISTS getMessageTime(integer, integer);
DROP FUNCTION IF EXISTS getMessageTime(integer, integer, integer);
DROP FUNCTION IF EXISTS deleteMessage(integer);
DROP FUNCTION IF EXISTS listQueue();
DROP FUNCTION IF EXISTS listQueues(integer);
DROP FUNCTION IF EXISTS listUsers();

CREATE TABLE IF NOT EXISTS client (
	id serial primary key
);

CREATE TABLE IF NOT EXISTS queue (
	id serial primary key
);

CREATE TABLE IF NOT EXISTS message (
	id serial primary key,
	sender_id int references client(id) NOT NULL,
	receiver_id int references client(id) DEFAULT NULL,
	queue_id int references queue(id) NOT NULL,
	context int DEFAULT 0,
	priority int DEFAULT 1 NOT NULL,
	arrive_time timestamp DEFAULT current_timestamp,
	text varchar(2000) NOT NULL
);

/* Explicit indexes */

CREATE INDEX arrive_time_idx ON message (arrive_time);
CREATE INDEX priority_idx ON message (priority);
CREATE INDEX sender_idx ON message (sender_id);
CREATE INDEX queue_idx ON message (queue_id);
CREATE INDEX receiver_idx ON message (receiver_id);
CREATE INDEX context_idx ON message (context);

/* Functions */

CREATE OR REPLACE FUNCTION createClient(OUT id   INT)
RETURNS INT 
AS 'INSERT INTO client values(default) RETURNING id'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION createQueue(OUT id   INT)
RETURNS INT
AS 'INSERT INTO queue values(default) RETURNING id'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION deleteQueue(id INT = NULL)
RETURNS INT
AS 'DELETE FROM queue WHERE id=$1 RETURNING id'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION insertMessageOne(sender_id INT, receiver_id INT, queue_id INT, context INT, priority INT, text varchar(2000))
RETURNS INT
AS 'INSERT INTO message(sender_id, receiver_id, queue_id, context, priority, text) values($1,$2,$3,$4,$5,$6) RETURNING id'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION insertMessageMany(sender_id INT, receiver_id INT, queue_ids integer[], context INT, priority INT, text varchar(2000))
RETURNS TABLE(
	id INT
) AS $$
DECLARE
	q INT;
BEGIN
	FOREACH q IN ARRAY $3
	LOOP
		INSERT INTO message(sender_id, receiver_id, queue_id, context, priority, text) VALUES ($1, $2, q, $4, $5, $6) RETURNING id;
	END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION readMessage(queue_id INT, receiver_id INT)
RETURNS TABLE(
	id INT,
	sender_id INT,
	receiver_id INT,
	queue_id INT,
	context INT,
	priority INT,
	arrive_time timestamp,
	text varchar(2000)
)
AS 'SELECT * FROM message WHERE queue_id=$1 AND (receiver_id=$2 OR receiver_id IS NULL)'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getMessage(queue_id INT, receiver_id INT)
RETURNS TABLE(
	id INT,
	sender_id INT,
	receiver_id INT,
	queue_id INT,
	context INT,
	priority INT,
	arrive_time timestamp,
	text varchar(2000)
)
AS 'DELETE FROM message WHERE ctid=(
	SELECT ctid FROM message WHERE queue_id=$1 AND (receiver_id=$2 OR receiver_id IS NULL) and context IS NULL ORDER BY priority LIMIT 1
	) RETURNING id,sender_id,receiver_id,queue_id,context,priority,arrive_time,text'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getMessage(queue_id INT, receiver_id INT, context INT)
RETURNS TABLE(
	id INT,
	sender_id INT,
	receiver_id INT,
	queue_id INT,
	context INT,
	priority INT,
	arrive_time timestamp,
	text varchar(2000)
)
AS 'DELETE FROM message WHERE ctid=(
	SELECT ctid FROM message WHERE queue_id=$1 AND (receiver_id=$2 OR receiver_id IS NULL) and context=$3 ORDER BY priority LIMIT 1
	) RETURNING id,sender_id,receiver_id,queue_id,context,priority,arrive_time,text'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getMessageTime(queue_id INT, receiver_id INT)
RETURNS TABLE(
	id INT,
	sender_id INT,
	receiver_id INT,
	queue_id INT,
	context INT,
	priority INT,
	arrive_time timestamp,
	text varchar(2000)
)
AS 'DELETE FROM message WHERE ctid=(
	SELECT ctid FROM message WHERE queue_id=$1 AND (receiver_id=$2 OR receiver_id IS NULL) and context IS NULL ORDER BY arrive_time LIMIT 1
	) RETURNING id,sender_id,receiver_id,queue_id,context,priority,arrive_time,text'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getMessageTime(queue_id INT, receiver_id INT, context INT)
RETURNS TABLE(
	id INT,
	sender_id INT,
	receiver_id INT,
	queue_id INT,
	context INT,
	priority INT,
	arrive_time timestamp,
	text varchar(2000)
)
AS 'DELETE FROM message WHERE ctid=(
	SELECT ctid FROM message WHERE queue_id=$1 AND (receiver_id=$2 OR receiver_id IS NULL) and context=$3 ORDER BY arrive_time LIMIT 1
	) RETURNING id,sender_id,receiver_id,queue_id,context,priority,arrive_time,text'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION deleteMessage(id INT = NULL)
RETURNS INT
AS 'DELETE FROM message WHERE id=$1 RETURNING id'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION listQueue()
RETURNS TABLE(
	id INT
)
AS 'SELECT id FROM queue'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION listQueues(id INT)
RETURNS TABLE(
	id INT
)
AS 'SELECT DISTINCT id FROM message WHERE (receiver_id=$1 OR receiver_id IS NULL)'
LANGUAGE SQL;

CREATE OR REPLACE FUNCTION listUsers()
RETURNS TABLE(
	id INT
)
AS 'SELECT id FROM client'
LANGUAGE SQL;
