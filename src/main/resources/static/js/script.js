var today = new Date();
var dd = today.getDate();
var mm = today.getMonth() + 1; //January is 0!
var yyyy = today.getFullYear();

if (dd < 10) {
    dd = '0' + dd
}

if (mm < 10) {
    mm = '0' + mm
}

today = yyyy + '-' + mm + '-' + dd;
console.log(today);

var imagesToDelete = [];
var attachmentsToDelete = [];

function restoreDefaultLabels() {
    var label = document.querySelector('#image_uploads_label');
    while (label.firstChild) {
        label.removeChild(label.firstChild);
    }
    var para = document.createElement('p');
    para.textContent = 'Wybierz zdjęcie';
    label.appendChild(para);
    var label_edit = document.querySelector('#editImage_uploads_label');
    while (label_edit.firstChild) {
        label_edit.removeChild(label_edit.firstChild);
    }
    var para = document.createElement('p');
    para.textContent = 'Wybierz zdjęcie';
    label_edit.appendChild(para);
    var label_attachment = document.querySelector('#attachment_uploads_label');
    while (label_attachment.firstChild) {
        label_attachment.removeChild(label_attachment.firstChild);
    }
    var para = document.createElement('p');
    para.textContent = 'Wybierz załącznik';
    label_attachment.appendChild(para);
    var label_attachment = document.querySelector('#editAttachment_uploads_label');
    while (label_attachment.firstChild) {
        label_attachment.removeChild(label_attachment.firstChild);
    }
    var para = document.createElement('p');
    para.textContent = 'Wybierz załącznik';
    label_attachment.appendChild(para);
}

function saveProblem() {
    var saveProblem = $('#problemForm').attr('action');

    var formData = new FormData();
    formData.append('description', $('#descriptionTextArea').val());
    formData.append('imgSrc', '');
    formData.append('attachmentSrc', '');
    formData.append('recommendation', $('#recommendActionTextArea').val());
    formData.append('category', $('#categoryControlSelect').val());
    formData.append('answer', $('#answerTextArea').val());
    formData.append('author', 1682);
    formData.append('priority', $('#priorityControlSelect').val());
    formData.append('closed', $('#statusControlSelect').val());
//    formData.append('_csrf', $("input[name='_csrf']").val());

    var warning1 =0;
    var warning2 =0;
    if($('#gridCheck1').is(':checked')) {
        warning1 = 1;
    }
    if($('#gridCheck2').is(':checked')) {
        warning2 = 2;
    }
    var warning = warning1+warning2;
    formData.append('warning',warning);
    var input = document.querySelector('#image_uploads');
    var curFiles = input.files;
    if (curFiles.length === 0) {
        console.log('No file selected');
    } else {
        for (var i = 0; i < curFiles.length; i++) {
            formData.append('images[]', curFiles[i]);
        }
    }
    var attachmentInput = document.querySelector('#attachment_uploads');
    var curAttachmentFiles = attachmentInput.files;
    if (curAttachmentFiles.length === 0) {
        console.log('No file selected');
    } else {
        for (var i = 0; i < curAttachmentFiles.length; i++) {
            formData.append('attachment', curAttachmentFiles[0]);
        }
    }

    $.ajax({
        type: 'POST',
        url: saveProblem, //'/products?id=' + $('#productID').val() + '&rev=' + $('#reportRev').val(),
        data: formData,
        enctype: 'multipart/form-data',
        processData: false,
        contentType: false,
        beforeSend: function() {
            $('#popup').attr('class', 'active')
        },
        success: function(response) {
            $('#resultsBlock').empty();
            $('#resultsBlock').append(response);
            $('.pop-row').popover({
                trigger: "hover"
            });
            $('.pop-button').popover({
                trigger: "hover"
            });
            $('.pop-warning').popover({
                trigger: "hover"
            });
        },
        error: function() {
            console.log("Błąd zapisu danych");
        },
        complete: function() {
            $('#problemForm')[0].reset();
            $('#popup').attr('class', '')
            restoreDefaultLabels();
        }
    });
}

$(document).ready(function() {
    $('.pop-warning').popover({
        trigger: "hover"
    });

    var url = '/products/problems?reportId=' + $('#reportRev').val();
    $("#resultsBlock").load(url, function() {
        $('.pop-row').popover({
            trigger: "hover"
        });
        $('.pop-button').popover({
                    trigger: "hover"
                });
                $('.pop-warning').popover({
                            trigger: "hover"
                        });
    });

    var url = '/report?reportId=' + $('#reportRev').val();
    $("#summaryBlock").load(url);

    var url = '/tasks?reportId=' + $('#reportRev').val();
    $("#tasksBlock").load(url, function() {
        $('.pop-button').popover({
            trigger: "hover"
        });
    });


    $('#resultsBlock').on('click', '.btnEdit', function() {
        $('#editFormBlock').attr('class', 'active')
        var updateUrl = $(this).attr('value');
        $('.btnUpdate').attr('value', updateUrl);
        $('#editDescriptionTextArea').val($(this).closest('tr').find('#descriptionTd').text());
        switch($(this).closest('tr').find('#warningTd').children().attr('id')) {
                    case 'warning1':
                        $('#editGridCheck1').attr('checked',true);
                    break;
                    case 'warning2':
                        $('#editGridCheck2').attr('checked',true);
                    break;
                    case 'warning3':
                        $('#editGridCheck1').attr('checked',true);
                        $('#editGridCheck2').attr('checked',true);
                    break;
                    default:
                        $('#editGridCheck1').attr('checked',false);
                        $('#editGridCheck2').attr('checked',false);
                    break;
                }

        if ($(this).closest('tr').find('a').attr('href') == '') {
            $('#editImageImg').attr('src', '/img/no-photo.png');
            $('#editImageA').removeAttr('href');
        } else {
            //$('#editImageImg').attr('src', $(this).closest('tr').find('a').attr('href'));
            //$('#editImageA').attr('href', $(this).closest('tr').find('a').attr('href'));
            //$('#editImageA').attr('target', '_blank');
            //var urls = [];
            $(this).closest('tr').find('a').each(function () {
                //urls.push($(this).attr('href'));
                var html = '<a href="' + $(this).attr('href') + '" target="_blank"><div class="image"><img class="img img-responsive full-width img-thumb" alt="image" src=' + $(this).attr('href') + '></image></div></a><img class="remove-image" src="img/glyphicons-208-remove.png" />';
                $('#editImageContainer').append(html);
                //imagesToDelete.push($(this).attr('href'));
            });
        }
        $('#editRecommendActionTextArea').val($(this).closest('tr').find('#recommendationTd').text());
        $('#editCategoryControlSelect').val($(this).closest('tr').find('#categoryTd').text());
        $('#editAnswerTextArea').val($(this).closest('tr').find('#answerTd').text());
        $('#editPriorityControlSelect').val($(this).closest('tr').find('#priorityTd').text());

        if ($(this).closest('tr').find('#statusTd').text() == 'open') {
            $('#editStatusControlSelect').val('false');
        } else {
            $('#editStatusControlSelect').val('true');
        }

    });

    $('#editFormBlock').on('click', '.remove-image', function() {
        console.log($(this).prev().attr('href'));
        imagesToDelete.push($(this).prev().attr('href'));
        $(this).prev().remove();
        $(this).remove();
    });

    $('#summaryBlock').on('click', '.remove-attachment-image', function() {
            console.log($(this).prev().attr('href'));
            attachmentsToDelete.push($(this).prev().attr('href'));
            $(this).prev().remove();
            $(this).remove();
        });

    function saveSummary() {
        var saveReportUrl = $('#summaryBlock').attr('value');
        var formData = new FormData();
        formData.append('summary', $('#economicalSummaryTextArea').val());
        formData.append('conclusion', $('#summaryTextArea').val());
//        formData.append('_csrf', $("input[name='_csrf']").val());
        var input = document.querySelector('#economicalSummaryFileInput');
        var curFiles = input.files;
        if (curFiles.length === 0) {
            console.log('No file selected');
        } else {
            for (var i = 0; i < curFiles.length; i++) {
               formData.append('attachment[]', curFiles[i]);
            }
        }
        formData.append('attachmentsToDelete[]', attachmentsToDelete);

        $.ajax({
            type: 'POST',
            url: saveReportUrl,
            data: formData,
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            beforeSend: function() {
                $('#popup').attr('class', 'active');
            },
            success: function(response) {
                $('#summaryBlock').empty();
                $('#summaryBlock').append(response);
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {
                $('#popup').attr('class', '');
                restoreDefaultLabels();
                imagesToDelete = [];
            }
        });
    }

    $('#summaryBlock').on('blur', '#economicalSummaryTextArea', function() {
        saveSummary();
    });

    $('#summaryBlock').on('blur', '#summaryTextArea', function() {
        saveSummary();
    });

    $('#summaryBlock').on('click', '.btnSaveSummaryAttachment', function() {
        saveSummary();
    });


    $('#tasksBlock').on('click', '.btnEditTask', function() {
        $('#editTaskFormBlock').attr('class', 'active')
        var updateTaskUrl = $(this).attr('value');
        $('.btnUpdateTask').attr('value', updateTaskUrl);
        $('#editTaskDescriptionTextArea').val($(this).closest('tr').find('#taskDescriptionTd').text());
        $('#editTaskResponsibleWorkerInput').val($(this).closest('tr').find('#taskResponsibleWorkerTd').text());
        if ($(this).closest('tr').find('#taskStatusTd').text() == 'open') {
            $('#editTaskStatusControlSelect').val('false');
        } else {
            $('#editTaskStatusControlSelect').val('true');
        }
        $('#editTaskComment').val($(this).closest('tr').find('#taskCommentTd').text());
        $('#editTaskRequiredDateInput').val($(this).closest('tr').find('#taskRequiredDateTd').text());
    });

    $('#tasksBlock').on('click', '.btnAddTask', function() {
        $('#addTaskFormBlock').attr('class', 'active')
        $('#addTaskRequiredDateInput').val(today);
    });

    $('#tasksBlock').on('click', '.btnSendReminder', function() {
        var reminderUrl = $(this).attr('value');
        var _csrf = $("input[name='_csrf']").val();
        $.ajax({
            type: "POST",
            url: reminderUrl,
//            data: {
//                _csrf: _csrf
//            },
            beforeSend: function() {},
            success: function(response) {
                alert('Wysłano powiadomienie');
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {}
        });
    });

    $('#resultsBlock').on('click', '.btnDelete', function() {
        var deleteUrl = $(this).attr('value');
        var _csrf = $("input[name='_csrf']").val();
        $.ajax({
            type: "POST",
            url: deleteUrl, //'/products?id=' + $('#productID').val() + '&rev=' + $('#reportRev').val() + '&problemId=' + $('#reportRev').val(),
            //data : problem,
//            data: {
//                 _csrf: _csrf
//            },
            beforeSend: function() {
                $('#popup').attr('class', 'active')
            },
            success: function(response) {
                $("#resultsBlock").empty();
                $('#resultsBlock').append(response);
                $('.pop-row').popover({
                    trigger: "hover"
                });
                $('.pop-button').popover({
                    trigger: "hover"
                });
                $('.pop-warning').popover({
                    trigger: "hover"
                });
                $('.popover').remove();
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {
                $('#popup').attr('class', '');
                restoreDefaultLabels();
            }
        });
    });

    $('#tasksBlock').on('click', '.btnDeleteTask', function() {
        var deleteTaskUrl = $(this).attr('value');
        var _csrf = $("input[name='_csrf']").val();
        $.ajax({
            type: "POST",
            url: deleteTaskUrl,
//            data: {
//                _csrf: _csrf
//            },
            beforeSend: function() {
                $('#popup').attr('class', 'active')
            },
            success: function(response) {
                $("#tasksBlock").empty();
                $('#tasksBlock').append(response);
                $('.pop-button').popover({
                    trigger: "hover"
                });
                $('.popover').remove();
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {
                $('#popup').attr('class', '');
                restoreDefaultLabels();
            }
        });
    });

    $('.btnUpdate').on('click', function() {
        var updateUrl = $(this).attr('value');
        var formData = new FormData();
        formData.append('description', $('#editDescriptionTextArea').val());
        var warning1 =0;
            var warning2 =0;
            if($('#editGridCheck1').is(':checked')) {
                warning1 = 1;
            }
            if($('#editGridCheck2').is(':checked')) {
                warning2 = 2;
            }
            var warning = warning1+warning2;
            formData.append('warning',warning);
        formData.append('attachmentSrc', '');
        formData.append('recommendation', $('#editRecommendActionTextArea').val());
        formData.append('category', $('#editCategoryControlSelect').val());
        formData.append('answer', $('#editAnswerTextArea').val());
        //formData.append('author', 1682);
        formData.append('priority', $('#editPriorityControlSelect').val());
        formData.append('closed', $('#editStatusControlSelect').val());
        formData.append('imagesToDelete[]', imagesToDelete);
        var input = document.querySelector('#editImage_uploads');
        var curFiles = input.files;
        if (curFiles.length === 0) {
            console.log('No file selected');
        } else {
            for (var i = 0; i < curFiles.length; i++) {
                formData.append('images[]', curFiles[i]);
            }
        }
        var input = document.querySelector('#editAttachment_uploads');
        var curFiles = input.files;
        if (curFiles.length === 0) {
            console.log('No file selected');
        } else {
            for (var i = 0; i < curFiles.length; i++) {
                formData.append('attachment', curFiles[0]);
            }
        }
//        formData.append('_csrf', $("input[name='_csrf']").val());

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
                $('#resultsBlock').empty();
                $('#resultsBlock').append(response);
                $('.pop-row').popover({
                    trigger: "hover"
                });
                $('.pop-button').popover({
                    trigger: "hover"
                });
                $('.pop-warning').popover({
                                    trigger: "hover"
                                });
                $('.popover').remove();
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {
                $('#editForm')[0].reset();
                $('#editFormBlock').attr('class', '');
                $('#popup').attr('class', '');
                restoreDefaultLabels();
                $('#editImageContainer').html("");
                imagesToDelete = [];
                $('#editGridCheck1').attr('checked',false);
                $('#editGridCheck2').attr('checked',false);
            }
        });
    });

    $('.btnSaveTask').on('click', function() {
        var addTaskUrl = $(this).attr('value');
        var formData = new FormData();
        formData.append('description', $('#addTaskDescriptionTextArea').val());
        formData.append('comment', $('#addTaskComment').val());
        /*if (isNaN(parseInt($('#addTaskResponsibleWorkerInput').val()))) {
            formData.append('responsibleWorker', 0);
        } else {
            formData.append('responsibleWorker', parseInt($('#addTaskResponsibleWorkerInput').val()));
        }*/
        formData.append('responsibleWorker', $('#addTaskResponsibleWorkerInput').val());
        formData.append('rDate', $('#addTaskRequiredDateInput').val());
        //formData.append('author', 1682);
        formData.append('closed', $('#addTaskStatusControlSelect').val());
//            formData.append('_csrf', $("input[name='_csrf']").val());

        $.ajax({
            type: 'POST',
            url: addTaskUrl,
            data: formData,
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            beforeSend: function() {
                $('#popup').attr('class', 'active')

            },
            success: function(response) {
                $('#tasksBlock').empty();
                $('#tasksBlock').append(response);
                $('.pop-button').popover({
                    trigger: "hover"
                });
                $('.popover').remove();
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {
                $('#addTaskForm')[0].reset();
                $('#addTaskFormBlock').attr('class', '');
                $('#popup').attr('class', '');
                restoreDefaultLabels();
            }
        });
    });

    $('.btnUpdateTask').on('click', function() {
        var updateTaskUrl = $(this).attr('value');
        var formData = new FormData();
        formData.append('description', $('#editTaskDescriptionTextArea').val());
        formData.append('comment', $('#editTaskComment').val());
        /*if (isNaN(parseInt($('#editTaskResponsibleWorkerInput').val()))) {
            formData.append('responsibleWorker', 0);
        } else {
            formData.append('responsibleWorker', parseInt($('#editTaskResponsibleWorkerInput').val()));
        }*/
        formData.append('responsibleWorker', $('#editTaskResponsibleWorkerInput').val());
        formData.append('rDate', $('#editTaskRequiredDateInput').val());
        //formData.append('author', 1682);
        formData.append('closed', $('#editTaskStatusControlSelect').val());
//            formData.append('_csrf', $("input[name='_csrf']").val());

        $.ajax({
            type: 'POST',
            url: updateTaskUrl,
            data: formData,
            enctype: 'multipart/form-data',
            processData: false,
            contentType: false,
            beforeSend: function() {
                $('#popup').attr('class', 'active')
            },
            success: function(response) {
                $('#tasksBlock').empty();
                $('#tasksBlock').append(response);
                $('.pop-button').popover({
                    trigger: "hover"
                });
                $('.popover').remove();
            },
            error: function() {
                console.log("Błąd zapisu danych");
            },
            complete: function() {
                $('#editTaskForm')[0].reset();
                $('#editTaskFormBlock').attr('class', '');
                $('#popup').attr('class', '');
                restoreDefaultLabels();
            }
        });
    });


    $('#btnCancel').on('click', function cancel() {
        $('.btnUpdate').attr('value', '');
        $('#editForm')[0].reset();
        $('#editFormBlock').attr('class', '');
        $('#editImageContainer').html("");
        imagesToDelete = [];
        $('#editGridCheck1').attr('checked',false);
        $('#editGridCheck2').attr('checked',false);
    });

    $('#btnCancelAddTask').on('click', function cancel() {
        //$('.btnUpdate').attr('value','');
        $('#addTaskForm')[0].reset();
        $('#addTaskFormBlock').attr('class', '');
    });

    $('#btnCancelEditTask').on('click', function cancel() {
        //$('.btnUpdate').attr('value','');
        $('#editTaskForm')[0].reset();
        $('#editTaskFormBlock').attr('class', '');
    });
});