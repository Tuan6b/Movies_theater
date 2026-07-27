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
                    searchResults.innerHTML = '<div style="padding: 10px 15px; color: #888;">Không tìm thấy phim phù hợp</div>';
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
    const badWords = ["ngu", "dm", "vl", "rac", "rác"]; 
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
                            alert('Ngôn từ không phù hợp! Bình luận của bạn chứa từ ngữ vi phạm tiêu chuẩn cộng đồng. Vui lòng sửa lại.');
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
    if (confirm('Bài đánh giá của bạn sẽ bị xóa vĩnh viễn khỏi phim này! Bạn có chắc chắn?')) {
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
    if (confirm('Bạn có chắc chắn muốn xóa đánh giá này không? Hành động này không thể hoàn tác.')) {
        formElement.submit();
    }
}
