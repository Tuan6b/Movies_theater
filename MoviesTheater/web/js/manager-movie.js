document.addEventListener("DOMContentLoaded", function() {


    // Form validation for Add and Edit Movie
    var form = document.querySelector('form');
    if (form && (window.location.pathname.includes("edit_movie.jsp") || window.location.pathname.includes("add_movie.jsp") || window.location.search.includes("action=edit") || window.location.search.includes("action=add"))) {
        form.addEventListener('submit', function (e) {
            var checkedGenres = document.querySelectorAll('input[name="genreIds"]:checked');
            if (checkedGenres.length === 0) {
                alert("Vui l\u00f2ng ch\u1ecdn \u00edt nh\u1ea5t m\u1ed9t th\u1ec3 lo\u1ea1i phim!");
                e.preventDefault();
                return;
            }

            var durationInput = document.querySelector('input[name="duration"]');
            if (durationInput) {
                var duration = parseInt(durationInput.value);
                if (duration < 40 || duration > 300) {
                    alert("L\u1ed7i: Th\u1eddi l\u01b0\u1ee3ng phim chi\u1ebfu r\u1ea1p ph\u1ea3i h\u1ee3p l\u00fd (n\u1eb1m trong kho\u1ea3ng t\u1eeb 40 \u0111\u1ebfn 300 ph\u00fat)!");
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
        if (confirm('B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n thay \u0111\u1ed5i tr\u1ea1ng th\u00e1i c\u1ee7a phim n\u00e0y?')) {
            formElement.submit();
        }
    } else {
        var inputValue = prompt(
            'C\u1ea2NH B\u00c1O KH\u1ea8N C\u1ea4P!\n\n' +
            'Phim n\u00e0y \u0111ang c\u00f3 l\u1ecbch chi\u1ebfu c\u00f4ng khai. H\u00e3y ng\u0103n s\u1ef1 \u1ea3nh h\u01b0\u1edfng nghi\u00eam tr\u1ecdng \u0111\u1ebfn v\u00e9 \u0111\u00e3 b\u00e1n!\n\n' +
            'H\u00e3y g\u00f5 ch\u00ednh x\u00e1c: ANPHIMKHANCAP \u0111\u1ec3 x\u00e1c nh\u1eadn:'
        );
        if (inputValue === 'ANPHIMKHANCAP') {
            formElement.submit();
        } else if (inputValue !== null) {
            alert('Sai m\u00e3 x\u00e1c nh\u1eadn! Y\u00eau c\u1ea7u g\u00f5 ch\u00ednh x\u00e1c: ANPHIMKHANCAP');
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
    if (confirm('B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n x\u00f3a th\u1ec3 lo\u1ea1i "' + name + '" kh\u00f4ng?\nH\u00e0nh \u0111\u1ed9ng n\u00e0y kh\u00f4ng th\u1ec3 ho\u00e0n t\u00e1c!')) {
        document.getElementById('delete-form-' + id).submit();
    }
}
