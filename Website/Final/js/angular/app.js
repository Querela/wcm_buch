var wcm_buch_app = angular.module("wcm_buch", [
    "ngRoute",
    "wcm_buch_controllers"
]);

// Define routes (URL to controller and template)

wcm_buch_app.config([
    "$routeProvider",
    function ($routeProvider) {
        $routeProvider
        // when we want an empty search result page
            .when("/search/", {
                templateUrl: "tpl_search.html",
                controller: "wcm_buch_search_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveSearchEmpty
            })
            // when we want the first page of the search results
            .when("/search/:searchTerm", {
                templateUrl: "tpl_search.html",
                controller: "wcm_buch_search_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveSearch
            })
            // when we want a specific page of the search results
            .when("/search/:searchTerm/:page", {
                templateUrl: "tpl_search.html",
                controller: "wcm_buch_search_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveSearchWithPage
            })
            // when we want the book details page
            .when("/book/:bookID", {
                templateUrl: "tpl_book.html",
                controller: "wcm_buch_book_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveBook
            })
            // when we want the book's series list
            .when("/series/:seriesID", {
                templateUrl: "tpl_series.html",
                controller: "wcm_buch_series_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveSeries
            })
            // otherwise redirect to ...
            .otherwise({
                /*TODO: create home page/controller ...*/
                redirectTo: "/search/Wissens- und Content-Management"
                    /*redirectTo: "/search/..."*/

                /*templateUrl: "tpl_search.html",
                controller: "wcm_buch_dummy_controller as controller",
                controllerAs: "controller"*/
            });
    }
]).config([
    "$httpProvider",
    function ($httpProvider) {
        // enable http caching
        $httpProvider.defaults.cache = true;
}]);
