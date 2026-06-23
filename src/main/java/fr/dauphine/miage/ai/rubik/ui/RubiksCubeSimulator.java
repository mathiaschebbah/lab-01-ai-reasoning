package fr.dauphine.miage.ai.rubik.ui;

import fr.dauphine.miage.ai.rubik.heuristic.HeuristicCube;
import fr.dauphine.miage.ai.rubik.heuristic.HeuristicLabel;
import fr.dauphine.miage.ai.rubik.heuristic.ZeroHeuristic;
import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.search.Astar;
import fr.dauphine.miage.ai.rubik.search.State;
import fr.dauphine.miage.ai.rubik.util.Scrambler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * Graphical interface that displays the cube and lets the user play with it.
 *
 * <p>This is the {@code RubiksCubeSimulator} class of the statement. It holds a
 * {@link Cube} shown in the interface and a {@code main} method that creates the
 * window. The {@link #actionPerformed(ActionEvent)} method reacts to every
 * button press; in particular the solve button launches the A* search and then
 * animates the returned sequence of moves on the cube.</p>
 */
public final class RubiksCubeSimulator extends JFrame implements ActionListener {

    private final Cube cube = new Cube();
    private final CubePanel cubePanel = new CubePanel(cube);
    private final JTextArea log = new JTextArea(8, 30);
    private final JLabel status = new JLabel("Ready.");
    private final JComboBox<String> heuristicChoice =
            new JComboBox<>(new String[] {"A* HeuristicLabel", "A* HeuristicCube", "UCS (h = 0)"});

    /** Buttons that must be disabled while a search or animation runs. */
    private final JPanel controls = new JPanel();
    private JButton solveButton;
    private JButton scrambleButton;

    public RubiksCubeSimulator() {
        super("Rubik's Cube Solver - A* (M1 MIAGE, AI and Reasoning)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(cubePanel, BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.SOUTH);
        add(buildSidePanel(), BorderLayout.EAST);

        applySelectedHeuristic();
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildControlPanel() {
        controls.setLayout(new GridLayout(2, 1, 6, 6));
        controls.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        // Row of the six clockwise and six counter clockwise moves.
        JPanel moves = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        for (int action = 0; action < 12; action++) {
            JButton button = new JButton(Astar.notationOf(action));
            button.setActionCommand("move:" + action);
            button.addActionListener(this);
            moves.add(button);
        }

        // Row of high level actions.
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        scrambleButton = makeButton("Scramble", "scramble", actions);
        solveButton = makeButton("Solve", "solve", actions);
        makeButton("Reset", "reset", actions);
        actions.add(new JLabel("  Strategy:"));
        actions.add(heuristicChoice);

        controls.add(moves);
        controls.add(actions);
        return controls;
    }

    private JButton makeButton(String label, String command, JPanel parent) {
        JButton button = new JButton(label);
        button.setActionCommand(command);
        button.addActionListener(this);
        parent.add(button);
        return button;
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel(new BorderLayout(6, 6));
        side.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        JLabel title = new JLabel("Solver log");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        side.add(title, BorderLayout.NORTH);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        side.add(new JScrollPane(log), BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        status.setForeground(new Color(0x33, 0x66, 0x99));
        side.add(status, BorderLayout.SOUTH);
        return side;
    }

    /**
     * Reacts to every button of the interface. Single moves are applied at once;
     * scramble shuffles the cube; reset restores the solved cube; solve runs the
     * A* search and animates the resulting plan.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.startsWith("move:")) {
            int action = Integer.parseInt(command.substring("move:".length()));
            cube.applyAction(action);
            cubePanel.setCube(cube);
            status.setText("Applied " + Astar.notationOf(action)
                    + (cube.isSolved() ? " - solved!" : ""));
        } else if ("scramble".equals(command)) {
            scramble();
        } else if ("reset".equals(command)) {
            reset();
        } else if ("solve".equals(command)) {
            solve();
        }
    }

    private void scramble() {
        Scrambler scrambler = new Scrambler();
        List<Integer> applied = scrambler.scramble(cube, 12);
        cubePanel.setCube(cube);
        StringBuilder sb = new StringBuilder();
        for (int action : applied) {
            sb.append(Astar.notationOf(action)).append(' ');
        }
        log.setText("Scrambled with 12 moves:\n" + sb.toString().trim() + "\n");
        status.setText("Scrambled. Press Solve.");
    }

    private void reset() {
        Cube solved = new Cube();
        copyInto(solved);
        cubePanel.setCube(cube);
        log.setText("");
        status.setText("Reset to solved cube.");
    }

    /**
     * Launches the A* search in a background worker so the interface stays
     * responsive, then animates the returned plan one move at a time.
     */
    private void solve() {
        if (cube.isSolved()) {
            status.setText("Already solved.");
            return;
        }
        applySelectedHeuristic();
        setBusy(true);
        status.setText("Solving with " + heuristicChoice.getSelectedItem() + " ...");

        final Cube snapshot = cube.copy();
        SwingWorker<SolveResult, Void> worker = new SwingWorker<>() {
            @Override
            protected SolveResult doInBackground() {
                Astar astar = new Astar(snapshot);
                long start = System.nanoTime();
                List<String> plan = astar.solve();
                long elapsed = System.nanoTime() - start;
                return new SolveResult(plan, astar.getExpandedCount(),
                        astar.getGeneratedCount(), elapsed);
            }

            @Override
            protected void done() {
                try {
                    SolveResult result = get();
                    handleSolveResult(result);
                } catch (Exception e) {
                    status.setText("Search failed: " + e.getMessage());
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private void handleSolveResult(SolveResult result) {
        if (result.plan == null) {
            log.append("No solution found within the search limit.\n");
            status.setText("Search gave up (too scrambled).");
            setBusy(false);
            return;
        }
        log.append("Solution found in " + result.plan.size() + " moves:\n");
        log.append(String.join(" ", result.plan) + "\n");
        log.append(String.format("expanded = %d, generated = %d, time = %.0f ms%n",
                result.expanded, result.generated, result.nanos / 1_000_000.0));
        status.setText("Animating solution (" + result.plan.size() + " moves)...");
        animate(result.plan);
    }

    /** Plays the plan on the cube, one move every 350 ms, via a Swing timer. */
    private void animate(List<String> plan) {
        final int[] step = {0};
        Timer timer = new Timer(350, null);
        timer.addActionListener(e -> {
            if (step[0] >= plan.size()) {
                timer.stop();
                setBusy(false);
                status.setText(cube.isSolved() ? "Solved!" : "Animation done.");
                return;
            }
            cube.applyAction(actionOf(plan.get(step[0])));
            cubePanel.setCube(cube);
            step[0]++;
        });
        timer.setInitialDelay(150);
        timer.start();
    }

    private void applySelectedHeuristic() {
        String choice = (String) heuristicChoice.getSelectedItem();
        if (choice != null && choice.contains("Label")) {
            State.heuristic = new HeuristicLabel();
        } else if (choice != null && choice.contains("Cube")) {
            State.heuristic = new HeuristicCube();
        } else {
            State.heuristic = new ZeroHeuristic();
        }
    }

    private void setBusy(boolean busy) {
        solveButton.setEnabled(!busy);
        scrambleButton.setEnabled(!busy);
        heuristicChoice.setEnabled(!busy);
    }

    /** Copies the stickers of the given cube into the displayed cube. */
    private void copyInto(Cube source) {
        for (var face : fr.dauphine.miage.ai.rubik.model.Face.values()) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    cube.set(face, row, col, source.get(face, row, col));
                }
            }
        }
    }

    private static int actionOf(String notation) {
        for (int a = 0; a < 12; a++) {
            if (Astar.notationOf(a).equals(notation)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown notation: " + notation);
    }

    /** Immutable result of one solve call, passed back from the worker. */
    private record SolveResult(List<String> plan, long expanded, long generated, long nanos) {
    }

    /**
     * Application entry point: creates and shows the window on the Swing thread.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RubiksCubeSimulator().setVisible(true));
    }
}
