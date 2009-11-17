Ext.ns('Martview');

Martview.Query = Ext.extend(Ext.Panel, {

  // hard config
  initComponent: function() {
    var config = {
      id: 'query',
      ref: '../query',
      region: 'west',
      layout: 'fit',
      title: 'Query',
      border: true,
      width: 400,
      split: true,
      bodyStyle: 'background-color:#dfe8f6;',
      tbar: {
        // cls: 'x-panel-header',
        // height: 26,
        items: [{
          itemId: 'select',
          ref: '../selectButton',
          cls: 'x-btn-text-icon',
          text: 'Advanced',
          iconCls: 'advanced-query-icon',
          disabled: true,
          tooltip: 'Click to select the query view format',
          menu: {
            items: [{
              itemId: 'simple',
              text: 'Simple',
              iconCls: 'simple-query-icon'
            },
            {
              itemId: 'guided',
              text: 'Guided',
              iconCls: 'guided-query-icon'
            },
            {
              itemId: 'advanced',
              text: 'Advanced',
              iconCls: 'advanced-query-icon'
            },
            {
              itemId: 'user',
              text: 'User-defined',
              // &sdot; <span style="text-decoration: underline !important;">Dimeric protein structures at high-res</span>',
              iconCls: 'user-query-icon',
              menu: [{
                text: 'Human genes on chromosome 1',
                iconCls: 'simple-query-icon'
              },
              {
                text: 'Dimeric protein structures at high-res',
                iconCls: 'guided-query-icon'
              },
              {
                text: 'Human genes on chromosome 7 associated with Huntington\'s disease',
                iconCls: 'advanced-query-icon'
              }]
            }],
            listeners: {
              'itemclick': {
                fn: function(item) {
                  var query = this;
                  var args = {
                    query: item.getItemId()
                  };
                  query.select(args);
                },
                scope: this // query
              }
            }
          }
        },
        '-', {
          itemId: 'customize',
          ref: '../customizeButton',
          cls: 'x-btn-text-icon',
          text: 'Add/remove filters',
          iconCls: 'customize-icon',
          disabled: true,
          tooltip: 'Click to customize the query form by adding/removing filters',
          handler: function() {
            var query = this;
            query.customize();
          },
          scope: this // query
        },
        {
          itemId: 'save',
          ref: '../saveButton',
          cls: 'x-btn-text-icon',
          text: 'Save query',
          iconCls: 'save-icon',
          disabled: true,
          tooltip: 'Click to save the current query in various formats',
          handler: function() {
            var query = this;
            query.save();
          },
          scope: this // query
        }]
      },
      bbar: ['->', {
        itemId: 'reset',
        ref: '../resetButton',
        cls: 'x-btn-text-icon',
        text: 'Reset',
        iconCls: 'reset-icon',
        disabled: true,
        tooltip: 'Click to reset the current query',
        handler: function() {
          var query = this;
          query.reset();
        },
        scope: this // query
      },
      {
        itemId: 'submit',
        ref: '../submitButton',
        cls: 'x-btn-text-icon',
        text: 'Submit',
        iconCls: 'submit-icon',
        disabled: true,
        tooltip: 'Click to submit the current query',
        handler: function() {
          var query = this;
          query.submit();
        },
        scope: this // query
      }],
      autoDestroy: true,
      autoScroll: true,
      defaults: {
        border: false,
        autoDestroy: true,
        autoWidth: true,
        autoHeight: true,
        fitToFrame: true,
        padding: 10,
        bodyStyle: 'background-color:#dfe8f6;',
        labelAlign: 'top',
        defaults: {
          anchor: '100%'
        }
      },
      items: []
    };

    // add custom events
    this.addEvents('select', 'submit', 'customize', 'save');

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.Query.superclass.initComponent.apply(this, arguments);
  },

  select: function(args) {
    var query = this;
    query.fireEvent('select', args);
  },

  submit: function() {
    var query = this;
    query.fireEvent('submit');
  },

  customize: function() {
    var query = this;
    query.fireEvent('customize');
  },

  save: function() {
    var query = this;
    query.fireEvent('save');
  },

  update: function(args) {
    var query = this;

    // if advanced query remember previously set filter values
    if (args.query == 'advanced' && query.current == 'advanced') {
      var filters = query.items.first().getForm().getValues();
    }

    // enable buttons
    query.selectButton.enable();
    query.selectButton.setIconClass(args.query + '-query-icon');
    query.selectButton.setText(args.query.charAt(0).toUpperCase() + args.query.slice(1));
    query.customizeButton.disable().hide();
    query.saveButton.enable();
    query.resetButton.enable();
    query.submitButton.enable();

    if (args.query == 'simple') {
      // pass
    } else if (args.query == 'guided') {
      query.submitButton.disable();
    } else if (args.query == 'advanced') {
      query.customizeButton.enable().show();
    } else if (args.query == 'user') {
      query.customizeButton.enable().show();
    }

    // update query panel
    query.removeAll();
    query.add({
      xtype: args.query + 'query',
      itemId: args.query,
      ref: args.query
    });

    // refresh query panel
    query.doLayout();

    // load filters
    query.items.first().load(args);

    // if advanced query set previously remembered filter values
    if (args.query == 'advanced' && query.current == 'advanced') {
      query.items.first().filters.items.each(function(filter) {
        var value = filters[filter.name];
        if (value) {
          filter.setValue(value);
        }
      });
    }

    // submit query upon select
    if (args.query != query.current) {
      query.submit();
    }

    // remember current query view
    query.current = args.query;
  },

  clear: function() {
    var query = this;
    delete query.current;
    query.removeAll();
    query.selectButton.disable();
    query.customizeButton.disable();
    query.saveButton.disable();
    query.resetButton.disable();
    query.submitButton.disable();
    query.doLayout();
  },

  reset: function() {
    var query = this;

    // reset form
    query.items.first().reset();
  },

  build: function(args) {
    var query = this;
    return query.items.first().build(args);
  },

  isValid: function() {
    return this.items.first().getForm().isValid();
  },

  focus: function() {
    try {
      this.items.first().focus();
    } catch(e) {
      // pass
    }
  }
});

Ext.reg('query', Martview.Query);
