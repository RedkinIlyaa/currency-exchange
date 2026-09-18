package listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import util.DataSourceManager;

@WebListener
public class MyAppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DataSourceManager.createHikariCP();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DataSourceManager.closeHikariCP();
    }
}
