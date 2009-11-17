Ext.ns('Martview.results');

Martview.results.Aggregated = Ext.extend(Ext.Panel, {

  // hard config
  initComponent: function() {
    var config = {
      border: false
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.results.Aggregated.superclass.initComponent.apply(this, arguments);
  },

  load: function(args) {
    var chart = this;
    // TODO
  },

  reset: function() {
    var chart = this;
    // TODO
  }
});

Ext.reg('aggregatedresults', Martview.results.Aggregated);
