import logger
from plumbum import local
from plumbum import SshMachine
from time import strftime

REMOTE_HOME = "/home/user40/"
REMOTE_SCRIPTS = REMOTE_HOME + "scripts/"
REMOTE_POSTGRES = REMOTE_SCRIPTS + "postgresql"

class Test:
    def __init__(self, dbNode, middlewareNodeList, clientNodeList):
        self.id = strftime("%Y-%m-%d %H:%M:%S") 
        self.dbNode = dbNode

    def initializeDBTier(self):
        # Run postgresql Server on remote machine 'dbNode'
        remote = SshMachine(self.dbNode, user = "user40")
        logger.info("Remote connection established to dbNode")
        r_postgres = remote[REMOTE_POSTGRES]
        r_postgres("restart")
        r_postgres("reload")
        logger.info("Remote postgresql server restarted")

        print remote.cwd
        r_ls = remote["ls"]
        print r_ls()
        remote.close()
                

t = Test("dryad01.ethz.ch", [], [])
t.initializeDBTier()


'''
ant = local["ant"]
bash = local["bash"]
# ant("create-db")
bash("db/db_setup.sh")
ant("clean")
ant("all")
print "====================Finished Build and JUnit Tests==========================="
'''

