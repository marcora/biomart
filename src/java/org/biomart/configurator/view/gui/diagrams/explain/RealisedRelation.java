package org.biomart.configurator.view.gui.diagrams.explain;

import org.biomart.builder.model.Key;
import org.biomart.builder.model.Relation;
import org.biomart.common.exceptions.AssociationException;
import org.biomart.configurator.utils.type.Cardinality;
import org.biomart.configurator.view.gui.diagrams.contexts.ExplainContext;

/**
 * A realised relation is a generic relation with a specific iteration.
 */
public class RealisedRelation extends Relation {
	private static final long serialVersionUID = 1L;

	private final Relation relation;

	private final int relationIteration;

	private final ExplainContext explainContext;

	/**
	 * Use this constant to refer to a relation that covers all iterations,
	 * not just the realised one.
	 */
	public static final int NO_ITERATION = -1;

/*		private final PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent e) {
				final PropertyChangeEvent ours = new PropertyChangeEvent(
						RealisedRelation.this, e.getPropertyName(), e
								.getOldValue(), e.getNewValue());
				ours.setPropagationId(e.getPropagationId());
				RealisedRelation.this.pcs.firePropertyChange(ours);
			}
		};
*/
	/**
	 * Constructs a realised relation.
	 * 
	 * @param sourceKey
	 *            the realised source key.
	 * @param targetKey
	 *            the realised target key.
	 * @param cardinality
	 *            the realised cardinality.
	 * @param relation
	 *            the original relation.
	 * @param relationIteration
	 *            the original relation iteration.
	 * @param explainContext
	 *            the explain context for displaying this realised relation.
	 * @throws AssociationException
	 *             if the relation could not be established.
	 */
	public RealisedRelation(final Key sourceKey, final Key targetKey,
			final Cardinality cardinality,
			final Relation relation, final int relationIteration,
			final ExplainContext explainContext)
			throws AssociationException {
		super(sourceKey, targetKey, cardinality);
		this.relation = relation;
		this.relationIteration = relationIteration;
		this.explainContext = explainContext;
//			relation.addPropertyChangeListener(this.listener);
	}

	/**
	 * @return the explainContext
	 */
	public ExplainContext getExplainContext() {
		return this.explainContext;
	}

	/**
	 * @return the relation
	 */
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * @return the relationIteration
	 */
	public int getRelationIteration() {
		return this.relationIteration;
	}
}
