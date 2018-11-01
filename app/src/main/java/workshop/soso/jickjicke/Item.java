package workshop.soso.jickjicke;

/**
 * Created by taroguru on 2015. 1. 30..
 */
public class Item {
    protected String name;
    protected long id;  //db item columns

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public  void setName(String name)
    {
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public Item()
    {
        id = 0;
        name = "";
    }
//    public void setName(String name);
}
