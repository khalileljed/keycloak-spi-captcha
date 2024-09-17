<#import "template.ftl" as layout>
<#import "password-commons.ftl" as passwordCommons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>
    <#if section = "header">
        ${msg("updatePasswordTitle")}
    <#elseif section = "form">
     <#if messagesPerField.existsError('password-confirm')>
 <div class="mb-3 alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                               <div class="pf-c-alert__icon">
                                   
                                  <span class="${properties.kcFeedbackErrorIcon!}"></span>
                                
                               </div>
                                   <span class="${properties.kcAlertTitleClass!}">      ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}</span>
                           </div>
                    </#if>
         <#if messagesPerField.existsError('password')>
                           <div class="mb-3 alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                               <div class="pf-c-alert__icon">
                                   
                                  <span class="${properties.kcFeedbackErrorIcon!}"></span>
                                
                               </div>
                                   <span class="${properties.kcAlertTitleClass!}">    ${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                           </div>
                       </#if>
   
        <form id="kc-passwd-update-form" class ="m-login-form-wrapper" action="${url.loginAction}" method="post">

            <div class="${properties.kcFormGroupClass!}">
              
                    <label for="password-new" class="${properties.kcLabelClass!}">${msg("passwordNew")}</label>
              
          
                    <div class="${properties.kcInputGroup!}">
                        <input type="password" id="password-new" name="password-new" class="${properties.kcInputClass!}"
                               autofocus autocomplete="new-password"
                               aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"
                        />
                        <button class="${properties.kcFormPasswordVisibilityButtonClass!}" type="button" aria-label="${msg('showPassword')}"
                                aria-controls="password-new"  data-password-toggle
                                data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}" data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                            <i class="${properties.kcFormPasswordVisibilityIconShow!}" aria-hidden="true"></i>
                        </button>
                    </div>

                   
           
            </div>

            <div class="${properties.kcFormGroupClass!}">
              
                    <label for="password-confirm" class="${properties.kcLabelClass!}">${msg("passwordConfirm")}</label>
               
              
                    <div class="${properties.kcInputGroup!}">
                        <input type="password" id="password-confirm" name="password-confirm"
                               class="${properties.kcInputClass!}"
                               autocomplete="new-password"
                               aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"
                        />
                        <button class="${properties.kcFormPasswordVisibilityButtonClass!}" type="button" aria-label="${msg('showPassword')}"
                                aria-controls="password-confirm"  data-password-toggle
                                data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}" data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                            <i class="${properties.kcFormPasswordVisibilityIconShow!}" aria-hidden="true"></i>
                        </button>
                    </div>

                   

             
            </div>
            <@passwordCommons.logoutOtherSessions/>
            <div class="${properties.kcFormGroupClass!}">
                

                <div id="kc-form-buttons">
                    <#if isAppInitiatedAction??>
                        <input class="${properties.kcButtonClass!}" type="submit" value="${msg("doSubmit")}" />
                        <button class="${properties.kcButtonClass!}" type="submit" name="cancel-aia" value="true" />${msg("doCancel")}</button>
                    <#else>
                        <input class="${properties.kcButtonClass!}" type="submit" value="${msg("doSubmit")}" />
                    </#if>
                </div>
            </div>
        </form>
        <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
    </#if>
</@layout.registrationLayout>
