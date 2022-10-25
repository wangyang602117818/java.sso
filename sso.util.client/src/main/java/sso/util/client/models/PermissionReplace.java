package sso.util.client.models;

import java.util.List;

public class PermissionReplace {
	public String Origin;
	public List<String> Names;

	public PermissionReplace(String origin, List<String> names) {
		Origin = origin;
		Names = names;
	}
}
