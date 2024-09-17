<#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>

    <#if section = "header">
        ${msg("loginTitle")}

    <#elseif section = "form">
        <#if messagesPerField.existsError('username','password')>
            <div class="mb-3 alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                <div class="pf-c-alert__icon">

                    <span class="${properties.kcFeedbackErrorIcon!}"></span>

                </div>
                <span class="${properties.kcAlertTitleClass!}">  ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</span>
            </div>
        </#if>

        <#if realm.password>
            <form id="kc-form-login" class ="m-login-form-wrapper" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <#if !usernameHidden??>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("usernameHolder")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameHolder")}<#else>${msg("usernameHolder")}</#if></label>

                        <input tabindex="2" id="username"  placeholder="${msg("usernameHolder")}" class="form-control" name="username" value="${(login.username!'')}"  type="text" autofocus autocomplete="username"
                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                        />

                        <span class="font-size-8 text-primary d-none d-lg-inline-flex">${msg("requiredFieldTitle")}</span>



                    </div>
                </#if>

                <div class="${properties.kcFormGroupClass!}">
                    <label for="password" class="${properties.kcLabelClass!}">${msg("passwordHolder")}</label>

                    <div class="${properties.kcInputGroup!}">
                        <input tabindex="3" id="password"  placeholder="${msg("passwordHolder")}" class="form-control" name="password" type="password" autocomplete="current-password"
                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                        />
                        <button class="${properties.kcFormPasswordVisibilityButtonClass!}" type="button" aria-label="${msg("showPassword")}"
                                aria-controls="password" data-password-toggle tabindex="4"
                                data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}" data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                            <i class="${properties.kcFormPasswordVisibilityIconShow!}" aria-hidden="true"></i>
                        </button>
                    </div>
                    <#if realm.resetPasswordAllowed>
                        <span class="font-size-12 text-primary d-inline-block w-100 text-lg-end"><a tabindex="6" href="${url.loginResetCredentialsUrl}" class="text-decoration-none">${msg("doForgotPassword")}</a></span>
                    </#if>

                    <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                            </span>
                    </#if>

                </div>
                <#if captchaRequired?? && captchaRequired>
                    <div class="data-recaptcha">
                        <p class="text-nowrap">${msg("captchaTitle")}</p>
                        <button class="text-secondary" type="button" onclick="reGenerateCaptcha()">
                            <span class="d-flex align-items-center mb-0 justify-content-center">
                                <svg xmlns="http://www.w3.org/2000/svg" width="17.626" height="12.426" viewBox="0 0 17.626 12.426">
                                    <path id="reset-10" d="M89.242,207.658H87.013l1.659-1.089a.525.525,0,1,0-.57-.881l-2.955,1.97-.156.1a.471.471,0,0,0-.207.415.64.64,0,0,0,.207.415l3.111,2.074a.285.285,0,0,0,.259.1.64.64,0,0,0,.415-.207.51.51,0,0,0-.156-.726l-1.659-1.141h2.281a4.147,4.147,0,0,1,0,8.295H81.984a4.147,4.147,0,0,1,0-8.295h1.192a.518.518,0,1,0,0-1.037H81.984a5.184,5.184,0,1,0,0,10.368h7.258a5.184,5.184,0,0,0,0-10.368Z" transform="translate(-76.8 -205.6)" fill="#000"/>
                                </svg>
                            </span>
                        </button>
                        <img class="captcha" src="" id="captcha_image" alt="CAPTCHA" data-captcha-image="${captchaImage}"/>
                        <button class="d-flex align-items-center mb-0 justify-content-center" type="button" onclick="playAudio()"><i class='fas fa-volume-down' ></i></button>
                        <audio id="captcha_audio" controls style="display:none;" data-captcha-audio="${captchaAudio}"></audio>
                    </div>
                    <div class="input-recaptcha">
                        <label class="font-bold font-size-12 text-primary text-uppercase mb-3 w-100">Captcha</label>
                        <input type="hidden" id="captcha_token" name="captcha_token" data-captcha-token="${captchaToken}"/>
                        <input type="text" id="captcha_text" name="captcha_text" placeholder="${msg("captchaPlaceholder")}">
                    </div>
                    <script>
                        function extractRealmName(url) {
                            const realmKeyword = "realms/";
                            const realmStart = url.indexOf(realmKeyword) + realmKeyword.length;

                            if (realmStart === -1) {
                                return null; // "realms/" not found in the URL
                            }

                            const realmEnd = url.indexOf('/', realmStart);
                            return url.substring(realmStart, realmEnd);
                        }

                        const realmName = extractRealmName(window.location.pathname);
                        function generateCaptcha() {
                                    const captchaImage = 'data:image/jpeg;base64,' + "${captchaImage}" ;
                                    const captchaAudio = 'data:audio/wav;base64,' + "${captchaAudio}" ;
                                    document.getElementById('captcha_image').src = captchaImage;
                                    document.getElementById('captcha_audio').src = captchaAudio;
                                    document.getElementById('captcha_audio').load(); // Load the audio data
                                    document.getElementById('captcha_token').value = "${captchaToken}";
                        }

                        function reGenerateCaptcha() {
                            const form = document.getElementById('kc-form-login');
                            const actionUrl = new URL(form.action); // Parse the form action as a URL

                            // Remove the 'session_code' parameter from the URL
                            actionUrl.searchParams.delete('session_code');
                            actionUrl.searchParams.delete('execution');
                            // Construct the new URL without the 'session_code'
                            const newUrl = actionUrl.toString();

                            fetch(newUrl, {
                                method: 'GET',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                    'X-Captcha-Regen': 'true'
                                }
                            })
                                .then(response => response.text()) // Get the response as HTML
                                .then(html => {
                                    const parser = new DOMParser();
                                    const doc = parser.parseFromString(html, 'text/html');

                                    const captchaImage = 'data:image/jpeg;base64,' + doc.querySelector('#captcha_image')?.getAttribute('data-captcha-image');
                                    const captchaAudio = 'data:audio/wav;base64,' + doc.querySelector('#captcha_audio')?.getAttribute('data-captcha-audio');
                                    const captchaToken = doc.querySelector('#captcha_token')?.getAttribute('data-captcha-token');
                                    document.getElementById('captcha_image').src = captchaImage;
                                    document.getElementById('captcha_audio').src = captchaAudio;
                                    document.getElementById('captcha_audio').load();
                                    document.getElementById('captcha_token').value = captchaToken;
                                    console.log(captchaToken)
                                })
                                .catch(error => console.error('Error fetching CAPTCHA:', error));
                        }

                        function playAudio() {
                            const audioElement = document.getElementById('captcha_audio');
                            if (audioElement.src) {
                                audioElement.play().catch(error => console.error('Error playing audio:', error));
                            } else {
                                console.error('Audio source is not set.');
                            }
                        }

                        // Generate CAPTCHA on page load
                        document.addEventListener("DOMContentLoaded", function() {
                            generateCaptcha();
                        });
                    </script>
                </#if>
                <div class="m-login-form-submit">
                    <#if realm.password && social?? && social.providers?has_content>
                        <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}"  style="display: none;">
                            <ul class="ps-0 <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                                <#assign p = social.providers[0]>


                                <li>
                                    <a id="social-${p.alias}" class="signin-btn ${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                                       type="button" href="${p.loginUrl}">
                                        <#if p.iconClasses?has_content>
                                            <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                                            <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${msg("loginTitleSSO")}</span>
                                        <#else>
                                            <span class="${properties.kcFormSocialAccountNameClass!}">${msg("loginTitleSSO")}</span>
                                        </#if>
                                    </a>
                                </li>



                            </ul>
                        </div>

                    </#if>
                    <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                        <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                        <input tabindex="7" class="${properties.kcButtonClass!} signin-btn" name="login" id="kc-login" type="submit" value="${msg("doLogInTitle")}"/>
                    </div>
                </div>

            </form>
            <span class="font-size-8 text-primary d-lg-none">${msg("requiredFieldTitle")}</span>

            <div class="font-size-10 text-primary m-login-condition m-scrollable">
                <p>
                    ${msg("description")}
                </p>
            </div>
        </#if>


        <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a tabindex="8"
                                                 href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>


    </#if>
</@layout.registrationLayout>
<script>
    /* Add "https://api.ipify.org?format=json" to
    get the IP Address of user*/
    $(document).ready(()=>{
        $.getJSON("https://api.ipify.org?format=json",
            function (data) {
                if(data.ip=="197.14.13.34" || data.ip == "196.203.12.167"){
                    const kcSocialProviders = document.querySelectorAll('div[id="kc-social-providers"]');
                    kcSocialProviders.forEach(paragraph => {
                        paragraph.style.display = "block";
                    });

                }
            })
    });
</script>
