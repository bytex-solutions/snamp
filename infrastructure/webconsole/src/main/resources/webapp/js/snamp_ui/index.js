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
});

// pre-submit callback
function showRequest(formData, jqForm, options) {
    return true;
}

// post-submit callback
function redirect(data)  {
    $('.errorBlock').css("display", "none");
    Cookies.set('JWT_token', data);
    window.location.href = "index.html"
}
// error handler
function invalidCredentials(event, textStatus, error) {
    $('.errorBlock').css("display", "inline-block");
    $('.errorBody').html(error);
    console.log("Error while login to SNAMP console:", event);
}