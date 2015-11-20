package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayOperations {


	public static <T> Collection<T> getSubsetByType(Collection<? super T> col, Class<T> type) {
		final ArrayList<T> ret = new ArrayList<T>();
		for (Object o : col)
			if (type.isInstance(o))
				ret.add((T)o);
		return ret;
	}



}
