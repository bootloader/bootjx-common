package com.boot.jx.mongo;

import java.io.IOException;

import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * Deliberately NOT nested inside {@link CommonDocInterfaces}: Spring Boot's
 * {@code JsonComponentModule} auto-discovers JsonSerializer/JsonDeserializer/
 * KeyDeserializer classes nested inside any {@code @JsonComponent} bean and
 * tries to register them on Spring's own (unrelated) autoconfigured
 * ObjectMapper too. For plain {@link KeyDeserializer} it additionally
 * *requires* the enclosing {@code @JsonComponent} to declare an explicit
 * target type (KeyDeserializer isn't generic, so Spring can't infer one) and
 * throws "Type must be specified for KeyDeserializer" otherwise - support for
 * KeyDeserializer here is new since Spring Boot 2.1 (Boot 2.0.5's
 * JsonComponentModule only handled JsonSerializer/JsonDeserializer), so this
 * crash only surfaced with the 2.7.18 upgrade. This class is only ever meant
 * to be wired into {@code JsonUtil}'s own ObjectMapper via the static block
 * in {@link CommonDocInterfaces}, so keeping it out of that
 * {@code @JsonComponent}-annotated class avoids Spring's unrelated
 * auto-registration path entirely.
 */
public class ResourceDocumentKeyDeserializer extends KeyDeserializer {
	@Override
	public Object deserializeKey(String key, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		return JsonUtil.getMapper().readValue(key, CommonDocInterfaces.ResourceDocumentImpl.class);
	}
}
