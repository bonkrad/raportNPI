$(document).ready(function() {
    $('#createReportButton').on('click', function() {
        var createReportUrl = '/report?productId=' + $('#productId').val();
        if ($('#revisionControlSelect').val() != "0") {
            $('#createReportBlock').attr('class', 'active')
        } else {
//            var _csrf = $("input[name='_csrf']").val();
            $.ajax({
                        type: "POST",
                        data: {
                            copyLast: false,
//                            _csrf: _csrf
                        },
                        url: createReportUrl,
                        beforeSend: function() {},
                        success: function(response) {
                            console.log("Utworzono raport");
                            //console.log(data);
                            //document.write(data);
                            window.location.replace(response);
                        },
                        error: function() {
                            console.log("Błąd zapisu danych");
                        },
                        complete: function() {}
                    });
        }
    });

$('#createReportBlock').on('click', '.btnCreateReportButtonYes', function() {
var createReportUrl = '/report?productId=' + $('#productId').val();
//var _csrf = $("input[name='_csrf']").val();
        $.ajax({
                    type: "POST",
                    data: {
                        copyLast: true,
                        //_csrf: _csrf
                    },
                    url: createReportUrl,
                    beforeSend: function() {},
                    success: function(response) {
                        console.log("Utworzono raport");
                        window.location.replace(response);
                    },
                    error: function() {
                        console.log("Błąd zapisu danych");
                    },
                    complete: function() {}
                });
    });

    $('#createReportBlock').on('click', '.btnCreateReportButtonNo', function() {
            var createReportUrl = '/report?productId=' + $('#productId').val();
//            var _csrf = $("input[name='_csrf']").val();
                    $.ajax({
                                type: "POST",
                                data: {
                                    copyLast: false,
//                                    _csrf: _csrf
                                },
                                url: createReportUrl,
                                beforeSend: function() {},
                                success: function(response) {
                                    console.log("Utworzono raport");
                                    window.location.replace(response);
                                },
                                error: function() {
                                    console.log("Błąd zapisu danych");
                                },
                                complete: function() {}
                            });
        });

        $('#createReportBlock').on('click', '.btnCreateReportButtonCancel', function() {
                $('#createReportBlock').attr('class', '');
            });


    $('#viewReportButton').on('click', function() {
        if ($('#revisionControlSelect').val() == 0) {
            alert('Wybierz poprawny raport');
        } else {
            var viewReportUrl = '/viewReport?reportId=' + $('#revisionControlSelect').val() + '&productId=' + $('#productId').val();
            console.log(viewReportUrl);
            window.location.href = viewReportUrl;

        }
    });
});