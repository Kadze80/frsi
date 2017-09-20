// Version 0.1
function loadProfiles(profilesSelectId) {
    var profilesString = document.app.getProfileNames('|');
    var profilesSelect = document.getElementById(profilesSelectId);
    profilesSelect.length = 0;
    var profiles = profilesString.split('|');
    for (var i = 0; i < profiles.length; i++) {
        var opt = document.createElement('option');
        opt.value = profiles[i];
        opt.text = profiles[i];
        profilesSelect.add(opt, null);
    }
}

function loadCertificates(profilesSelectId, profilePasswordId, certificatesSelectId) {
    var profile = document.getElementById(profilesSelectId).options[document.getElementById(profilesSelectId).selectedIndex].text;
    var profilePassword = document.getElementById(profilePasswordId).value;
    var certificates = document.app.getCertificatesInfo(profile, profilePassword, 0, '', true, false, '|').split('|');
    var certificatesSelect = document.getElementById(certificatesSelectId);
    certificatesSelect.length = 0;
    for (var i = 0; i < certificates.length; i++) {
        var opt = document.createElement('option');
        opt.value = certificates[i];
        opt.text = certificates[i];
        certificatesSelect.add(opt, null);
    }
}

function performAppletCode(count) {
    var applet = document.app;
    if (!applet.getProfileNames && count > 0) {
        setTimeout(function() { performAppletCode(--count); }, 2000);
    }
    else if (applet.getProfileNames) {
        loadProfiles();
    }
    else {
        alert('applet failed to load');
    }
}

function sign(profilesSelectId, profilePasswordId, certificatesSelectId, inputValue) {
    var profile = document.getElementById(profilesSelectId).options[document.getElementById(profilesSelectId).selectedIndex].text;
    var profilePassword = document.getElementById(profilePasswordId).value;
    var certificate = document.getElementById(certificatesSelectId).options[document.getElementById(certificatesSelectId).selectedIndex].text;
    var algorithmId = '1.3.6.1.4.1.6801.1.5.8';
    var pkcs7 = document.app.createPKCS7(inputValue, 0, null, certificate, true, profile, profilePassword, algorithmId, true);
    return pkcs7;
}

function signInValuesAndSetOutValues(liferayNamespace) {
    var hashInputs = document.querySelectorAll('input[id^=' + liferayNamespace + '][id$=reportHash]');
    var signInputs = document.querySelectorAll('input[id^=' + liferayNamespace + '][id$=reportSign]');
    for (var i = 0; i < hashInputs.length; i++) {
        var inId = hashInputs[i].id;
        var outId = signInputs[i].id;
        signInValueAndSetOutValue(inId, outId);
    }
}

function signInValueAndSetOutValue(inId, outId) {
    var inValue = document.getElementById(inId).value;
    var outValue = sign('profilesSelect', 'profilePassword', 'certificatesSelect', inValue);
    document.getElementById(outId).value = outValue;
}

//document.write("JavaScript version: 4 <br>");

if (deployJava.versionCheck('1.6+')) {
    var path = '/frsi-portlet/lib';
    var attributes = {codebase: './', code:'kz.gamma.TumarCSP',
        archive: path + '/commons-logging.jar, ' + path + '/xmlsec-1.3.0.jar, ' + path + '/crypto-common.jar, ' + path + '/crypto.gammaprov.jar, ' + path + '/sign-applet.jar',
        width: 0, height: 0, hspace: 0, vspace: 0, name: 'app'};
    var parameters = {java_arguments: '-Xmx256m'};
    var version = '1.6';
    deployJava.runApplet(attributes, parameters, version);
} else {
    document.write("Для подписывания данных на Вашем компьютере должна быть установлена Java версии 1.8 или выше.<br>Пожалуйста, установите или обновите Java Runtime Environment.");
    //deployJava.installLatestJRE();
}
