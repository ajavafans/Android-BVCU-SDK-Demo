package com.example.TreeView;

import java.util.ArrayList;
import java.util.List;

public class PuDeviceNode {
	private int id;
	/**
	 * 根节点pId为0
	 */
	private int pId = 0; //父ID
	private String name; //名称，用于UI显示（设备名/通道名）
	private String PUID; //PUID
	private int onlineStatus; //在线状态
	private int targetIndex;//通道号
	private String pName;//通道的父名称即为设备名
	private int iAVStreamDir = 0;

	/**
	 * 当前的级别
	 */
	private int level;

	/**
	 * 是否展开
	 */
	private boolean isExpand = false;

	private int icon;

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

	/**
	 * 下一级的子Node
	 */
	private List<PuDeviceNode> children = new ArrayList<PuDeviceNode>();

	/**
	 * 父Node
	 */
	private PuDeviceNode parent;

	public PuDeviceNode() {
	}

	public PuDeviceNode(int id, int pId, String name, String PUID, int onlineStatus,
						int targetIndex, String pName, int iAVStreamDir) {
		super();
		this.id = id;
		this.pId = pId;
		this.name = name;
		this.PUID = PUID;
		this.onlineStatus = onlineStatus;
		this.targetIndex = targetIndex;
		this.pName = pName;
		this.iAVStreamDir = iAVStreamDir;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
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

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isExpand() {
		return isExpand;
	}

	public List<PuDeviceNode> getChildren() {
		return children;
	}

	public void setChildren(List<PuDeviceNode> children) {
		this.children = children;
	}

	public PuDeviceNode getParent() {
		return parent;
	}

	public void setParent(PuDeviceNode parent) {
		this.parent = parent;
	}

	public int getTargetIndex() {
		return targetIndex;
	}

	public void setTargetIndex(int targetIndex) {
		this.targetIndex = targetIndex;
	}

	public int getiAVStreamDir() {
		return iAVStreamDir;
	}

	public void setiAVStreamDir(int iAVStreamDir) {
		this.iAVStreamDir = iAVStreamDir;
	}

	/**
	 * 是否为跟节点
	 *
	 * @return
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * 判断父节点是否展开
	 *
	 * @return
	 */
	public boolean isParentExpand() {
		if (parent == null)
			return false;
		return parent.isExpand();
	}

	/**
	 * 是否是叶子界点
	 *
	 * @return
	 */
	public boolean isLeaf() {
		return children.size() == 0;
	}

	/**
	 * 获取level
	 */
	public int getLevel() {
		return parent == null ? 0 : parent.getLevel() + 1;
	}

	/**
	 * 设置展开
	 *
	 * @param isExpand
	 */
	public void setExpand(boolean isExpand) {
		this.isExpand = isExpand;
		if (!isExpand) {

			for (PuDeviceNode node : children) {
				node.setExpand(isExpand);
			}
		}
	}
}
