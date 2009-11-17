//+ Jonas Raoni Soares Silva
//@ http://jsfromhell.com/array/permute [rev. #1]
var permute = function(v, m) {
  for (var p = -1, j, k, f, r, l = v.length, q = 1, i = l + 1; --i; q *= i);
  for (x = [new Array(l), new Array(l), new Array(l), new Array(l)], j = q, k = l + 1, i = -1; ++i < l; x[2][i] = i, x[1][i] = x[0][i] = j /= --k);
  for (r = new Array(q); ++p < q;)
  for (r[p] = new Array(l), i = -1; ++i < l; ! --x[1][i] && (x[1][i] = x[0][i], x[2][i] = (x[2][i] + 1) % l), r[p][i] = m ? x[3][i] : v[x[3][i]])
  for (x[3][i] = x[2][i], f = 0; ! f; f = !f)
  for (j = i; j; x[3][--j] == x[2][i] && (x[3][i] = x[2][i] = (x[2][i] + 1) % l, f = 1));
  return r;
};

// post data to another page
function post(url, data) {
  var form = document.createElement('form');
  form.method = 'post';
  form.action = url;
  form.target = '_blank';
  for (var key in data) {
    var input = document.createElement("input");
    input.setAttribute('name', key);
    input.setAttribute('type', 'hidden');
    input.setAttribute('value', data[key]);
    form.appendChild(input);
  }
  document.body.appendChild(form);
  form.submit();
  document.body.removeChild(form);
}

// add the capitalize method to string
String.prototype.capitalize = function() {
  return this.replace(/\w+/g, function(a) {
    return a.charAt(0).toUpperCase() + a.substr(1).toLowerCase();
  });
};

// add the has method to array
Array.prototype.contains = function(o) {
  return this.indexOf(o) > -1;
};

// add the get method to array
Array.prototype.get = function(i) {
  return this[i];
};

// add the first method to array
Array.prototype.first = function() {
  return this[0];
};

// add the last method to array
Array.prototype.last = function() {
  return this[this.length - 1];
};

// add the map method to array
Array.prototype.map = function(fn) {
  var r = [];
  var l = this.length;
  for (i = 0; i < l; i++) {
    r.push(fn(this[i]));
  }
  return r;
};

// add the getText method to Ext.menu.BaseItem to match the setText method
Ext.override(Ext.menu.BaseItem, {
  getText: function() {
    return this.el.child('.x-menu-item-text').dom.innerHTML;
  }
});

// add the getText method to Ext.menu.Menu to match the setText method
Ext.override(Ext.menu.Menu, {
  getText: function() {
    return this.el.child('.x-menu-item-text').dom.innerHTML;
  }
});

// make form.removeAll() work the right way <http://extjs.com/forum/showthread.php?p=120152#post120152>
Ext.override(Ext.layout.FormLayout, {
  renderItem: function(c, position, target) {
    if (c && !c.rendered && (c.isFormField || c.fieldLabel) && c.inputType != 'hidden') {
      var args = this.getTemplateArgs(c);
      if (typeof position == 'number') {
        position = target.dom.childNodes[position] || null;
      }
      if (position) {
        c.itemCt = this.fieldTpl.insertBefore(position, args, true);
      } else {
        c.itemCt = this.fieldTpl.append(target, args, true);
      }
      c.actionMode = 'itemCt';
      c.render('x-form-el-' + c.id);
      c.container = c.itemCt;
      c.actionMode = 'container';
    } else {
      Ext.layout.FormLayout.superclass.renderItem.apply(this, arguments);
    }
  }
});

Ext.override(Ext.form.Field, {
  getItemCt: function() {
    return this.itemCt;
  }
});

// add tooltip to field
Ext.sequence(Ext.form.Field.prototype, 'afterRender', function() {

  var findLabel = function(field) {
    var wrapDiv = null;
    var label = null;
    // find form-element and label?
    wrapDiv = field.getEl().up('div.x-form-element');
    if (wrapDiv) {
      label = wrapDiv.child('label');
    }
    if (label) {
      return label;
    }

    //find form-item and label
    wrapDiv = field.getEl().up('div.x-form-item');
    if (wrapDiv) {
      label = wrapDiv.child('label');
    }
    if (label) {
      return label;
    }
    return null;
  };

  var title = this.fieldLabel;
  var text = this.tooltip;
  var label = findLabel(this);

  if (title) {
    // Ext.QuickTips.register({
    //   target: this,
    //   title: title,
    //   text: text
    // });
    if (label) {
      Ext.QuickTips.register({
        target: label,
        title: title,
        text: text
      });
    }
  }
});

// add the focus method to Ext.form.FormPanel
Ext.override(Ext.form.FormPanel, {
  focus: function() {
    try {
      if (this.items.first().isXType('fieldset')) {
        this.items.first().items.first().focus('', 200);
      } else {
        this.items.first().focus('', 200);
      }
    } catch(e) {
      // do nothing if no form fields
    }
  }
});
