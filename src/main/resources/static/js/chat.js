$(window).on('load' , function() {
    $('#chat-container').children().last()[0].scrollIntoView({ behavior: 'smooth'});
});

function sendMessage() {
    $('#message').removeClass("error").attr("placeholder" , "Message...")
    const message = $("#message").val();

    if(!message) {
        $('#message').addClass("error").attr("placeholder" , "Please Enter A Message...")
        return;
    }

    const dataToSend = new FormData();
    dataToSend.append("message" , message)
    dataToSend.append("userId" , $("#userId").val());
    dataToSend.append("providerId" , $("#providerId").val());

    $.ajax({
            type: "POST",
            url: '/chat/save',
            data: dataToSend,
            processData: false,
            contentType: false,
            success: function(data, textStatus, jqXHR) {
               //process data
               $("#chat-container").append(`
                <div class="d-flex justify-content-end"><div class="card bg-warning w-25 my-1"><div class="card-body"><h6 class="card-text">${message}</h6></div></div></div>
               `);
               $("#message").val("");
               $('#chat-container').children().last()[0].scrollIntoView({ behavior: 'smooth'});
            },
            error: function(data, textStatus, jqXHR) {
               //process error msg
            },
    });
}

$("#message").keyup(function(event) {
    if (event.keyCode === 13) {
       sendMessage();
    }
});

$('#sendBtn').on('click' , sendMessage);
