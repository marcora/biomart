Ext.ns('Martview');

Martview.Service = Ext.extend(Ext.util.Observable, {
  constructor: function(config) {
    this.conn = new Ext.data.Connection({
      loading: new Martview.windows.Loading(),
      timeout: 120000,
      listeners: {
        beforerequest: function() {
          this.loading.show();
        },
        requestcomplete: function() {
          this.loading.hide();
          // main.footer.updateMessageIfError('info', Martview.SAVE_RESULTS_MSG);
        },
        requestexception: function() {
          this.loading.hide();
          Ext.Msg.show({
            title: Martview.APP_TITLE,
            msg: Martview.CONN_ERROR_MSG,
            // icon: Ext.Msg.ERROR,
            buttons: Ext.Msg.OK,
            width: 300
          });
        }
      }
    });

    // add custom events
    this.addEvents('load', 'select', 'search');

    // call parent
    Martview.Service.superclass.constructor.call(config);
  },

  // load all datasets
  load: function() {
    var service = this;
    var datasets_url = './json/datasets.json';
    service.conn.request({
      url: datasets_url,
      success: function(response) {
        service.datasets = Ext.util.JSON.decode(response.responseText);
        service.fireEvent('load', service.datasets);
      }
    });
  },

  // select a specific dataset
  select: function(dataset) {
    var service = this;
    var dataset_url = './json/' + dataset.mart_name + '.' + dataset.dataset_name + '.json';
    service.conn.request({
      url: dataset_url,
      success: function(response) {
        service.dataset = Ext.util.JSON.decode(response.responseText);
        service.fireEvent('select', service.dataset);
      }
    });
  },

  // query martservice for results
  query: function(query_params) {
    var service = this;
    var query_url = '../martservice';
    service.conn.request({
      url: query_url,
      params: query_params,
      success: function(response) {
        service.results = Ext.util.JSON.decode(response.responseText);
        service.fireEvent('query', service.results);
      }
    });
  }
});
