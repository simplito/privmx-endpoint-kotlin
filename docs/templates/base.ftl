<#import "includes/page_metadata.ftl" as page_metadata>
<#import "includes/header.ftl" as header>
<#import "includes/footer.ftl" as footer>
<!DOCTYPE html>
<html class="no-js">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1" charset="UTF-8">
    <@page_metadata.display/>
    <@template_cmd name="pathToRoot">
        <script>var pathToRoot = "${pathToRoot}";</script></@template_cmd>
    <script>document.documentElement.classList.replace("no-js", "js");</script>
    <#-- This script doesn't need to be there but it is nice to have
    since app in dark mode doesn't 'blink' (class is added before it is rendered) -->
    <script>
        window.addEventListener('load', function () {
            let searchBtn = document.getElementById("pages-search")
            let searchBtnSvg = searchBtn.getElementsByTagName("svg").item(0)
            searchBtnSvg.setAttribute("fill", "currentColor")
            let searchBtnPath = searchBtnSvg.getElementsByTagName("path").item(0)
            searchBtnPath.removeAttribute("fill")
        });
    </script>
    <script>const themeStorage = localStorage.getItem("theme")
        if (themeStorage == null) {
            const osDarkSchemePreferred = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
            if (osDarkSchemePreferred === true) {
                document.getElementsByTagName("html")[0].classList.add("theme-dark")
            }
        } else {
            if (themeStorage === "dark") {
                document.getElementsByTagName("html")[0].classList.add("theme-dark")
            }
        }
    </script>
    <#-- Resources (scripts, stylesheets) are handled by Dokka.
    Use customStyleSheets and customAssets to change them. -->
    <@resources/>
</head>
<body>
<div class="root">
    <@header.display/>
    <div id="container">
        <div class="sidebar" id="leftColumn">
            <div class="dropdown theme-dark_mobile" data-role="dropdown" id="toc-dropdown">
                <a role="button" class="custom-back-button"
                   href="https://docs.privmx.dev">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                         stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="m15 18-6-6 6-6"></path>
                    </svg>
                    <span>Back to Start</span>
                </a>
                <ul role="listbox" id="toc-listbox" class="dropdown--list dropdown--list_toc-list"
                    data-role="dropdown-listbox">
                    <div class="dropdown--header">
                            <span>
                                <@template_cmd name="projectName">
                                    ${projectName}
                                </@template_cmd>
                            </span>
                        <button class="button" data-role="dropdown-toggle" aria-label="Close table of contents">
                            <i class="ui-kit-icon ui-kit-icon_cross"></i>
                        </button class="button">
                    </div>
                    <div class="sidebar--inner" id="sideMenu"></div>
                </ul>
                <div class="dropdown--overlay"></div>
            </div>
        </div>
        <div id="main">
            <@content/>
            <@footer.display/>
        </div>
    </div>
</div>
</body>
</html>