import java.util.*;

// таблиці символів
public class Table {
	private HashMap<Token, Id> table;
	protected Table prev;

	// створення нової таблиці символів 
	// і додавання її до ланцюжка таблиць
	public Table(Table n) { table = new HashMap<Token, Id>(); prev = n; }

	// додавання нового запису в таблицю
	public void put(Token w, Id i) { table.put(w, i); }

	// отримання запису для ідентифікатора
	// шляхом пошуку в ланцюжку таблиць
	public Id get(Token w) {
		for( Table e = this; e != null; e = e.prev ) {
			Id found = (Id)(e.table.get(w));
			if( found != null ) return found;
		}
		return null;
	}
}