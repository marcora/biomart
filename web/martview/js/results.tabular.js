Ext.ns('Martview.results');

Martview.results.Tabular = Ext.extend(Ext.grid.GridPanel, {
                                        
  // soft config
  fields: [],
  columns: [],

  // hard config
  initComponent: function() {
    var config = {
      enableColumnHide: false,
      enableHdMenu: false,
      disableSelection: true,
      stripeRows: true,
      border: false,
      plugins: ['autosizecolumns'],
      store: new Ext.data.JsonStore({
        autoDestroy: true,
        root: 'rows',
        fields: this.fields
      }),
      // autoExpandColumn: this.columns.first() ? this.columns.first().id : '',
      cm: new Ext.grid.ColumnModel({
        columns: [new Ext.grid.RowNumberer()].concat(this.columns),
        defaults: {
          width: 100,
          sortable: true
        }
      })
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.results.Tabular.superclass.initComponent.apply(this, arguments);
  },

  load: function(args) {
    var grid = this;
    var store = grid.getStore();
    store.removeAll();
    store.loadData({
      rows: args.rows
    });
    grid.ownerCt.counter.setText(store.getTotalCount() + ' of ' + args.count);
  },

  reset: function() {
    var grid = this;
    // TODO
  }
});

Ext.reg('tabularresults', Martview.results.Tabular);
