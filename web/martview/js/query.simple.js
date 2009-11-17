Ext.ns('Martview.query');

Martview.query.Simple = Ext.extend(Ext.form.FormPanel, {

  // hard config
  initComponent: function() {
    var config = {
      items: [{
        xtype: 'textfield',
        itemId: 'query',
        ref: 'query',
        fieldLabel: 'Enter terms to filter the results',
        labelStyle: 'font-weight: bold !important; font-size: 8pt !important; color: #444 !important;'
      },
      {
        // Lucene query syntax help
        xtype: 'fieldset',
        itemId: 'help',
        ref: 'help',
        title: 'Help',
        // title: '<img src="./ico/question.png" style="vertical-align: text-bottom !important;" /> <span style="font-weight: normal !important; color: #000 !important;">Help</span>',
        autoHeight: true,
        autoDestroy: true,
        defaults: {
          xtype: 'displayfield',
          anchor: '100%',
          labelStyle: 'font-weight: bold !important; font-size: 8pt !important; color: #444 !important;'
        }
      }]
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.query.Simple.superclass.initComponent.apply(this, arguments);
  },

  load: function(args) {
    var form = this;

    // remove fields from help fieldset
    form.help.removeAll();

    // add fields to help fieldset
    form.help.add({
      hideLabel: true,
      value: 'For more advanced queries, you can enter terms using the <a href="http://lucene.apache.org/java/2_4_1/queryparsersyntax.html" target="_blank">Lucene query syntax</a>' + ((args.help_fields.length > 0) ? ' and the following fields:' : '.')
    });

    // FIXME: remove try when generator is fixed to include help data
    try {
      Ext.each(args.help_fields, function(help_field) {
        form.help.add({
          fieldLabel: help_field.name,
          value: help_field.description
        });
      });
    } catch(e) {
      // pass
    }

    // refresh form layout
    form.doLayout();

    // submit query on enter key
    form.items.first().on('specialkey', function(f, o) {
      if (o.getKey() == 13) {
        form.ownerCt.submit();
      }
    });
  },

  reset: function() {
    var form = this;
    form.getForm().reset();
  },

  focus: function() {
    var form = this;
    form.query.focus(true, true);
  },

  build: function() {
    var form = this;
    var q = form.items.first().getValue().trim() || '*:*';
    var query_params = {
      type: 'search',
      q: q
    };
    return query_params;
  }
});

Ext.reg('simplequery', Martview.query.Simple);
