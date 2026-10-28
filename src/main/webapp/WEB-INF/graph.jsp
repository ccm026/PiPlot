<%--
  Created by IntelliJ IDEA.
  User: Christopher V M
  Date: 25-Oct-17
  Time: 10:30 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Graph</title>
    <script type="text/javascript"
            src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
    <script
            src="http://cdn.crunchify.com/wp-content/uploads/code/jquery.sparkline.js"></script>
        <script>
            window.onload = function () {

                var dps = []; // dataPoints
                var chart = new CanvasJS.Chart("chartContainer", {
                    title :{
                        text: "Measured Distance Plot"
                    },
                    axisY: {
                        includeZero: false,
                        title: "Distance (cm)"
                    },

                    axisX:{
                        title: "Time"
                    },
                    data: [{
                        type: "line",
                        dataPoints: dps
                    }]
                });
                function crunchifyData() {
                    $.ajax({
                        url : '${pageContext.request.contextPath}/run',
                        dataType : "json",
                        cache : false,
                        contentType : 'application/json; charset=utf-8',
                        type : 'GET',
                        success : function(result) {
                            var one = result.measuredData;
                            updateChart(one);
                        }
                    });
                }
                var xVal ;
                var yVal ;
                var updateInterval = 1000;
                var dataLength = 20; // number of dataPoints visible at any point

                var updateChart = function (count) {
                        yVal = parseFloat(count[0][0]);
                        xVal = new Date();
                        dps.push({
                            x: xVal,
                            y: yVal
                        });

                    if (dps.length > dataLength) {
                        dps.shift();
                    }

                    chart.render();
                };

                setInterval(function(){crunchifyData()}, updateInterval);

            }
        </script>
    </head>
<body>
<div id="chartContainer" style="height: 370px; width:100%;"></div>
<script type="text/javascript" src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
<script src="https://canvasjs.com/assets/script/jquery-1.11.1.min.js"></script>
</body>
</html></html>