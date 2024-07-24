  function showTooltip(element) {
        var tooltip = element.querySelector('.tooltip');
        console.log(tooltip);
        tooltip.classList.add('show');
        var rect = element.getBoundingClientRect();
        console.log(rect);
        tooltip.style.top = rect.top + window.scrollY + element.offsetHeight + 'px';
        tooltip.style.left = rect.left + window.scrollX + 'px';
    }

    function hideTooltip(element) {
        var tooltip = element.querySelector('.tooltip');
        console.log(tooltip);
        tooltip.classList.remove('show');
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