$(document).ready(function() {
    $(".answerTd").click(function() {
        var OriginalContent = $(this).text();
        var updateUrl = $(this).attr('value');
        var formData = new FormData();

        var inputNewText = prompt("Add answer:", OriginalContent);
        formData.append('answer', inputNewText);
        if (inputNewText != null) {
            $(this).text(inputNewText)
            $.ajax({
                type: 'POST',
                url: updateUrl,
                data: formData,
                enctype: 'multipart/form-data',
                processData: false,
                contentType: false,
                beforeSend: function() {
                    $('#popup').attr('class', 'active')
                },
                success: function(response) {
                    $(this).text(inputNewText)

                },
                error: function() {
                    $('#popup').attr('class', '');
                    alert("Error while saving anser")
                    console.log("Błąd zapisu danych");
                },
                complete: function() {
                    OriginalContent = "";
                    updateUrl = "";
                    formData = new FormData();
                    $('#popup').attr('class', '');
                }
            });
        };
    })
});