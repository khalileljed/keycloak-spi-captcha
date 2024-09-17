<#macro logoutOtherSessions>
    <div id="kc-form-options" class="pb-2 d-block">
        <div class="${properties.kcFormOptionsWrapperClass!}">
            <div class="checkbox ps-1">
                <label class="font-size-12 text-primary">
                    <input type="checkbox" id="logout-sessions" name="logout-sessions" value="on" checked>
                    ${msg("logoutOtherSessions")}
                </label>
            </div>
        </div>
    </div>
</#macro>
