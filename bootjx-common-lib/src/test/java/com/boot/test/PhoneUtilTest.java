package com.boot.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.logger.LoggerService;
import com.boot.jx.logger.LoggerService.LogTimer;
import com.boot.utils.PhoneUtil;
import com.boot.utils.PhoneUtil.PhoneNumberResult;
import com.google.i18n.phonenumbers.NumberParseException;

public class PhoneUtilTest { // Noncompliant
	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");
	private static Logger LOGGER = LoggerFactory.getLogger(PhoneUtilTest.class);
	private static final List<String> MOCK_NUMBERS = List.of(//
			"+155500000001", "155500000001", "+155500000003",
			//
			"+91 10000 00001", "91 10000 00001", "+911000000001",
			//
			"+99 90100 00001", "99 90100 00001", "+99 90100 00001",
			// Real number
			"+99 90001 04050");

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws URISyntaxException
	 * @throws NumberParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws URISyntaxException, NumberParseException, IOException {
		// validatetestNumbers();
		generateMockNumbers();
	}

	private static void validatetestNumbers() {
		for (String string : MOCK_NUMBERS) {
			PhoneNumberResult reslt = PhoneUtil.parse(string);
			LOGGER.info("[{}] {} = {} -> Mock({}) : Valid({}) ISD({}) LOCAL({})",
					(reslt.isValid() || reslt.isMock()) ? "✔" : "✘", string, reslt.getNumber(), reslt.isMock(),
					reslt.isValid(), reslt.getPhone().getCountryCode(), reslt.getPhone().getNationalNumber());
		}
	}

	public static void generateMockNumbers() throws IOException {
		LogTimer timer = LoggerService.getTimer();
		List<String> numbers = PhoneUtil.generateMockNumbers(4 * 10_000);
		Path file = Path.of("target", "test-data", "mock-numbers.txt");
		Files.createDirectories(file.getParent());
		Files.write(file, numbers, StandardCharsets.UTF_8);
		System.out.println("TimeTaken " + timer.log("Generate").toString());
		System.out.println("Written " + numbers.size() + " numbers to " + file.toAbsolutePath());
		System.out.println("TimeTake " + timer.log("Write").toString());
	}
}
