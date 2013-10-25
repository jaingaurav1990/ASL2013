package db;

/**
 * This class will be used for automating the deployment of new changes in DB.
 * It is still not working properly though. You can use the below shell command 
 * for re-creating tables in the database in the meantime.
 * "psql -U db_user -d db_name -h db_host -f schema_path"
 * The parameters can be found in Constants interface. 
 */
import helperClasses.Constants;

import java.io.*;

public class Init_db implements Constants {

	public static void main(String argv[]) {
		try {
			String line;
			Process p = Runtime.getRuntime().exec(
					"psql -U " + db_user + " -d " + db_name + " -h " + db_host
							+ " -f " + schema_path);
			BufferedReader input = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}