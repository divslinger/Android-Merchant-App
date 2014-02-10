package info.blockchain.util;
 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class EmailAddressCheck {
 
	private static Matcher matcher;
	private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static Pattern pattern = Pattern.compile(EMAIL_REGEX);
 
	private EmailAddressCheck() { ; }

	public static boolean isValid(final String email) {
		matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
