<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}"<#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}"</#if>>

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="importmap">
        {
            "imports": {
                "rfc4648": "${url.resourcesCommonPath}/node_modules/rfc4648/lib/rfc4648.js"
            }
        }
    </script>
    <script src="${url.resourcesPath}/js/menu-button-links.js" type="module"></script>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="module">
        import { checkCookiesAndSetTimer } from "${url.resourcesPath}/js/authChecker.js";

        checkCookiesAndSetTimer(
          "${url.ssoLoginInOtherTabsUrl?no_esc}"
        );
    </script>
     <script src=
"https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js">
    </script>
<link rel="preload" href="https://gamuatcdn.azureedge.net/ui-kit/2024-05-08-ceee9142/resources/fonts/CanaroBoldfont.otf"as="font" type="font/otf" crossorigin="">
<link rel="stylesheet" href="https://gamuatcdn.azureedge.net/ui-kit/2024-08-20-d86e99d9/resources/css/website-theme.css">
</head>
<body class="${properties.kcBodyClass!}">
 <header>
     <div class="m-global-menu-content m-extra-gam-login-menu">
         <div class="m-super-header-wrapper bg-primary-color-1">
             <div class="container h-100">
                 <div class="m-super-header-content h-100 d-flex justify-content-between align-items-center">
                     <div>
                         <a href="${msg("homePageLink")}" class="m-back-website">
                             <span class="m-back-website-icon-box">
                                 <i class="fa-regular fa-arrow-left-long text-white"></i>
                             </span>
                             <span class="font-size-12 text-white">${msg("homePageTitle")}</span>
                         </a>
                     </div>
                     <div class="super-header-dropdown position-relative">
                        <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
         <div class="${properties.kcLocaleMainClass!}" id="kc-locale">
             <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                 <div id="kc-locale-dropdown" class="${properties.kcLocaleDropDownClass!}">
                   <button id="dropdownCountries" type="button" data-bs-toggle="dropdown" aria-expanded="true" class="show">
    <span class="flag flag-${locale.current}"></span>${locale.current?upper_case} <i class="fa fa-chevron-down"></i>
    </button>
           
    <ul role="menu" tabindex="-1" aria-labelledby="dropdownCountries" aria-activedescendant="" id="language-switch1" data-popper-placement="bottom-start" class="${properties.kcLocaleListClass!}">
        <#assign i = 1>
        <#list locale.supported as l>
            <li>
                <span class="flag flag-${l.label}"></span>
                <a role="menuitem" id="language-${i}" class="${properties.kcLocaleItemClass!}" href="${l.url}" onclick="setLanguageCookie('${l.label}')">${l.label?upper_case}</a>
            </li> 
            <#assign i++>
        </#list>
    </ul>
                 </div>
             </div>
         </div>
     </#if>
                     </div>
                 </div>
             </div>
         </div>
     </div>
 </header>
<div class="${properties.kcLoginClass!}">
    <div class="container h-100">
        <div class="row h-100">
            <div class="col-md-10 col-lg-8 m-auto">
                 <div class="${properties.kcFormCardClass!}">
                       
                     <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
                         <#if displayRequiredFields>
                             <div class="${properties.kcContentWrapperClass!}">
                                 <div class="${properties.kcLabelWrapperClass!} subtitle">
                                     <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                                 </div>
                                 <div class="col-md-10">
                                   <div class="text-center pb-4">
                                         <img src="${url.resourcesPath}/img/groupama-logo.png" alt="Groupama Asset Management Logo" />
                                   </div>

                                  <h1 class="font-size-22 text-primary text-uppercase pb-3 mb-4 border-bottom"><#nested "header"></h1>
                                 </div>
                             </div>
                         <#else>
                             <div class="text-center pb-4">
                                         <img src="${url.resourcesPath}/img/groupama-logo.png" alt="Groupama Asset Management Logo" />
                                   </div>

                                  <h1 class="font-size-22 text-primary text-uppercase pb-3 mb-4 border-bottom"><#nested "header"></h1>
                         </#if>
                     <#else>
                         <#if displayRequiredFields>
                             <div class="${properties.kcContentWrapperClass!}">
                                 <div class="${properties.kcLabelWrapperClass!} subtitle">
                                     <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                                 </div>
                                 <div class="col-md-10">
                                     <#nested "show-username">
                                     <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                         <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                         <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                                             <div class="kc-login-tooltip">
                                                 <i class="${properties.kcResetFlowIcon!}"></i>
                                                 <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                             </div>
                                         </a>
                                     </div>
                                 </div>
                             </div>
                         <#else>
                             <#nested "show-username">
                             <div id="kc-username" class="${properties.kcFormGroupClass!}">
                                 <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                                 <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                                     <div class="kc-login-tooltip">
                                         <i class="${properties.kcResetFlowIcon!}"></i>
                                         <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                     </div>
                                 </a>
                             </div>
                         </#if>
                     </#if>
                   
                
                

                       <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                       <#-- during login.                                                                               -->
                       <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                           <div class="mb-3 alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                               <div class="pf-c-alert__icon">
                                   <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                   <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                   <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                   <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                               </div>
                                   <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                           </div>
                       </#if>

                       <#nested "form">

                       <#if auth?has_content && auth.showTryAnotherWayLink()>
                           <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                               <div class="${properties.kcFormGroupClass!}">
                                   <input type="hidden" name="tryAnotherWay" value="on"/>
                                   <a href="#" id="try-another-way"
                                      onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                               </div>
                           </form>
                       </#if>

                     

                       <#if displayInfo>
                           <div id="kc-info" class="${properties.kcSignUpClass!}">
                               <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                   <#nested "info">
                               </div>
                           </div>
                       </#if>
                    
                   

                 </div>
            </div>
        </div>
    </div>
</div>
<footer class="m-footer m-extra-gam-footer">
     <div class="container">
         <div class="row">
             <div class="col-lg-8">
                 <ul class="ps-4 pb-5 pb-lg-0 ps-lg-0 flex-column flex-lg-row">
                     <li>
                         <a class="font-size-12 text-decoration-none text-secondary-color-3" href="${msg("informationLegalesLink")}">${msg("informationLegalesTitle")}</a>
                     </li>
                     <li>
                         <a class="font-size-12 text-decoration-none text-secondary-color-3" href="${msg("politiquesProtectionLink")}">${msg("politiquesProtectionTitle")}</a>
                     </li>
                     <li>
                         <a class="font-size-12 text-decoration-none text-secondary-color-3" href="${msg("politiqueCookiesLink")}">${msg("politiqueCookiesTitle")}</a>
                     </li>
                 </ul>
             </div>
             <div class="col-lg-4">
                 <ul class="social-media ps-0">
                     <li>
                         <a
                             class="font-size-12 text-decoration-none d-flex align-items-center"
                             href="${msg("linkedInLink")}"
                             target="_blank"
                         >
                             <i class="fa-brands fa-linkedin font-size-24 pe-2"></i><span>${msg("linkedInTitle")}</span></a
                         >
                     </li>
                     <li>
                         <a
                             class="font-size-12 text-decoration-none d-flex align-items-center"
                             href="${msg("youtubeLink")}"
                             target="_blank"
                         >
                             <i class="fa-brands fa-youtube font-size-24 pe-2"></i><span>${msg("youtubeTitle")}</span></a
                         >
                     </li>
                     <li>
                         <a
                             class="font-size-12 text-decoration-none d-flex align-items-center"
                             href="${msg("instagramLink")}"
                             target="_blank"
                         >
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                                <path id="icons8-instagram_1_" data-name="icons8-instagram (1)" d="M10,3a7.008,7.008,0,0,0-7,7V20a7.008,7.008,0,0,0,7,7H20a7.008,7.008,0,0,0,7-7V10a7.008,7.008,0,0,0-7-7ZM22,7a1,1,0,1,1-1,1A1,1,0,0,1,22,7ZM15,9a6,6,0,1,1-6,6A6.006,6.006,0,0,1,15,9Zm0,2a4,4,0,1,0,4,4,4,4,0,0,0-4-4Z" transform="translate(-3 -3)" fill="#1c6954"/>
                            </svg>
                            <span class="ps-2">${msg("instagramTitle")}</span></a
                         >
                     </li>
                 </ul>
             </div>
         </div>
     </div>
 </footer>
 <script src="https://gamuatcdn.azureedge.net/ui-kit/2024-05-08-ceee9142/resources/js/popper.min.js"></script>
 <script src="https://gamuatcdn.azureedge.net/ui-kit/2024-05-08-ceee9142/resources/js/bootstrap.min.js"></script>
 <script>
     // Define the setLanguageCookie function
     function setLanguageCookie(langCode) {
         const cookieValue = langCode == 'fr' ? 'French' : langCode == 'en' ? 'English' : '';
         document.cookie = "languageName=" + cookieValue + "; path=/";
         console.log(langCode);
     }

     // Execute the setLanguageCookie function after the DOM has loaded
     document.addEventListener('DOMContentLoaded', function() {
         <#if realm.internationalizationEnabled && locale.supported?size gt 1>
         setLanguageCookie('${locale.current}');
         </#if>
     });
 </script> 
</body>

</html>
</#macro>
