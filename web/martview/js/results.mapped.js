Ext.ns('Martview.results');

Martview.results.Mapped = Ext.extend(Ext.Panel, {

  // hard config
  initComponent: function() {
    var config = {
      border: false
    };

    // apply config
    Ext.apply(this, Ext.apply(this.initialConfig, config));

    // call parent
    Martview.results.Mapped.superclass.initComponent.apply(this, arguments);
  },

  load: function(args) {
    var map = this;
    // TODO
  },

  reset: function() {
    var map = this;
    // TODO
  }
});

Ext.reg('mappedresults', Martview.results.Mapped);

//     results.enableHeaderButtons();
//     store.filter('chromosome_name', '1');
//     var chromosome_1 = store.getCount();
//     if (debug) console.log('chromosome_1:' + chromosome_1);
//     store.filter('chromosome_name', '2');
//     var chromosome_2 = store.getCount();
//     store.filter('chromosome_name', '3');
//     var chromosome_3 = store.getCount();
//     store.filter('chromosome_name', '4');
//     var chromosome_4 = store.getCount();
//     store.filter('chromosome_name', '5');
//     var chromosome_5 = store.getCount();
//     store.filter('chromosome_name', '6');
//     var chromosome_6 = store.getCount();
//     store.filter('chromosome_name', '7');
//     var chromosome_7 = store.getCount();
//     store.filter('chromosome_name', '8');
//     var chromosome_8 = store.getCount();
//     store.filter('chromosome_name', '9');
//     var chromosome_9 = store.getCount();
//     store.filter('chromosome_name', '10');
//     var chromosome_10 = store.getCount();
//     store.filter('chromosome_name', '11');
//     var chromosome_11 = store.getCount();
//     store.filter('chromosome_name', '12');
//     var chromosome_12 = store.getCount();
//     store.filter('chromosome_name', '13');
//     var chromosome_13 = store.getCount();
//     store.filter('chromosome_name', '14');
//     var chromosome_14 = store.getCount();
//     store.filter('chromosome_name', '15');
//     var chromosome_15 = store.getCount();
//     store.filter('chromosome_name', '16');
//     var chromosome_16 = store.getCount();
//     store.filter('chromosome_name', '17');
//     var chromosome_17 = store.getCount();
//     store.filter('chromosome_name', '18');
//     var chromosome_18 = store.getCount();
//     store.filter('chromosome_name', '19');
//     var chromosome_19 = store.getCount();
//     store.filter('chromosome_name', '20');
//     var chromosome_20 = store.getCount();
//     store.filter('chromosome_name', '21');
//     var chromosome_21 = store.getCount();
//     store.filter('chromosome_name', '22');
//     var chromosome_22 = store.getCount();
//     store.filter('chromosome_name', 'X');
//     var chromosome_X = store.getCount();
//     store.filter('chromosome_name', 'Y');
//     var chromosome_Y = store.getCount();
//     var chart_store = new Ext.data.JsonStore({
//       fields: ['category', 'data'],
//       data: [{
//         category: '1',
//         data: chromosome_1
//       },
//       {
//         category: '2',
//         data: chromosome_2
//       },
//       {
//         category: '3',
//         data: chromosome_3
//       },
//       {
//         category: '4',
//         data: chromosome_4
//       },
//       {
//         category: '5',
//         data: chromosome_5
//       },
//       {
//         category: '6',
//         data: chromosome_6
//       },
//       {
//         category: '7',
//         data: chromosome_7
//       },
//       {
//         category: '8',
//         data: chromosome_8
//       },
//       {
//         category: '9',
//         data: chromosome_9
//       },
//       {
//         category: '10',
//         data: chromosome_10
//       },
//       {
//         category: '11',
//         data: chromosome_11
//       },
//       {
//         category: '12',
//         data: chromosome_12
//       },
//       {
//         category: '13',
//         data: chromosome_13
//       },
//       {
//         category: '14',
//         data: chromosome_14
//       },
//       {
//         category: '15',
//         data: chromosome_15
//       },
//       {
//         category: '16',
//         data: chromosome_16
//       },
//       {
//         category: '17',
//         data: chromosome_17
//       },
//       {
//         category: '18',
//         data: chromosome_18
//       },
//       {
//         category: '19',
//         data: chromosome_19
//       },
//       {
//         category: '20',
//         data: chromosome_20
//       },
//       {
//         category: '21',
//         data: chromosome_21
//       },
//       {
//         category: '22',
//         data: chromosome_22
//       },
//       {
//         category: 'X',
//         data: chromosome_X
//       },
//       {
//         category: 'Y',
//         data: chromosome_Y
//       }]
//     });
//     var rows = new Ext.chart.ColumnChart({
//       store: chart_store,
//       yField: 'data',
//       xField: 'category'
//     });
//   }
