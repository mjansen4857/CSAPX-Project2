package place.client.ptui;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A class to do console-based user interaction in a manner similar to
 * how JavaFX does window-based interaction.
 * This class is to be inherited by any console application.
 * When {@link #launch(Class,String[])} is called with the subclass object, an
 * instance of the given class is created and the argument array is saved.
 * (There must be a default constructor.)
 * The subclass's {@link #go(Scanner, PrintWriter) start} method
 * is called in a separate thread, causing
 * {@link #go(Scanner, PrintWriter) start} to return immediately.
 * This method is supplied with a Scanner and Writer connected to
 * a console (terminal).
 * The subclass may optionally override {@link #init() init} and
 * {@link #stop() stop} to do additional setup and teardown.
 * {@link #go(Scanner, PrintWriter)} (Scanner, PrintWriter) start} runs
 * after {@link #init() init}. After the
 * {@link #go(Scanner, PrintWriter)} (Scanner, PrintWriter) start} method
 * returns, {@link #stop() stop} is executed.
 *
 * @author James Heliotis
 */
public abstract class ConsoleApplication {

    private String[] cmdLineArgs;

    private Thread eventThread;

    /**
     * Run a console application.
     * <ol>
     * <li>An instance of a ConsoleApplication subclass is created.</li>
     * <li>The class's default constructor is run.</li>
     * <li>The subclass's {@link #init() init} method is run.</li>
     * <li>A {@link Scanner Scanner} and
     * {@link java.io.Writer Writer} are created and connected to a
     * text console (currently stdin and stdout).</li>
     * <li>A new thread is spawned. It calls the subclass's
     * {@link #go(Scanner, PrintWriter)} method
     * with the {@link Scanner} and
     * {@link java.io.Writer} as arguments.</li>
     * <li>(At this point the {@code launch} method returns.)</li>
     * <li>When the {@link #go(Scanner, PrintWriter)} method
     * returns the subclass's {@link #stop() stop} method is run.</li>
     * </ol>
     *
     * @param ptuiClass the class object that refers to the class to
     *             be instantiated
     */
    public static void launch(
            Class< ? extends ConsoleApplication > ptuiClass
    ) {
        launch( ptuiClass, new String[ 0 ] );
    }

    /**
     * Run a console application, with command line arguments.
     * <ol>
     * <li>An instance of a ConsoleApplication subclass is created.</li>
     * <li>The passed in string arguments are copied and saved.</li>
     * <li>The class's default constructor is run.</li>
     * <li>The subclass's {@link #init() init} method is run.</li>
     * <li>A {@link Scanner Scanner} and
     * {@link java.io.Writer Writer} are created and connected to a
     * text console (currently stdin and stdout).</li>
     * <li>A new thread is spawned. It calls the subclass's
     * {@link #go(Scanner, PrintWriter)} method
     * with the {@link Scanner} and
     * {@link java.io.Writer} as arguments.</li>
     * <li>(At this point the {@code launch} method returns.)</li>
     * <li>When the {@link #go(Scanner, PrintWriter)} method
     * returns the subclass's {@link #stop() stop} method is run.</li>
     * </ol>
     *
     * @param ptuiClass the class object that refers to the class to
     *             be instantiated
     * @param args the array of strings from the command line
     */
    public static void launch(
            Class< ? extends ConsoleApplication > ptuiClass,
            String[] args
    ) {
        try {
            ConsoleApplication ptuiApp = ptuiClass.newInstance();
            ptuiApp.cmdLineArgs = Arrays.copyOf( args, args.length );

            try {
                ptuiApp.init();
                ptuiApp.eventThread = new Thread( new Runner( ptuiApp ) );
                ptuiApp.eventThread.start();
                ptuiApp.eventThread.join();
            }
            catch( InterruptedException ie ) {
                System.err.println( "Console event thread interrupted" );
            }
            finally {
                ptuiApp.stop();
            }
        }
        catch( InstantiationException ie ) {
            System.err.println( "Can't instantiate Console App:" );
            System.err.println( ie.getMessage() );
        }
        catch( IllegalAccessException iae ) {
            System.err.println( iae.getMessage() );
        }
    }

    private static class Runner implements Runnable {
        private final ConsoleApplication ptuiApp;

        public Runner( ConsoleApplication ptuiApp ) { this.ptuiApp = ptuiApp; }

        public void run() {
            // We don't put the PrintWriter in try-with-resources because
            // we don't want it to be closed. The Scanner can close.
            PrintWriter out = null;
            try ( Scanner consoleIn = new Scanner( System.in ) ) {
                do {
                    try {
                        out = new PrintWriter(
                                new OutputStreamWriter( System.out ), true );
                        ptuiApp.go( consoleIn, out );
                        out = null;
                    }
                    catch( Exception e ) {
                        e.printStackTrace();
                        if ( out != null ) {
                            out.println( "\nRESTARTING...\n" );
                        }
                    }
                } while ( out != null );
            }
        }
    }

    /**
     * Fetch the application's command line arguments
     * @return the string array that was passed to launch, if any, or else
     *         an empty array
     */
    public List< String > getArguments() {
        return Arrays.asList( this.cmdLineArgs );
    }

    /**
     * A do-nothing setup method that can be overwritten by subclasses
     * when necessary
     */
    public void init() {}

    /**
     * The method that is expected to run the main loop of the console
     * application, prompting the user for text input and displaying
     * text output. It is named differently than
     * {@link javafx.application.Application#start(javafx.stage.Stage)}
     * to emphasize that this method can keep executing (looping,
     * probably) as long as the application is running.
     *
     * @param consoleIn  the source of the user input
     * @param consoleOut the destination where text output should be printed
     */
    public abstract void go( Scanner consoleIn, PrintWriter consoleOut );

    /**
     * A do-nothing teardown method that can be overwritten by subclasses
     * when necessary.
     */
    public void stop() {}

}
