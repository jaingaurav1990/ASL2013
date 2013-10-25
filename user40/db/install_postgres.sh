#!/bin/bash

set -x
PGUSER="user13"
PGDB="/home/user13/db/"
HOST="dryad05.ethz.ch"
PREFIX=$PGDB"pgsql/"
PGDATA=$PREFIX"data/"
BIN=$PREFIX"bin/"

ssh $PGUSER@$HOST "mkdir -p /home/user13/db/pgsql/data"
scp postgresql-9.1.9.tar.gz $PGUSER@$HOST:$PGDB
ssh $PGUSER@$HOST "cd $PGDB; tar -zxf postgresql-9.1.9.tar.gz; cd postgresql-9.1.9; ./configure --prefix=$PREFIX; \" 
    make; \
    make install "

echo "PATH=$BIN:\$PATH
export PATH" | ssh $PGUSER@$HOST "cat >> ~/.bashrc"
ssh $PGUSER@$HOST "PATH=$BIN; \
    initdb -D $PGDATA"

