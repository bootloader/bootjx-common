package com.boot.json;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.Map;

import org.springframework.boot.jackson.JacksonComponent;

import com.boot.json.NamedEntityDeserializer.NamedMapModel;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonProcessingException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

@JacksonComponent
public class NamedEntityDeserializer extends StdDeserializer<NamedMapModel> {

    private static final long serialVersionUID = 1L;

    @JsonDeserialize(as = NamedMapModel.class, using = NamedEntityDeserializer.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public interface NamedEntity {
	public String name(String key);
    }

    @JsonDeserialize(using = NamedEntityDeserializer.class)
    public static class NamedMapModel extends MapModel implements NamedEntity {

	private String defaultTokenValue;

	public NamedMapModel(Map<String, Object> map, String defaultTokenValue) {
	    super(map);
	    this.defaultTokenValue = defaultTokenValue;
	}

	@Override
	public String name(String key) {
	    return this.getString(key, defaultTokenValue);
	}

	public String name() {
	    return this.getString("name", defaultTokenValue);
	}

	public String id() {
	    return this.getString("id", defaultTokenValue);
	}
    }

    protected NamedEntityDeserializer(Class<?> vc) {
	super(vc);
    }

    public NamedEntityDeserializer() {
	this(null);
    }

    @Override
    public NamedMapModel deserialize(JsonParser jp, DeserializationContext ctxt)
	    throws IOException, JsonProcessingException {
	JsonNode jsonNode = jp.getCodec().readTree(jp);
	String text = jsonNode.asText();
	Map<String, Object> map = null;
	if (ArgUtil.isEmpty(text)) {
	    map = JsonUtil.getMapper().convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
	    });
	}
	return new NamedMapModel(map, text);
    }

    public static class NamedEntityEditor extends PropertyEditorSupport {

	private ObjectMapper objectMapper;

	public NamedEntityEditor(ObjectMapper objectMapper) {
	    this.objectMapper = objectMapper;
	}

	public NamedEntityEditor() {
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
	    if (StringUtils.isEmpty(text)) {
		setValue(new NamedMapModel(null, null));
	    } else {
		NamedMapModel prod = JsonUtil.parse(text, NamedMapModel.class);
		setValue(prod);
	    }
	}

    }

}
