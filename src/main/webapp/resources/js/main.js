
let Main = {

    init: function () {
        this.wrapper = $(document.body).children('.wrapper');
        this.topbar = this.wrapper.find('.topbar');
        this.menuButton = this.topbar.find('#button-menu');

        this._bindEvents();
    },

    _bindEvents: function () {
        let $this = this;

        if($this.menuButton.length > 0) {

            this.menuButton.off('click.menu').on('click.menu', function () {

                let wrapper = document.querySelector('.wrapper');
                let mask = document.querySelector('.mask');

                let menuIcon = document.getElementById('button-menu-icon');


                if($this.isMobile()) {
                    wrapper.classList.toggle('menu-mobile-active');

                    menuIcon.classList.toggle('fi-rr-menu-burger');
                    menuIcon.classList.toggle('fi-rr-cross');

                    mask.classList.toggle('show');
                } else {

                    if($this.isStaticMenu()) {
                        wrapper.classList.toggle('menu-static-inactive');
                    } else {
                        wrapper.classList.toggle('menu-overlay-active');
                        menuIcon.classList.toggle('fi-rr-menu-burger');
                        menuIcon.classList.toggle('fi-rr-cross');

                        mask.classList.toggle('show');
                    }

                }
            });

        }
    },

    toggleUserDropdown: function () {
        let dropdown = document.getElementById("userInfoDropdown");
        if (dropdown.style.display === "block") {
            dropdown.style.display = "none";
        } else {
            dropdown.style.display = "block";
        }
    },

    isMobile: function( ){
        return $(window).width() <= 991;
    },

    isDesktop: function() {
        return window.innerWidth > 991;
    },

    isStaticMenu: function() {
        return this.wrapper.hasClass('menu-static') && this.isDesktop();
    },

    changeChooseLabel: function () {
        let chooseButton = this.wrapper.find('.ui-fileupload-choose');
        chooseButton.find('.ui-button-text').html('Elegir');
    },

};

$(document).ready(function () {
    Main.init();
});

