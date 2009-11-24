package org.biomart.configurator.utils.type;

import java.util.HashMap;
import java.util.Map;



	/**
	 * This internal singleton class represents the cardinality of a relation.
	 * Note that the names of cardinality objects are case-sensitive.
	 */
	public class Cardinality implements Comparable<Cardinality> {
		private static final long serialVersionUID = 1L;

		private static final Map<String,Cardinality> singletons = new HashMap<String,Cardinality>();

		/**
		 * Use this constant to refer to a relation with many values at the
		 * second key end.
		 */
		public static final Cardinality MANY_B = Cardinality.get("M(b)");

		/**
		 * Use this constant to refer to a relation with many values at the
		 * first key end.
		 */
		public static final Cardinality MANY_A = Cardinality.get("M(a)");

		// TODO This is a backwards-compatibility clause that needs to
		// stay in throughout the 0.7 release. It can be removed in 0.8.
		/**
		 * Use this constant to refer to a relation with many values at the
		 * first key end.
		 */
		public static final Cardinality MANY = Cardinality.get("M");

		// End fudge-mode.

		/**
		 * Use this constant to refer to a 1:1 relation.
		 */
		public static final Cardinality ONE = Cardinality.get("1");

		/**
		 * The static factory method creates and returns a cardinality with the
		 * given name. It ensures the object returned is a singleton. Note that
		 * the names of cardinality objects are case-sensitive.
		 * 
		 * @param name
		 *            the name of the cardinality object.
		 * @return the cardinality object or null if null was passed in.
		 */
		public static Cardinality get(String name) {
			// Return null for null name.
			if (name == null)
				return null;

			// Do we already have this one?
			// If so, then return it.
			if (Cardinality.singletons.containsKey(name))
				return (Cardinality) Cardinality.singletons.get(name);

			// Otherwise, create it, remember it.
			final Cardinality c = new Cardinality(name);
			Cardinality.singletons.put(name, c);

			// Return it.
			return c;
		}

		private final String name;

		/**
		 * The private constructor takes a single parameter, which defines the
		 * name this cardinality object will display when printed.
		 * 
		 * @param name
		 *            the name of the cardinality.
		 */
		private Cardinality(final String name) {
			this.name = name;
		}

		public int compareTo(final Cardinality o) throws ClassCastException {
			final Cardinality c = (Cardinality) o;
			return this.toString().compareTo(c.toString());
		}

		public boolean equals(final Object o) {
			// We are dealing with singletons so can use == happily.
			return o == this;
		}

		/**
		 * Displays the name of this cardinality object.
		 * 
		 * @return the name of this cardinality object.
		 */
		public String getName() {
			return this.name;
		}

		public int hashCode() {
			return this.toString().hashCode();
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Always returns the name of this cardinality.
		 */
		public String toString() {
			return this.name;
		}
	}
