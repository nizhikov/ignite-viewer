(function () {
  'use strict'
    $('form#connect-form').submit(function (e) {
        var url =  $('#connect-url').val();

        if (url == null || url == '') {
            return false;
        }

        loadNodes(url);
        loadViews(url);
        loadMetrics(url);

        return false;
    })

    $('a.nav-link').click(function(e) {
        if ($(e.target).hasClass('dropdown-toggle'))
            return;

        var id = $(e.target).attr('href').substring(1);

        $('main').addClass('hidden')
        $('main#' + id).removeClass('hidden')

        $('a.nav-link').parent().removeClass('active');
        $(e.target).parent().addClass('active');
    });

    $('#nodes-menu').on('click', '.node-item', function (e) {
        var url = $(e.target).attr('href').substring(1);

        alert(url);
    });

    $('#views-bar').on('click', '.sysview-link', function (e) {
        $('.sysview-link.active').removeClass('active');
        $(e.target).addClass('active');

        var url =  $('#connect-url').val();
        var viewID = $(e.target).attr('href').substring(1);

        $.getJSON(url + "/views/" + viewID, {}, function(data) {
            var container = $('#system-views .table-responsive');

            container.empty();

            if (data.length == 0) {
                container.append('<h1>No data found</h1>');
                return;
            }

            var keys = Object.keys(data[0]);

            var tableHtml = "<table class='table table-striped table-sm'>" +
                "<thead>" +
                    "<tr>" +
                        "<th>#</th>";

            $.each(keys, function(index, key) {
                tableHtml += "<th>" + key + "</th>";
            })

            tableHtml += "</tr>" +
                "</thead>" +
                "<tbody>";

            $.each(data, function(index, row) {
                tableHtml += "<tr><td>" + index + "</td>";

                $.each(keys, function(index, key) {
                    tableHtml += "<td>" + row[key] + "</td>";
                })

                tableHtml += "</tr>";
            });

            tableHtml += "</tbody>" +
                "</table>";

            container.append(tableHtml);
        })
    });

    $('#metrics-bar').on('click', '.metric-link', function (e) {
        $('.metric-link.active').removeClass('active');
        $(e.target).addClass('active');

        var url =  $('#connect-url').val();
        var mregID = $(e.target).attr('href').substring(1);

        $.getJSON(url + "/metrics/" + mregID, {}, function(data) {
            var container = $('#metrics .table-responsive');

            container.empty();

            if (data.length == 0) {
                container.append('<h1>No data found</h1>');
                return;
            }

            var keys = Object.keys(data[0]);

            var tableHtml = "<table class='table table-striped table-sm'>" +
                "<thead>" +
                    "<tr>" +
                        "<th>#</th>";

            $.each(keys, function(index, key) {
                tableHtml += "<th>" + key + "</th>";
            })

            tableHtml += "</tr>" +
                "</thead>" +
                "<tbody>";

            $.each(data, function(index, row) {
                tableHtml += "<tr><td>" + index + "</td>";

                $.each(keys, function(index, key) {
                    tableHtml += "<td>" + row[key] + "</td>";
                })

                tableHtml += "</tr>";
            });

            tableHtml += "</tbody>" +
                "</table>";

            container.append(tableHtml);
        })
    });
}())

function loadViews(url) {
    $.getJSON(url + "/views/", {}, function(views) {
        views.sort(function (a, b) {
          if ( a.name < b.name ) return -1;
          if ( a.name > b.name ) return 1;
          return 0;
        });

        var bar = $("#views-bar");

        bar.empty();

        $.each(views, function(index, view) {
            bar.append("<li class='nav-item'><a class='nav-link sysview-link' href='#" + view.name + "'>" + view.name + "</a></li>");
        });
    })
}

function loadNodes(url) {
    $.getJSON(url + "/views/nodes", {}, function(nodes) {
        nodes.sort(function (a, b) {
          if ( a.nodeId < b.nodeId ) return -1;
          if ( a.nodeId > b.nodeId ) return 1;
          return 0;
        });

        var menu = $("#nodes-menu");

        menu.empty();

        $.each(nodes, function(index, node) {
            menu.append("<a class='dropdown-item node-item' href='#" + node.addresses[node.addresses.length - 1] + "'>" + node.nodeId + "</a>");
        });
    })
}

function loadMetrics(url) {
    $.getJSON(url + "/metrics/", {}, function(mregs) {
        mregs.sort(function (a, b) {
          if ( a.name < b.name ) return -1;
          if ( a.name > b.name ) return 1;
          return 0;
        });

        var bar = $("#metrics-bar");

        bar.empty();

        $.each(mregs, function(index, mreg) {
            bar.append("<li class='nav-item'><a class='nav-link metric-link' href='#" + mreg.name + "'>" + mreg.name + "</a></li>");
        });
    })
}
