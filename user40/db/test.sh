#!/bin/bash

superuser=postgres
sqlfile=db_setup.sql
host=localhost
postgres_path="/etc/init.d/postgresql"

export PGUSER={$PGUSER-${USER}}
export PGDATABASE={$PGDATABASE-db}
export PGPASSWORD={$PGPASSWORD-''}

set -u 
if [ "$DEBUG" == "1" ] 
then
	set -v 
	set -x 
fi

# Start Postgres data server
sudo $postgres_path restart
psql $database -c '\q' 2>&1
if [ $? -eq 0 ]
then
    dropdb -i 
    createdb -O ${PGUSER} 
fi

psql \
    -X \
    -f $sqlfile \
    --echo-all \
    --set AUTOCOMMIT=off \
    --set ON_ERROR_STOP=on \
    
status=$?
if [ "$status" -ne 0 ]
then
    echo "ERROR executing psql with file $sqlfile"
    exit $status
fi

echo "DATABASE successfully set up....Proceed."
exit 0
