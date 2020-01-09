package com.example.TreeView;

public class PuDeviceBean {
	/**
	 * 节点Id
	 */
	private int id;
	/**
	 * 节点父id
	 */
	private int pId;
	/**
	 * 节点name
	 */
	private String name;// 用于UI显示
	private String PUID;//PUID,查询文件时用到，设备名为空时显示PUID
	private int onlineStatus;//根据状态显示不同颜色的图标
	private int targetIndex;//通道号
	private String pName;//父名称，通道的父名称即为设备名，设备的父名称为IP
	private int iAVStreamDir = 0;

	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}

	public String getPUID() {
		return PUID;
	}

	public void setPUID(String PUID) {
		this.PUID = PUID;
	}

	public int getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public int getTargetIndex() {
		return targetIndex;
	}

	public void setTargetIndex(int targetIndex) {
		this.targetIndex = targetIndex;
	}

	public PuDeviceBean(int id, int pId, String name, String PUID, int status,
						int targetIndex, String pName, int iAVStreamDir) {
		super();
		this.id = id;
		this.pId = pId;
		this.name = name;
		this.targetIndex = targetIndex;
		this.PUID = PUID;
		this.onlineStatus = status;
		this.pName = pName;
		this.iAVStreamDir = iAVStreamDir;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getpId() {
		return pId;
	}

	public void setpId(int pId) {
		this.pId = pId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPuid() {
		return PUID;
	}

	public void setPuid(String puid) {
		this.PUID = puid;
	}

	public int getStatus() {
		return onlineStatus;
	}

	public void setStatus(int status) {
		this.onlineStatus = status;
	}

	public int getiAVStreamDir() {
		return iAVStreamDir;
	}

	public void setiAVStreamDir(int iAVStreamDir) {
		this.iAVStreamDir = iAVStreamDir;
	}

}
