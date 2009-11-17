Ext.ns('Ext.ux.form');

Ext.ux.form.TreeFilterField = Ext.extend(Ext.form.TwinTriggerField, {
  // assume placed in tree panel toolbar
  initComponent: function() {
    Ext.ux.form.TreeFilterField.superclass.initComponent.call(this);
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
      this.ownerCt.ownerCt.filter.clear();
      this.ownerCt.ownerCt.root.collapse(true);
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

    // filter tree on fulltext using search regexp
    this.ownerCt.ownerCt.filter.clear();
    this.ownerCt.ownerCt.filter.filter(re, 'fulltext');
    this.hasSearch = true;
    this.triggers[0].show();
    this.focus();
  }
});

Ext.reg('treefilterfield', Ext.ux.form.TreeFilterField);
