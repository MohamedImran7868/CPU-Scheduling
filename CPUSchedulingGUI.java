import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class CPUSchedulingGUI extends JFrame {

    private JComboBox<String> algorithmComboBox;
    private JTextField processesTextField;
    private JTextField quantumTextField;
    private JTable processTable;
    private JTextArea display;
    private int numberOfProcesses;
    private int quantum;
    private List<Integer> arrivalTimes = new ArrayList<>();
    private List<Integer> burstTimes = new ArrayList<>();
    private List<Integer> priorities = new ArrayList<>();
    private List<Integer> finishTimes = new ArrayList<>();
    private List<Integer> turnTimes = new ArrayList<>();
    private List<Integer> waitTimes = new ArrayList<>();
    private List<Integer> burstleft = new ArrayList<>();
    private List<Boolean> used = new ArrayList<>();

    public CPUSchedulingGUI() {
        // Set up the main frame
        setTitle("CPU Scheduling Simulator");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        processesTextField = new JTextField(5);
        JButton startButton = new JButton("Start");

        // Add action listener to the start button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        // Create panel for start menu
        JPanel startMenuPanel = new JPanel();
        startMenuPanel.setLayout(new FlowLayout());

        startMenuPanel.add(new JLabel("Number of Processes:"));
        startMenuPanel.add(processesTextField);
        startMenuPanel.add(startButton, BorderLayout.SOUTH);

        add(startMenuPanel);
        setVisible(true);
    }

    private void startSimulation() {
        algorithmComboBox = new JComboBox<>(new String[]{"Preemptive SJF", "Non Preemptive SJF", "Non Preemptive Priority", "Round Robin"});

        // Get number of processes
        numberOfProcesses = Integer.parseInt(processesTextField.getText());

        // Create a new frame for process details
        JFrame processDetailsFrame = new JFrame("Calculation");
        processDetailsFrame.setLayout(new FlowLayout());
        processDetailsFrame.setSize(800, 550);
        processDetailsFrame.setLocationRelativeTo(null);

        // Create a table model for process details
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Process");
        tableModel.addColumn("Arrival Time");
        tableModel.addColumn("Burst Time");
        tableModel.addColumn("Priority");

        // Populate the table with rows based on the number of processes
        for (int i = 0; i < numberOfProcesses; i++) {
            tableModel.addRow(new Object[]{String.format("P%d", i), "", "", ""});
        }

        processTable = new JTable(tableModel);
        display = new JTextArea();
        quantumTextField = new JTextField(5);

        JPanel tablePanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JPanel displayPanel = new JPanel();

        JScrollPane tablescroll = new JScrollPane(processTable);
        tablescroll.setPreferredSize(new Dimension(400, 200));
        tablescroll.setBorder( new TitledBorder("Process Table") );

        JScrollPane displayscroll = new JScrollPane(display);
        displayscroll.setPreferredSize(new Dimension(700, 700));
        displayscroll.setBorder( new TitledBorder("Output:") );

        JButton calculate = new JButton("Calculate");

        buttonPanel.add(new JLabel("Select Algorithm:"));
        buttonPanel.add(new JLabel("Quatum:"));

        tablePanel.add(tablescroll);
        buttonPanel.add(algorithmComboBox);
        buttonPanel.add(quantumTextField);
        buttonPanel.add(calculate);
        displayPanel.add(displayscroll);

        processDetailsFrame.add(tablePanel, BorderLayout.NORTH);
        processDetailsFrame.add(buttonPanel);
        processDetailsFrame.add(displayPanel, BorderLayout.SOUTH);

        calculate.addActionListener(new CalculateListener());

        processDetailsFrame.setVisible(true);
    }

    // ActionListener class for the Calculate button
    private class CalculateListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            save();

            String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
            display.append(selectedAlgorithm + " calculated:");

            if ("Non Preemptive SJF".equals(selectedAlgorithm)) {
                nonpreemptiveSJF();
            } else if ("Non Preemptive Priority".equals(selectedAlgorithm)) {
                nonpreemptivepriority();
            } else if ("Preemptive SJF".equals(selectedAlgorithm)){
                preemptiveSJF();
            } else if ("Round Robin".equals(selectedAlgorithm)){
                roundrobin();
            }

            display.append("\n------------------------------------------------------------------------------------------------------------------------------\n");
            display.append("|Process\t|Arrival Time\t|Burst Time\t|Finish Time\t|Turn Time\t|Waiting Time\t|\n");
            display.append("------------------------------------------------------------------------------------------------------------------------------\n");

            for (int i = 0; i < numberOfProcesses; i++) {
                display.append("|" + i + "\t" + arrivalTimes.get(i) + "\t" + burstTimes.get(i) + "\t" + finishTimes.get(i) + "\t" + turnTimes.get(i) + "\t" + waitTimes.get(i) + "\t"+ "|\n");
            }

            float avgturn_time = 0;
            float avgwait_time = 0;
            for (int i = 0; i < numberOfProcesses; i++) {
                avgturn_time += turnTimes.get(i);
                avgwait_time += waitTimes.get(i);
            }

            display.append(String.format("Average Turn Around Time: %.2f\n", (avgturn_time/numberOfProcesses)));
            display.append(String.format("Average Wait Time: %.2f\n\n", (avgwait_time/numberOfProcesses)));

            finishTimes.clear();
            waitTimes.clear();
            turnTimes.clear();
            used.clear();

        }
        
    }

    public void nonpreemptiveSJF() {
        int time = 0;
        int done = 0;
    
        while (done != numberOfProcesses) {
            int index = -1;
            int min = 1000;
    
            for (int i = 0; i < arrivalTimes.size(); i++) {
                if (arrivalTimes.get(i) <= time && !used.get(i)) {
                    if (burstTimes.get(i) < min) {
                        min = burstTimes.get(i);
                        index = i;
                    } else if (burstTimes.get(i) == min && arrivalTimes.get(i) < arrivalTimes.get(index)) {
                        min = burstTimes.get(i);
                        index = i;
                    }
                }
            }
    
            if (index != -1) {
                int finish = time + burstTimes.get(index);
                finishTimes.set(index, finish);
                int turn = finishTimes.get(index) - arrivalTimes.get(index);
                turnTimes.set(index, turn);
                int wait = turnTimes.get(index) - burstTimes.get(index);
                waitTimes.set(index, wait);
    
                used.set(index, true);
                done++;
                time = finishTimes.get(index);
            } else {
                time++;
            }
        }
    }

    public void nonpreemptivepriority() {
        int time = 0;
        int done = 0;
    
        while (done != numberOfProcesses) {
            int index = -1;
            int min = 1000;
    
            for (int i = 0; i < numberOfProcesses; i++) {
                if (arrivalTimes.get(i) <= time && !used.get(i)) {
                    if (priorities.get(i) < min) {
                        min = priorities.get(i);
                        index = i;
                    }
                    if (priorities.get(i) == min) {
                        if (arrivalTimes.get(i) < arrivalTimes.get(index)) {
                            min = priorities.get(i);
                            index = i;
                        }
                    }
                }
            }
    
            if (index != -1) {
                int finish = time + burstTimes.get(index);
                finishTimes.set(index, finish);
                int turn = finishTimes.get(index) - arrivalTimes.get(index);
                turnTimes.set(index, turn);
                int wait = turnTimes.get(index) - burstTimes.get(index);
                waitTimes.set(index, wait);
    
                used.set(index, true);
                done++;
                time = finishTimes.get(index);
            } else {
                time++;
            }
        }

    }

    public void preemptiveSJF() {
        int time = 0;
        int done = 0;
    
        while (done != numberOfProcesses) {
            int index = -1;
            int min = 1000;
    
            for (int i = 0; i < numberOfProcesses; i++) {
                if (arrivalTimes.get(i) <= time && !used.get(i)) {
                    if (burstTimes.get(i) < min) {
                        min = burstTimes.get(i);
                        index = i;
                    }
                    if (burstTimes.get(i) == min) {
                        if (arrivalTimes.get(i) < arrivalTimes.get(index)) {
                            min = burstTimes.get(i);
                            index = i;
                        }
                    }
                }
            }
    
            if (index != -1) {
                if (burstleft.get(index) == burstTimes.get(index)) {
                    int temp = time;
                    finishTimes.set(index, temp);
                }
                int b = burstleft.get(index) - 1;
                burstleft.set(index, b);
                time++;

                if (burstleft.get(index) == 0) {
                    finishTimes.set(index, time);
                    int turn = finishTimes.get(index) - arrivalTimes.get(index);
                    turnTimes.set(index, turn);
                    int wait = turnTimes.get(index) - burstTimes.get(index);
                    waitTimes.set(index, wait);

                    used.set(index, true);;
                    done++;
                }
            } else {
                time++;
            }
        }

    }

    public void roundrobin() {
        // Get number of processes
        quantum = Integer.parseInt(quantumTextField.getText());
        display.append("Quantum: " + quantum);

        Queue<Integer> processQueue = new LinkedList<>();
        int current_time = 0;
        int completed = 0;
        int idx;

        while (completed != numberOfProcesses) {
            for (int i = 0; i < numberOfProcesses; i++) {
                if (burstleft.get(i) > 0 && arrivalTimes.get(i) <= current_time && !used.get(i)) {
                    processQueue.add(i);
                    used.set(i, true);
                }
            }

            if (!processQueue.isEmpty()) {
                idx = processQueue.poll();

                if (burstleft.get(idx) > quantum) {
                    burstleft.set(idx, burstleft.get(idx) - quantum);
                    current_time += quantum;
                } else {
                    current_time += burstleft.get(idx);
                    burstleft.set(idx, 0);
                    completed++;

                    finishTimes.set(idx, current_time);
                    turnTimes.set(idx, finishTimes.get(idx) - arrivalTimes.get(idx));
                    waitTimes.set(idx, turnTimes.get(idx) - burstTimes.get(idx));
                }

                for (int i = 0; i < numberOfProcesses; i++) {
                    if (burstleft.get(i) > 0 && arrivalTimes.get(i) <= current_time && !used.get(i)) {
                        processQueue.add(i);
                        used.set(i, true);
                    }
                }

                if (burstleft.get(idx) > 0) {
                    processQueue.add(idx);
                }
            } else {
                current_time++;
            }
        }
    }

    public void save() {
        arrivalTimes.clear();
        burstTimes.clear();
        priorities.clear();

        // Save inputs
        DefaultTableModel model = (DefaultTableModel) processTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                arrivalTimes.add(Integer.parseInt(model.getValueAt(i, 1).toString()));
                burstTimes.add(Integer.parseInt(model.getValueAt(i, 2).toString()));
                priorities.add(Integer.parseInt(model.getValueAt(i, 3).toString()));
                used.add(false);
                finishTimes.add(0);
                turnTimes.add(0);
                waitTimes.add(0);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "There is an invalid input, Please Check!");
                return;
            }
            burstleft.add(burstTimes.get(i));
        }
    }

    public static void main(String[] args) {
        new CPUSchedulingGUI();
    }
}
