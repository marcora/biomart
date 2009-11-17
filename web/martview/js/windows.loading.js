Ext.ns('Martview.windows');

/* --------------
   Loading window
   -------------- */

Martview.windows.Loading = Ext.extend(Object, {
  show: function() {
    if (!this.msg || !this.msg.isVisible()) {
      this.msg = Ext.Msg.show({
        cls: 'loading',
        title: Martview.APP_TITLE,
        msg: 'Connecting to BioMart...',
        width: 300,
        wait: true,
        waitConfig: {
          interval: 200
        }
      });
    }
  },
  hide: function() {
    if (this.msg && this.msg.isVisible()) {
      this.msg.hide();
    }
  }
});
