var wcm_buch_app = angular.module("wcm_buch", [
    "ngRoute",
    "wcm_buch_controllers"
]);

wcm_buch_app.config([
    "$routeProvider",
    function ($routeProvider) {
        $routeProvider
            .when("/search/:searchTerm", {
                templateUrl: "tpl_search.html",
                controller: "wcm_buch_search_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveSearch
            })
            .when("/search/:searchTerm/:page", {
                templateUrl: "tpl_search.html",
                controller: "wcm_buch_search_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveSearchWithPage
            })
            .when("/book/:bookID", {
                templateUrl: "tpl_book.html",
                controller: "wcm_buch_book_controller",
                controllerAs: "controller",
                resolve: wcm_buch_controllers.resolveBook
            })
            .when("/series/:seriesID", {
                templateUrl: "",
                controller: "wcm_buch_series_controller",
                controllerAs: "controller"
            })
            .otherwise({
                /*TODO: create home page/controller ...*/
                /*redirectTo: "/"*/
                templateUrl: "tpl_search.html",
                controller: "wcm_buch_dummy_controller as controller",
                controllerAs: "controller"
            });
    }
]);
