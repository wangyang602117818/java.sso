package sso.util.client.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import sso.util.client.ApplicationProperties;
import sso.util.client.HttpClientRequestHelper;
import sso.util.client.JsonSerializerHelper;
import sso.util.client.JwtManager;
import sso.util.client.StringExtention;
import sso.util.client.models.DownloadFileItem;
import sso.util.client.models.ExtensionMap;
import sso.util.client.models.FileItem;
import sso.util.client.models.FileResponse;
import sso.util.client.models.FileType;
import sso.util.client.models.FromData;
import sso.util.client.models.ServiceModel;
import sso.util.client.models.UploadFileItem;

public class FileClientService {
	private HttpClientRequestHelper requestHelper = new HttpClientRequestHelper();
	public String RemoteUrl;
	public String Token;
	HashMap<String, String> headers = new HashMap<String, String>();
	JwtManager jwtManager = new JwtManager();

	public FileClientService(String token) {
		RemoteUrl = StringExtention.Trim(jwtManager.fileUrl, "/");
		Token = token;
		headers.put("Authorization", token);
	}

	public ServiceModel<FileResponse> Upload(String fileName, String contentType, InputStream stream,
			List<String> roles, List<String> users) throws Exception {
		ArrayList<UploadFileItem> files = new ArrayList<UploadFileItem>();
		UploadFileItem uploadFileItem = new UploadFileItem();
		uploadFileItem.FileName = fileName;
		uploadFileItem.ContentType = contentType;
		uploadFileItem.FileStream = stream;
		files.add(uploadFileItem);
		var result = this.Uploads(files, roles, users);
		ServiceModel<FileResponse> responServiceModel = new ServiceModel<FileResponse>();
		responServiceModel.code = result.code;
		responServiceModel.message = result.message;
		responServiceModel.result = result.result.get(0);
		responServiceModel.count = result.count;
		return responServiceModel;
	}

	public ServiceModel<List<FileResponse>> Uploads(List<UploadFileItem> files, List<String> roles, List<String> users)
			throws Exception {
		HashMap<String, String> param = new HashMap<String, String>();
		if (roles != null && !roles.isEmpty())
			param.put("roles", JsonSerializerHelper.Serialize(roles));
		if (users != null && !users.isEmpty())
			param.put("users", JsonSerializerHelper.Serialize(users));

		String result = requestHelper.PostFile(RemoteUrl + "/upload/file", files, param, headers);
		return JsonSerializerHelper.Deserialize(result, new TypeReference<ServiceModel<List<FileResponse>>>() {
		});
	}

	public DownloadFileItem DownloadFile(String id, String filename) throws Exception {
		return DownloadFile(id, filename, "");
	}

	public DownloadFileItem DownloadFile(String id, String filename, String flag) throws Exception {
		return requestHelper.GetFile(RemoteUrl + "/file/" + id + "/" + filename + "?mode=download&flag=" + flag,
				headers);
	}

	public DownloadFileItem DownloadFileIcon(String id, String filename) throws Exception {
		return requestHelper.GetFile(RemoteUrl + "/file/GetFileIconWrapId/" + id + StringExtention.GetFileExt(filename),
				headers);
	}

	public ServiceModel<Integer> FileState(String id) throws Exception {
		String state = requestHelper.Get(RemoteUrl + "/data/FileState/" + id, headers);
		return JsonSerializerHelper.Deserialize(state, new TypeReference<ServiceModel<Integer>>() {
		});
	}

	public ServiceModel<List<FileItem>> GetFileList() throws Exception {
		return GetFileList(1, 10, "", "", FileType.all, null, null, null, false);
	}

	public ServiceModel<List<FileItem>> GetFileList(int pageIndex, int pageSize, String from, String filter,
			FileType fileType, LocalDateTime startTime, LocalDateTime endTime, HashMap<String, String> sorts,
			Boolean delete) throws Exception {
		var url = RemoteUrl + "/data/GetFiles?pageIndex=" + pageIndex + "&pageSize=" + pageSize;
		if (!StringExtention.IsNullOrEmpty(from))
			url += "&from=" + from;
		if (!StringExtention.IsNullOrEmpty(filter))
			url += "&filter=" + filter;
		if (fileType != null && fileType != FileType.all)
			url += "&fileType=" + fileType.toString();
		if (startTime != null)
			url += "&startTime=" + startTime.format(ApplicationProperties.dateTimeFormatter);
		if (endTime != null)
			url += "&endTime=" + endTime.format(ApplicationProperties.dateTimeFormatter);
		var index = 0;
		if (sorts != null) {
			for (String key : sorts.keySet()) {
				url += "&sorts[" + index + "].key=" + key;
				url += "&sorts[" + index + "].value=" + sorts.get(key);
				index++;
			}
		}
		url += "&delete=" + delete;
		String list = requestHelper.Get(url, headers);
		return JsonSerializerHelper.Deserialize(list, new TypeReference<ServiceModel<List<FileItem>>>() {
		});
	}

	public ServiceModel<FileItem> GetFileInfo(String id) throws Exception {
		var url = RemoteUrl + "/data/GetFileInfo/" + id;
		String item = requestHelper.Get(url, headers);
		return JsonSerializerHelper.Deserialize(item, new TypeReference<ServiceModel<FileItem>>() {
		});
	}

	public ServiceModel<List<FileItem>> GetFileInfos(List<String> ids) throws Exception {
		var url = RemoteUrl + "/data/GetFileInfos";
		ModelIds model = new ModelIds();
		model.ids = ids;
		String result = requestHelper.Post(url, model, headers);
		return JsonSerializerHelper.Deserialize(result, new TypeReference<ServiceModel<List<FileItem>>>() {
		});
	}

	public ServiceModel<List<FromData>> GetFromList() throws Exception {
		var url = RemoteUrl + "/data/GetFromList";
		String list = requestHelper.Get(url, headers);
		return JsonSerializerHelper.Deserialize(list, new TypeReference<ServiceModel<List<FromData>>>() {
		});
	}

	public ServiceModel<List<ExtensionMap>> GetExtensionMap() throws Exception {
		var url = RemoteUrl + "/data/GetExtensionsMap";
		String list = requestHelper.Get(url, headers);
		return JsonSerializerHelper.Deserialize(list, new TypeReference<ServiceModel<List<ExtensionMap>>>() {
		});
	}

	public ServiceModel<String> RemoveFile(String fileId) throws Exception {
		var url = RemoteUrl + "/data/Remove/" + fileId;
		String result = requestHelper.Get(url, headers);
		return JsonSerializerHelper.Deserialize(result, new TypeReference<ServiceModel<String>>() {
		});
	}

	public ServiceModel<String> RemoveFiles(List<String> fileIds) throws Exception {
		var url = RemoteUrl + "/data/Removes";
		ModelIds model = new ModelIds();
		model.ids = fileIds;
		String result = requestHelper.Post(url, model, headers);
		return JsonSerializerHelper.Deserialize(result, new TypeReference<ServiceModel<String>>() {
		});
	}

	public ServiceModel<String> RestoreFile(String fileId) throws Exception {
		var url = RemoteUrl + "/data/Restore/" + fileId;
		String result = requestHelper.Get(url, headers);
		return JsonSerializerHelper.Deserialize(result, new TypeReference<ServiceModel<String>>() {
		});
	}

	public ServiceModel<String> RestoreFiles(List<String> fileIds) throws Exception {
		var url = RemoteUrl + "/data/Restores";
		ModelIds model = new ModelIds();
		model.ids = fileIds;
		String result = requestHelper.Post(url, model, headers);
		return JsonSerializerHelper.Deserialize(result, new TypeReference<ServiceModel<String>>() {
		});
	}

	public DownloadFileItem M3u8MultiStream(String id, String filename, int time) throws Exception {
		if (time > 0)
			headers.put("time", String.valueOf(time));
		return requestHelper.GetFile(RemoteUrl + "/file/" + id + "/" + filename, headers);
	}

	public DownloadFileItem M3u8(String id, String filename, int time) throws Exception {
		if (time > 0)
			headers.put("time", String.valueOf(time));
		return requestHelper.GetFile(RemoteUrl + "/file/" + id + "/" + filename, headers);
	}

	public DownloadFileItem Ts(String id, String filename) throws Exception {
		return requestHelper.GetFile(RemoteUrl + "/file/" + id + "/" + filename, headers);
	}

	class ModelIds {
		public List<String> ids;
	}
}
