 document.addEventListener('DOMContentLoaded', (event) => {
        function initializeEditTaskScripts() {
            // Attach event listeners for the plus buttons to open the respective popups
            document.querySelectorAll('.plus-button').forEach(button => {
                button.addEventListener('click', function() {
                    const type = this.getAttribute('data-type');
                    if (type === 'employee') {
                        openAddPopup();
                    } else if (type === 'resource') {
                        openResourcePopup();
                    }
                });
            });

            // Attach event listeners for the close buttons inside the popups
            document.querySelectorAll('.close-popup').forEach(button => {
                button.addEventListener('click', function() {
                    const popupId = this.getAttribute('data-popup-id');
                    document.getElementById(popupId).style.display = 'none';
                });
            });

            // Attach event listeners for the add buttons inside the popups
            document.querySelectorAll('.add-button').forEach(button => {
                button.addEventListener('click', function() {
                    const popupId = this.getAttribute('data-popup-id');
                    addDataFromPopup(popupId);
                });
            });

            // Attach event listeners for the remove buttons
            document.querySelectorAll('.remove-button').forEach(button => {
                button.addEventListener('click', function() {
                    const entryId = this.getAttribute('data-entry-id');
                    removeEntry(entryId);
                });
            });
        }

        document.querySelectorAll('.edit-link').forEach(link => {
            link.addEventListener('click', function(event) {
                event.preventDefault();
                fetch(link.href)
                    .then(response => response.text())
                    .then(html => {
                        document.getElementById('editContainer').innerHTML = html;
                        document.getElementById('editContainer').style.display = 'block';
                        document.getElementById('filesContainer').style.display = 'none';
                        initializeEditTaskScripts();
                    })
                    .catch(error => console.warn('Error fetching edit task content:', error));
            });
        });

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


    // Initial call to setup any existing content
    initializeEditTaskScripts();
});