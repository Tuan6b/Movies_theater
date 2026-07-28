function toggleRoom(idx, checked) {
    var card = document.getElementById('card_' + idx);
    var container = document.getElementById('times_' + idx);
    var all = container.querySelectorAll('input, button');
    if (checked) {
        card.classList.add('checked');
        container.style.display = 'block';
        all.forEach(function(el) { el.disabled = false; });
    } else {
        card.classList.remove('checked');
        container.style.display = 'none';
        all.forEach(function(el) { el.disabled = true; });
    }
}

function updateEndTime(input) {
    var preview = input.parentElement.querySelector('.end-preview');
    if (!input.value) { preview.textContent = ''; return; }
    var parts = input.value.split(':');
    if (parts.length < 2) { preview.textContent = ''; return; }
    var h = parseInt(parts[0], 10);
    var m = parseInt(parts[1], 10);
    if (isNaN(h) || isNaN(m)) { preview.textContent = ''; return; }
    var tm = (typeof TOTAL_MINUTES !== 'undefined') ? TOTAL_MINUTES : 0;
    var total = h * 60 + m + tm;
    var eh = Math.floor(total / 60) % 24;
    var em = total % 60;
    preview.textContent = '→ ' + String(eh).padStart(2, '0') + ':' + String(em).padStart(2, '0');
}

function addTime(idx, roomId) {
    var container = document.getElementById('slots_' + idx);
    var row = document.createElement('div');
    row.className = 'time-row';
    row.innerHTML = '<input class="cgv-input" type="time" name="startTime_' + roomId + '" required'
                  + ' oninput="updateEndTime(this)">'
                  + '<span class="end-preview sa-end-preview"></span>'
                  + '<button type="button" class="btn--cgv-outline sa-btn-remove" '
                  + 'onclick="this.parentElement.remove()" '
                  + 'title="Remove">✖</button>';
    container.appendChild(row);
}

document.addEventListener('DOMContentLoaded', function() {
    var form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            var checked = document.querySelectorAll('input[name="roomIds"]:checked');
            if (checked.length === 0) { e.preventDefault(); alert('Please select at least one room.'); return; }
            for (var i = 0; i < checked.length; i++) {
                var cb = checked[i];
                var roomId = cb.value;
                var roomName = cb.dataset.roomName || 'Room ' + roomId;
                var times = document.querySelectorAll('input[name="startTime_' + roomId + '"]');
                var hasValue = false;
                for (var j = 0; j < times.length; j++) {
                    if (times[j].value && times[j].value.trim() !== '') { hasValue = true; break; }
                }
                if (!hasValue) { e.preventDefault(); alert('Please add at least one start time for ' + roomName + '.'); return; }
            }
        });
    }
});
