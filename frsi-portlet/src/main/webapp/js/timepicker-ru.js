/* Russian (UTF-8) initialisation for the jQuery UI time picker plugin. */
/* Written by Andrew Stromnov (stromnov@gmail.com). */
(function( factory ) {
	if ( typeof define === "function" && define.amd ) {

		// AMD. Register as an anonymous module.
		define([ "../timepicker" ], factory );
	} else {

		// Browser globals
		factory( jQuery.timepicker );
	}
}(function( timepicker ) {

	timepicker.regional['ru'] = {
		currentText: 'Сейчас',
		closeText: 'Готово',
		timeFormat: 'hh:mm:ss',
		timeText: 'Время',
		hourText: 'Часов',
		minuteText: 'Минут',
		secondText: 'Секунд'
	};
	timepicker.setDefaults(timepicker.regional['ru']);

	return timepicker.regional['ru'];

}));