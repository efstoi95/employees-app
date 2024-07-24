 function openAddPopup() {
        document.getElementById("addEmployeePopup").style.display = "block";
    }

    function closeAddPopup() {
        document.getElementById("addEmployeePopup").style.display = "none";
    }

    function openResourcePopup() {
        document.getElementById("addResourcePopup").style.display = "block";
    }

    function closeResourcePopup() {
        document.getElementById("addResourcePopup").style.display = "none";
    }

    function addSelectedEmployees() {
        var checkboxes = document.querySelectorAll('#addEmployeePopup input[type="checkbox"]');
        var select = document.getElementById("eligibleEmployees");

        checkboxes.forEach(function(checkbox) {
            if (checkbox.checked) {
                var exists = Array.from(select.options).some(option => option.value === checkbox.value);
                if (!exists) {
                    var option = document.createElement("option");
                    option.value = checkbox.value;
                    option.text = checkbox.closest('td').previousElementSibling.textContent;
                    option.selected = true;
                    select.appendChild(option);
                }
            }
        });
        closeAddPopup();
    }

    function addSelectedResources() {
        var checkboxes = document.querySelectorAll('#addResourcePopup input[type="checkbox"]');
        var select = document.getElementById("resources");

        checkboxes.forEach(function(checkbox) {
            if (checkbox.checked) {
                var exists = Array.from(select.options).some(option => option.value === checkbox.value);
                if (!exists) {
                    var option = document.createElement("option");
                    option.value = checkbox.value;
                    option.text = checkbox.closest('td').previousElementSibling.textContent;
                    option.selected = true;
                    select.appendChild(option);
                }
            }
        });
        closeResourcePopup();
    }