package com.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.boot.utils.ClazzUtil;

/**
 * Pins down {@link ClazzUtil}'s reflection/annotation-lookup helpers (used
 * heavily by AOP-adjacent code), so behavior can be diffed after Spring
 * upgrades.
 */
@RunWith(SpringRunner.class)
public class ClazzUtilTest {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	public @interface Marker {
		String value() default "";
	}

	@Marker("on-class")
	static class AnnotatedClass {
		@Marker("on-method")
		public void annotatedMethod() {
		}

		public void plainMethod() {
		}
	}

	static class PlainClass {
	}

	static class Base {
		private String baseField;
	}

	static class Derived extends Base {
		private String derivedField;
	}

	@Test
	public void fromName_resolvesAndCachesKnownClass() {
		Class<String> clz = ClazzUtil.fromName("java.lang.String");
		assertEquals(String.class, clz);
		// second call should hit the internal cache and still resolve correctly.
		assertEquals(String.class, ClazzUtil.fromName("java.lang.String"));
	}

	@Test
	public void fromName_unknownClass_returnsNullInsteadOfThrowing() {
		assertNull(ClazzUtil.fromName("com.boot.does.not.Exist"));
	}

	@Test
	public void getGenericTypePattern_buildsPatternMatchingGenericToString() {
		Pattern pattern = ClazzUtil.getGenericTypePattern(java.util.List.class);
		assertTrue(pattern.matcher("java.util.List<java.lang.String>").matches());
		assertTrue(pattern.matcher("java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>").matches());
	}

	@Test
	public void getClassName_ofTopLevelClass_returnsFullNameUnchanged() {
		assertEquals("java.lang.Object", ClazzUtil.getClassName(new Object()));
	}

	@Test
	public void getClassName_ofNestedOrProxiedClass_keepsOnlyPartBeforeDollarSign() {
		// getClassName splits on '$' and keeps index 0: this is what lets it unwrap
		// CGLIB proxy class names (e.g. "MyClass$$EnhancerBySpringCGLIB$$abc") back
		// to the original class name; the trade-off is that for a genuine static
		// nested class it collapses to the *enclosing* class name, not the nested one.
		String className = ClazzUtil.getClassName(new PlainClass());
		assertEquals(ClazzUtilTest.class.getName(), className);
		assertFalse(className.contains("$"));
	}

	@Test
	public void getSimpleClassName_returnsBareNameWithoutOuterOrDollarSign() {
		assertEquals("PlainClass", ClazzUtil.getSimpleClassName(new PlainClass()));
	}

	@Test
	public void getAnnotation_findsDirectTypeAnnotation() {
		Marker marker = ClazzUtil.getAnnotation(AnnotatedClass.class, Marker.class);
		assertNotNull(marker);
		assertEquals("on-class", marker.value());
	}

	@Test
	public void getAnnotation_missingAnnotation_returnsNull() {
		assertNull(ClazzUtil.getAnnotation(PlainClass.class, Marker.class));
	}

	@Test
	public void getAnnotationFromBean_resolvesThroughNonProxiedInstance() {
		Marker marker = ClazzUtil.getAnnotationFromBean(new AnnotatedClass(), Marker.class);
		assertNotNull(marker);
		assertEquals("on-class", marker.value());
	}

	@Test
	public void getUltimateClass_resolvesNonProxiedObjectToItsOwnClass() {
		PlainClass instance = new PlainClass();
		assertEquals(PlainClass.class, ClazzUtil.getUltimateClass(instance));
	}

	@Test
	public void getUltimateClassName_ofTopLevelClass_returnsFullNameUnchanged() {
		assertEquals("java.lang.Object", ClazzUtil.getUltimateClassName(new Object()));
	}

	@Test
	public void getAllFields_includesFieldsFromEntireSuperclassChain() {
		Field[] fields = ClazzUtil.getAllFields(Derived.class);
		boolean hasDerived = false;
		boolean hasBase = false;
		for (Field f : fields) {
			if (f.getName().equals("derivedField"))
				hasDerived = true;
			if (f.getName().equals("baseField"))
				hasBase = true;
		}
		assertTrue("expected derivedField in " + java.util.Arrays.toString(fields), hasDerived);
		assertTrue("expected baseField in " + java.util.Arrays.toString(fields), hasBase);
	}

	@Test
	public void findMethodAnnotation_findsMergedAnnotationDirectlyOnMethod() throws Exception {
		AnnotatedClass target = new AnnotatedClass();
		Method method = AnnotatedClass.class.getMethod("annotatedMethod");
		Marker marker = ClazzUtil.findMethodAnnotation(target, method, Marker.class);
		assertNotNull(marker);
		assertEquals("on-method", marker.value());
	}

	@Test
	public void findMethodAnnotation_returnsNullWhenMethodHasNoSuchAnnotation() throws Exception {
		AnnotatedClass target = new AnnotatedClass();
		Method method = AnnotatedClass.class.getMethod("plainMethod");
		assertNull(ClazzUtil.findMethodAnnotation(target, method, Marker.class));
	}
}
