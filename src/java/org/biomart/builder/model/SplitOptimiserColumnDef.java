package org.biomart.builder.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.biomart.common.utils.Transaction;
import org.biomart.common.utils.Transaction.TransactionEvent;
import org.biomart.common.utils.Transaction.TransactionListener;


	/**
	 * Defines an split optimiser column for a table.
	 */
	public  class SplitOptimiserColumnDef implements TransactionListener {
		private static final long serialVersionUID = 1L;

		private String separator;

		private String contentCol;

		private boolean directModified = false;

		private boolean prefix = true;

		private boolean suffix = true;

		private int size = 255;

		private final PropertyChangeSupport pcs = new PropertyChangeSupport(
				this);

		private final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent e) {
				SplitOptimiserColumnDef.this.setDirectModified(true);
			}
		};

		/**
		 * This constructor makes a new split opt definition.
		 * 
		 * @param colKey
		 *            the name of the column to get values from.
		 * @param separator
		 *            the separator to put between values.
		 */
		public SplitOptimiserColumnDef(final String colKey, String separator) {
			// Test for good arguments.
			if (separator == null)
				separator = "";

			// Remember the settings.
			this.contentCol = colKey;
			this.separator = separator;

			Transaction.addTransactionListener(this);

			this.addPropertyChangeListener(this.listener);
		}

		/**
		 * Construct an exact replica.
		 * 
		 * @return the replica.
		 */
		public SplitOptimiserColumnDef replicate() {
			return new SplitOptimiserColumnDef(this.contentCol, this.separator);
		}

		public boolean isDirectModified() {
			return this.directModified;
		}

		public void setDirectModified(final boolean modified) {
			if (modified == this.directModified)
				return;
			final boolean oldValue = this.directModified;
			this.directModified = modified;
			this.pcs.firePropertyChange("directModified", oldValue, modified);
		}

		public boolean isVisibleModified() {
			return false;
		}

		public void setVisibleModified(final boolean modified) {
			// Ignore, for now.
		}

		public void transactionResetVisibleModified() {
			// Ignore, for now.
		}

		public void transactionResetDirectModified() {
			this.directModified = false;
		}

		public void transactionStarted(final TransactionEvent evt) {
			// Don't really care for now.
		}

		public void transactionEnded(final TransactionEvent evt) {
			// Ignore for now.
		}

		/**
		 * Adds a property change listener.
		 * 
		 * @param listener
		 *            the listener to add.
		 */
		public void addPropertyChangeListener(
				final PropertyChangeListener listener) {
			this.pcs.addPropertyChangeListener(listener);
		}

		/**
		 * Adds a property change listener.
		 * 
		 * @param property
		 *            the property to listen to.
		 * @param listener
		 *            the listener to add.
		 */
		public void addPropertyChangeListener(final String property,
				final PropertyChangeListener listener) {
			this.pcs.addPropertyChangeListener(property, listener);
		}

		/**
		 * Returns the separator.
		 * 
		 * @return the separator.
		 */
		public String getSeparator() {
			return this.separator;
		}

		/**
		 * Get the name of the value column.
		 * 
		 * @return the name.
		 */
		public String getContentCol() {
			return this.contentCol;
		}

		/**
		 * @return the prefix
		 */
		public boolean isPrefix() {
			return this.prefix;
		}

		/**
		 * @param prefix
		 *            the prefix to set
		 */
		public void setPrefix(boolean prefix) {
			if (prefix == this.prefix)
				return;
			final boolean oldValue = this.prefix;
			this.prefix = prefix;
			this.pcs.firePropertyChange("prefix", oldValue, prefix);
		}

		/**
		 * @return the suffix
		 */
		public boolean isSuffix() {
			return this.suffix;
		}

		/**
		 * @param suffix
		 *            the suffix to set
		 */
		public void setSuffix(boolean suffix) {
			if (suffix == this.suffix)
				return;
			final boolean oldValue = this.suffix;
			this.suffix = suffix;
			this.pcs.firePropertyChange("suffix", oldValue, suffix);
		}

		/**
		 * @return the size
		 */
		public int getSize() {
			return this.size;
		}

		/**
		 * @param size
		 *            the size to set
		 */
		public void setSize(int size) {
			if (size == this.size)
				return;
			final int oldValue = this.size;
			this.size = size;
			this.pcs.firePropertyChange("size", oldValue, size);
		}
	}
