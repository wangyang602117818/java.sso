package sso.util.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import sso.util.client.models.DownloadFileItem;
import sso.util.client.models.UploadFileItem;

public class HttpClientRequestHelper {
	private static final String BOUNDARY_PREFIX = "--";
	private static final String LINE_END = "\r\n";
	HttpClient httpClient = HttpClient.newHttpClient();

	public String Get(String url, HashMap<String, String> headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(new URI(url));
		if (headers != null) {
			for (String key : headers.keySet()) {
				builder.header(key, headers.get(key));
			}
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
	}

	public String Post(String url, Object obj, HashMap<String, String> headers) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(new URI(url)).header("Content-Type", "application/json");
		if (headers != null) {
			for (String key : headers.keySet()) {
				builder.header(key, headers.get(key));
			}
		}
		builder.POST(HttpRequest.BodyPublishers.ofString(JsonSerializerHelper.Serialize(obj))).build();
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString()).body();
	}

	public String PostFile(String url, UploadFileItem file, HashMap<String, String> paras,
			HashMap<String, String> headers) throws Exception {
		List<UploadFileItem> files = new ArrayList<UploadFileItem>();
		files.add(file);
		return PostFile(url, files, paras, headers);
	}

	public String PostFile(String url, List<UploadFileItem> files, HashMap<String, String> paras,
			HashMap<String, String> headers) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(50000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Charset", "utf-8");
		conn.setRequestProperty("connection", "keep-alive");
		if (headers != null) {
			for (String key : headers.keySet()) {
				conn.setRequestProperty(key, headers.get(key));
			}
		}
		String boundary = "----" + UUID.randomUUID().toString().replace("-", "");
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		// 写普通参数
		if (paras != null && !paras.isEmpty()) {
			for (String key : paras.keySet()) {
				String boundaryStr = BOUNDARY_PREFIX + boundary + LINE_END;
				out.write(boundaryStr.getBytes());
				String contentDispositionStr = String.format("Content-Disposition: form-data; name=\"%s\"", key)
						+ LINE_END + LINE_END;
				out.write(contentDispositionStr.getBytes());
				String valueStr = paras.get(key) + LINE_END;
				out.write(valueStr.getBytes());
			}
		}
		// 写文件数据
		if (files != null && !files.isEmpty()) {
			for (UploadFileItem file : files) {
				String contentDisposition = BOUNDARY_PREFIX + boundary + LINE_END
						+ "Content-Disposition: form-data;name=\"files\";filename=\""
						+ StringExtention.GetFileName(file.FileName) + "\"" + LINE_END + "Content-Type: "
						+ file.ContentType + "; charset=utf-8" + LINE_END + LINE_END;
				out.write(contentDisposition.getBytes());

				DataInputStream in = new DataInputStream(file.FileStream);
				int bytes = 0;
				byte[] bufferOut = new byte[4096];
				while ((bytes = in.read(bufferOut)) != -1) {
					out.write(bufferOut, 0, bytes);
				}
				in.close();
				// 回车换行
				out.write(LINE_END.getBytes());
			}
		}
		String endStr = BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX;
		out.write(endStr.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder responseContent = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			responseContent.append(line);
		}
		conn.disconnect();
		return responseContent.toString();
	}

	public DownloadFileItem GetFile(String url, HashMap<String, String> headers) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(50000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Charset", "utf-8");
		if (headers != null) {
			for (String key : headers.keySet()) {
				conn.setRequestProperty(key, headers.get(key));
			}
		}
		DownloadFileItem downloadFileItem = new DownloadFileItem();
		conn.getHeaderFields();
		var contentDisposition = conn.getHeaderField("Content-Disposition");
		String name = "";
		if (contentDisposition != null) {
			String[] names = contentDisposition.split(";");
			if (names.length > 1) {
				name = StringExtention.Trim(names[1], "\"").split("=")[1];
			}
		}
		downloadFileItem.FileName = name;
		downloadFileItem.ContentType = conn.getContentType();
		downloadFileItem.ContentLength = conn.getContentLengthLong();
		downloadFileItem.FileStream = conn.getInputStream();
//		 conn.disconnect();
		return downloadFileItem;
	}
}
