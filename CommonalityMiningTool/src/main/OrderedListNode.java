package main;

public class OrderedListNode {

	private OrderedListNode next=null;
	private OrderedListNode prev=null;
	private Object element=null;
	
	public OrderedListNode(Object element){
		this.element=element;
	}
	
	public 	OrderedListNode getPrev(){
		return prev;
	}

	public 	OrderedListNode getNext(){
		return next;
	}

	public void setPrev(OrderedListNode node){
		prev=node;
	}

	public void	setNext(OrderedListNode node){
		next=node;
	}

	public Object getElement(){
		return element;
	}
}