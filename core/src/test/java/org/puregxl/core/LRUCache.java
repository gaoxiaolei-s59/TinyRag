package org.puregxl.core;

import java.util.HashMap;
import java.util.Map;

public class LRUCache {
    class Node {
        int key;
        int val;
        Node next;
        Node pre;

        public Node(int val, Node next, Node pre) {
            this.val = val;
            this.next = next;
            this.pre = pre;
        }

        public Node(int key, int val) {
            this.key = key;
            this.val = val;
        }

        public Node(int val) {
            this.val = val;
        }

    }
    Node head;
    Node tail;
    int capacity;
    int size;
    Map<Integer, Node> map = new HashMap<>();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head = new Node(-1);
        tail = new Node(-1);
        size = 0;
        head.next = tail;
        tail.pre = head;
    }

    public int get(int key) {
        if (map.containsKey(key)) {
            removeHead(map.get(key));
            return map.get(key).val;
        }
        return -1;
    }

    public void put(int key, int value) {
        if (map.get(key) != null) {
            map.get(key).val = value;
            removeHead(map.get(key));
            return;
        }

        if (size == capacity) {
            Node removeLast = removeLast();
            map.remove(removeLast.key);
            Node node = new Node(key, value);
            addToHead(node);
            map.put(key, node);
        } else {
            Node node = new Node(key, value);
            addToHead(node);
            map.put(key, node);
            size++;
        }
    }

    public void removeHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private void addToHead(Node node) {
        node.pre = head;
        node.next = head.next;
        head.next.pre = node;
        head.next = node;
    }


    private void removeNode(Node node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }

    public Node removeLast() {
        Node last = tail.pre;
        removeNode(last);
        return last;
    }


}
