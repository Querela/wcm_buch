var API_URI = "api/";

var SEARCH_ROUTE_URI = "/search/";
var BOOK_ROUTE_URI = "#/book/";

var wcm_buch_controllers = angular.module("wcm_buch_controllers", []);

function log() {
    window.console.log.apply(console, arguments);
}

wcm_buch_controllers.controller(
    "wcm_buch_search_header_controller", ["$scope", "$rootScope", "$location",
        function ($scope, $rootScope, $location) {
            $scope.search = function (searchTerm) {
                log("Redirect to: \"" + SEARCH_ROUTE_URI + searchTerm + "\"");
                $location.path(SEARCH_ROUTE_URI + searchTerm);
            };
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_search_controller", ["$scope", "$rootScope", "$http", "$routeParams",
        function ($scope, $rootScope, $http, $routeParams) {
            var url = API_URI + "search/" + $routeParams.searchTerm;
            log("Search using url: \"" + url + "\"");
            $rootScope.title = "Search \"" + $routeParams.searchTerm + "\"";

            $http.get(url)
                .success(function (data, status, headers, config) {
                    // TODO:
                    log("get success", url, data, status, headers, config);

                    var search = {};
                    search.results_total = data.search.resultsTotal;
                    search.results_start = data.search.resultsStart;
                    search.results_end = data.search.resultsEnd;
                    search.searchTerm = data.search.searchTerm;
                    search.provider = "Goodreads";
                    search.timeToSearch = data.search.timeToSearch;

                    search.results = [];
                    for (var idx = 0; idx < data.search.books.book.length; idx++) {
                        var book = data.search.books.book[idx];

                        var title = book.title;
                        var series_name = null;
                        var series_number = null;

                        if (title.indexOf("#") !== -1 && title.indexOf("(") !== -1) {
                            var i = title.lastIndexOf("(");

                            series_name = title.substring(i + 1, title.length - 1);
                            title = title.substring(0, i).trim();

                            i = series_name.lastIndexOf(",");
                            series_number = +(series_name.substring(i + 3));
                            series_name = series_name.substring(0, i);
                        }

                        search.results.push({
                            url: BOOK_ROUTE_URI + book.goodreadsID,
                            grID: book.goodreadsID,
                            grEdID: book.goodreadsEditionsID,
                            imageUrl: book.imageURL,
                            rating: book.averageRating,
                            title: title,
                            series: {
                                name: series_name,
                                number: series_number
                            },
                            author: {
                                name: book.authors.author[0].name,
                                grID: book.authors.author[0].goodreadsID
                            },
                            description: ""
                        });
                    }

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
                })
                .error(function (data, status, headers, config) {
                    // TODO:
                    log("get error", url, data, status, headers, config);
                });
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_book_controller", ["$scope", "$rootScope", "$http", "$routeParams",
        function ($scope, $rootScope, $http, $routeParams) {
            var url = API_URI + "book/" + $routeParams.bookID;
            $http.get(url)
                .success(function (data, status, headers, config) {
                    // TODO:
                    log("get success", url, data, status, headers, config);
                })
                .error(function (data, status, headers, config) {
                    // TODO:
                    log("get error", url, data, status, headers, config);
                });
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_series_controller", ["$scope", "$rootScope", "$http", "$routeParams",
        function ($scope, $rootScope, $http, $routeParams) {
            var url = API_URI + "series/" + $routeParams.seriesID;
            $http.get(url)
                .success(function (data, status, headers, config) {
                    // TODO:
                    log("get success", url, data, status, headers, config);
                })
                .error(function (data, status, headers, config) {
                    // TODO:
                    log("get error", url, data, status, headers, config);
                });
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_dummy_controller", ["$scope", "$rootScope", "$http", "$routeParams",
        function ($scope, $rootScope, $http, $routeParams) {
            log("Dummy controller ...");

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
