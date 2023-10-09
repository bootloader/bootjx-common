package com.boot.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneUtil {

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

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
}