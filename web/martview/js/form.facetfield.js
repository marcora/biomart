Ext.ns('Martview.form');

Martview.form.FacetField = Ext.extend(Ext.form.TriggerField, {

  fieldClass: "x-form-facet-field",
  triggerClass: 'x-form-clear-trigger',

  initComponent: function() {
    Ext.form.TriggerField.superclass.initComponent.call(this);
    this.addEvents('triggerclick');
  },

  onTriggerClick: function() {
    this.fireEvent('triggerclick', this);
  }
});

Ext.reg('facetfield', Martview.form.FacetField);

Martview.form.UnFacetField = Ext.extend(Ext.form.Hidden, {});

Ext.reg('unfacetfield', Martview.form.UnFacetField);
