var input = document.querySelector('#image_uploads');
var label = document.querySelector('#image_uploads_label');
input.addEventListener('change', updateImageInput);

function updateImageInput() {
    while (label.firstChild) {
        label.removeChild(label.firstChild);
    }

    var curFiles = input.files;
    if (curFiles.length === 0) {
        var para = document.createElement('p');
        para.textContent = 'Wybierz zdjęcie';
        label.appendChild(para);
    } else {
        for (var i = 0; i < curFiles.length; i++) {
            var para = document.createElement('p');
            if (validFileType(curFiles[i])) {
            console.log(curFiles[i].size);
                if (curFiles[i].size < 5097152) {
                    //para.textContent = curFiles[i].name;
                    //var imgSrc = window.URL.createObjectURL(curFiles[i]);
                    while (label.firstChild) {
                        label.removeChild(label.firstChild);
                    }
                    var counter=i+1;
                    para.textContent = 'Ilość wybranych: ' + counter;
                    label.appendChild(para);

                } else {
                    alert('Maksymalny rozmiar pliku 5MB');
                    $('#problemForm')[0].reset();
                    para.textContent = 'Wybierz zdjęcie';
                    label.appendChild(para);
                }
            } else {
                alert('Nie właściwy format pliku');
                $('#problemForm')[0].reset();
                para.textContent = 'Wybierz zdjęcie';
                label.appendChild(para);
            }
        }
    }
}

var fileTypes = [
    'image/jpeg',
    'image/jpg',
    'image/png'
]

function validFileType(file) {
    for (var i = 0; i < fileTypes.length; i++) {
        if (file.type === fileTypes[i]) {
            return true;
        }
    }

    return false;
}

var input_edit = document.querySelector('#editImage_uploads');
var label_edit = document.querySelector('#editImage_uploads_label');
input_edit.addEventListener('change', updateEditImageInput);

function updateEditImageInput() {
    while (label_edit.firstChild) {
        label_edit.removeChild(label_edit.firstChild);
    }

    var curFiles = input_edit.files;
    if (curFiles.length === 0) {
        var para = document.createElement('p');
        para.textContent = 'Wybierz zdjęcie';
        label_edit.appendChild(para);
    } else {
        for (var i = 0; i < curFiles.length; i++) {
            var para = document.createElement('p');
            if (validFileType(curFiles[i])) {
            console.log(curFiles[i].size);
                if (curFiles[i].size < 5097152) {
                    //para.textContent = curFiles[i].name;
                     //var imgSrc = window.URL.createObjectURL(curFiles[i]);
                     while (label_edit.firstChild) {
                        label_edit.removeChild(label_edit.firstChild);
                     }
                     var counter=i+1;
                     para.textContent = 'Ilość wybranych: ' + counter;
                     label_edit.appendChild(para);

                } else {
                    alert('Maksymalny rozmiar pliku 5MB');
                    $('#editForm')[0].reset();
                    para.textContent = 'Wybierz zdjęcie';
                    label_edit.appendChild(para);
                }
            } else {
                alert('Nie właściwy format pliku');
                $('#editForm')[0].reset();
                para.textContent = 'Wybierz zdjęcie';
                label_edit.appendChild(para);
            }
        }
    }
}

var fileTypes = [
    'image/jpeg',
    'image/jpg',
    'image/png'
]

function validFileType(file) {
    for (var i = 0; i < fileTypes.length; i++) {
        if (file.type === fileTypes[i]) {
            return true;
        }
    }

    return false;
}

var input_attachment = document.querySelector('#attachment_uploads');
var label_attachment = document.querySelector('#attachment_uploads_label');
input_attachment.addEventListener('change', updateAttachmentInput);

function updateAttachmentInput() {
    while (label_attachment.firstChild) {
        label_attachment.removeChild(label_attachment.firstChild);
    }

    var curFiles = input_attachment.files;
    if (curFiles.length === 0) {
        var para = document.createElement('p');
        para.textContent = 'Wybierz załącznik';
        label_attachment.appendChild(para);
    } else {
        for (var i = 0; i < curFiles.length; i++) {
            var para = document.createElement('p');
            console.log(curFiles[i].size);
                if (curFiles[i].size < 5097152) {
                    para.textContent = curFiles[i].name;
                    var imgSrc = window.URL.createObjectURL(curFiles[i]);
                    label_attachment.appendChild(para);
                } else {
                    alert('Maksymalny rozmiar pliku 5MB');
                    para.textContent = 'Wybierz załącznik';
                    label_attachment.appendChild(para);
                }
        }
    }
}

var edit_input_attachment = document.querySelector('#editAttachment_uploads');
var edit_label_attachment = document.querySelector('#editAttachment_uploads_label');
edit_input_attachment.addEventListener('change', updateAttachmentInput);

function updateAttachmentInput() {
    while (edit_label_attachment.firstChild) {
        edit_label_attachment.removeChild(edit_label_attachment.firstChild);
    }

    var curFiles = edit_input_attachment.files;
    if (curFiles.length === 0) {
        var para = document.createElement('p');
        para.textContent = 'Wybierz załącznik';
        edit_label_attachment.appendChild(para);
    } else {
        for (var i = 0; i < curFiles.length; i++) {
            var para = document.createElement('p');
            console.log(curFiles[i].size);
                if (curFiles[i].size < 5097152) {
                    para.textContent = curFiles[i].name;
                    var imgSrc = window.URL.createObjectURL(curFiles[i]);
                    edit_label_attachment.appendChild(para);
                } else {
                    alert('Maksymalny rozmiar pliku 5MB');
                    para.textContent = 'Wybierz załącznik';
                    edit_label_attachment.appendChild(para);
                }
        }
    }
}