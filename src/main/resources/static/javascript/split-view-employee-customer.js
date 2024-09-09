 function showTooltip(element) {
        element.querySelector('.tooltip').classList.remove('hidden');
    }
    function hideTooltip(element) {
        element.querySelector('.tooltip').classList.add('hidden');
    }

    document.addEventListener('DOMContentLoaded', (event) => {
         document.querySelectorAll('.files-link').forEach(link => {
            link.addEventListener('click', function(event) {
                event.preventDefault();
                fetch(link.href)
                    .then(response => response.text())
                    .then(html => {
                        document.getElementById('filesContainer').innerHTML = html;
                        document.getElementById('filesContainer').style.display = 'block';
                        document.getElementById('editContainer').style.display = 'none';
                    })
                    .catch(error => console.warn('Error fetching files content:', error));
            });
        });
    });