package helperClasses;

import java.util.logging.Level;

/**
 * Defines some constants to be used in this project.
 * @author Severin Wischmann <wiseveri@student.ethz.ch>
 *
 */
public interface Constants {
	public final static int PORT = 3456;
	public final static String db_name = "asl_hs13_g13";
	public final static String schema_path = "..\\..\\..\\specifications\\DBscheme.sql";
	public final static int db_port = 5432;
	public final static String db_user = "postgres";
	public final static String db_password = "1111";
	public final static int db_con_pool_size = 100;
	
	public final static Level WARNING = Level.WARNING;
	public final static Level ERROR = Level.SEVERE;
	public final static Level STAT_MSG = Level.INFO;
	
	public final static String OK = "OK";
}
