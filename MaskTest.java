import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Simple test for mask deliver program.
 * <p>
 * Compile command(jdk1.8 is suggested):
 * <pre><code> javac MaskTest.java </code></pre>
 * Run command:
 * <pre><code> java MaskTest &lt;cmd for your program&gt; </code></pre>
 * For example:
 * <pre><code> java MaskTest java Main </code></pre>
 * <pre><code> java MaskTest Main.exe(or ./Main.out for linux) </code></pre>
 * <pre><code> java MaskTest python Main.py </code></pre>
 */
public class MaskTest {
    private static final Logger LOGGER = Logger.getLogger(MaskTest.class.toString());
    private final Charset CHARSET = StandardCharsets.UTF_8;
    private final int MAP_SIZE = 12;
    private final int COURIER_CAPACITY = 100;
    private final int MAX_MOVE_AMOUNT = 500;
    /**
     * 0: blank
     * negative: demand
     * positive: store or donate
     */
    private final int[][] maskMap = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };
	
	
	
    private final Courier courier = new Courier(11, 11) {{
		
        // courier start from store
		int ranAmount = 0;
		int max=11,min=0;
		int maxNum = 200,minNum = 50;
		for(int i = 0; i < 5; i++){
			int ran1 = (int) (Math.random()*(max-min)+min);
			int ran2 = (int) (Math.random()*(max-min)+min);
			ranAmount = (int) (Math.random()*(maxNum-minNum)+minNum);
			if(maskMap[ran1][ran2] < 0){
				ran1 = (int) (Math.random()*(max-min)+min);
				ran2 = (int) (Math.random()*(max-min)+min);
				maskMap[ran1][ran2] = (-1)*ranAmount;
				if(maskMap[ran1][ran2] < 0){
					ran1 = (int) (Math.random()*(max-min)+min);
					ran2 = (int) (Math.random()*(max-min)+min);
					maskMap[ran1][ran2] = (-1)*ranAmount;
				}
			}else{
				maskMap[ran1][ran2] = (-1)*ranAmount;
			}
		}
		
        assert maskMap.length == MAP_SIZE && Arrays.stream(maskMap).allMatch(row -> row.length == MAP_SIZE)
                : "invalid Map size";
        assert validPos(x, y) && maskMap[x][y] == 0 : "invalid courier position";
        maskMap[x][y] = Integer.MAX_VALUE;
    }};
    private int nextDonate = -1;
    private final Queue<Donate> DonateQ = new LinkedList<Donate>() {{
		
		int maxId=6,minId=1;
		int max = 11, min = 0;
		int maxNum = 100,minNum = 0;
		int ranIdLen= (int) (Math.random()*(maxId-minId)+minId); 
		int ranAmount = 0;
		int keyTime = 2;
		for(int i = 0; i < ranIdLen; i++){
			int ran1 = (int) (Math.random()*(max-min)+min);
			int ran2 = (int) (Math.random()*(max-min)+min);
			if( maskMap[ran1][ran2] != 0 ){
				ran1 = (int) (Math.random()*(max-min)+min);
				ran2 = (int) (Math.random()*(max-min)+min);
				if( maskMap[ran1][ran2] != 0 ){
					ran1 = (int) (Math.random()*(max-min)+min);
					ran2 = (int) (Math.random()*(max-min)+min);
				}
			}
			ranAmount = (int) (Math.random()*(maxNum-minNum)+minNum);
			add(new Donate(ran1, ran2, ranAmount, keyTime));
			keyTime += 2;
		}
			
		
        //add(new Donate(11, 9, 100, 2));
        //add(new Donate(11, 7, 100, 4));
        //add(new Donate(11, 5, 100, 6));
        //add(new Donate(11, 3, 100, 8));

        assert stream().allMatch(
                donate ->
                        validPos(donate.x, donate.y) && maskMap[donate.x][donate.y] == 0
                                && donate.amount > 0 && donate.appearTime > 0
        ) : "invalid donate queue";
        nextDonate = peek().appearTime;
    }};

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.warning("please add your command");
            return;
        }
        MaskTest maskTest = new MaskTest();
        maskTest.runTest(args);
    }

    private boolean validPos(int x, int y) {
        return 0 <= x && x < MAP_SIZE && 0 <= y && y < MAP_SIZE;
    }

    private List<String> interact(final String input) throws MaskException {
        List<String> output;
        // first interact
        if (input == null) {
            output = new ArrayList<>();
            output.add(String.format(Locale.ROOT, "S %d %d", courier.x, courier.y));
            for (int x = 0; x < MAP_SIZE; x++) {
                for (int y = 0; y < MAP_SIZE; y++) {
                    if (maskMap[x][y] < 0) {
                        output.add(String.format(Locale.ROOT, "R %d %d %d", x, y, maskMap[x][y]));
                    }
                }
            }
            output.add("G");
        } else {
            try {
                output = courier.move(Direction.valueOf(input));
            } catch (IllegalArgumentException e) {
                throw new MaskException("bad input(should be E|W|S|N)");
            }
        }
        return output;
    }

    private void runTest(String[] cmd) {
        long startTime = System.currentTimeMillis();
        Process process;
        try {
            process = new ProcessBuilder()
                    .command(cmd)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("fail to run command");
            return;
        }

        boolean success = false;
        try (
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), CHARSET);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream(), CHARSET);
                BufferedReader br = new BufferedReader(inputStreamReader);
                BufferedWriter bw = new BufferedWriter(outputStreamWriter)
        ) {
            String input = null;
            List<String> output;
            do {
                if (input != null) {
                    LOGGER.info("<<" + input);
                }
                try {
                    // input is null at first time
                    if ((output = interact(input)).isEmpty()) {
                        LOGGER.info("delivery finished");
                        success = true;
                        break;
                    }
                } catch (MaskException e) {
                    LOGGER.warning(e.getMessage());
                    process.destroyForcibly();
                    return;
                }
                for (String outputLine : output) {
                    LOGGER.info(">>" + outputLine);
                    bw.write(outputLine);
                    bw.newLine();
                }
                try {
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.warning("pipe broken, may because your program exited without finish delivery or crashed");
                    break;
                }
            } while ((input = br.readLine()) != null);

            LOGGER.info("MaskTest is waiting for a graceful exit of your program...");
            try {
                process.waitFor();
            } catch (InterruptedException ignore) {
            }

            if (process.exitValue() != 0) {
                LOGGER.warning("your program crashed with exit code: " + process.exitValue());
                success = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("unexpected exception");
            success = false;
        }
        LOGGER.info(String.format(
                Locale.ROOT,
                "test %s in %d ms",
                success ? "success" : "failed", System.currentTimeMillis() - startTime)
        );
    }

    private enum Direction {
        // east
        E(0, 1),
        // west
        W(0, -1),
        // south
        S(1, 0),
        // north
        N(-1, 0);

        int dx;
        int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    private class Position {
        public int x;
        public int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private class Donate extends Position {
        public int amount;
        public int appearTime;

        public Donate(int x, int y, int amount, int appearTime) {
            super(x, y);
            this.amount = amount;
            this.appearTime = appearTime;
        }
    }

    private class Courier extends Position {
        public int carryAmount = COURIER_CAPACITY;
        private int moveCnt = 0;

        public Courier(int x, int y) {
            super(x, y);
        }

        public List<String> move(Direction direction) throws MaskException {
            if (moveCnt++ > MAX_MOVE_AMOUNT) {
                throw new MaskException("too many moves");
            }
            List<String> output = new ArrayList<>();
            if (moveCnt == nextDonate) {
                Donate donate = DonateQ.poll();
                maskMap[donate.x][donate.y] = donate.amount;
                output.add(String.format(Locale.ROOT, "R %d %d %d", donate.x, donate.y, donate.amount));
                nextDonate = DonateQ.isEmpty() ? -1 : DonateQ.peek().appearTime;
            }
            x += direction.dx;
            y += direction.dy;
            if (!validPos(x, y)) {
                throw new MaskException("move out from map");
            }
            int maskAmount = maskMap[x][y];
            if (maskAmount < 0) {
                int deliverAmount = Math.min(-maskAmount, carryAmount);
                carryAmount -= deliverAmount;
                maskMap[x][y] += deliverAmount;
                if (maskMap[x][y] == 0) {
                    if (Arrays.stream(maskMap).allMatch(row -> Arrays.stream(row).allMatch(pos -> pos >= 0))) {
                        // finished(clear donation output)
                        output.clear();
                        return output;
                    }
                }
            } else {
                int deliverAmount = Math.min(maskAmount, COURIER_CAPACITY - carryAmount);
                maskMap[x][y] -= deliverAmount;
                carryAmount += deliverAmount;
            }
            output.add("G");
            return output;
        }
    }

    private class MaskException extends Exception {
        public MaskException(String msg) {
            super(msg);
        }
    }
}
