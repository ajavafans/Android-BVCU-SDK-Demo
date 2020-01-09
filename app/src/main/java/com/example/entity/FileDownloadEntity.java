package com.example.entity;

public class FileDownloadEntity {
	private String fileName;
	private String szRemoteFilePath;
	private String szLocalFilePath;
	private String szSourceID;
	private int downloadPercent;
	private int downloadStatus;
	private int token;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSzRemoteFilePath() {
		return szRemoteFilePath;
	}

	public void setSzRemoteFilePath(String szRemoteFilePath) {
		this.szRemoteFilePath = szRemoteFilePath;
	}

	public String getSzLocalFilePath() {
		return szLocalFilePath;
	}

	public void setSzLocalFilePath(String szLocalFilePath) {
		this.szLocalFilePath = szLocalFilePath;
	}

	public String getSzSourceID() {
		return szSourceID;
	}

	public void setSzSourceID(String szSourceID) {
		this.szSourceID = szSourceID;
	}

	public int getDownloadPercent() {
		return downloadPercent;
	}

	public void setDownloadPercent(int downloadPercent) {
		this.downloadPercent = downloadPercent;
	}

	public int getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(int downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}
}
