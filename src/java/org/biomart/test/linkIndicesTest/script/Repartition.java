package org.biomart.test.linkIndicesTest.script;

public enum Repartition {
	TOP				{	int getRangeStart(int size, int overlap) {	return 0;	}	},
	MIDDLE			{	int getRangeStart(int size, int overlap) {	return (int)((double)size/2)-(int)((double)overlap/2);	}	},
	BOTTOM			{	int getRangeStart(int size, int overlap) {	return size-overlap;	}	},
	SCATTERED		{};

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}	
	
	int getRangeStart(int size, int overlap) {return -1;};
}
