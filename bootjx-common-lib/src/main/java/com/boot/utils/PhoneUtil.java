package com.boot.utils;

import java.util.List;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneUtil {

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	private static final List<String> MOCK_PREFIXES = List.of("+1555", "15550", "9990", "+9990");
	public static final String PLUS_SIGN = "+";

	public static class PhoneNumberResult {
		PhoneNumber phone;
		boolean mock;
		String number;

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		private PhoneNumberResult(String normalized, PhoneNumber phone, boolean mock) {
			this.phone = phone;
			this.mock = mock;
			this.number = normalized;
		}

		public PhoneNumber getPhone() {
			return phone;
		}

		public void setPhone(PhoneNumber phone) {
			this.phone = phone;
		}

		public boolean isMock() {
			return mock;
		}

		public void setMock(boolean mock) {
			this.mock = mock;
		}

		public boolean isValid() {
			return phone != null && PHONE_NUMBER_UTIL.isValidNumber(phone);
		}

		public String toE164() {
			if (phone == null) {
				return number;
			}
			return PLUS_SIGN + phone.getCountryCode() + phone.getNationalNumber();
		}

		@Override
		public String toString() {
			if (phone == null) {
				return number;
			}
			return phone.getCountryCode() + "" + phone.getNationalNumber();
		}

	}

	private static String toDigitsWithCountryCode(String input) {
		if (!ArgUtil.is(input))
			return input;

		StringBuilder digits = new StringBuilder(input.length());

		// keep only digits
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c >= '0' && c <= '9') {
				digits.append(c);
			}
		}

		// remove all leading zeros (safe under your contract)
		while (digits.length() > 0 && digits.charAt(0) == '0') {
			digits.deleteCharAt(0);
		}

		return digits.toString();
	}

	public static String toE164(String input) {
		String digits = toDigitsWithCountryCode(input);
		if (!ArgUtil.is(digits))
			return digits;
		return PLUS_SIGN + digits;
	}

	private static boolean isMockNumber(String number) {
		return number != null && MOCK_PREFIXES.stream().anyMatch(number::startsWith);
	}

	public static PhoneNumberResult parse(String numberToParse, String defaultRegion) throws NumberParseException {

		if (!ArgUtil.is(numberToParse)) {
			return new PhoneNumberResult(null, null, false);
		}
		String normalized = toDigitsWithCountryCode(numberToParse);

		// mock detection
		if (isMockNumber(normalized)) {
			return new PhoneNumberResult(normalized, null, true);
		}

		PhoneNumberResult phoneNumberWrap = new PhoneNumberResult(normalized, null, false);

		try {
			PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse(PLUS_SIGN + normalized, defaultRegion);
			phoneNumberWrap.setPhone(phoneNumber);
		} catch (NumberParseException e) {
			// phone = String.format("%s", phone);
		}
		return phoneNumberWrap;
	}

	public static String phone(String phoneNo) {
		if (ArgUtil.is(phoneNo)) {
			String phone = phoneNo.replace(" ", "").replaceAll("^[\\+0\\s]+(?!$)", "").trim();
			try {
				PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse("+" + phone, "IN");
				phone = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			} catch (NumberParseException e) {
				// phone = String.format("%s", phone);
			}
			return phone;
		}
		return phoneNo;
	}

	/** adding + sign in a phone if not there **/
	public static String addPlusSign(String phoneNo) {
		if (ArgUtil.is(phoneNo)) {
			if (!phoneNo.startsWith(PLUS_SIGN)) {
				phoneNo = PLUS_SIGN.concat(phoneNo);
			}
		}
		return phoneNo;
	}

}
