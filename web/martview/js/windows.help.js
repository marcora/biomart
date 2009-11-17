Ext.ns('Martview.windows');

/* -----------
   Help window
   ----------- */

Martview.windows.Help = Ext.extend(Ext.Window, {
  id: 'help',
  title: Martview.APP_TITLE,
  modal: true,
  width: 400,
  height: 300,
  html: 'biomart help'
});
