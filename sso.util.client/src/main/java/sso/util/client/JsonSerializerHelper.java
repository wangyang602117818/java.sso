package sso.util.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializerHelper {
	// 序列化
	public static String Serialize(Object obj) throws Exception {
		return new ObjectMapper().writeValueAsString(obj);
	}

	// 反序列化
	public static <T> T Deserialize(String str, Class<T> valueType) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(str, valueType);
	}
	
	public static <T> T Deserialize(String str, TypeReference<T> valueTypeRef) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(str, valueTypeRef);
	}
}
