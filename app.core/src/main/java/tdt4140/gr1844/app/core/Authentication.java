package tdt4140.gr1844.app.core;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;

/**
* Contains the methods for handling/mutating the user's login state.
*/
public class Authentication {
	/**
	* Logs the user in.
	* @param onlineOrOffline Check if the database is online or offline.
	* @param username The user's username.
	* @param password The user's password.
	* @return {@code true} if the login was successful, {@code false} otherwise.
	*/
	public static boolean login(boolean onlineOrOffline, String username, String password) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		try {
            SqlConnect conn = new SqlConnect();
			//Retrieve the users salt from the database, if no salt is returned then the user doesn't exist
			PreparedStatement saltRetrieval = conn.connect(onlineOrOffline).prepareStatement("select salt from users where email = ?");
			saltRetrieval.setString(1, username);
			saltRetrieval.execute();
			ResultSet retrievedSalt = saltRetrieval.getResultSet();
			
			//Check if any salt was returned by the query. If no salt was returned then the user doesn't exst and return false
			if (!retrievedSalt.isBeforeFirst()) {
				System.out.println("Email error error");
				return false;
			}
			retrievedSalt.next();
            //use salt and password to make a password hash
			String salt = retrievedSalt.getString("salt");
			String passwordHash = BCrypt.hashpw(password, salt);
			
			saltRetrieval.close();
			//Make a query that checks if the user and password is valid
			PreparedStatement authenticationQuery = conn.connect(onlineOrOffline).prepareStatement("select * from users where (email = ? and passwordHash = ?)");
			authenticationQuery.setString(1, username);
			authenticationQuery.setString(2, passwordHash);
			authenticationQuery.execute();
			ResultSet authenticationResponse = authenticationQuery.getResultSet();
			//If query returned any results then username and password were correct and returns true, else it returns false
			Boolean validLogin = authenticationResponse.isBeforeFirst();
			authenticationQuery.close();
			conn.disconnect();
			return validLogin;
		}
		catch(SQLException e){
			//conn.disconnect();
			System.err.println(e);
			return false;
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 *
	 * @param onlineOrOffline Check if the database is online or offline.
	 * @param cookie Session Cookie for the user that we want to logout
	 * @return {@code true} if the login was successful, {@code false} otherwise.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static Boolean logout(boolean onlineOrOffline, String cookie) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		try {
			SqlConnect conn = new SqlConnect();
			PreparedStatement statement = conn.connect(onlineOrOffline).prepareStatement("update users set cookie = null where cookie = ?");
			statement.setString(1, cookie);
			int updateCheck = statement.executeUpdate();
			//Returns true if something was updated
			conn.disconnect();
			return updateCheck > 0;
		}
		catch(SQLException e) {
			System.err.println(e);
			return false;
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return true;
	}
}