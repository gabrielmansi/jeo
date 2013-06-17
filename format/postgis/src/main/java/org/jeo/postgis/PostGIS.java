package org.jeo.postgis;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.Driver;
import org.jeo.util.Key;

public class PostGIS implements Driver<PostGISWorkspace>{

    /**
     * Database key.
     */
    public static final Key<String> DB = new Key<String>("db", String.class);

    /**
     * Host key, defaults to 'localhost'.
     */
    public static final Key<String> HOST = new Key<String>("host", String.class, "localhost");

    /**
     * Port key, defaults to 5432.
     */
    public static final Key<Integer> PORT = new Key<Integer>("port", Integer.class, 5432);

    /**
     * User key, defaults to current user, obtained via <tt>System.getProperty("user.name")</tt>
     */
    public static final Key<String> USER = 
            new Key<String>("user", String.class, System.getProperty("user.name"));

    /**
     * Password key. 
     */
    public static final Key<String> PASSWD = new Key<String>("passwd", String.class);

    public PostGISWorkspace open(PostGISOpts opts) throws IOException {
        return new PostGISWorkspace(opts);
    }

    @Override
    public String getName() {
        return "PostGIS";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pg");
    }

    @Override
    public Class<PostGISWorkspace> getType() {
        return PostGISWorkspace.class;
    }

    @Override
    public List<Key<? extends Object>> getKeys() {
        return (List) Arrays.asList(DB, HOST, PORT, USER, PASSWD);
    }

    @Override
    public boolean canOpen(Map<?, Object> opts) {
        return DB.has(opts);
    }

    @Override
    public PostGISWorkspace open(Map<?, Object> opts) throws IOException {
        PostGISOpts pgopts = new PostGISOpts(DB.get(opts));

        if (HOST.has(opts)) {
            pgopts.host(HOST.get(opts));
        }
        if (PORT.has(opts)) {
            pgopts.port(PORT.get(opts));
        }
        if (USER.has(opts)) {
            pgopts.user(USER.get(opts));
        }
        if (PASSWD.has(opts)) {
            pgopts.passwd(PASSWD.get(opts));
        }

        return new PostGISWorkspace(pgopts);
    }

}
