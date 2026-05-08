package com.boot.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneUtil {

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	public static class PhoneNumberWrapper {
		PhoneNumber phoneNumber;
		boolean dummy;

		public PhoneNumber getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(PhoneNumber phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public boolean isDummy() {
			return dummy;
		}

		public void setDummy(boolean dummy) {
			this.dummy = dummy;
		}

	}

	public PhoneNumberWrapper parse(CharSequence numberToParse, String defaultRegion) throws NumberParseException {
		PhoneNumberWrapper phoneNumberWrap = new PhoneNumberWrapper();
		PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse(numberToParse, defaultRegion);
		phoneNumberWrap.setPhoneNumber(phoneNumber);
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

	public static final String PLUS_SIGN = "+";

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
