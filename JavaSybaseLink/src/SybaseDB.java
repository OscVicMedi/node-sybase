
import java.sql.Connection;
import java.sql.DriverManager;
import com.sybase.jdbc3.jdbc.SybDriver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 *
 * @author rod
 */
public class SybaseDB {

	public static final int TYPE_TIME_STAMP = 93;
	public static final int TYPE_DATE = 91;

	public static final int NUMBER_OF_THREADS = 10;

	String host;
	Integer port;
	String dbname;
	String username;
	String password;
	Properties props;
	Connection conn;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
	ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	public SybaseDB(String host, Integer port, String dbname, String username, String password)
	{
		this(host, port, dbname, username, password, new Properties());
	}
	public SybaseDB(String host, Integer port, String dbname, String username, String password, Properties props)
	{
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.username = username;
		this.password = password;
		this.props = props;
		this.props.put("user", username);
		this.props.put("password", password);		
		df.setTimeZone(TimeZone.getTimeZone("UTC")); 
	}

	public boolean connect(boolean autocommit)
	{
		try {
			SybDriver sybDriver = (SybDriver) Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
			conn = DriverManager.getConnection("jdbc:sybase:Tds:" + host + ":" + port + "?ServiceName=" + dbname, props);                        
                        //jdbc:sybase:Tds:{host}:{port}?ServiceName={dbname}  
                        if (autocommit == false) {                            
                            conn.setAutoCommit(false); //Se agrego esto para permitir autocommit, necesario para el consumo de sp                        
                        }
			return true;

		} catch (ClassNotFoundException ex) {
			System.err.println(ex);
			System.err.println(ex.getMessage());
			return false;
		} catch (IllegalAccessException ex) {
                    System.err.println(ex);
                    System.err.println(ex.getMessage());
                    return false;
            } catch (InstantiationException ex) {
                System.err.println(ex);
                System.err.println(ex.getMessage());
                return false;
            } catch (SQLException ex) {
                System.err.println(ex);
                System.err.println(ex.getMessage());
                return false;
            }
	}
        
        //Funcion para hacer la desconecion correctamente
        public boolean disconnect() {
            try {
                if(!conn.isClosed()) {
                    System.out.println("Disconnecting...");
                    conn.close();
                    return true;
                } else
                {
                    return true;
                }                
            } catch (SQLException ex) {
		System.err.println(ex);
		System.err.println(ex.getMessage());
		return false;
            }
        }

	public void execSQL(SQLRequest request)
	{
		Future f = executor.submit(new ExecSQLCallable(conn, df, request));
		// prints to system.out its self.
	}

}
