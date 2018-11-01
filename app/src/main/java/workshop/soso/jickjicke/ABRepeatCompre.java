package workshop.soso.jickjicke;

import java.util.Comparator;

public class ABRepeatCompre implements Comparator< ABRepeat> {

	@Override
	public int compare(ABRepeat lhs, ABRepeat rhs) {
		int result = 0;
		if(lhs.getStart()  < rhs.getStart())		{	result = 	-1;}
		else if(lhs.getStart() == rhs.getStart())	{	result = 	 0;}
		else										{	result = 	 1;}
		return result;
	}

}
