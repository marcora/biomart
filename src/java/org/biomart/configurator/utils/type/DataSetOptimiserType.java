package org.biomart.configurator.utils.type;

import java.util.LinkedHashMap;
import java.util.Map;

	/**
	 * This class defines the various different ways of optimising a dataset
	 * after it has been constructed, eg. adding boolean columns.
	 */
	public class DataSetOptimiserType implements Comparable {
		private static final long serialVersionUID = 1L;

		/**
		 * Use this constant to refer to no optimisation.
		 */
		public static final DataSetOptimiserType NONE = new DataSetOptimiserType(
				"NONE", false, false, false);

		/**
		 * Parent tables will inherit copies of count columns from child tables.
		 */
		public static final DataSetOptimiserType COLUMN_INHERIT = new DataSetOptimiserType(
				"COLUMN_INHERIT", false, false, false);

		/**
		 * Parent tables will inherit copies of count tables from child tables.
		 */
		public static final DataSetOptimiserType TABLE_INHERIT = new DataSetOptimiserType(
				"TABLE_INHERIT", false, true, false);

		/**
		 * Parent tables will inherit copies of bool columns from child tables.
		 */
		public static final DataSetOptimiserType COLUMN_BOOL_INHERIT = new DataSetOptimiserType(
				"COLUMN_BOOL_INHERIT", true, false, false);

		/**
		 * Parent tables will inherit copies of bool tables from child tables.
		 */
		public static final DataSetOptimiserType TABLE_BOOL_INHERIT = new DataSetOptimiserType(
				"TABLE_BOOL_INHERIT", true, true, false);

		/**
		 * Parent tables will inherit copies of bool columns from child tables.
		 */
		public static final DataSetOptimiserType COLUMN_BOOL_NULL_INHERIT = new DataSetOptimiserType(
				"COLUMN_BOOL_NULL_INHERIT", true, false, true);

		/**
		 * Parent tables will inherit copies of bool tables from child tables.
		 */
		public static final DataSetOptimiserType TABLE_BOOL_NULL_INHERIT = new DataSetOptimiserType(
				"TABLE_BOOL_NULL_INHERIT", true, true, true);

		private final String name;

		private final boolean bool;

		private final boolean table;

		private final boolean useNull;

		/**
		 * The private constructor takes a single parameter, which defines the
		 * name this optimiser type object will display when printed.
		 * 
		 * @param name
		 *            the name of the optimiser type.
		 * @param bool
		 *            <tt>true</tt> if bool values (0,1) should be used
		 *            instead of counts.
		 * @param table
		 *            <tt>true</tt> if columns should live in their own
		 *            tables.
		 * @param useNull
		 *            if this is a bool column, use null/1 instead of 0/1.
		 */
		private DataSetOptimiserType(final String name, final boolean bool,
				final boolean table, final boolean useNull) {
			this.name = name;
			this.bool = bool;
			this.table = table;
			this.useNull = useNull;
		}

		public int compareTo(final Object o) throws ClassCastException {
			final DataSetOptimiserType c = (DataSetOptimiserType) o;
			return this.toString().compareTo(c.toString());
		}

		public boolean equals(final Object o) {
			// We are dealing with singletons so can use == happily.
			return o == this;
		}

		/**
		 * Displays the name of this optimiser type object.
		 * 
		 * @return the name of this optimiser type object.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Return <tt>true</tt> if columns counts should be replaced by 0/1
		 * boolean-style values.
		 * 
		 * @return <tt>true</tt> if columns counts should be replaced by 0/1
		 *         boolean-style values.
		 */
		public boolean isBool() {
			return this.bool;
		}

		/**
		 * Return <tt>true</tt> if columns 0/1 values should be replaced by
		 * null/1 equivalents.
		 * 
		 * @return <tt>true</tt> if columns 0/1 values should be replaced by
		 *         null/1 equivalents.
		 */
		public boolean isUseNull() {
			return this.useNull;
		}

		/**
		 * Return <tt>true</tt> if columns should live in their own table.
		 * 
		 * @return <tt>true</tt> if columns should live in their own table.
		 */
		public boolean isTable() {
			return this.table;
		}

		public int hashCode() {
			return this.toString().hashCode();
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * The method simply returns the name of the optimiser type.
		 */
		public String toString() {
			return this.getName();
		}

		/**
		 * Return the types of optimiser column available.
		 * 
		 * @return the types available. Keys are internal names for the types,
		 *         values are the actual type instances.
		 */
		public static Map getTypes() {
			final Map optimiserTypes = new LinkedHashMap();
			optimiserTypes.put("None", DataSetOptimiserType.NONE);
			optimiserTypes.put("ColumnInherit",
					DataSetOptimiserType.COLUMN_INHERIT);
			optimiserTypes.put("ColumnBoolInherit",
					DataSetOptimiserType.COLUMN_BOOL_INHERIT);
			optimiserTypes.put("ColumnBoolNullInherit",
					DataSetOptimiserType.COLUMN_BOOL_NULL_INHERIT);
			optimiserTypes.put("TableInherit",
					DataSetOptimiserType.TABLE_INHERIT);
			optimiserTypes.put("TableBoolInherit",
					DataSetOptimiserType.TABLE_BOOL_INHERIT);
			optimiserTypes.put("TableBoolNullInherit",
					DataSetOptimiserType.TABLE_BOOL_NULL_INHERIT);
			return optimiserTypes;
		}
	}
