package org.vaadin.elements.impl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jsoup.nodes.Node;

public class Context {
    private Map<org.jsoup.nodes.Node, NodeImpl> fromJsoup = new IdentityHashMap<>();

    public Context() {
    }

    public NodeImpl resolve(org.jsoup.nodes.Node soupNode) {
        if (soupNode == null) {
            return null;
        }

        NodeImpl node = fromJsoup.get(soupNode);

        if (node == null) {
            throw new IllegalStateException();
        }

        return node;
    }

    public void wrapChildren(NodeImpl node) {
        assert node.context == this;

        Queue<Node> queue = new LinkedList<>(node.node.childNodes());

        while (!queue.isEmpty()) {
            Node soupChild = queue.poll();
            // Only relevant for newer JSoup versions
            if (soupChild.childNodeSize() > 0) {
            	queue.addAll(soupChild.childNodes());
            }

            NodeImpl child = ElementReflectHelper.wrap(soupChild);
            adopt(child);
        }
    }

    protected void adopt(NodeImpl node) {
        if (node.context != null) {
            node.context.remove(node);
        }

        fromJsoup.put(node.node, node);
        node.context = this;
    }

    public RootImpl getRoot() {
        return null;
    }

    public void adoptAll(NodeImpl child) {
        Deque<NodeImpl> queue = new LinkedList<>();
        queue.add(child);

        while (!queue.isEmpty()) {
            NodeImpl node = queue.removeFirst();
            // Find child nodes with old context
            queue.addAll(node.getChildren());

            adopt(node);
        }
    }

    protected void remove(NodeImpl node) {
        fromJsoup.remove(node.node);
    }
}
