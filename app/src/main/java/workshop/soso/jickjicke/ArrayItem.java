package workshop.soso.jickjicke;

import android.database.Cursor;

/**
 * Created by taroguru on 2017. 2. 11..
 */

public class ArrayItem extends Item{

    public ArrayItem(){
        super();
        check = false;
    }
    private boolean check;
    public boolean isChecked() {
        return check;
    }

    public void setChecked(boolean b) {
        check = b;
    }
    public void setData(Cursor cursor) {
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
