package model_safecollection;

import java.util.List;
import java.util.Stack;

import model.model;

public abstract class safecollection <T extends model> {
	
	private Stack<T> list;
	private int SIZE=0;
	protected boolean NotifyPush;
	//public 	ObservableList<String> 	keyList;
	
	public safecollection()
	{
		list= new Stack<T>();
		//keyList= FXCollections.observableList(new ArrayList<String>());
	}
	
	public synchronized int push(T t)
	{
		list.push(t);
		//keyList.add(t.key());
		SIZE++;
		
		if(NotifyPush)
			synchronized(this){
				this.notifyAll();
			}
		return SIZE;
	}
	
	public synchronized T pop()
	{
		if(list.isEmpty())
			return null;
		
		SIZE--;
		return list.remove(0);
	}
	
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	public int size()
	{
		return SIZE;
	}
	
	public T find(String key)
	{
		T t= null;
		int size= SIZE;
		for(int i=0;i<size;i++)
		{
			t= list.get(i);
			if(t.key().contentEquals(key))
				return t;
		}
		
		return null;
	}
	
	public int indexOf(String key)
	{
		T t= null;
		int size= SIZE;
		for(int i=0;i<size;i++)
		{
			t= list.get(i);
			if(t.key().contentEquals(key))
				return i;
		}
		
		return -1;
	}
	
	public List<T> getList()
	{
		return list.subList(0, SIZE);
	}
	
	public List<T> getSublist(int initIndex)
	{
		return list.subList(initIndex, SIZE);
	}
}