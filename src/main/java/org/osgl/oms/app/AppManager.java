package org.osgl.oms.app;

import org.osgl._;
import org.osgl.oms.OMS;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.Iterator;
import java.util.Map;

/**
 * Manage applications deployed on OMS
 */
public class AppManager {

    private Map<Integer, App> byPort = C.newMap();
    private Map<String, App> byContextPath = C.newMap();

    private AppManager() {
        OMS.mode().appScanner().scan(F.loadApp(this));
    }

    public void deploy(App app) {
        load(app);
        refresh(app);
    }

    public void refresh() {
        Iterator<App> itr = appIterator();
        while (itr.hasNext()) {
            refresh(itr.next());
        }
    }

    public void refresh(App app) {
        E.tbd();
    }

    private void load(App app) {
        int port = app.config().port();
        if (port < 0) {
            loadIntoContextMap(app.config().urlContext(), app);
        } else {
            loadIntoPortMap(port, app);
        }
        AppBuilder.build(app);
    }

    private void loadIntoPortMap(int port, App app) {
        App app0 = byPort.get(port);
        if (null != app0) {
            E.invalidConfigurationIf(!app.equals(app0), "Another application has already been deployed using port %s", port);
        } else {
            byPort.put(port, app);
        }
    }

    private void loadIntoContextMap(String context, App app) {
        App app0 = byContextPath.get(context);
        if (null != app0) {
            E.invalidConfigurationIf(!app.equals(app0), "Another application has already been deployed using context %s", context);
        } else {
            byContextPath.put(context, app);
        }
    }

    private Iterator<App> appIterator() {
        final Iterator<App> itrByPort = byPort.values().iterator();
        final Iterator<App> itrByContext = byContextPath.values().iterator();
        return new Iterator<App>() {
            boolean byPortFinished = !itrByPort.hasNext();

            @Override
            public boolean hasNext() {
                if (!byPortFinished) {
                    byPortFinished = !itrByPort.hasNext();
                }
                return !byPortFinished || itrByContext.hasNext();
            }

            @Override
            public App next() {
                return byPortFinished ? itrByContext.next() : itrByPort.next();
            }

            @Override
            public void remove() {
                E.unsupport();
            }
        };
    }

    public static AppManager scan() {
        return new AppManager();
    }

    private enum F {
        ;

        static final _.F1<App, ?> loadApp(final AppManager mgr) {
            return new _.Visitor<App>() {
                @Override
                public void visit(App app) throws _.Break {
                    mgr.load(app);
                }
            };
        }
    }
}