import java.util.*;

// ������� �������
public class Table {
	private HashMap<Token, Id> table;
	protected Table prev;

	// ��������� ���� ������� ������� 
	// � ��������� �� �� �������� �������
	public Table(Table n) { table = new HashMap<Token, Id>(); prev = n; }

	// ��������� ������ ������ � �������
	public void put(Token w, Id i) { table.put(w, i); }

	// ��������� ������ ��� ��������������
	// ������ ������ � �������� �������
	public Id get(Token w) {
		for( Table e = this; e != null; e = e.prev ) {
			Id found = (Id)(e.table.get(w));
			if( found != null ) return found;
		}
		return null;
	}
}