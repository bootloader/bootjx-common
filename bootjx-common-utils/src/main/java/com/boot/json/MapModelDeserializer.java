package com.boot.json;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.Map;

import org.springframework.boot.jackson.JacksonComponent;

import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.StringUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonProcessingException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;

@JacksonComponent
public class MapModelDeserializer extends StdDeserializer<MapModel> {

	private static final long serialVersionUID = 1L;

	protected MapModelDeserializer(Class<?> vc) {
		super(vc);
	}

	public MapModelDeserializer() {
		this(null);
	}

	@Override
	public MapModel deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode jsonNode = jp.getCodec().readTree(jp);
		String text = jsonNode.asText();
		Map<String, Object> map = null;
		if (ArgUtil.isEmpty(text)) {
			map = JsonUtil.getMapper().convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
			});
		}
		return new MapModel(map);
	}

	public static class MapModelEditor extends PropertyEditorSupport {

		private ObjectMapper objectMapper;

		public MapModelEditor(ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		public MapModelEditor() {
		}

		@Override
		public void setAsText(String text) throws IllegalArgumentException {
			if (StringUtils.isEmpty(text)) {
				setValue(new MapModel());
			} else {
				MapModel prod = JsonUtil.parse(text, MapModel.class);
				setValue(prod);
			}
		}

	}

}
