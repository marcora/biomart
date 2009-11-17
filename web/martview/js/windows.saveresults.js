Ext.ns('Martview.windows');

/* -------------------
   Save results window
   ------------------- */

Martview.windows.SaveResults = Ext.extend(Ext.Window, {

  // hard config
  initComponent: function() {
    var config = {
      title: 'Save results',
      modal: true,
      layout: 'fit',
      autoHeight: true,
      // autoWidth: true,
      width: 300,
      closeAction: 'hide',
      plain: true,
      border: false,
      autoDestroy: true,
      iconCls: 'save-icon',
      buttonAlign: 'center',
      buttons: [{
        text: 'Cancel',
        cls: 'x-btn-text-icon',
        iconCls: 'close-icon',
        handler: function() {
          this.destroy();
        },
        scope: this // window
      },
      {
        text: 'Save',
        ref: '../okButton',
        cls: 'x-btn-text-icon',
        iconCls: 'submit-icon'
      }],
      items: [{
        xtype: 'form',
        ref: '../form',
        frame: true,
        autoHeight: true,
        autoWidth: true,
        labelAlign: 'top',
        bodyStyle: 'padding:5px',
        items: [{
          xtype: 'combo',
          name: 'format',
          ref: 'format',
          anchor: '100%',
          fieldLabel: 'Save current results as',
          editable: false,
          forceSelection: true,
          lastSearchTerm: false,
          triggerAction: 'all',
          mode: 'local',
          value: 'csv',
          store: [['csv', 'CSV'], ['tsv', 'TSV'], ['xls', 'Excel'], ['xml', 'XML'], ['json', 'JSON']]
        },
        {
          xtype: 'textfield',
          anchor: '100%',
          fieldLabel: 'Email to (optional but recommended for large datasets)'
        }]
      }]
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.windows.SaveResults.superclass.initComponent.apply(this, arguments);
  }
});
