package workshop.soso.jickjicke;

import java.util.ArrayList;


/**
 * Created by taroguru on 2015. 1. 31..
 */
public class ItemList <T extends Item> extends ArrayItem{

    protected ArrayList<T> itemlist;

    public ItemList() {
        super();
        id = -1;
        name = new String();
        itemlist = new ArrayList<>();
    }

    public T getLast()
    {
        T result = null;
        if(itemlist != null && itemlist.size() >= 1)    result = itemlist.get(itemlist.size()-1);
        return result;
    }

    public ArrayList<T> getItemlist() {
        return itemlist;
    }

    public void setItemlist(ArrayList<T> itemlist) {
        this.itemlist = itemlist;
    }

    public boolean addItem(T item) {
        return itemlist.add(item);
    }

    public void removeAllItems() {
        itemlist.clear();
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int size(){return itemlist.size();}

    public void clear() {
        if(itemlist != null)    itemlist.clear();
    }

    public T get(int position) {
        T result = null;
        if(itemlist != null && itemlist.size() > position)
        {
            result = itemlist.get(position);
        }
        return result;
    }
}
