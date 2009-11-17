Ext.ns('Martview');

Ext.BLANK_IMAGE_URL = './ext/resources/images/default/s.gif';
Ext.chart.Chart.CHART_URL = './ext/resources/charts.swf';
// Ext.state.Manager.setProvider(new Ext.state.CookieProvider()); // disable during development

/*jslint rhino: true, nomen: true, debug: true, onevar: true, indent: 2, white: false, eqeqeq: false, browser: true, undef: true */
/*global Ext, Martview */

// FIXME: Attributes with the same name are included in results even if not selected
//        Disambiguate attribute name by its position in hierarchy!
//        Look for or code 'find by uuid' in both extjs tree and ruby collection of attributes on server side
// TODO:  Column order for query results should follow attribute order
// TODO:  Logged in users can mark datasets as favorites
try {
  console.log(); // http://code.google.com/p/fbug/issues/detail?id=1014
  var debug = true;
} catch(e) {
  var debug = false;
}

Ext.onReady(function() {

  /* ==============
     Initialization
     ============== */

  // init tips
  Ext.QuickTips.init();
  Ext.apply(Ext.QuickTips.getQuickTip(), {
    // dismissDelay: 0,
    autoWidth: true
  });

  // service, viewport, and windows global vars
  var service = new Martview.Service();
  var main = new Martview.Main();
  var datasets_win;
  var filters_win;
  var attributes_win;

  // init params global var with query params
  var params = Ext.urlDecode(window.location.search.substring(1));

  // if not specified the default query is 'advanced' and the default results is 'tabular'
  Ext.applyIf(params, {
    query: 'advanced',
    results: 'tabular',
    filters: [],
    attributes: []
  });

  // // transform filters param into an array of objects
  // (function() {
  //   var filters = [];
  //   Ext.each(params.filters.split('|'), function(filter) {
  //     filters.push({
  //       id: filter.split(':').first(),
  //       value: filter.split(':').last()
  //     });
  //   });
  //   params.filters = filters;
  // } ());

  // // transform attributes param into an array of objects
  // (function() {
  //   var attributes = [];
  //   Ext.each(params.attributes.split('|'), function(attribute) {
  //     attributes.push({
  //       id: attribute
  //     });
  //   });
  //   params.attributes = attributes;
  // } ());

  /* ==============
     Event handlers
     ============== */

  service.on('load', function(datasets) {
    // exit if empty datasets
    if (service.datasets.rows.length === 0) {
      return;
    }

    // create datasets window
    datasets_win = new Martview.windows.Datasets({
      datasets: datasets
    });

    // show datasets window on header select button click
    main.header.on('select', function() {
      datasets_win.show();
    });

    // load dataset on datasets window select
    datasets_win.on('select', function(dataset) {
      service.select(dataset);
    });

    // if only one dataset select bypassing datasets window
    if (service.datasets.rows.length == 1) {
      service.select(datasets.rows[0]);
      return;
    }

    // validate params against datasets and if correct select bypassing datasets window
    if (params.mart && params.dataset) {
      var valid = false;
      var dataset = {};
      Ext.each(service.datasets.rows, function(row) {
        if (row.mart_name == params.mart && row.dataset_name == params.dataset) {
          valid = true;
          Ext.apply(dataset, row);
        }
      });
      if (valid) {
        service.select(dataset);
        return;
      }
    }

    // show datasets window if nothing else worked
    datasets_win.show();
  });

  service.on('select', function(dataset) {
    // update params with dataset data (overwrite params.filters/attributes)
    // var selected_filters = params.filters;
    // var selected_attributes = params.attributes;
    Ext.apply(params, dataset);

    // destroy and create filters window
    try {
      filters_win.destroy();
    } catch(e) {
      // pass
    }

    filters_win = new Martview.windows.Fields({
      id: 'filters',
      title: 'Add filters to query form',
      display_name: 'filters',
      field_iconCls: 'filter-icon',
      dataset: dataset,
      selected_fields: [] // selected_filters
    });

    // destroy and create attributes window
    try {
      attributes_win.destroy();
    } catch(e) {
      // pass
    }

    attributes_win = new Martview.windows.Fields({
      id: 'attributes',
      title: 'Add columns to results grid',
      display_name: 'columns',
      field_iconCls: 'attribute-icon',
      dataset: dataset,
      selected_fields: [] // selected_attributes
    });

    // show filters window on query customize button click
    main.query.on('customize', function() {
      filters_win.show();
    });

    // show attributes window on results customize button click
    main.results.on('customize', function() {
      attributes_win.show();
    });

    // update query on filters window update
    filters_win.on('update', function() {
      main.query.fireEvent('select', params); // query.select -> query.update
    });

    // update results on attributes window update
    attributes_win.on('update', function() {
      main.query.fireEvent('submit', params); // query.submit -> service.query -> results.select -> results.update
    });

    // update header
    main.header.update(params);

    // clear query and results
    main.query.clear();
    main.results.clear();

    // update query
    main.query.fireEvent('select', params); // query.select -> query.update
  });

  main.query.on('select', function(args) {
    // update params with args and filters data
    Ext.apply(params, args);
    if (params.query == 'advanced') {
      params.filters = filters_win.getSelectedFields();
    }

    // update query
    main.query.update(params);
  });

  main.query.on('submit', function() {
    // update params with filters/attributes data
    if (params.query == 'advanced') {
      Ext.apply(params, {
        filters: filters_win.getSelectedFields(),
        attributes: attributes_win.getSelectedFields(),
        formatter: 'CSV',
        limitSize: 100
      });
    }

    // build query params
    var query_params = main.query.build(params);

    // query service
    service.query(query_params);
  });

  service.on('query', function(results) {
    // update params with results data
    Ext.apply(params, results);

    // update results
    main.results.fireEvent('select', params); // results.select -> results.update

    // update query if guided
    if (params.query == 'guided') {
      main.query.update(params);
    }
  });

  main.results.on('select', function(args) {
    // update params with args and attributes data
    Ext.apply(params, args);
    if (params.query == 'advanced') {
      params.attributes = attributes_win.getSelectedFields();
    }

    // update results
    main.results.update(params);
  });

  main.query.on('save', function() {
    // show save query dialog
    var dialog = new Martview.windows.SaveQuery();
    dialog.show();
    dialog.center();
    dialog.okButton.on('click', function() {
      var dialog = this;

      // update params with filters/attributes data
      if (params.query == 'advanced') {
        Ext.apply(params, {
          filters: filters_win.getSelectedFields(),
          attributes: attributes_win.getSelectedFields(),
          formatter: dialog.form.format.getValue().toUpperCase(),
          limitSize: 0
        });

        // build query params
        var query_params = main.query.build(params);
        query_params.type = 'savequery';

        // redirect to martservice
        post('../martservice', query_params);
      }

      // destroy dialog
      dialog.destroy();
    },
    dialog);
  });

  main.results.on('save', function() {
    // show save results dialog
    var dialog = new Martview.windows.SaveResults();
    dialog.show();
    dialog.center();
    dialog.okButton.on('click', function() {
      var dialog = this;

      // update params with filters/attributes data
      if (params.query == 'advanced') {
        Ext.apply(params, {
          filters: filters_win.getSelectedFields(),
          attributes: attributes_win.getSelectedFields(),
          formatter: dialog.form.format.getValue().toUpperCase(),
          limitSize: 0
        });

        // build query params
        var query_params = main.query.build(params);
        query_params.type = 'saveresults';

        // redirect to martservice
        post('../martservice', query_params);
      }

      // destroy dialog
      dialog.destroy();
    },
    dialog);
  });

  // load all datasets to start app
  service.load();
});
