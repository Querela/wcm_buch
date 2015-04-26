// Constants

var API_URI = "api/";

var SEARCH_ROUTE_URI = "/search/";
var BOOK_ROUTE_URI = "/book/";
var SERIES_ROUTE_URI = "/series/";

var HREF_SEARCH_ROUTE_URI = "#" + SEARCH_ROUTE_URI;
var HREF_BOOK_ROUTE_URI = "#" + BOOK_ROUTE_URI;
var HREF_SERIES_ROUTE_URI = "#" + SERIES_ROUTE_URI;

var PAGINATION_MAX_PAGES = 6;
var PAGINATION_IS_LAST_PAGE_ENABLED = false;

function log() {
    window.console.log.apply(console, arguments);
}

// -----------------------------------------------------------------------------

// Helper methods

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

// Define everything (controllers etc.)

var wcm_buch_controllers = angular.module("wcm_buch_controllers", []);

// Define resolve methods (return promises for lazy actualization) ...

wcm_buch_controllers.resolveSearchEmpty = {
    get_data: function ($q, $http, $route) {
        // Return empty data ...
        return {
            search: {
                books: {
                    book: []
                },
                resultsEnd: 0,
                resultsPerPage: 0,
                resultsStart: 0,
                resultsTotal: 0,
                searchTerm: "...",
                timeToSearch: 0.0
            }
        };
    }
};

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

wcm_buch_controllers.resolveSeries = {
    get_data: function ($http, $route) {
        log("resolveBook", $route);
        return $http.get(API_URI + "series/" + $route.current.params.seriesID).then(function (response) {
            return response.data;
        });
    }
};

// -----------------------------------------------------------------------------

// HTML directives -> new html tags

// Override a tag to disable hyperlinking if we don't want it
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

// Controllers (Control templates and define functions as reactions to website interactions)

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

            var search = {
                searchTerm: data.search.searchTerm,
                provider: "Goodreads",
                results_total: data.search.resultsTotal,
                results_start: data.search.resultsStart,
                results_end: data.search.resultsEnd,
                timeToSearch: data.search.timeToSearch,
                results: []
            };

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
            var data = get_data;
            log("Book", data);

            $rootScope.title = "\"" + data.book.title + "\" (" + $routeParams.bookID + ")";

            var parsed = parseTitleSeries(data.book.title);
            var book = {
                title: parsed.title,
                series: {
                    name: parsed.series.name,
                    number: parsed.series.number
                },
                hasSeries: parsed.hasSeries,
                description: data.book.description,
                grUrl: data.book.url,
                imageUrl: data.book.imageURL,
                grID: data.book.goodreadsID,
                grEdID: data.book.goodreadsEditionsID,
                language: (data.book.language) ? data.book.language : "?",
                authors: [],
                moreAuthors: [],
                shelves: data.book.shelves.shelf
            };
            if (book.hasSeries) {
                book.series.grID = data.book.series.goodreadsID;
                book.series.description = data.book.series.description;
                book.series.url = HREF_SERIES_ROUTE_URI + data.book.series.goodreadsID;
            }
            for (var idx = 0; idx < data.book.authors.author.length; idx++) {
                var author = data.book.authors.author[idx];
                if (idx == 0) {
                    book.authors.push({
                        url: "",
                        grID: author.goodreadsID,
                        name: author.name
                    });
                } else {
                    book.moreAuthors.push({
                        url: "",
                        grID: author.goodreadsID,
                        name: author.name
                    });
                }
            }
            $scope.book = book;

            // TODO: get languages?
            // -> https://github.com/Querela/wcm_buch/commit/816f50874cc30dfa8cca7548d868bc6ff2d807a1#diff-e4c5ba9eb397068b0df08219d7f4e953L12
    }]
);

wcm_buch_controllers.controller(
    "wcm_buch_series_controller", ["$scope", "$rootScope", "$http", "$routeParams", "get_data",
        function ($scope, $rootScope, $http, $routeParams, get_data) {
            var data = get_data;
            log("Series", data);

            $rootScope.title = "Series \"" + data.series.title + "\" (" + $routeParams.seriesID + ")";

            var series = {
                title: data.series.name,
                description: data.series.description,
                numberOfBooks: data.series.numberOfBooks,
                numberOfAllBooks: data.series.books.book.length,
                books: []
            };

            for (var idx = 0; idx < data.series.books.book.length; idx++) {
                var book = data.series.books.book[idx];

                var parsed = parseTitleSeries(book.title);

                series.books.push({
                    url: HREF_BOOK_ROUTE_URI + book.goodreadsID,
                    grID: book.goodreadsID,
                    grEdID: book.goodreadsEditionsID,
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

            $scope.series = series;
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
