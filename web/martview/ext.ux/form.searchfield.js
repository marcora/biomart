Ext.ns('Ext.ux.form');

Ext.ux.form.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
  // assume placed in grid panel toolbar
  initComponent: function() {
    Ext.ux.form.SearchField.superclass.initComponent.call(this);
    this.on('specialkey', function(f, e) {
      if (e.getKey() == e.ENTER) {
        this.onTrigger2Click();
      }
    },
    this);
  },

  validationEvent: false,
  validateOnBlur: false,
  trigger1Class: 'x-form-clear-trigger',
  trigger2Class: 'x-form-search-trigger',
  hideTrigger1: true,
  hasSearch: false,

  onTrigger1Click: function() {
    if (this.hasSearch) {
      var store = this.ownerCt.ownerCt.getStore();
      store.clearFilter();
      try { // if grid is also a tree
        store.collapseAll();
      } catch(e) {
        // pass
      }
      this.setValue('');
      this.hasSearch = false;
      this.triggers[0].hide();
      this.focus();
    }
  },

  onTrigger2Click: function() {
    var val = this.getRawValue().trim();

    if (val.length < 1) {
      this.onTrigger1Click();
      return;
    }

    // generate search regexp that matches all complete permutations of search terms
    var terms = val.split(/\s+/);
    var stems = [];
    Ext.each(terms, function(term) {
      stems.push(stemmer(term));
    });
    var permutations = permute(stems);
    var s = '';
    Ext.each(permutations, function(permutation) {
      if (permutation.length == terms.length) {
        if (s.length == 0) {
          s += ('(' + permutation.join('.*'));
        } else {
          s += ('|' + permutation.join('.*'));
        }
      }
    });
    if (s.length > 0) s += ')';
    var re = new RegExp(s, 'i');

    // filter store on fulltext using search regexp
    var store = this.ownerCt.ownerCt.getStore();
    store.filter('fulltext', re);
    try { // if grid is also a tree
      store.expandAll();
    } catch(e) {
      // pass
    }
    this.hasSearch = true;
    this.triggers[0].show();
    this.focus();
  }
});

Ext.reg('searchfield', Ext.ux.form.SearchField);
