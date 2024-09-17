<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayMessage=!messagesPerField.existsError('username'); section>
    <#if section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">
        <form id="kc-reset-password-form" class ="m-login-form-wrapper" action="${url.loginAction}" method="post">
     <#if messagesPerField.existsError('username')>
                           <div class="mb-3 alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                               <div class="pf-c-alert__icon">
                                   
                                  <span class="${properties.kcFeedbackErrorIcon!}"></span>
                                
                               </div>
                                   <span class="${properties.kcAlertTitleClass!}">   ${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
                           </div>
                       </#if>
             <div class="${properties.kcFormGroupClass!}">
          
                        <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>
                    <input type="text" id="username" name="username" class="form-control" autofocus value="${(auth.attemptedUsername!'')}" aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>

            
            </div>
            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span><a href="${url.loginUrl}" class="kc-form-options">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>

                <div id="kc-form-buttons">
                    <input class="${properties.kcButtonClass!}" type="submit" value="${msg("doSubmit")}"/>
                </div>
            </div>

        </form>
    <#elseif section = "info" >
        <#if realm.duplicateEmailsAllowed>
            ${msg("emailInstructionUsername")}
        <#else>
            ${msg("emailInstruction")}
        </#if>
    </#if>
</@layout.registrationLayout>
