package sso.util.client.models;

import java.time.LocalDateTime;
import java.util.List;

public class FileItem {
	public String Id;
	public String From;
	public String FileId;
	public String FileName;
	public String Machine;
	public String Folder;
	public String Length;
	public int Width;
	public int Height;
	public int Download;
	public String FileType;
	public String ContentType;
	public int Duration;
	public String Owner;
	public int State;
	public int Percent;
	public int ProcessCount;
	public boolean Delete;
	public boolean Exception;
	public List<ConvertFile> Thumbnails;
	public List<ConvertFile> Videos;
	public LocalDateTime DeleteTime;
	public LocalDateTime CreateTime;
}
