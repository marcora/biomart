package org.biomart.builder.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.biomart.common.resources.Resources;

	/**
	 * This implementation is a simple foreign key.
	 */
	public  class ForeignKey extends Key {
		private static final long serialVersionUID = 1L;

		private final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				if (!ForeignKey.this.getTable().getForeignKeys().contains(
						ForeignKey.this)) {
					final List<Relation> deadRels = new ArrayList<Relation>(ForeignKey.this
							.getRelations());
					for (final Iterator<Relation> i = deadRels.iterator(); i.hasNext();) {
						final Relation rel = (Relation) i.next();
						rel.getOtherKey(ForeignKey.this).getRelations().remove(rel);
						i.remove();
						//ForeignKey.this.getRelations().remove(rel);
					}
				}
			}
		};

		/**
		 * The constructor passes on all its work to the {@link Key}
		 * constructor. It then adds itself to the set of foreign keys on the
		 * parent table.
		 * 
		 * @param columns
		 *            the list of columns to form the key over.
		 */
		public ForeignKey(final Column[] columns) {
			super(columns);

			// If we are removed from the table, remove all our relations.
			this.getTable().getForeignKeys().addPropertyChangeListener(
					this.listener);
		}

		public String toString() {
			return super.toString() + " {" + Resources.get("fkPrefix") + "}";
		}
	}
