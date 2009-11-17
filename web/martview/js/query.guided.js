Ext.ns('Martview.query');

Martview.query.Guided = Ext.extend(Ext.form.FormPanel, {

  // hard config
  initComponent: function() {
    var config = {
      items: [{
        xtype: 'fieldset',
        itemId: 'filters',
        ref: 'filters',
        title: 'Filters',
        autoHeight: true,
        autoDestroy: true,
        defaults: {
          anchor: '100%',
          // labelSeparator: '',
          labelStyle: 'font-weight: bold !important; font-size: 8pt !important; color: #444 !important;'
        }
      }]
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.query.Guided.superclass.initComponent.apply(this, arguments);
  },

  load: function(args) {
    var form = this;

    // remove fields from filters fieldset
    form.filters.removeAll();

    // add fields to filters fieldset
    if (args.facets) form.filters.add(args.facets);

    // refresh form layout
    form.doLayout();

    // add handlers to combo/facet fields
    form.filters.items.each(function(item) {
      if (item.xtype == 'combo') {
        item.on('select', function(item) {
          form.filters.add({
            xtype: 'hidden',
            name: item.getName(),
            value: item.getValue()
          });
          // submit query
          form.ownerCt.submit();
        });
      } else if (item.xtype == 'facetfield') {
        item.on('triggerclick', function(item) {
          form.filters.add({
            xtype: 'unfacetfield',
            name: item.getName(),
            value: item.getValue()
          });
          // submit query
          form.ownerCt.submit();
        });
      }
    });
  },

  reset: function() {
    var form = this;
    form.filters.items.each(function(item) {
      if (item.xtype == 'facetfield') {
        form.filters.add({
          xtype: 'unfacetfield',
          name: item.getName(),
          value: item.getValue()
        });
      }
    });

    // submit query
    form.ownerCt.submit();
  },

  focus: function() {
    form.filters.items.first().focus(false, true);
  },

  build: function(args) {
    var form = this;

    // build array of facet field names
    var facet_fields = [];
    Ext.each(args.facet_fields, function(facet_field) {
      facet_fields.push(facet_field.name);
    });

    // build array of filters
    var filters = [];
    form.filters.items.each(function(item) {
      var filter = {
        name: item.getName(),
        value: item.getValue()
      };
      if (item.xtype in {
        'hidden': '',
        'facetfield': ''
      }) {
        filters.push(filter.name + ':' + filter.value);
      }
    });
    form.filters.items.each(function(item) {
      var filter = {
        name: item.getName(),
        value: item.getValue()
      };
      if (item.xtype in {
        'unfacetfield': ''
      }) {
        filters.remove(filter.name + ':' + filter.value);
      }
    });

    // build query params
    var query_params = {
      type: 'search',
      q: '*:*',
      facet_fields: facet_fields.join('|'),
      filters: filters.join('|')
    };

    return query_params;
  }
});

Ext.reg('guidedquery', Martview.query.Guided);
