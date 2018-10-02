
/*
 * The idea is to recive json messages in containing
 * { "msgId" : 1, "sql" : "select * from blar"}   on standard in.
 *
 * Then on standard out send
 * { "msgId" : 1, "rows" : [{},{}]}  back on standard out where the msgId matches the sent message.
 */

public class Main implements SQLRequestListener {

	String host;
	Integer port;
	String dbname;
	String username;
	String password;
	SybaseDB db;
	StdInputReader input;
        boolean allownull;
        boolean autocommit;
        int res;

    public static void main(String[] args) {

		Main m;
		String pw = "";
                boolean allownull = false;
                boolean autocommit = false;
		if (args.length != 7 && args.length != 6 && args.length != 5 && args.length != 4)
		{
			System.err.println("Expecting the arguments: host, port, dbname, username, password, allownull, autocommit");
			System.exit(1);
		}
		if (args.length > 4) {
                    pw = args[4];
                }
                if (args.length > 5) {
                    allownull = Boolean.parseBoolean(args[5]);
                }
                if (args.length > 6){
                    autocommit = Boolean.parseBoolean(args[6]);               
                }
                
		m = new Main(args[0], Integer.parseInt(args[1]), args[2], args[3], pw, allownull, autocommit);
    }

	public Main(String host, Integer port, String dbname, String username, String password, boolean allownull, boolean autocommit) {
		this.host = host;
		this.port = port;
		this.dbname = dbname;
		this.username = username;
		this.password = password;
                this.allownull = allownull;
                this.autocommit = autocommit;
                
		input = new StdInputReader(allownull);
		input.addListener(this);

		MyProperties props = new MyProperties("sybaseConfig.properties");
		db = new SybaseDB(host, port, dbname, username, password, props.properties);
		if (!db.connect(autocommit))
			System.exit(1);

		// send the connected message.
		System.out.println("connected");

		// blocking call don't do anything under here.                
		//input.startReadLoop();
                
                res = input.startReadLoop_();
                System.out.println(res);
                if (res == 0)
                {                    
                    db.disconnect();
                    System.out.println("disconnected");
                }
                
	}

	public void sqlRequest(SQLRequest request)
	{
		db.execSQL(request);
		//System.out.println(result);
	}


}