var API_URI = "api/";

var SEARCH_ROUTE_URI = "/search/";
var BOOK_ROUTE_URI = "/book/";

var HREF_SEARCH_ROUTE_URI = "#" + SEARCH_ROUTE_URI;
var HREF_BOOK_ROUTE_URI = "#" + BOOK_ROUTE_URI;

var PAGINATION_MAX_PAGES = 6;
var PAGINATION_IS_LAST_PAGE_ENABLED = false;

function log() {
    window.console.log.apply(console, arguments);
}

// -----------------------------------------------------------------------------

function computePagination(start, end, total, baseUri) {
    // how many results per page
    var resultsPerPage = end - start + 1;
    // how many pages
    var countPages = ((total + resultsPerPage - 1) / resultsPerPage);
    // on which page currently
    var isOnPage = Math.floor((resultsPerPage + start) / resultsPerPage);
    // how many pagination pages
    var isOnPaginationPage = Math.floor((PAGINATION_MAX_PAGES + isOnPage - 1) / PAGINATION_MAX_PAGES);

    var maxPages = Math.floor((total - 1) / resultsPerPage) + 1;
    var maxPaginationPages = Math.floor((maxPages - 1) / PAGINATION_MAX_PAGES) + 1;
    var pagesToNextPaginationPage = ((Math.floor(isOnPage / PAGINATION_MAX_PAGES) + 1) * PAGINATION_MAX_PAGES) - isOnPage + 1;

    var pagination = {
        first: (isOnPaginationPage <= 1) ? null : baseUri + "1",
        prev: (isOnPaginationPage <= 1) ? null : baseUri + (isOnPage - PAGINATION_MAX_PAGES),
        next: (isOnPaginationPage == maxPaginationPages) ? null : baseUri + (Math.min(isOnPage + pagesToNextPaginationPage)),
        last: (!PAGINATION_IS_LAST_PAGE_ENABLED) ? null : ((isOnPaginationPage >= maxPaginationPages) ? null : baseUri + (maxPages - 1)),
        numbers: []
    };
    for (var p = Math.max(isOnPage - Math.floor(PAGINATION_MAX_PAGES / 2), 1);
        (p <= Math.max(isOnPage, 3) + Math.ceil(PAGINATION_MAX_PAGES / 2)) && (p <= maxPages); p++) {
        pagination.numbers.push({
            number: "" + p,
            url: baseUri + p,
            selected: p === isOnPage
        });
    }
    return pagination;
}

function parseTitleSeries(titleSeries) {
    var parsed = {
        title: titleSeries,
        series: {
            name: null,
            number: null
        },
        hasSeries: false
    };

    if (titleSeries.indexOf("#") !== -1 && titleSeries.indexOf("(") !== -1) {
        var i = titleSeries.lastIndexOf("(");

        parsed.title = titleSeries.substring(0, i).trim();

        titleSeries = titleSeries.substring(i + 1, titleSeries.length - 1);

        i = titleSeries.lastIndexOf(",");
        parsed.series.number = +(titleSeries.substring(i + 3));
        parsed.series.name = titleSeries.substring(0, i);
        parsed.hasSeries = true;
    }

    return parsed;
}

// -----------------------------------------------------------------------------

var wcm_buch_controllers = angular.module("wcm_buch_controllers", []);

wcm_buch_controllers.resolveSearch = {
    get_data: function ($q, $http, $route) {
        return $http.get(API_URI + "search/" + $route.current.params.searchTerm).then(function (response) {
            return response.data;
        });
    }
};

wcm_buch_controllers.resolveSearchWithPage = {
    get_data: function ($q, $http, $route) {
        return $http.get(API_URI + "search/" + $route.current.params.searchTerm + "/" + $route.current.params.page).then(function (response) {
            return response.data;
        });
    }
};

wcm_buch_controllers.resolveBook = {
    get_data: function ($http, $route) {
        log("resolveBook", $route);
        return $http.get(API_URI + "book/" + $route.current.params.bookID).then(function (response) {
            return response.data;
        });
    }
};

// -----------------------------------------------------------------------------

wcm_buch_controllers.directive("a", function () {
    return {
        restrict: "E",
        link: function (scope, elem, attrs) {
            if (attrs.href === "" || attrs.href === "#") {
                elem.on("click", function (event) {
                    event.preventDefault();

                    if (attrs.ngClick) {
                        scope.$eval(attrs.ngClick);
                    }
                });
            }
        }
    };
});

// -----------------------------------------------------------------------------

wcm_buch_controllers.controller(
    "wcm_buch_search_header_controller", ["$scope", "$rootScope", "$location",
        function ($scope, $rootScope, $location) {
            $scope.doSearch = function (searchTerm) {
                log("Redirect to: \"" + SEARCH_ROUTE_URI + searchTerm + "\"");
                $location.path(SEARCH_ROUTE_URI + searchTerm);
                // $scope.searchTerm = ""; // Uncomment to clear field after search
            };
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_search_controller", ["$scope", "$rootScope", "$location", "$http", "$routeParams", "get_data",
        function ($scope, $rootScope, $location, $http, $routeParams, get_data) {
            $rootScope.title = "Search \"" + $routeParams.searchTerm + "\"";

            var data = get_data;
            log("search", data);

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

                var parsed = parseTitleSeries(book.title);

                search.results.push({
                    url: HREF_BOOK_ROUTE_URI + book.goodreadsID,
                    grID: book.goodreadsID,
                    grEdID: book.goodreadsEditionsID,
                    imageUrl: book.imageURL,
                    rating: book.averageRating,
                    title: parsed.title,
                    series: {
                        name: parsed.series.name,
                        number: parsed.series.number
                    },
                    hasSeries: parsed.hasSeries,
                    author: {
                        name: book.authors.author[0].name,
                        grID: book.authors.author[0].goodreadsID
                    },
                    description: ""
                });
            }

            var baseSearchUri = HREF_SEARCH_ROUTE_URI + $routeParams.searchTerm + "/";
            search.pagination = computePagination(search.results_start, search.results_end, search.results_total, baseSearchUri);

            $scope.search = search;

            $scope.doSearch = function (searchTerm) {
                log("Redirect to: \"" + SEARCH_ROUTE_URI + searchTerm + "\"");
                $location.path(SEARCH_ROUTE_URI + searchTerm);
            };
}]
);

wcm_buch_controllers.controller(
    "wcm_buch_book_controller", ["$scope", "$rootScope", "$http", "$routeParams", "get_data",
        function ($scope, $rootScope, $http, $routeParams, get_data) {
            var url = API_URI + "book/" + $routeParams.bookID;

            log(get_data);
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