Ext.ns('Martview');

Martview.Main = Ext.extend(Ext.Viewport, {

  // hard config
  initComponent: function() {
    var config = {
      id: 'main',
      layout: 'border',
      items: [{
        xtype: 'header'
      },
      {
        xtype: 'query'
      },
      {
        xtype: 'results'
      },
      {
        xtype: 'footer'
      }]
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.Main.superclass.initComponent.apply(this, arguments);
  }
});
