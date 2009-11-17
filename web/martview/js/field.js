Ext.ns('Martview');

Martview.Field = Ext.extend(Ext.Panel, {

  // soft config
  // - node
  // - field_iconCls
  // - display_name

  // hard config
  initComponent: function() {
    var config = {
      itemId: this.node.id,
      cls: 'field',
      border: false,
      shadow: new Ext.Shadow(),
      draggable: {
        ddGroup: 'allDDGroup',
        containerScroll: true,
        startDrag: function(x, y) {
          // get panel ghost
          var ghost = Ext.get(this.getDragEl());
          // customize panel ghost
          try {
            ghost.child('ul').remove(); // remove multi element drag "ghost" if present
          } catch(e) {
            // pass
          }
          ghost.removeClass('x-panel-ghost');
          ghost.addClass('x-dd-drag-proxy');
          ghost.addClass('x-dd-drop-nodrop');
          Ext.DomHelper.append(ghost, {
            tag: 'div',
            cls: 'x-dd-drop-icon'
          });
          // copy field html into panel ghost
          var el = Ext.get(this.proxy.panel.getEl());
          Ext.DomHelper.append(ghost, {
            tag: 'div',
            cls: 'x-dd-drag-ghost field-ghost',
            html: el.child('div.x-panel-tbar').dom.innerHTML
          });
          // add shadow to panel ghost
          this.shadow = new Ext.Shadow();
          this.shadow.show(ghost);
        },
        onDrag: function() {
          // align shadow with ghost while dragging
          var pel = this.proxy.getEl();
          this.shadow.realign(pel.getLeft(true), pel.getTop(true), pel.getWidth(), pel.getHeight());
        },
        endDrag: function() {
          // hide shadow when dragging ends
          this.shadow.hide();
        },
        onDragOver: function(e, targetId) {
          // change drop icon to ok when inside drop zone
          var ghost = Ext.get(this.getDragEl());
          ghost.removeClass('x-dd-drop-nodrop');
          ghost.addClass('x-dd-drop-ok');
        },
        onDragOut: function(e, targetId) {
          // change drop icon to nodrop when outside of drop zone
          var ghost = Ext.get(this.getDragEl());
          ghost.addClass('x-dd-drop-nodrop');
          ghost.removeClass('x-dd-drop-ok');
        }
      },
      tbar: [{
        xtype: 'tbtext',
        itemId: 'title',
        ref: '../title',
        text: '<span class="title"><img src="./ico/' + ((this.field_iconCls == 'filter-icon') ? 'ui-search-field' : 'table-select-column') + '.png"/>&nbsp;' + this.node.attributes.display_name || this.node.attributes.name + '</span>',
        listeners: {
          afterrender: function(title) {
            var field = this;

            // create tooltip
            Ext.QuickTips.register({
              target: title.el.child('span.title'),
              title: field.node.parentNode.parentNode.text + ' > ' + field.node.parentNode.text + ' > ' + field.node.text,
              text: field.node.attributes.description,
              dismissDelay: 0,
              showDelay: 0
            });
          },
          scope: this // field
        }
      },
      '->', {
        itemId: 'moveup',
        ref: '../moveUpButton',
        text: 'Up',
        iconCls: 'move-up-icon',
        cls: 'x-btn-text-icon',
        tooltip: 'Click to move this ' + this.display_name.substr(0, this.display_name.length - 1) + ' up',
        handler: function() {
          var field = this;
          var window = field.ownerCt.ownerCt;
          window.moveFieldUp(field);
        },
        scope: this // field
      },
      {
        itemId: 'movedn',
        ref: '../moveDnButton',
        text: 'Down',
        iconCls: 'move-dn-icon',
        cls: 'x-btn-text-icon',
        tooltip: 'Click to move this ' + this.display_name.substr(0, this.display_name.length - 1) + ' down',
        handler: function() {
          var field = this;
          var window = field.ownerCt.ownerCt;
          window.moveFieldDn(field);
        },
        scope: this // field
      },
      {
        text: 'Remove',
        iconCls: 'delete-icon',
        cls: 'x-btn-text-icon',
        tooltip: 'Click to remove this ' + this.display_name.substr(0, this.display_name.length - 1),
        handler: function() {
          var field = this;
          var window = field.ownerCt.ownerCt;
          window.removeFields(field);
        },
        scope: this // field
      }]
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.Field.superclass.initComponent.apply(this, arguments);
  },

  constructor: function(config) {
    config = config || {};
    config.listeners = config.listeners || {};
    Ext.applyIf(config.listeners, {
      // configure listeners here
      afterrender: function(field) {
        // disable node
        field.node.disable();

        // make field a drop target for other fields and tree nodes
        field.dd.addToGroup('selectedDDGroup');
        var selectedDropTargetEl = field.el;
        var selectedDropTarget = new Ext.dd.DropTarget(selectedDropTargetEl, {
          ddGroup: 'selectedDDGroup',
          notifyDrop: function(dd, e, data) {
            var window = field.ownerCt.ownerCt;
            if (dd.tree) { // treenode
              var node = dd.tree.selModel.selNode;
              window.moveFieldTo(node, field);
              return true;
            } else if (dd.panel) { // field
              var node = dd.panel.node;
              window.moveFieldTo(node, field);
              return true;
            } else {
              return false;
            }
          }
        });
      },
      beforedestroy: function(field) {
        // enable node
        field.node.enable();
      }
    });

    // call parent
    Martview.Field.superclass.constructor.call(this, config);
  }
});

Ext.reg('field', Martview.Field);
