#!/bin/bash

# Use 'trust' authentication method in pg_hba.conf file for this
# script to not prompt for password every now and then
# Add following to your pg_hba.conf
# local all all trust
superuser=postgres
sqlfile="db/db_setup.sql"
postgres_path="/etc/init.d/postgresql"

export PGUSER=${PGUSER-${USER}}
export PGDATABASE=${PGDATABASE-db}
export DEBUG=${DEBUG-1}
#export PGPASSWORD={$PGPASSWORD-'abcdefgh'}

set -u 
if [ "$DEBUG" == "1" ] 
then
	set -v 
	set -x 
fi

# Start Postgres data server
sudo $postgres_path restart
sudo $postgres_path reload # For some weird reason, restart ain't enough

# Check if the role ${PGUSER} exists or not
sudo su postgres -c "createuser --createdb --no-createrole --no-superuser --no-password ${PGUSER}" 2>/dev/null

psql ${PGDATABASE} -c '\q' 2>&1
if [ $? -eq 0 ]
then
    dropdb ${PGDATABASE}
fi

createdb -O ${PGUSER} ${PGDATABASE}

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
