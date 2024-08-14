let Validator = {

    validateInputText: function (input) {
        let regex = /^[\p{L}0-9 ]*$/u;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^\p{L}0-9 ]/gu, '');
        }
    },

    validateInputTextCode: function (input) {
        let regex = /^[\p{L}0-9-]*$/u;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^\p{L}0-9-]/gu, '');
        }
    },

    validateInputTextArea: function (input) {
        let regex = /^[\p{L}0-9/(),. ]*$/u;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^\p{L}0-9/(),. ]/gu, '');
        }
    },

    validateInputTextUsername: function (input) {
        let regex = /^[a-zA-Z0-9._]*$/;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^a-zA-Z0-9._]/g, '');
        }
    },

    validateInputTextEmail: function (input) {
        let regex = /^[a-zA-Z0-9._@]*$/;
        if (!regex.test(input.value)) {
            input.value = input.value.replace(/[^a-zA-Z0-9._@]/g, '');
        }
    }

}
