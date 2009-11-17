Ext.ns('Martview.windows');

/* ---------------
   Datasets window
   --------------- */
Martview.windows.Datasets = Ext.extend(Ext.Window, {

  // hard config
  initComponent: function() {
    var config = {
      modal: true,
      width: 900,
      height: 500,
      layout: 'fit',
      closeAction: 'hide',
      plain: true,
      autoDestroy: true,
      title: 'Choose dataset',
      iconCls: 'selectdb-icon',
      buttonAlign: 'left',
      buttons: [{
        xtype: 'tbtext',
        text: '<img src="./ico/information.png" style="vertical-align: text-bottom;" />&nbsp;Double-click on a folder to expand/collapse it or on a dataset to choose it'
      },
      {
        xtype: 'tbfill'
      },
      {
        cls: 'x-btn-text-icon',
        text: 'Cancel',
        iconCls: 'reset-icon',
        handler: function() {
          this.hide();
        },
        scope: this // window
      },
      {
        cls: 'x-btn-text-icon',
        text: 'OK',
        iconCls: 'submit-icon',
        handler: function() {
          this.select();
        },
        scope: this // window
      }],
      items: [{
        xtype: 'treegrid',
        itemId: 'all',
        ref: '../all',
        cls: 'itemized',
        border: false,
        title: 'All datasets',
        tbar: {
          layout: 'hbox',
          items: [{
            xtype: 'searchfield',
            itemId: 'search',
            ref: '../search',
            flex: 1,
            emptyText: 'Enter terms (for example, "uniprot" or "human genes" or "protein structures") to find a specific dataset'
          },
          ' ', {
            itemId: 'expand_all',
            cls: 'x-btn-text-icon',
            iconCls: 'icon-expand-all',
            text: 'Expand all',
            tooltip: 'Click to fully expand the tree',
            handler: function() {
              this.all.getStore().expandAll();
            },
            scope: this // window
          },
          {
            itemId: 'collapse_all',
            cls: 'x-btn-text-icon',
            iconCls: 'icon-collapse-all',
            text: 'Collapse all',
            tooltip: 'Click to fully collapse the tree',
            handler: function() {
              this.all.getStore().collapseAll();
            },
            scope: this // window
          }]
        },
        listeners: {
          'dblclick': {
            fn: function() {
              this.select();
            },
            scope: this // window
          }
        },
        hideHeaders: true,
        // autoHeight: true,
        // autoWidth: true,
        // fitToFrame: true,
        // singleSelect: true,
        enableColumnHide: false,
        enableHdMenu: false,
        stripeRows: true,
        // view: new Ext.ux.grid.BufferView({
        //   // render rows as they come into viewable area.
        //   scrollDelay: false
        // }),
        sm: new Ext.grid.RowSelectionModel({
          singleSelect: true
        }),
        store: new Ext.ux.maximgb.tg.AdjacencyListStore({ // new Ext.data.JsonStore({
          autoLoad: true,
          autoDestroy: true,
          proxy: new Ext.data.MemoryProxy(this.datasets),
          reader: new Ext.data.JsonReader({
            root: 'rows',
            idProperty: '_id',
            fields: [{
              name: '_id',
              type: 'auto'
            },
            {
              name: '_parent',
              type: 'auto'
            },
            {
              name: '_is_leaf',
              type: 'bool'
            },
            'dataset_display_name', 'mart_display_name', 'description', 'keywords', 'fulltext']
          })
        }),
        // view: new Ext.ux.grid.BufferView({
        //   // // custom row height
        //   // rowHeight: 10,
        //   forceFit: true,
        //   scrollOffset: 1,
        //   emptyText: 'There are no datasets to select',
        //   // render rows as they come into viewable area.
        //   scrollDelay: false
        // }),
        viewConfig: {
          forceFit: true,
          enableRowBody: true
        },
        master_column_id: 'item',
        autoExpandColumn: 'item',
        colModel: new Ext.grid.ColumnModel({
          columns: [{
            xtype: 'templatecolumn',
            id: 'item',
            sortable: false,
            tpl: new Ext.XTemplate( //
            '<tpl for=".">', //
            '<div class="item">', //
            '<h2 class="title">', //
            '<tpl if="dataset_display_name">', //
            '<img src="./ico/database.png"/>{dataset_display_name}&nbsp;', //
            '<span class="source">', //
            '[{mart_display_name}]', //
            '</span>', //
            '</tpl>', //
            '<tpl if="!dataset_display_name">', //
            '<img src="./ico/folder-horizontal.png"/>{mart_display_name}', //
            '</tpl>', //
            '</h2>', //
            '<p class="description">', //
            '{description}', //
            '</p>', //
            // '<p class="keywords">', //
            // '<tpl for="keywords">', //
            // '<span class="keyword">{.}</span>', //
            // '</tpl>', //
            // '</p>', //
            '</div>', //
            '</tpl>' //
            )
          }]
        })
      }]
    };

    // add custom events
    this.addEvents('select');

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.windows.Datasets.superclass.initComponent.apply(this, arguments);
  },

  constructor: function(config) {
    config = config || {};
    config.listeners = config.listeners || {};
    Ext.applyIf(config.listeners, {
      // listeners
      'hide': function(window) {
        window.reset();
      },
      'show': function(window) {
        (function() {
          window.all.search.getEl().frame("ff0000", 1);
        }).defer(300);
      }
    });

    // call parent
    Martview.windows.Datasets.superclass.constructor.call(this, config);
  },

  reset: function() {
    var window = this;
    window.all.getSelectionModel().clearSelections(true);
    window.all.getStore().clearFilter();
    window.all.getStore().collapseAll();
    window.all.search.setValue('');
    window.all.search.hasSearch = false;
    window.all.search.triggers[0].hide();
  },

  select: function() {
    var store = this.all.getStore();
    var node = store.getActiveNode();
    if (node) {
      if (store.isLeafNode(node)) {
        this.fireEvent('select', node.json);
        this.hide();
      } else { if (store.isExpandedNode(node)) {
          store.collapseNode(node);
        } else {
          store.expandNode(node);
        }
      }
    }
  }
});
