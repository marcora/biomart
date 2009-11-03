package org.biomart.builder.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.biomart.common.resources.Resources;

	/**
	 * This implementation is a simple primary key.
	 */
	public class PrimaryKey extends Key {
		private static final long serialVersionUID = 1L;

		private final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				if (!PrimaryKey.this.equals(PrimaryKey.this.getTable()
						.getPrimaryKey())) {
					final List deadRels = new ArrayList(PrimaryKey.this
							.getRelations());
					for (final Iterator i = deadRels.iterator(); i.hasNext();) {
						final Relation rel = (Relation) i.next();
						PrimaryKey.this.getRelations().remove(rel);
						rel.getOtherKey(PrimaryKey.this).getRelations().remove(
								rel);
					}
				}
			}
		};

		/**
		 * The constructor passes on all its work to the {@link Key}
		 * constructor.
		 * 
		 * @param columns
		 *            the list of columns to form the key over.
		 */
		public PrimaryKey(final Column[] columns) {
			super(columns);

			// If we are removed from the table, remove all our relations.
			this.getTable().addPropertyChangeListener("primaryKey",
					this.listener);
		}

		public String toString() {
			return super.toString() + " {" + Resources.get("pkPrefix") + "}";
		}
	}
