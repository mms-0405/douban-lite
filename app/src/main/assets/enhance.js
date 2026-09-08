(function () {
    'use strict';

    function hide(element) {
        if (element) {
            element.style.setProperty('display', 'none', 'important');
            element.setAttribute('aria-hidden', 'true');
        }
    }

    function isCaptcha(element) {
        return Boolean(element && element.closest(
            '.geetest_panel, .geetest_box, .geetest_holder, .geetest_widget, ' +
            '.captcha, .captcha-container, [class*="captcha"], [id*="captcha"]'
        ));
    }

    function promotionContainer(element) {
        var fallback = element;
        var node = element;
        for (var depth = 0; node && depth < 7; depth += 1, node = node.parentElement) {
            if (isCaptcha(node)) {
                return null;
            }
            var className = typeof node.className === 'string' ? node.className : '';
            var role = node.getAttribute && node.getAttribute('role');
            var style = window.getComputedStyle(node);
            if (role === 'dialog' || style.position === 'fixed' || style.position === 'sticky' ||
                /(popup|modal|dialog|overlay|promotion|download|open.?app|app.?banner|advert)/i.test(className)) {
                return node;
            }
            var text = (node.textContent || '').replace(/\s+/g, ' ').trim();
            if (text.length <= 100) {
                fallback = node;
            } else {
                break;
            }
        }
        return fallback;
    }

    function cleanPage() {
        document.querySelectorAll(
            'a[href^="douban://"], a[href^="market://"], ' +
            'a[href^="intent://"], a[href*="apps.apple.com"][href*="douban"]'
        ).forEach(hide);

        var removedPromotion = false;
        document.querySelectorAll('a, button, [role="button"], div, span').forEach(function (element) {
            var text = (element.textContent || '').replace(/\s+/g, ' ').trim();
            if (text.length <= 100 &&
                (/^(打开|下载|使用).{0,8}(豆瓣)?\s*(App|客户端)$/i.test(text) ||
                 /(打开豆瓣\s*App|豆瓣App内打开|下载豆瓣|广告|立即打开)/i.test(text))) {
                var container = promotionContainer(element);
                if (container) {
                    hide(container);
                    removedPromotion = true;
                }
            }
        });

        document.querySelectorAll('[role="dialog"], [class*="popup"], [class*="modal"], [class*="overlay"]').forEach(function (element) {
            if (element.closest('.geetest_panel, .geetest_box, .geetest_holder, .captcha, .captcha-container')) {
                return;
            }
            var text = (element.textContent || '').replace(/\s+/g, ' ').trim();
            if (/(打开豆瓣\s*App|豆瓣App内打开|下载豆瓣|广告|立即打开)/i.test(text)) {
                hide(element);
                removedPromotion = true;
            }
        });

        if (removedPromotion) {
            document.documentElement.style.setProperty('overflow-y', 'auto', 'important');
            if (document.body) {
                document.body.style.setProperty('overflow-y', 'auto', 'important');
            }
        }

        document.querySelectorAll('a[target="_blank"]').forEach(function (link) {
            try {
                var host = new URL(link.href, location.href).hostname.toLowerCase();
                if (host === 'douban.com' || host.endsWith('.douban.com')) {
                    link.target = '_self';
                }
            } catch (ignored) {
                // Leave malformed links untouched.
            }
        });
    }

    document.addEventListener('click', function (event) {
        var link = event.target.closest && event.target.closest('a[href]');
        if (!link) {
            return;
        }
        var href = link.getAttribute('href') || '';
        if (/^(douban|market|intent):\/\//i.test(href)) {
            event.preventDefault();
            event.stopPropagation();
        }
    }, true);

    cleanPage();
    if (!window.__doubanLiteObserver) {
        var scheduled = false;
        window.__doubanLiteObserver = new MutationObserver(function () {
            if (!scheduled) {
                scheduled = true;
                requestAnimationFrame(function () {
                    scheduled = false;
                    cleanPage();
                });
            }
        });
        window.__doubanLiteObserver.observe(document.documentElement, {
            childList: true,
            subtree: true
        });
    }
})();
