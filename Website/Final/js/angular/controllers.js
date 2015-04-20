var API_URI = "api/";
var wcm_buch_controllers = angular.module("wcm_buch_controllers", []);

wcm_buch_controllers.controller(
    "wcm_buch_search_header_controller", ["$scope", "$http",
        function ($scope, $http) {
            $scope.search = function (searchTerm) {
                var url = API_URI + "search/" + searchTerm;



                $http.get(url)
                    .success(function (data, status, headers, config) {
                        // TODO:
                        console.log("get success", url, data, status, headers, config);
                    })
                    .error(function (data, status, headers, config) {
                        // TODO:
                        console.log("get error", url, data, status, headers, config);
                    });
            };
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_search_controller", ["$scope", "$http", "$routeParams",
        function ($scope, $http, $routeParams) {
            var url = API_URI + "search/" + $routeParams.searchTerm;
            $http.get(url)
                .success(function (data, status, headers, config) {
                    // TODO:
                    console.log("get success", url, data, status, headers, config);
                })
                .error(function (data, status, headers, config) {
                    // TODO:
                    console.log("get error", url, data, status, headers, config);
                });
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_book_controller", ["$scope", "$http", "$routeParams",
        function ($scope, $http, $routeParams) {
            var url = API_URI + "book/" + $routeParams.bookID;
            $http.get(url)
                .success(function (data, status, headers, config) {
                    // TODO:
                    console.log("get success", url, data, status, headers, config);
                })
                .error(function (data, status, headers, config) {
                    // TODO:
                    console.log("get error", url, data, status, headers, config);
                });
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_series_controller", ["$scope", "$http", "$routeParams",
        function ($scope, $http, $routeParams) {
            var url = API_URI + "series/" + $routeParams.seriesID;
            $http.get(url)
                .success(function (data, status, headers, config) {
                    // TODO:
                    console.log("get success", url, data, status, headers, config);
                })
                .error(function (data, status, headers, config) {
                    // TODO:
                    console.log("get error", url, data, status, headers, config);
                });
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_dummy_controller", ["$scope", "$http", "$routeParams",
        function ($scope, $http, $routeParams) {
            var url = API_URI + "series/" + $routeParams.seriesID;

            var search = {};
            search.results_total = 3;
            search.results_start = 1;
            search.results_end = 5;
            search.searchTerm = "The Eden Trilogy";
            search.provider = "dummy provider";
            search.timeToSearch = 8.4;

            search.results = [];
            search.results.push({
                url: "#",
                imageUrl: "https://d.gr-assets.com/books/1307489089s/9635325.jpg",
                title: "Eden",
                series: {
                    name: "The Eden Trilogy",
                    number: 1
                },
                author: {
                    name: "Keary Taylor"
                },
                description: "Short description ..?"
            });
            search.results.push({
                url: "#",
                imageUrl: "https://d.gr-assets.com/books/1366235220s/17379615.jpg",
                title: "The Human",
                series: {
                    name: "The Eden Trilogy",
                    number: 2
                },
                author: {
                    name: "Keary Taylor"
                },
                description: ""
            });
            search.results.push({
                url: "#",
                imageUrl: "https://d.gr-assets.com/books/1366225538s/17379619.jpg",
                title: "The Eve",
                series: {
                    name: "The Eden Trilogy",
                    number: 3
                },
                author: {
                    name: "Keary Taylor"
                },
                description: "Short description 3 ..?"
            });
            // search.results = [];

            search.pagination = {
                first: null,
                prev: "#prev",
                next: null,
                last: "#last",
                numbers: [
                    {
                        number: "1",
                        url: "#1",
                        selected: true
                    },
                    {
                        number: "2",
                        url: "#2"
                    },
                    {
                        number: "3",
                        url: "#3"
                    },
                    {
                        number: "4",
                        url: "#4"
                    },
                    {
                        number: "...",
                        url: "#...",
                        title: "More pages"
                    }
                ]
            };

            $scope.search = search;
    }]
);
