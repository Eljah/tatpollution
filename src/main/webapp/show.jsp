<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<html>
<head>
    <title>Архив уровней Волги по данным водомерных постов имени Эрнста Галимовича Улумбекова</title>
    <meta charset="UTF-8">
    <!--Load the AJAX API-->
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
        //Load the Visualization API and the ready-made Google table visualization
        google.load('visualization', '1', {'packages': ['corechart']});
        // Set a callback to run when the API is loaded.
        google.setOnLoadCallback(init);
        // Send the query to the data source.
        function init() {
            // Specify the data source URL.
            var query = new google.visualization.Query('visualize?station=${station}&parameter=${parameter}&from=${from}&to=${to}');
            // Send the query with a callback function.
            query.send(handleQueryResponse);
        }
        // Handle the query response.
        function handleQueryResponse(response) {
            if (response.isError()) {
                alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
                return;
            }

            var options = {
                title: 'Уровень воды, м',
                tooltip: {isHtml: true},

                //vAxis: {title: 'level', minValue: 0, maxValue: 15},
                legend: 'none',
                width: 600, height: 300,
                curveType: "function",
                //trendlines: {
                //    0: {
                //        type: 'linear',
                //        color: 'green',
                //        lineWidth: 3,
                //        opacity: 0.3,
                //        showR2: true,
                //        visibleInLegend: true
                //    }
                //},
                series: {
                    0: {
                        0: {axis: 'days of measurement', targetAxisIndex: 0},
                        1: {axis: 'water level', targetAxisIndex: 1},
                        2: {}
                    },
                    1: {
                        0: {axis: 'days of measurement', targetAxisIndex: 0},
                        3: {axis: 'water level extrapolated', targetAxisIndex: 1},
                        4: {}
                    }
                }

                ,
                vAxes: {
                    0: {logScale: false},
                    1: {logScale: false, maxValue: 2}
                },
                hAxis: {
                    format: 'd/M/yy/hh:mm:ss',
                    //gridlines: {count: 15}
                    viewWindow: {
                                         min: new Date("${fromC}"),
                                         max: new Date("${toС}")
                                         }
                },
                //axes: {
                //    y: {
                //        'hours studied': {label: 'Hours Studied'},
                //        'final grade': {label: 'Final Exam Grade'}
                //    }
                //       }
            };

            // Draw the visualization.
            var data = response.getDataTable();
            //dirty hack
            //data.Ff[2].p.p={html: true};
            //data.Ff[2].p.role="tooltip";
            //data.Ff[2].p={html: true};
            //data.Ff[2].role="tooltip";
            data.setColumnProperties(2, {
                role: 'tooltip',
                html: true
            });

            data.setColumnProperties(4, {
                role: 'tooltip',
                html: true
            });

            var chart = new google.visualization.ScatterChart(document.getElementById('chart_div'));
            chart.draw(data, options);

        }
    </script>
</head>
<body>
<form id="kms" action="/welcome">
<select name="parameter" onchange="submit()">
    <option value="" ${parameter==null  ? 'selected' : ''}>Все параметры</option>
    <c:forEach items="${parameters}" var="parameterIT">
        <option value="${parameterIT[0]}" ${parameterIT[0]==parameter  ? 'selected' : ''}>${parameterIT[1]}</option>
    </c:forEach>
</select>
<select name="station" onchange="submit()">
    <option value="" ${station==null  ? 'selected' : ''}>Все станции</option>
    <c:forEach items="${stations}" var="stationIT">
        <option value="<c:out value='${stationIT}'/>" ${stationIT==station  ? 'selected' : ''}>${stationIT}</option>
    </c:forEach>
</select>

From <input type="date" name="from"  onchange="submit()" value="${from}"><br>
to <input type="date" name="to"  onchange="submit()" value="${to}"><br>

</form>

<!--Div that will hold the visualization-->
<div id="chart_div">
</div>
</body>
</html>