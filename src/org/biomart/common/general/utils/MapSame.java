package org.biomart.common.general.utils;

import java.util.ArrayList;
import java.util.List;

import org.biomart.transformation.helpers.RelationalInfo;

public class MapSame<K,V> {
	
	public static void main(String[] args) {
		
		MapSame<RelationalInfo, String> map = new MapSame<RelationalInfo, String>();
		RelationalInfo r1 = new RelationalInfo("t1", "k1", "c1");
		RelationalInfo r2 = new RelationalInfo("t2", "k2", "c2");
		RelationalInfo r3 = new RelationalInfo("t3", "k3", "c3");
		RelationalInfo r4 = r1;
		RelationalInfo r5 = new RelationalInfo("t1", "k1", "c1");
		
		map.put(r1, "r1");
		map.put(r2, "r2");
		map.put(r3, "r3");
		map.put(r4, "r4");
		map.put(r5, "r5");
		
		System.out.println(map);	// ==> [{tableName = t1, keyName = k1, columnName = c1=r4},{tableName = t2, keyName = k2, columnName = c2=r2},{tableName = t3, keyName = k3, columnName = c3=r3},{tableName = t1, keyName = k1, columnName = c1=r5}]
	}
	
	private List<K> keys = null;
	private List<V> values = null;
	public MapSame() {
		super();
		this.keys = new ArrayList<K>();
		this.values = new ArrayList<V>();
	}
	public void put(K k, V v) {
		Integer index = getIndex(k);
		if (null==index) {
			keys.add(k);
			values.add(v);
		} else {
			values.set(index, v);
		}
	}
	public V get(K k) {
		Integer index = getIndex(k);
		return index!=null ? this.values.get(index) : null;
	}
	private Integer getIndex(K k) {
		for (int i = 0; i < keys.size(); i++) {
			K k2 = keys.get(i);
			if (k2==k) {	// "==" not "equals" !
				return i;
			}
		}
		return null;
	}
	public List<K> keys() {
		return this.keys;
	}
	public List<V> values() {
		return this.values;
	}
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("[");
		for (int i = 0; i < this.keys.size(); i++) {	// they should be the same size
			K k = this.keys.get(i);
			V v = this.values.get(i);			
			stringBuffer.append((i==0 ? "" : ",") + "{" + k + "=" + v + "}");
		}
		stringBuffer.append("]");
		return stringBuffer.toString();
	}
}
