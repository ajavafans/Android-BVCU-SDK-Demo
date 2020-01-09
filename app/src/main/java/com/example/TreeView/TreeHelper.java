package com.example.TreeView;

import com.example.myapplication1.R;

import java.util.ArrayList;
import java.util.List;

public class TreeHelper {
	/**
	 * 传入我们的普通bean，转化为我们排序后的Node
	 *
	 * @param datas
	 * @param defaultExpandLevel
	 * @return
	 */
	public static List<PuDeviceNode> getSortedNodes(List<PuDeviceBean> datas,
														 int defaultExpandLevel) throws IllegalArgumentException,
			IllegalAccessException

	{
		List<PuDeviceNode> result = new ArrayList<PuDeviceNode>();
		// 将用户数据转化为List<Node>
		List<PuDeviceNode> nodes = convetData2Node(datas);
		// 拿到根节点
		List<PuDeviceNode> rootNodes = getRootNodes(nodes);
		// 排序以及设置Node间关系
		for (PuDeviceNode node : rootNodes) {
			addNode(result, node, defaultExpandLevel, 1);
		}
		return result;
	}

	/**
	 * 过滤出所有可见的Node
	 *
	 * @param nodes
	 * @return
	 */
	public static List<PuDeviceNode> filterVisibleNode(List<PuDeviceNode> nodes) {
		List<PuDeviceNode> result = new ArrayList<PuDeviceNode>();

		for (PuDeviceNode node : nodes) {
			// 如果为跟节点，或者上层目录为展开状态
			if (node.isRoot() || node.isParentExpand()) {
				setNodeIcon(node);
				result.add(node);
			}
		}
		return result;
	}

	/**
	 * 将我们的数据转化为树的节点
	 *
	 * @param datas
	 * @return
	 */
	private static List<PuDeviceNode> convetData2Node(List<PuDeviceBean> datas)
			throws IllegalArgumentException, IllegalAccessException {
		List<PuDeviceNode> nodes = new ArrayList<PuDeviceNode>();
		PuDeviceNode node = null;
		int id = -1;
		int pId = -1;
		String name = null;
		String PUID = null;
		int onlineStatus = 0;
		int targetIndex = 0;
		String pName = null;
		int iAVStreamDir = 0;
		for (PuDeviceBean puDeviceBean : datas){
			id = puDeviceBean.getId();
			pId = puDeviceBean.getpId();
			name = puDeviceBean.getName();
			PUID = puDeviceBean.getPUID();
			onlineStatus = puDeviceBean.getOnlineStatus();
			targetIndex = puDeviceBean.getTargetIndex();
			pName = puDeviceBean.getpName();
			iAVStreamDir = puDeviceBean.getiAVStreamDir();
			node = new PuDeviceNode(id, pId, name, PUID, onlineStatus, targetIndex, pName, iAVStreamDir);
			nodes.add(node);
		}
		/**
		 * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
		 */
		for (int i = 0; i < nodes.size(); i++) {
			PuDeviceNode n = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++) {
				PuDeviceNode m = nodes.get(j);
				if (m.getpId() == n.getId()) {
					n.getChildren().add(m);
					m.setParent(n);
				} else if (m.getId() == n.getpId()) {
					m.getChildren().add(n);
					n.setParent(m);
				}
			}
		}

		// 设置图片
		for (PuDeviceNode n : nodes) {
			setNodeIcon(n);
		}
		return nodes;
	}

	private static List<PuDeviceNode> getRootNodes(List<PuDeviceNode> nodes) {
		List<PuDeviceNode> root = new ArrayList<PuDeviceNode>();
		for (PuDeviceNode node : nodes) {
			if (node.isRoot())
				root.add(node);
		}
		return root;
	}

	/**
	 * 把一个节点上的所有的内容都挂上去
	 */
	private static void addNode(List<PuDeviceNode> nodes, PuDeviceNode node,
								int defaultExpandLeval, int currentLevel) {

		nodes.add(node);
		if (defaultExpandLeval >= currentLevel) {
			node.setExpand(true);
		}

		if (node.isLeaf())
			return;
		for (int i = 0; i < node.getChildren().size(); i++) {
			addNode(nodes, node.getChildren().get(i), defaultExpandLeval,
					currentLevel + 1);
		}
	}

	/**
	 * 设置节点的图标
	 *
	 * @param node
	 */
	private static void setNodeIcon(PuDeviceNode node) {
		if (node.getChildren().size() > 0 && node.isExpand()) {
			node.setIcon(R.mipmap.device_list_tree_node_close_image);
		} else if (node.getChildren().size() > 0 && !node.isExpand()) {
			node.setIcon(R.mipmap.device_list_tree_node_open_image);
		} else
			node.setIcon(-1);

	}

}
