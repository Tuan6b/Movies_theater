document.addEventListener("DOMContentLoaded", function() {


    // Form validation for Add and Edit Movie
    var form = document.querySelector('form');
    if (form && (window.location.pathname.includes("edit_movie.jsp") || window.location.pathname.includes("add_movie.jsp") || window.location.search.includes("action=edit") || window.location.search.includes("action=add"))) {
        form.addEventListener('submit', function (e) {
            var checkedGenres = document.querySelectorAll('input[name="genreIds"]:checked');
            if (checkedGenres.length === 0) {
                alert("Vui lòng chọn ít nhất một thể loại phim!");
                e.preventDefault();
                return;
            }

            var durationInput = document.querySelector('input[name="duration"]');
            if (durationInput) {
                var duration = parseInt(durationInput.value);
                if (duration < 40 || duration > 300) {
                    alert("Lỗi: Thời lượng phim chiếu rạp phải hợp lý (nằm trong khoảng từ 40 đến 300 phút)!");
                    e.preventDefault();
                    return;
                }
            }
        });
    }
});

// Manage Movie: Emergency Hide function
function confirmEmergencyHide(event, formElement, isEmergency) {
    event.preventDefault();
    if (!isEmergency) {
        if (confirm('Bạn có chắc chắn muốn thay đổi trạng thái của phim này?')) {
            formElement.submit();
        }
    } else {
        var inputValue = prompt(
            'CẢNH BÁO KHẨN CẤP!\n\n' +
            'Phim này đang có lịch chiếu công khai. Hãy ngăn sự ảnh hưởng nghiêm trọng đến vé đã bán!\n\n' +
            'Hãy gõ chính xác: ANPHIMKHANCAP để xác nhận:'
        );
        if (inputValue === 'ANPHIMKHANCAP') {
            formElement.submit();
        } else if (inputValue !== null) {
            alert('Sai mã xác nhận! Yêu cầu gõ chính xác: ANPHIMKHANCAP');
        }
    }
}

// Genre Manage: Edit mode toggles
function enableEdit(id) {
    document.getElementById('view-mode-' + id).style.display = 'none';
    document.getElementById('edit-form-' + id).style.display = 'flex';
}

function cancelEdit(id) {
    document.getElementById('view-mode-' + id).style.display = 'flex';
    var form = document.getElementById('edit-form-' + id);
    form.style.display = 'none';
    var input = form.querySelector('input[name="genreName"]');
    if (input) {
        input.value = input.defaultValue;
    }
}

function confirmDeleteGenre(id, name) {
    if (confirm('Bạn có chắc chắn muốn xóa thể loại "' + name + '" không?\nHành động này không thể hoàn tác!')) {
        document.getElementById('delete-form-' + id).submit();
    }
}
