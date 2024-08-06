let Validator = {

    validateInputText: function (input) {
        let regex = /^[a-zA-Z0-9,. ]*$/; // Incluye el espacio
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^a-zA-Z0-9,. ]/g, '');
        }
    },

    validateInputTextPlaca: function (input) {
        let regex = /^[a-zA-Z0-9-]*$/;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^a-zA-Z0-9-]/g, '');
        }
    },

    validateInputTextArea: function (input) {
        let regex = /^[a-zA-Z0-9,. ]*$/;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^a-zA-Z0-9,. ]/g, '');
        }
    },

    validateInputTextUsername: function (input) {
        let regex = /^[a-zA-Z0-9._]*$/;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^a-zA-Z0-9._]/g, '');
        }
    }

}