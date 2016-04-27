/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.ssegateway.sse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EventDispatcherFactory {
    
    private static Class<? extends EventDispatcher> runtimeClass;
    
    static {
        try {
            if (isAsyncSupported()) {
                runtimeClass = (Class<? extends EventDispatcher>) Class.forName(EventDispatcherFactory.class.getPackage().getName() + ".AsynchEventDispatcher");
            } else {
                runtimeClass = (Class<? extends EventDispatcher>) Class.forName(EventDispatcherFactory.class.getPackage().getName() +   ".SynchEventDispatcher");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unexpected Exception.", e);
        }
    }
    
    public static EventDispatcher start(HttpServletRequest request, HttpServletResponse response) {
        try {
            EventDispatcher instance = runtimeClass.newInstance();
            instance.start(request, response);
            instance.setDefaultHeaders();
            instance.dispatchEvent("open", null);
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected Exception.", e);
        }
    }

    private static boolean isAsyncSupported() {
        // We can use a system property for test overriding.
        String asyncSupportedProp = System.getProperty("jenkins.eventbus.web.asyncSupported");
        if (asyncSupportedProp != null) {
            return asyncSupportedProp.equals("true");
        }
        
        try {
            HttpServletRequest.class.getMethod("startAsync");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}