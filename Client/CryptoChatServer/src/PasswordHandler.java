import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;



/**
 * Password salting and hashing library
 * Passwords are stored on disk in the following format
 * [ITERATION][SALT][PBKDF2_HASH]
 * ITERATION: The number of hashing iterations performed
 * SALT: The salt used in hash
 * PBKDF2_HASH: The final hash result
 */
public class PasswordHandler {
  /** The following constants may be changed without breaking existing hashes.*/
  private static final int SALT_BYTES = 24;
  private static final int HASH_BYTES = 24;
  private static final int PBKDF2_ITERATIONS = 1000;

  /** the index of sub-strings stored 
   as a whole pass string including iteration times, salt and hashed(with salt) password */
  private static final int ITERATION_INDEX = 0;
  private static final int SALT_INDEX = 1;
  private static final int PBKDF2_INDEX = 2;

  
  /**
   * get the salt from a password string
   *
   * @param   password_string    the password string
   * @return  the salt
   */
  public static byte[] getSalt(String password_string) {
  	String[] params = password_string.split(":");
      byte[] salt = fromHex(params[SALT_INDEX]);
      return salt;
  }
    
    
  /**
   * get the salt from a password byte array
   *
   * @param   password_string    the password bytes
   * @return  the hashed password
   */
  public static byte[] getHashedPassword(String password_string) {
  	String[] params = password_string.split(":");  
    return fromHex(params[PBKDF2_INDEX]);
  }
    
    
  /**
   * get the iterate times from a password string
   *
   * @param   password_string    the password string
   * @return  the iterate times
   */
  public static int getNumIterations(String password_string) {
  	String[] params = password_string.split(":");
    return Integer.parseInt(params[ITERATION_INDEX]);
   
  }
  /**
   * Returns a salted PBKDF2 hash of the password.
   *
   * @param   password    the password to hash
   * @return              a salted PBKDF2 hash of the password
   */
  public static String createHash(String password) {
      return createHash(password.toCharArray());
  }

  /**
   * Returns a salted PBKDF2 hash of the password.
   *
   * @param   password    the password to hash
   * @return  the hash    string or null on failure
   */
  public static String createHash(char[] password) {
  	try {
  	  
  		// Generate a random salt
  		SecureRandom random = new SecureRandom();
  		byte[] salt = new byte[SALT_BYTES];
  		random.nextBytes(salt);

  		// Hash the password
  		byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
  		return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" +  toHex(hash);
  	} 
  	catch(Exception e) {
  		System.out.println(e.toString());
  	}
  	return null;
  }

  /**
   * Validates a password using a hash.
   *
   * @param   password    the password to check
   * @param   goodHash    the hash of the valid password
   * @return              true if the password is correct, false if not
   */
  public static boolean validatePassword(String password, String goodHash)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    
      return validatePassword(password.toCharArray(), goodHash);
  }

  /**
   * Validates a password using a hash.
   *
   * @param   password    the password to check
   * @param   goodHash    the hash of the valid password
   * @return              true if the password is correct, false if not
   */
  public static boolean validatePassword(char[] password, String goodHash)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    
      // Decode the hash into its parameters
      String[] params = goodHash.split(":");
      int iterations = Integer.parseInt(params[ITERATION_INDEX]);
      byte[] salt = fromHex(params[SALT_INDEX]);
      byte[] hash = fromHex(params[PBKDF2_INDEX]);
      
      // Compute the hash
      byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
      
      // Compare the hashes in linear time
      return slowEquals(hash, testHash);
      
  }

  /**
   * Compares two byte arrays in linear time. 
   * Used so that password hashes cannot be extracted from an on-line
   * system using a timing attack and then attacked off-line.
   *
   * @param   a       the first byte array
   * @param   b       the second byte array
   * @return          true if both byte arrays are the same, false if not
   */
  private static boolean slowEquals(byte[] a, byte[] b)
  {
      int diff = a.length ^ b.length;
      
      // flags diff if there is difference between any bits, but finishes comparison
      for(int i = 0; i < a.length && i < b.length; i++){
          diff |= a[i] ^ b[i];
      }
      return diff == 0;
  }

  /**
   *  Computes the PBKDF2 hash of a password.
   *
   * @param   password    the password to hash.
   * @param   salt        the salt
   * @param   iterations  the iteration count (slowness factor)
   * @param   bytes       the length of the hash to compute in bytes
   * @return              the PBDKF2 hash of the password
   */
  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
      
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * Byte.SIZE);
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      return skf.generateSecret(spec).getEncoded();
  }

  /**
   * Converts a string of hexadecimal characters into a byte array.
   *
   * @param   hex         the hex string
   * @return              the hex string decoded into a byte array
   */
  private static byte[] fromHex(String hex) {
    
    // Each 8 bit hex char is 4 bit binary
    byte[] binary = new byte[hex.length() / 2];
    
    // Grab each byte from hex string
    for(int i = 0; i < binary.length; i++) {
        binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
    }
    return binary;
  }

  /**
   * Converts a byte array into a hexadecimal string.
   *
   * @param   array       the byte array to convert
   * @return              a character string encoding the byte array
   */
  private static String toHex(byte[] array) {
    
    // Convert array to integer equivalent
    BigInteger bi = new BigInteger(1, array);
    String hex = bi.toString(16);
    
    // Pad front string and return
    int paddingLength = (array.length * 2) - hex.length();
    if(paddingLength > 0) {
      return String.format("%0" + paddingLength + "d", 0) + hex;
    } 
    else {
      return hex;
    }
  }

    
    
  /**
   * Tests the basic functionality of the PasswordHash class
   *
   * @param   args        ignored
   */
  public static void main(String[] args) {
    try {
      
      // Print out 10 hashes
      for (int i = 0; i < 10; i++) {
          System.out.println(PasswordHandler.createHash("p\r\nassw0Rd!"));
      }
      
      // Test password validation
      boolean failure = false;
      System.out.println("Running tests...");
      
      for(int i = 0; i < 100; i++) {
        String password = ""+i;
        String hash = createHash(password);
        String secondHash = createHash(password);
                     
        // Test if salt works
        if(hash.equals(secondHash)) {
          System.out.println("FAILURE: TWO HASHES ARE EQUAL!");
          failure = true;
        }
                 
        // Test an incorrect password
        String wrongPassword = ""+(i+1);
        if(validatePassword(wrongPassword, hash)) {
          System.out.println("FAILURE: WRONG PASSWORD ACCEPTED!");
          failure = true;
        }

        // Test a correct password
        if(!validatePassword(password, hash)) {
            System.out.println("FAILURE: GOOD PASSWORD NOT ACCEPTED!");
            failure = true;
        }
      }
      
      if(failure)
        System.out.println("TESTS FAILED!");
        else {
          System.out.println("TESTS PASSED!");
        }
      }
    catch(Exception ex) {
          System.out.println("ERROR: " + ex);
    }
  }
}