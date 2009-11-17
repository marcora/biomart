Ext.ns('Martview');

Martview.Header = Ext.extend(Ext.Toolbar, {

  // hard config
  initComponent: function() {
    var config = {
      region: 'north',
      id: 'header',
      ref: '../header',
      // FIX: should adjust height automatically
      height: 26,
      border: false,
      items: [{
        itemId: 'select',
        ref: 'selectButton',
        cls: 'x-btn-text-icon',
        text: 'Choose dataset',
        iconCls: 'selectdb-icon',
        tooltip: 'Click to choose the dataset you want to query',
        handler: function() {
          var header = this;
          header.select();
        },
        scope: this // header
      },
      {
        xtype: 'tbtext',
        itemId: 'start',
        ref: 'start',
        text: '<span class="start"><img src="./ico/arrow-180.png"/>&nbsp;START HERE</span>'
      },
      {
        xtype: 'tbtext',
        itemId: 'sep',
        ref: 'separator',
        text: '>',
        hidden: true
      },
      {
        itemId: 'dataset',
        ref: 'datasetButton',
        cls: 'x-btn-text-icon',
        text: 'dataset_name',
        iconCls: 'dataset-icon',
        hidden: true,
        tooltip: 'Click to get more information about the chosen dataset',
        handler: function() {
          Ext.MessageBox.alert(Martview.APP_TITLE, 'More info about chosen dataset');
        }
      },
      '->', {
        itemId: 'login',
        ref: 'loginButton',
        cls: 'x-btn-text-icon',
        text: 'Login',
        iconCls: 'user-icon',
        tooltip: 'Click to log into BioMart',
        handler: function() {
          Ext.MessageBox.alert(Martview.APP_TITLE, 'Login');
        }
      },
      {
        itemId: 'help',
        ref: 'HelpButton',
        cls: 'x-btn-text-icon',
        text: 'Help',
        iconCls: 'help-icon',
        tooltip: 'Click to get help on how to use BioMart',
        handler: function() {
          Ext.MessageBox.alert(Martview.APP_TITLE, 'Help');
        }
      }]
    };

    // add custom events
    this.addEvents('select');

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.Header.superclass.initComponent.apply(this, arguments);
  },

  select: function() {
    var header = this;
    header.fireEvent('select');
  },

  update: function(args) {
    var header = this;
    header.start.hide();
    header.separator.show();
    header.datasetButton.setIconClass(args.iconCls);
    header.datasetButton.setText('<span style="color: #333; font-weight: bold;">' + (args.dataset_display_name || args.dataset_name) + '</span>&nbsp;<span style="color: #666;">[' + (args.mart_display_name || args.mart_name) + ']</span>');
    header.datasetButton.show();
    document.title = (args.dataset_display_name || args.dataset_name) + ' [' + (args.mart_display_name || args.mart_name) + ']';
  },

  clear: function() {
    var header = this;
    header.datasetButton.hide();
    header.separator.hide();
    header.start.show();
    document.title = '';
  }

});

Ext.reg('header', Martview.Header);
