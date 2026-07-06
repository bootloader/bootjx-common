package com.boot.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.utils.PhoneUtil;
import com.boot.utils.PhoneUtil.PhoneNumberResult;
import com.google.i18n.phonenumbers.NumberParseException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class PhoneUtilTest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	private static Logger LOGGER = LoggerFactory.getLogger(PhoneUtilTest.class);

	private static final List<String> MOCK_NUMBERS = List.of(
			//
			"+155500000001", "155500000001",
			//
			"+91 10000 00001", "91 10000 00001",
			//
			"+99 90100 00001", "99 90100 00001", "+99 90100 00001",
			// Real number
			"+91 85000 00000");

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws NumberParseException
	 */
	public static void main(String[] args) throws MalformedURLException, URISyntaxException, NumberParseException {

		for (String string : MOCK_NUMBERS) {
			PhoneNumberResult reslt = PhoneUtil.parse(string, null);

			LOGGER.info("{} = {} -> Mock({}) : Valid({}) ISD({}) LOCAL({})", string, reslt.getNumber(), reslt.isMock(),
					reslt.isValid(), reslt.getPhone().getCountryCode(), reslt.getPhone().getNationalNumber()

			);
		}

	}
}
