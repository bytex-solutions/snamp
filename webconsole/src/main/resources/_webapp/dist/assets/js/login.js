// prepare the form when the DOM is ready
$(document).ready(function() {
    var options = {
        beforeSubmit:  showRequest,  // pre-submit callback
        success:       redirect,  // post-submit callback
        error:         invalidCredentials,
        dataType:  'json',       // 'xml', 'script', or 'json' (expected server response type)
        timeout:   3000
    };

    // bind to the form's submit event
    $('#loginForm').submit(function() {
        // inside event callbacks 'this' is the DOM element so we first
        // wrap it in a jQuery object and then invoke ajaxSubmit
        $(this).ajaxSubmit(options);

        // !!! Important !!!
        // always return false to prevent standard browser submit and page navigation
        return false;
    });

    if ($.urlParam("tokenExpired") == "true") {
        invalidCredentials("Token has been expired", "", "Token has been expired. Please re-login")

    }
});

$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null){
       return null;
    }
    else{
       return results[1] || 0;
    }
}

// pre-submit callback
function showRequest(formData, jqForm, options) {
    return true;
}

// post-submit callback
function redirect(data)  {
    $('.errorBlock').css("display", "none");
    console.log("Success!")
    window.location.href = "."
}
// error handler
function invalidCredentials(event, textStatus, error) {
    $('.errorBlock').css("display", "inline-block");
    $('.errorBody').html(error);
    console.log("Error while login to SNAMP console:", event, textStatus, error);
}