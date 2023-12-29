package server;

/**
 * Class to store user authentication details.
 */
public final class PlayerDetails {
	private String username;
	private String password;

	/**
	 * DEFAULT CONSTRUCTOR for player details.
	 * 
	 */
	public PlayerDetails() {
		username = null;
		password = null;
	}

	/**
	 * CONSTRUCTOR for player details accepting username and password.
	 * 
	 * @param username the players name
	 * @param password the players password
	 */
	public PlayerDetails(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Compares the PlayerDetails object username and password for equality.
	 * 
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object object) {
//		System.out.println("In PlayerDetails equals method");
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass()) {
			System.out.println("null object or class mismatch in PlayerDetails equals method");
			return false;
		}
		PlayerDetails playerDetails = (PlayerDetails) object; // class cast
		return (playerDetails.password.equals(this.password)) && (playerDetails.username.equals(this.username));

	}

	/**
	 * Encrypts the user password and username with the provided encryption key.
	 * 
	 * @param encryptionKey
	 */
	public PlayerDetails encrypt(int encryptionKey) {
		PlayerDetails encryptedDetails = new PlayerDetails();
		char[] array;
		if (notNull()) {
			// encrypt password
			array = password.toCharArray();
			for (int i = 0; i < array.length; i++) {
				array[i] = (char) (array[i] + encryptionKey);
			}
			encryptedDetails.password = new String(array);
			// encrypt username
			array = username.toCharArray();
			for (int i = 0; i < array.length; i++) {
				array[i] = (char) (array[i] + encryptionKey);
			}
			encryptedDetails.username = new String(array);
		}
		return encryptedDetails;
	}

	/**
	 * Decrypts the PlayerDetails object with the provided encryption key.
	 * 
	 * @param encryptionKey
	 * @return decrypted clone of the playerDetails object
	 */
	protected PlayerDetails decrypt(int encryptionKey) {
		char[] array;
		PlayerDetails decryptedPlayerDetails = new PlayerDetails();
		if (notNull()) {
			// decrypt password
			array = password.toCharArray();
			for (int i = 0; i < array.length; i++) {
				array[i] = (char) (array[i] - encryptionKey);
			}
			decryptedPlayerDetails.password = new String(array);
			System.out.println(
					"In playerDetails.decrypt() -  decrypted password is " + decryptedPlayerDetails.getPassword());

			// decrypt username
			array = username.toCharArray();
			for (int i = 0; i < array.length; i++) {
				array[i] = (char) (array[i] - encryptionKey);
			}
			decryptedPlayerDetails.username = new String(array);
			System.out.println(
					"In playerDetails.decrypt() -  decrypted username is " + decryptedPlayerDetails.getUsername());
		}
		return decryptedPlayerDetails;

	}

	/**
	 * Returns true if the password and username fields are not null.
	 * 
	 * @return false if password or username fields are null
	 */
	public boolean notNull() {
		if (password == null) {
			return false;
		} else if (username == null) {
			return false;
		}
		return true;
	}

	// ============ SETTER & GETTER METHODS ============

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// TODO for console printing only. remove later
	public String getUsername() {
		return username;
	}

	// TODO for console printing only. remove later
	public String getPassword() {
		return password;
	}
}
