Ext.ns('Martview.search');

Martview.search.User = Ext.extend(Ext.form.FormPanel, {

  // hard config
  initComponent: function() {
    var config = {
      padding: 10,
      bodyStyle: 'background-color:#dfe8f6;',
      labelAlign: 'top',
      defaults: {
        anchor: '100%'
      },
      items: [{
        xtype: 'fieldset',
        title: 'Filters',
        itemId: 'filters',
        ref: 'filters',
        autoHeight: true,
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
    Martview.search.Advanced.superclass.initComponent.apply(this, arguments);
  },

  update: function(args) {
    var form = this;

    // remove fields from search form
    form.removeAll();

    // add fieldset to search form
    var fieldset = form.add({
      xtype: 'fieldset',
      title: 'Filters',
      itemId: 'filters',
      ref: 'filters',
      autoHeight: true,
      defaults: {
        anchor: '100%',
        // labelSeparator: '',
        labelStyle: 'font-weight: bold !important; font-size: 8pt !important; color: #444 !important;'
      }
    });

    // add filters to search form
    Ext.each(args.filters, function(filter) {
      if (filter.qualifier) { // filter.qualifier should never be null or undefined!
        if (filter.qualifier in {
          '=': '',
          '>': '',
          '<': '',
          '>=': '',
          '<=': ''
        }) {
          if (filter.options) {
            fieldset.add([{
              xtype: 'combo',
              itemId: filter.name,
              name: filter.name,
              fieldLabel: filter.display_name || filter.name,
              editable: false,
              forceSelection: true,
              lastSearchTerm: false,
              triggerAction: 'all',
              mode: 'local',
              store: filter.options.split(',')
            }]);
          } else {
            fieldset.add([{
              xtype: 'textfield',
              itemId: filter.name,
              name: filter.name,
              fieldLabel: filter.display_name || filter.name
            }]);
          }
        } else if (filter.qualifier.split(',').remove('=') in {
          'in': ''
        }) {
          fieldset.add({
            xtype: 'textarea',
            height: '100',
            itemId: filter.name + '_text',
            name: filter.name,
            fieldLabel: filter.display_name || filter.name
          });
          fieldset.add({
            xtype: 'fileuploadfield',
            itemId: filter.name + '_file',
            name: filter.name,
            hideLabel: true,
            // buttonOnly: true,
            buttonText: 'Upload file&hellip;'
          });
        } else if (filter.qualifier.split(',')[0] in {
          'only': '',
          'excluded': ''
        }) {
          var items = [];
          Ext.each(filter.qualifier.split(','), function(item) {
            items.push({
              inputValue: item,
              name: filter.name,
              boxLabel: item
            });
          });
          fieldset.add({
            xtype: 'radiogroup',
            itemId: filter.name,
            name: filter.name,
            fieldLabel: filter.display_name || filter.name,
            items: items
            // vertical: true,
            // columns: 1,
          });
        }

        // set field value if defined
        var field = fieldset.get(filter.name);
        if (field && filter.value) {
          field.setValue(filter.value);
        }
      }
    });

    // refresh form layout and focus
    form.doLayout();
    form.focus();
  },

  reset: function() {
    var form = this;
    form.getForm().reset();
    form.focus();
  },

  focus: function() {
    try {
      this.filters.items.first().focus(false, true);
    } catch(e) {
      // pass
    }
  },

  build: function() {
    // TODO
  }
});

Ext.reg('usersearch', Martview.search.User);
