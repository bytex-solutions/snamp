$(document).ready(function() {
    $.ajax({
        method: "GET",
        url: "/snamp/console/username",
        headers: {"Authorization" : "Bearer " + Cookies.get("snamp-auth-token")},
        success: function (event) {
            $("#username").html(event);
        },
         error : function (event, textStatus, error) {
                console.log("Auth is not working.", textStatus, error);
                window.location.href = "login.html?tokenExpired=true"
         }
      })
});