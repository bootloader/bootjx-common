package com.boot.test;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.utils.JsonUtil;
import com.boot.utils.PhoneUtil;
import com.boot.utils.PhoneUtil.PhoneNumberResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.i18n.phonenumbers.NumberParseException;

public class PhoneUtilTest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	private static Logger LOGGER = LoggerFactory.getLogger(PhoneUtilTest.class);

	private static final List<String> MOCK_NUMBERS = List.of("+155500000001", "155500000001", "+999000000001",
			"999000000001", "+9990 00000001");

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
			LOGGER.info("{} = {} = Mock({}) : Valid({})", string, reslt.getNumber(), reslt.isMock(),
					reslt.isValid());
		}

	}
}
