package main;

class OrderedListNode {

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


public class OrderedList{
	private OrderedListNode first=null;
	private OrderedListNode last=null;
	
	OrderedList(Object element){
		first=new OrderedListNode(element);
		last=first;
	}

	public OrderedList() {}

	public OrderedListNode getFirst(){
		return first;
	}

	public OrderedListNode getLast(){
		return last;		
	}

	public boolean isEmpty(){
		return first==null? true:false;
	}

	public int getSize(){
		OrderedListNode tmp=first;
		int i=0;
		while(tmp!=null){
		  ++i;
		  tmp=tmp.getNext();
		}
		return i;
	}

	/**
	 * Adds the specified element to the top of the list
	 * @param element - the Object to be added 
	 */
	public void addToTop(Object element){
		OrderedListNode tmp=first;
		first=new OrderedListNode(element);
		if(tmp==null) last=first; 
		else {
		  first.setNext(tmp);
		  tmp.setPrev(first);
		}
	}
	
	/**
	 * Adds the specified element to the bottom of the list
	 * @param element - the Object to be added 
	 */		
	public void addToBottom(Object element){
		if (last==null){ addToTop(element); return;}
		OrderedListNode tmp=last;
		last.setNext(new OrderedListNode(element));
		last=last.getNext();
		last.setPrev(tmp);
	}

	/**
	 * Moves the specified element to the top of the list. <br>
	 * If element is not present in this list, it will be added to the top.
	 * 
	 * @param element - the Object to be moved 
	 */
	public void moveToTop(Object element){
		//if list is empty, element is added to the top
		if (first==null){ addToTop(element); return;}
		
		//if element is already at the top of the list, nothing to do
		if(first.getElement().equals(element)) return;
		
		//searching for element in the list
		OrderedListNode tmp=first;
		while(tmp!=null){
		  if(element.equals(tmp.getElement())){
			//the element before tmp must point(next) to the element after tmp
			tmp.getPrev().setNext(tmp.getNext());
			//the element after tmp must point(prev) to the element before tmp
			if(tmp.getNext()!=null)  tmp.getNext().setPrev(tmp.getPrev());
			//element is moved to the top
			addToTop(element);
			return;
		  }
		  tmp=tmp.getNext();
		}

		//element is not present in the list, so it's added to the top
		addToTop(element);
	}
	
	/**
	 * Moves the specified element to the bottom of the list. <br>
	 * If element is not present in this list, it will be added to the bottom.
	 * @param element - the Object to be added 
	 */
	public void moveToBottom(Object element){
		//if list is empty, element is added to the top, which is also the bottom
		if (first==null){ addToTop(element); return;}
		
		//if element is already at the bottom of the list, nothing to do
		if(last.getElement().equals(element)) return;
		
		//searching for element in the list
		OrderedListNode tmp=first;
		while(tmp!=null){
		  if(element.equals(tmp.getElement())){
			//the element before tmp must point(next) to the element after tmp
			tmp.getPrev().setNext(tmp.getNext());
			//the element after tmp must point(prev) to the element before tmp
			if(tmp.getNext()!=null)  tmp.getNext().setPrev(tmp.getPrev());
			//element is moved to the bottom
			addToBottom(element);
			return;
		  }
		  tmp=tmp.getNext();
		}

		//element is not present in the list, so it's added to the bottom
		addToBottom(element);
	}
	
	/**
	 * Removes the specified element from the list, if present
	 * .
	 * @param element - the Object to be removed 
	 */
	public void remove(Object element){
		//empty list
		if(first==null) return;
		
		//element is at the top of the list
		if (first.getElement().equals(element)){
		  first=first.getNext();
		  if (first!=null) first.setPrev(null);
		  return;
		}
		
		//element is at the bottom of the list
		if (last.getElement().equals(element)){
		  last=last.getPrev();
		  if (last!=null) last.setNext(null);
		  return;
		}
		
		//searching for element in the list
		OrderedListNode tmp=first;
		while(tmp!=null){
		  if(element.equals(tmp.getElement())){			  
			//the element before tmp must point to the element after tmp as next
			tmp.getPrev().setNext(tmp.getNext());
			//the element after tmp must point to the element before tmp as previous
			if(tmp.getNext()!=null)  tmp.getNext().setPrev(tmp.getPrev());
			return;
		  }
		  tmp=tmp.getNext();
		}
		
	}
	
}

