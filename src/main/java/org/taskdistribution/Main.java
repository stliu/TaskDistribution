package org.taskdistribution;

import lombok.extern.slf4j.Slf4j;
import org.jgroups.util.UUID;
import org.jgroups.util.Util;

import java.util.Date;

/**
 * @author stliu at apache.org
 * @since 6/17/16
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        String props = "udp.xml";
        String name = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-props")) {
                props = args[++i];
                continue;
            }
            if (args[i].equals("-name")) {
                name = args[++i];
                continue;
            }
            help();
            return;
        }
        Server server = new Server(props);
        server.start(name);

        loop(server);
        server.stop();

    }
    private static void loop(Server server) {
        boolean looping = true;
        while (looping) {
            int key = Util.keyPress("[1] Submit [2] Submit long running task [3] Info [q] Quit");
            switch (key) {
                case '1':
                    Task task = new Task() {
                        private static final long serialVersionUID = 5102426397394071700L;
                        private final String id = UUID.randomUUID().toString();

                        public Object execute() {
                            return new Date();
                        }
                        @Override
                        public String toString() {
                            return "Task["+id+"]";
                        }
                    };
                    _submit(task, server);
                    break;
                case '2':
                    task = new Task() {
                        private static final long serialVersionUID = 5102426397394071700L;
                        private final String id = UUID.randomUUID().toString();
                        public Object execute() {
                            System.out.println("sleeping for 15 secs...");
                            Util.sleep(15000);
                            System.out.println("done");
                            return new Date();
                        }

                        @Override
                        public String toString() {
                            return "Task["+id+"]";
                        }
                    };
                    _submit(task, server);
                    break;
                case '3':
                    System.out.println(server.info());
                    break;
                case 'q':
                    looping = false;
                    break;
                case 'r':
                    break;
                case '\n':
                    break;
                case -1:
                    looping = false;
                    break;
            }
        }
    }
    private static void _submit(Task task, Server masterNode) {
        try {
           masterNode.submit(task, 30000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void help() {
        System.out.println("Server [-props <XML config file>] [-name <name>]");
    }

}
