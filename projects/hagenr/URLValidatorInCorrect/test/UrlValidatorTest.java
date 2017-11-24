/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs Validation Test for url validations.
 *
 * @version $Revision: 1128446 $ $Date: 2011-05-27 13:29:27 -0700 (Fri, 27 May 2011) $
 */
public class UrlValidatorTest extends TestCase {
	private boolean printStatus = false;
	private boolean printIndex = false;

	private static final String[] VALID_SCHEMES = {"http", "https", "ftp"};
	private static final String[] VALID_TLDS = {"com", "gov", "edu", "org"};
	private static final String URL_REGEX =    "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
	private static final int NUM_TESTS = 1;

	private UrlValidatorTest(String testName) {
		super(testName);
	}

	public void testManualTest()
	{
		// System.out.println(urlVal.isValid("http://www.amazon.com"));

		// test cases: HashMap with URL and expected value
		HashMap<String, Boolean> urls = new HashMap<String, Boolean>();
		urls.put("http://www.amazon.com", true);
		urls.put("htp://www.amazon.com", false);
		urls.put("http://www.amazon.a", false);
		urls.put("http://www.amazon.com:-1", false);
		urls.put("http://www.amazon.com//contact", false);
		urls.put("http://www.amazon.com/?action=delete", true);

		// test results
		String[] results;
		results = new String[urls.size()];
		int errCount = 0;

		UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);

		// test each case and add a message for each failure to the results array
		for (HashMap.Entry<String, Boolean> entry : urls.entrySet()) {
			try {
				boolean expected = Boolean.parseBoolean(String.valueOf(entry.getValue()));
				assertEquals(entry.getKey() + " fails", expected, urlVal.isValid(entry.getKey()));
			} catch (AssertionError e) {
				results[errCount++] = e.getMessage();
			}
		}

		// print the results
		for (int i = 0; i < errCount; i++) {
			System.out.println(results[i]);
		}
	}


	public void testYourFirstPartition()
	{

	}

	public void testYourSecondPartition(){

	}

	/**
	 * prints an error if there is a mismatch between actual and expected
	 * test results for a randomly generated URL
	 * to get the expected: first break the URL into its components using
	 *   URL_REGEX is from rfc 2396 (https://www.ietf.org/rfc/rfc2396.txt):
	 *   ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
	 *    12            3  4          5       6  7        8 9
	 *   scheme = $2, authority = $4, path = $5, query = $7, fragment = $9
	 *   test each component using simple tests
	 * to get the actual: use URLValidator
	 */

	public void testIsValid()
	{
		Pattern urlPattern = Pattern.compile(URL_REGEX);
		UrlValidator urlVal = new UrlValidator();
		String testUrl, scheme, authority, query;
		boolean expected;

		for (int i = 0; i < NUM_TESTS; i++) {
			testUrl = generateURL();
			expected = true;
			// Matcher urlMatcher = urlPattern.matcher("http://www.google.com:8080?adsf=asdf");
			Matcher urlMatcher = urlPattern.matcher(testUrl);
			// System.out.println("test url: " + testUrl);
			while (urlMatcher.find()) {
				scheme = urlMatcher.group(1);
				authority = urlMatcher.group(3);
				query = urlMatcher.group(6);

				// test the scheme
				expected &= isSchemeValid(scheme);

				// test the authority and port
				if (authority == null) {
					expected = false;
				} else {
					String[] auths = authority.split(":");
					expected &= isAuthValid(auths[0]);

					// test the optional port
					if (auths.length == 2) {
						// port = auths[1];
						expected &= isPortValid(auths[1]);
					}
				}

				// test the optional query string
				if (query != null) {
					expected &= isQueryValid(query);
				}
			}
			// System.out.println("the url: " + testUrl + " tests: " + expected);
			try {
				assertEquals("url mismatch: " + testUrl, expected, urlVal.isValid(testUrl));
			} catch (AssertionError e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * generate a 'random' URL
	 * @return String
	 */
	String generateURL() {
		Random rand = new Random();
		final String[] schemes = {"http", "https", "ftp", "", "htp"};
		final String[] tlds = {"com", "gov", "org", "", "edu", "zog"};
		final String[] names = {"yahoo.", "", "a.", "123.", "google."};
		StringBuilder sb = new StringBuilder();

		// pick a random scheme
		sb.append(schemes[rand.nextInt(schemes.length)]);

		// 1 in 10 chance to not add the '://'
		if (rand.nextInt(10) != 0) {
			sb.append("://");
		}

		// 1 in 10 chance to not add the 'www'
		if (rand.nextInt(10) != 0) {
			sb.append("www.");
		}

		// pick a random name
		sb.append(names[rand.nextInt(names.length)]);

		// pick a random tld
		sb.append(tlds[rand.nextInt(tlds.length)]);

		// add a port 5% of the time, use an upper bound for port number that's out of valid range
		if (rand.nextInt(20) == 10) {
			sb.append(":");
			sb.append(rand.nextInt(66000));
		}

		// add a query string 5% of the time, sometimes it will be invalid
		if (rand.nextInt(20) == 10) {
			if (rand.nextBoolean()) {
				sb.append("?");
			}
			sb.append("delete");
			if (rand.nextBoolean()) {
				sb.append("=");
			}
			sb.append("all");
		}

		// return "http://www.yahoo.gov:31349";
		return "https://www.google.edu?delete=all";
		// return sb.toString();
	}

	/**
	 * compare input scheme in format ":abc[d]" against list of valid schemes.
	 * @param scheme
	 * @return true if a match is found, else return false
	 */
	public boolean isSchemeValid(String scheme) {
		if (scheme != null) {
			String tmp;
			for (String s : VALID_SCHEMES) {
				if (scheme.endsWith(":")) {
					tmp = scheme.substring(0, scheme.length() - 1);
					if (tmp.equals(s)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * compare input auth in format "//www.yahoo.tld" against list of valid auths
	 * one-character domain names and domains that start with a number are valid
	 * @param auth
	 * @return true if a match is found, else return false
	 */
	public boolean isAuthValid(String auth) {
		// System.out.println(authority + ", " + authority.substring(0, 2) + ", " + authority.substring(2, authority.length()));
		if (auth.substring(0, 2).equals("//")) {
			String[] words = auth.substring(2, auth.length()).split("\\.");

			if (words.length < 2) {
				return false;
			}
			// this test makes sense, but I can't verify www.com is invalid
			/*if (words.length == 2 && words[0].equals("www")) {
			  return false;
			  }*/
			for (String a: VALID_TLDS) {
				if (words[words.length-1].equals(a)) {
					return true;
				}
			}
		}
		return false;
	}

	//

	/**
	 * test whether input port is a number and is in range from 1024 - 49151.
	 * range from http://www.ncftp.com/ncftpd/doc/misc/ephemeral_ports.html
	 * @param rawPort
	 * @return true if a number and in range, else return false
	 */
	public boolean isPortValid(String rawPort) {
		if (rawPort.matches("\\d+")) {
			if (Integer.parseInt(rawPort) >= 1024 && Integer.parseInt(rawPort) <= 49151) {
				return true;
			}
		}
		return false;
	}

	/**
	 * test whether input query string is in correct format: ?method=action
	 * @param query
	 * @return true if query string is correct, else return false
	 */
	public boolean isQueryValid(String query) {
		if (query.substring(0, 1).equals("?") && query.indexOf("=") > 0) {
			return true;
		}
		return false;
	}

	public void testAnyOtherUnitTest()
	{

	}
	/**
	 * Create set of tests by taking the testUrlXXX arrays and
	 * running through all possible permutations of their combinations.
	 *
	 * @param testObjects Used to create a url.
	 */

	public static void main(String[] args)
	{
		UrlValidatorTest mytest = new UrlValidatorTest("basic test");
		// mytest.testManualTest();
		mytest.testIsValid();
	}

}

