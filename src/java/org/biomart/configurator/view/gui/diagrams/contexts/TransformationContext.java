/*
 Copyright (C) 2006 EBI
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the itmplied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.biomart.configurator.view.gui.diagrams.contexts;

import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.biomart.builder.model.Column;
import org.biomart.builder.model.DataSet;
import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.Mart;

/**
 * This context is basically the same as {@link TransformationContext}, except
 * it only provides context menus and adaptations for {@link DataSetColumn}
 * instances.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.16 $, $Date: 2007/10/31 10:32:56 $, modified by
 *          $Author: rh4 $
 * @since 0.5
 */
public class TransformationContext extends DataSetContext {

	/**
	 * Creates a new context that will adapt objects according to the settings
	 * in the specified dataset.
	 * 
	 * @param martTab
	 *            the mart tab this context appears in.
	 * @param dataset
	 *            the dataset this context will use for customising menus and
	 *            colours.
	 */
	public TransformationContext(final Mart mart, final DataSet dataset) {
		super(mart, dataset);
	}

	public void customiseAppearance(final JComponent component,
			final Object object) {
		super.customiseAppearance(component, object);
	}

	public boolean isMasked(final Object object) {
		return false;
	}

	public void populateContextMenu(final JPopupMenu contextMenu,
			final Object object) {
		super.populateContextMenu(contextMenu, object);
	}

	public void populateMultiContextMenu(final JPopupMenu contextMenu,
			final Collection selectedItems, final Class clazz) {

		// Don't process anything except columns.
		if (Column.class.isAssignableFrom(clazz))
			super.populateMultiContextMenu(contextMenu, selectedItems, clazz);
	}

}
