package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Database that stores the player details of authorised players.
 */
public final class UserDatabase {
	protected static final List<PlayerDetails> players = new ArrayList<>(
			Arrays.asList(new PlayerDetails("1", "[1]"), new PlayerDetails("Ryan", "[1, 2, 3]"),
					new PlayerDetails("Bevan", "[1, 2, 3, 4]"), new PlayerDetails("Tony", "[1, 2, 3, 4, 5]")));

	/**
	 * CONSTRUCTOR for user database.
	 */
	public UserDatabase() {
	}

	/**
	 * Thread safe method that compares provided player details to user database.
	 * 
	 * @param playerDetails an object containing player password and username
	 * @param encryptionKey
	 * @return true if the player was found in the DB
	 */
	public synchronized boolean authenticate(PlayerDetails playerDetails, int encryptionKey) {
		// console print for debug
		System.out.println("In UserDataBase.authenticate() - username is " + playerDetails.getUsername()
				+ " password is " + playerDetails.getPassword());
		// decrypt player details
		PlayerDetails decryptedPlayerDetails = playerDetails.decrypt(encryptionKey);
		// console print for debug
		System.out.println(
				"In UserDataBase.authenticate() - decrypted username is " + decryptedPlayerDetails.getUsername()
						+ " decrypted password is " + decryptedPlayerDetails.getPassword());
		// check DB for player
		for (PlayerDetails player : players) {
			if (player.equals(decryptedPlayerDetails)) {
				return true;
			}
		}
		return false;
	}
}
