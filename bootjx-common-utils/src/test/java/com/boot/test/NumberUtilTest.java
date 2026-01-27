package com.boot.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.boot.utils.NumberUtil;

@ExtendWith(SpringExtension.class)
public class NumberUtilTest {

	NumberUtil util = new NumberUtil();

	@Test
	public void testisNumber() {
		assertFalse(util.isIntegerValue(null));
		assertFalse(util.isIntegerValue(new BigDecimal(9.30)));
		assertTrue(util.isIntegerValue(new BigDecimal(9)));
	}
}
