package com.boot.json;

import java.io.IOException;

import org.springframework.boot.jackson.JacksonComponent;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonProcessingException;
import tools.jackson.databind.JsonSerializer;
import tools.jackson.databind.SerializerProvider;

/**
 * The Class JsonSerializerTypeSerializer.
 */
@SuppressWarnings("rawtypes")
@JacksonComponent
public class JsonSerializerTypeSerializer extends JsonSerializer<JsonSerializerType> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.codehaus.jackson.map.JsonSerializer#serialize(java.lang.Object,
	 * org.codehaus.jackson.JsonGenerator,
	 * org.codehaus.jackson.map.SerializerProvider)
	 */
	@Override
	public void serialize(JsonSerializerType value, JsonGenerator jgen, SerializerProvider sp)
			throws IOException, JsonProcessingException {
		jgen.writeObject(value.toObject());
	}
}
