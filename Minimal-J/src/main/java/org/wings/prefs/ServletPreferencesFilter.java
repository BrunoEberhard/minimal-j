package org.wings.prefs;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class ServletPreferencesFilter
    implements Filter
{
    private Preferences systemRoot;

    static {
        System.setProperty("java.util.prefs.PreferencesFactory", ServletPreferencesFactory.class.getName());
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException
    {
        ServletPreferences.set((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse);
        flusher((HttpServletRequest)servletRequest);
        filterChain.doFilter(servletRequest, servletResponse);
        ServletPreferences.unset();
    }

    private void flusher(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("ServletPreferences") == null) {
            systemRoot = ServletPreferences.getSystemRoot();
            Preferences userRoot = ServletPreferences.getUserRoot();
            session.setAttribute("ServletPreferences", new Flusher(userRoot));
        }
    }

    public void destroy() {
        if (systemRoot != null)
            try {
                systemRoot.flush();
            }
            catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }
    }

    static class Flusher
        implements HttpSessionBindingListener
    {
        Preferences userRoot;

        Flusher(Preferences userRoot) {
            this.userRoot = userRoot;
        }

        public void valueBound(HttpSessionBindingEvent event) {
        }

        public void valueUnbound(HttpSessionBindingEvent event) {
            if (userRoot != null)
                try {
                    userRoot.flush();
                }
                catch (BackingStoreException e) {
                    throw new RuntimeException(e);
                }
        }
    }
}
