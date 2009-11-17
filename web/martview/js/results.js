Ext.ns('Martview');

Martview.Results = Ext.extend(Ext.Panel, {

  // hard config
  initComponent: function() {
    var config = {
      id: 'results',
      ref: '../results',
      region: 'center',
      layout: 'fit',
      title: 'Results',
      border: true,
      tbar: {
        // cls: 'x-panel-header',
        // height: 26,
        items: [{
          itemId: 'select',
          ref: '../selectButton',
          cls: 'x-btn-text-icon',
          text: 'Tabular',
          iconCls: 'tabular-results-icon',
          disabled: true,
          tooltip: 'Click to select the results view format',
          menu: {
            items: [{
              text: 'Tabular',
              itemId: 'tabular',
              iconCls: 'tabular-results-icon'
            },
            {
              text: 'Itemized',
              itemId: 'itemized',
              iconCls: 'itemized-results-icon'
            },
            {
              text: 'Mapped',
              itemId: 'mapped',
              iconCls: 'mapped-results-icon'
            },
            {
              text: 'Aggregated',
              itemId: 'aggregated',
              iconCls: 'aggregated-results-icon'
            }],
            listeners: {
              'itemclick': {
                fn: function(item) {
                  var results = this;
                  var args = {
                    results: item.getItemId()
                  };
                  results.select(args);
                },
                scope: this // results
              }
            }
          }
        },
        '-', {
          itemId: 'customize',
          ref: '../customizeButton',
          cls: 'x-btn-text-icon',
          text: 'Add/remove columns',
          iconCls: 'customize-icon',
          disabled: true,
          tooltip: 'Click to customize the results grid by adding/removing columns',
          handler: function() {
            var results = this;
            results.customize();
          },
          scope: this // results
        },
        {
          itemId: 'save',
          ref: '../saveButton',
          cls: 'x-btn-text-icon',
          text: 'Save results',
          iconCls: 'save-icon',
          disabled: true,
          tooltip: 'Click to save the current results in various formats',
          handler: function() {
            var results = this;
            results.save();
          },
          scope: this // results
        }]
      },
      bbar: [{
        xtype: 'tbtext',
        itemId: 'counter',
        ref: '../counter',
        text: '&nbsp;'
      },
      {
        text: '&nbsp;',
        disabled: true
      }],
      autoDestroy: true,
      defaults: {
        border: false,
        autoDestroy: true
        // autoWidth: true,
        // autoHeight: true,
        // fitToFrame: true
      },
      items: []
    };

    // add custom events
    this.addEvents('select', 'customize', 'save');

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.Results.superclass.initComponent.apply(this, arguments);
  },

  select: function(args) {
    var results = this;
    results.fireEvent('select', args);
  },

  customize: function() {
    var results = this;
    results.fireEvent('customize');
  },

  save: function() {
    var results = this;
    results.fireEvent('save');
  },

  update: function(args) {
    var results = this;

    // enable buttons
    results.selectButton.enable();
    results.selectButton.setIconClass(args.results + '-results-icon');
    results.selectButton.setText(args.results.charAt(0).toUpperCase() + args.results.slice(1));
    results.customizeButton.disable().hide();
    results.saveButton.enable();

    if (args.results == 'tabular') {
      results.customizeButton.enable().show();
    } else if (args.results == 'itemized') {
      // pass
    } else if (args.results == 'mapped') {
      // pass
    } else if (args.results == 'aggregated') {
      // pass
    }

    // update results panel
    results.removeAll();
    if (args.results == 'tabular' && (!args.columns || args.columns.length == 0)) {
      Ext.MessageBox.alert(Martview.APP_TITLE, 'To see the results, please click on the "Add/remove columns" button to add at least one column to the results grid.');
    } else {
      results.add({
        xtype: args.results + 'results',
        itemId: args.results,
        ref: args.results,
        fields: args.fields,
        columns: args.columns
      });
    }

    // refresh results panel
    results.doLayout();

    // load rows
    if (results.items.first()) {
      results.items.first().load(args);
    }

    // remember current results view
    results.current = args.results;
  },

  clear: function() {
    var results = this;
    delete results.current;
    results.removeAll();
    results.selectButton.disable();
    results.customizeButton.disable();
    results.saveButton.disable();
    results.counter.setText('&nbsp;');
    results.doLayout();
  }
});

Ext.reg('results', Martview.Results);
