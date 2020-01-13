const time_picker_element_2 = document.querySelector('.time-picker-2');

const hr_element_2 = document.querySelector('.time-picker-2 .hour-2 .hr-2');
const min_element_2 = document.querySelector('.time-picker-2 .minute-2 .min-2');

const hr_up_2 = document.querySelector('.time-picker-2 .hour-2 .hr-up-2');
const hr_down_2 = document.querySelector('.time-picker-2 .hour-2 .hr-down-2');

const min_up_2 = document.querySelector('.time-picker-2 .minute-2 .min-up-2');
const min_down_2 = document.querySelector('.time-picker-2 .minute-2 .min-down-2');

let d_2 = new Date();

let hour_2 = d_2.getHours();
let minute_2 = d_2.getMinutes();

hr_element_2.value = formatTime(hour_2);
min_element_2.value = formatTime(minute_2);
time_picker_element_2.dataset.time = formatTime(hour_2) + ':' + formatTime(minute_2);

// EVENT LISTENERS
hr_up_2.addEventListener('click', hour_up_2);
hr_down_2.addEventListener('click', hour_down_2);

min_up_2.addEventListener('click', minute_up_2);
min_down_2.addEventListener('click', minute_down_2);

hr_element_2.addEventListener('change', hour_change_2);
min_element_2.addEventListener('change', minute_change_2);

function formatTime(time) {
    if (time < 10) {
        time = '0' + time;
    }
    return time;
}

function hour_change_2(e) {
    if (e.target.value > 23) {
        e.target.value = 23;
    } else if (e.target.value < 0) {
        e.target.value = '00';
    }

    if (e.target.value == "") {
        e.target.value = formatTime(hour_2);
    }

    hour_2 = e.target.value;
}

function minute_change_2(e) {
    if (e.target.value > 59) {
        e.target.value = 59;
    } else if (e.target.value < 0) {
        e.target.value = '00';
    }

    if (e.target.value == "") {
        e.target.value = formatTime(minute_2);
    }

    minute_2 = e.target.value;
}

function hour_up_2() {
    hour_2++;
    if (hour_2 > 23) {
        hour_2 = 0;
    }

    hr_element_2.value = formatTime(hour_2);
    min_element_2.value = formatTime(minute_2);
    time_picker_element_2.dataset.time = formatTime(hour_2) + ':' + formatTime(minute_2);
}
function hour_down_2() {
    hour_2--;
    if (hour_2 < 0) {
        hour_2 = 23;
    }

    hr_element_2.value = formatTime(hour_2);
    min_element_2.value = formatTime(minute_2);
    time_picker_element_2.dataset.time = formatTime(hour_2) + ':' + formatTime(minute_2);
}

function minute_up_2() {
    minute_2++;
    if (minute_2 > 59) {
        minute_2 = 0;
        hour_2++;
    }

    hr_element_2.value = formatTime(hour_2);
    min_element_2.value = formatTime(minute_2);
    time_picker_element_2.dataset.time = formatTime(hour_2) + ':' + formatTime(minute_2);
}

function minute_down_2() {
    minute_2--;
    if (minute_2 < 0) {
        minute_2 = 59;
        hour_2--;
    }

    hr_element_2.value = formatTime(hour_2);
    min_element_2.value = formatTime(minute_2);
    time_picker_element_2.dataset.time = formatTime(hour_2) + ':' + formatTime(minute_2);
}
