// customer-movie.js
document.addEventListener("DOMContentLoaded", function() {
    // Live Search for home.jsp
    const searchInput = document.getElementById('liveSearchInput');
    const searchResults = document.getElementById('liveSearchResults');
    if (searchInput && searchResults) {
        let debounceTimer;
        searchInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            const keyword = this.value.trim();

            if (keyword.length < 2) {
                searchResults.style.display = 'none';
                return;
            }

            debounceTimer = setTimeout(() => {
                const allMovies = document.querySelectorAll('.movie-title');
                let matches = [];

                allMovies.forEach(movie => {
                    const title = movie.getAttribute('title');
                    if (title && title.toLowerCase().includes(keyword.toLowerCase())) {
                        matches.push({
                            title: title,
                            url: movie.closest('a').getAttribute('href')
                        });
                    }
                });

                if (matches.length > 0) {
                    let html = '<ul style="list-style: none; padding: 0; margin: 0;">';
                    matches.forEach(m => {
                        html += `<li style="border-bottom: 1px solid #f0f0f0;">
                                    <a href="` + m.url + `" style="display: block; padding: 10px 15px; color: #333; text-decoration: none;">
                                        <i class="fa-solid fa-film" style="margin-right: 8px; color: #888;"></i> ` + m.title + `
                                    </a>
                                 </li>`;
                    });
                    html += '</ul>';
                    searchResults.innerHTML = html;
                    searchResults.style.display = 'block';
                } else {
                    searchResults.innerHTML = '<div style="padding: 10px 15px; color: #888;">Kh\u00f4ng t\u00ecm th\u1ea5y phim ph\u00f9 h\u1ee3p</div>';
                    searchResults.style.display = 'block';
                }
            }, 300);
        });

        document.addEventListener('click', function (e) {
            if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
                searchResults.style.display = 'none';
            }
        });
    }

    const reviewForms = document.querySelectorAll('form[action="ReviewController"]');
    const badWords = ["ngu", "dm", "vl", "rac", "r\u00e1c"]; 
    if (reviewForms.length > 0) {
        reviewForms.forEach(form => {
            if (form.id !== 'delete-review-form') {
                form.addEventListener('submit', function (e) {
                    const commentBox = this.querySelector('textarea[name="comment"]');
                    if (commentBox) {
                        const commentText = commentBox.value.toLowerCase();
                        const containsBadWord = badWords.some(word => commentText.includes(word));

                        if (containsBadWord) {
                            e.preventDefault();
                            alert('Ng\u00f4n t\u1eeb kh\u00f4ng ph\u00f9 h\u1ee3p! B\u00ecnh lu\u1eadn c\u1ee7a b\u1ea1n ch\u1ee9a t\u1eeb ng\u1eef vi ph\u1ea1m ti\u00eau chu\u1ea9n c\u1ed9ng \u0111\u1ed3ng. Vui l\u00f2ng s\u1eeda l\u1ea1i.');
                        }
                    }
                });
            }
        });
    }

    // Handle Edit Menu dropdown click outside
    window.addEventListener('click', function(e) {
        var menu = document.getElementById("edit-dropdown-menu");
        if (menu && menu.style.display === 'block') {
            if (!e.target.closest('h3') && !e.target.closest('#edit-dropdown-menu')) {
                menu.style.display = 'none';
            }
        }
    });
});

// movie-detail.jsp specific functions
function confirmDeleteReview(event, formElement) {
    event.preventDefault();
    if (confirm('B\u00e0i \u0111\u00e1nh gi\u00e1 c\u1ee7a b\u1ea1n s\u1ebd b\u1ecb x\u00f3a v\u0129nh vi\u1ec5n kh\u1ecfi phim n\u00e0y! B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn?')) {
        formElement.submit();
    }
}



function toggleEditMenu() {
    var menu = document.getElementById("edit-dropdown-menu");
    menu.style.display = (menu.style.display === "none" || menu.style.display === "") ? "block" : "none";
}
function showEditForm() {
    document.getElementById("edit-dropdown-menu").style.display = "none";
    document.getElementById("user-review-display").style.display = "none";
    document.getElementById("user-review-edit-form").style.display = "block";
}
function cancelEditForm() {
    document.getElementById("user-review-display").style.display = "block";
    document.getElementById("user-review-edit-form").style.display = "none";
    const editForm = document.querySelector('#user-review-edit-form form');
    if (editForm) {
        editForm.reset();
    }
}
function submitDeleteReview() {
    document.getElementById("edit-dropdown-menu").style.display = "none";
    var formElement = document.getElementById("delete-review-form");
    if (confirm('B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n x\u00f3a \u0111\u00e1nh gi\u00e1 n\u00e0y kh\u00f4ng? H\u00e0nh \u0111\u1ed9ng n\u00e0y kh\u00f4ng th\u1ec3 ho\u00e0n t\u00e1c.')) {
        formElement.submit();
    }
}
