Ext.ns('Martview.results');

Martview.results.Itemized = Ext.extend(Ext.grid.GridPanel, {
                                         
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
      cls: 'itemized',
      hideHeaders: true,
      viewConfig: {
        forceFit: true,
        enableRowBody: true
      },
      store: new Ext.data.JsonStore({
        autoDestroy: true,
        root: 'rows',
        fields: this.fields
      }),
      autoExpandColumn: 'item',
      cm: new Ext.grid.ColumnModel({
        columns: [{
          xtype: 'templatecolumn',
          id: 'item',
          sortable: false,
          tpl: new Ext.XTemplate( //
          '<tpl for=".">', //
          '<table style="width: 100%;">', //
          '<tr>', //
          '<td style="width: 50px; align: right; vertical-align: top; font-weight: bold; color: #333;">', //
          '{pdb_id}<img src="./ico/arrow-000-small.png" style="vertical-align: middle;" />', //
          '</td>', //
          '<td style="font-weight: bold; color: #333;">', //
          '{title}', //
          '</td>', //
          '</tr>', //
          '<tr>', //
          '<td style="width: 50px; align: center; vertical-align: top;">', //
          '<img style="width: 50px;" src="http://www.rcsb.org/pdb/images/{pdb_id}_asym_r_250.jpg" />', //
          '</td>', //
          '<td>', //
          '<div style="color: #666;">Experiment type: {experiment_type}</div>', //
          '<div style="color: #666;">Resolution: {resolution}</div>', //
          '<div style="color: #666;">Space group: {space_group}</div>', //
          '<div style="color: #666;">R work: {r_work}</div>', //
          '</td>', //
          '</tr>', //
          '</table>', //
          '</tpl>' //
          )
        }]
      })
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.results.Itemized.superclass.initComponent.apply(this, arguments);
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

Ext.reg('itemizedresults', Martview.results.Itemized);
