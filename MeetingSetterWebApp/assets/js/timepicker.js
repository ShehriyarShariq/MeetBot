let TimePicker = (function () {
        return function (t) {
                let selectors = {
                        timepicker: null,
                        hour: null,
                        min: null,
                        hourElem: null,
                        hourUp: null,
                        hourDown: null,
                        minElem: null,
                        minUp: null,
                        minDown: null
                };

                function hour_change(e) {
                        if (e.target.value > 23) {
                                e.target.value = 23;
                        } else if (e.target.value < 0) {
                                e.target.value = '00';
                        }

                        if (e.target.value == "") {
                                e.target.value = formatTime(hour);
                        }

                        hour = e.target.value;
                }

                function minute_change(e) {
                        if (e.target.value > 59) {
                                e.target.value = 59;
                        } else if (e.target.value < 0) {
                                e.target.value = '00';
                        }

                        if (e.target.value == "") {
                                e.target.value = formatTime(minute);
                        }

                        minute_2 = e.target.value;
                }

                function hour_up() {
                        hour++;
                        if (hour > 23) {
                                hour = 0;
                        }
                        setTime();
                }
                function hour_down() {
                        hour--;
                        if (hour < 0) {
                                hour = 23;
                        }
                        setTime();
                }

                function minute_up() {
                        minute++;
                        if (minute > 59) {
                                minute = 0;
                                hour++;
                        }
                        setTime();
                }
                function minute_down() {
                        minute--;
                        if (minute < 0) {
                                minute = 59;
                                hour--;
                        }
                        setTime();
                }

                function setTime() {
                        hr_element.value = formatTime(hour);
                        min_element.value = formatTime(minute);
                        time_picker_element.dataset.time = formatTime(hour) + ':' + formatTime(minute);
                }

                function formatTime(time) {
                        if (time < 10) {
                                time = '0' + time;
                        }
                        return time;
                }


                (
                        this.init = function () {
                                const time_picker = document.querySelector(selectors.timepicker);

                                const hr_element = document.querySelector(selectors.timepicker + ' ' + selectors.hour + ' ' + selectors.hourElem);
                                const min_element = document.querySelector(selectors.timepicker + ' ' + selectors.min + ' ' + selectors.minElem);

                                const hr_up = document.querySelector(selectors.timepicker + ' ' + selectors.hour + ' ' + selectors.hourUp);
                                const hr_down = document.querySelector(selectors.timepicker + ' ' + selectors.hour + ' ' + selectors.hourDown);

                                const min_up = document.querySelector(selectors.timepicker + ' ' + selectors.min + ' ' + selectors.minUp);
                                const min_down = document.querySelector(selectors.timepicker + ' ' + selectors.min + ' ' + selectors.minDown);

                                let d = new Date();

                                let hour = d.getHours();
                                let minute = d.getMinutes();
                                setTime();

                                // EVENT LISTENERS
                                hr_up.addEventListener('click', hour_up);
                                hr_down.addEventListener('click', hour_down);

                                min_up.addEventListener('click', minute_up);
                                min_down.addEventListener('click', minute_down);

                                hr_element.addEventListener('change', hour_change);
                                min_element.addEventListener('change', minute_change);
                        }
                ), this.init();
        }
})();
window.TimePicker = TimePicker;