import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class CPU_scheduling_algorithms extends JFrame {

    private JComboBox<String> algorithmComboBox;
    private JTextField processesTextField;
    private JTextField quantumTextField;
    private JTable processTable;
    private JTextArea display;
    private int numberOfProcesses;
    private int quantum;
    private List<Integer> chart = new ArrayList<>();
    private List<Integer> arrivalTimes = new ArrayList<>();
    private List<Integer> burstTimes = new ArrayList<>();
    private List<Integer> priorities = new ArrayList<>();
    private List<Integer> finishTimes = new ArrayList<>();
    private List<Integer> turnTimes = new ArrayList<>();
    private List<Integer> waitTimes = new ArrayList<>();
    private List<Integer> burstleft = new ArrayList<>();
    private List<Boolean> used = new ArrayList<>();

    public CPU_scheduling_algorithms() {
        // Set up the main frame
        setTitle("CPU Scheduling Algorithm Simulator");
        setSize(400, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);



        // Create components
        processesTextField = new JTextField(5);
        JButton startButton = new JButton("Start");
        startButton.setBackground(new Color (255, 102, 102));

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
        startMenuPanel.setBackground(new Color(0,255,255));

        add(startMenuPanel);
        setVisible(true);

    }

    private void startSimulation() {
        algorithmComboBox = new JComboBox<>(new String[]{"Preemptive SJF", "Non Preemptive SJF", "Non Preemptive Priority", "Round Robin"});

        // Validate number of processes
        try {
            numberOfProcesses = Integer.parseInt(processesTextField.getText());
            if (numberOfProcesses <= 0) {
                JOptionPane.showMessageDialog(null, "Number of processes should be greater than 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid input for number of processes. Please enter a valid number.");
            return;
        }

        // Create a new frame for process details
        JFrame processDetailsFrame = new JFrame("Calculation");
        processDetailsFrame.setLayout(new FlowLayout());
        processDetailsFrame.setSize(700, 800);
        processDetailsFrame.setLocationRelativeTo(null);
        
        // Create a table model for process details
        DefaultTableModel tableModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
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

        JLabel warning = new JLabel("NOTE: Please press ENTER after filling up the table!!!");

        JScrollPane tablescroll = new JScrollPane(processTable);
        tablescroll.setPreferredSize(new Dimension(400, 200));
        tablescroll.setBorder( new TitledBorder("Process Table") );
        tablescroll.setBackground(new Color (0,255,255));

        JScrollPane displayscroll = new JScrollPane(display);
        displayscroll.setPreferredSize(new Dimension(600, 475));
        displayscroll.setBorder( new TitledBorder("Output:") );
        displayscroll.setBackground(new Color (0,255,255));

        JButton calculate = new JButton("Calculate");
        calculate.setBackground(new Color (255, 102, 102));

        buttonPanel.add(new JLabel("Select Algorithm:"));
        buttonPanel.add(algorithmComboBox);

        buttonPanel.add(new JLabel("Quatum:"));
        buttonPanel.add(quantumTextField);

        tablePanel.add(tablescroll);
        buttonPanel.add(calculate);
        displayPanel.add(displayscroll);

        processDetailsFrame.add(tablePanel, BorderLayout.NORTH);
        processDetailsFrame.add(warning);
        processDetailsFrame.add(buttonPanel, BorderLayout.CENTER);
        processDetailsFrame.add(displayPanel, BorderLayout.SOUTH);
        

        calculate.addActionListener(new CalculateListener());

        processDetailsFrame.setResizable(false);
        processDetailsFrame.setVisible(true);

    }

    // ActionListener class for the Calculate button
    private class CalculateListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            save();

            String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();

            if ("Non Preemptive SJF".equals(selectedAlgorithm)) {
                nonpreemptiveSJF();
            } else if ("Non Preemptive Priority".equals(selectedAlgorithm)) {
                nonpreemptivepriority();
            } else if ("Preemptive SJF".equals(selectedAlgorithm)){
                preemptiveSJF();
            } else if ("Round Robin".equals(selectedAlgorithm)){
                try { // Validate quantum
                    quantum = Integer.parseInt(quantumTextField.getText());
                    if (quantum <= 0) {
                        JOptionPane.showMessageDialog(null, "Quantum should be greater than 0.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input for quantum. Please enter a valid number.");
                    return;
                }
                roundrobin();
            }
            print(selectedAlgorithm);

            finishTimes.clear();
            waitTimes.clear();
            turnTimes.clear();
            burstleft.clear();
            used.clear();
            chart.clear();
        }
    }

    public void print(String selectedAlgorithm) {
        display.append(selectedAlgorithm + " calculated:");
        if ("Round Robin".equals(selectedAlgorithm)) {
            display.append("\nQuantum: " + quantum);
        }
        display.append("\n\nGantt Chart:\n");

        for (Integer i : chart) {
            display.append("-----------");
        }
        display.append("\n");

        for (Integer i : chart) {
            display.append(String.format("|    P%d    ", i));
        }
        display.append("|\n");

        for (Integer i : chart) {
            display.append("-----------");
        }

        display.append("\nTable: \n");
        display.append("------------------------------------------------------------------------------------------------------------------------------\n");
        display.append("| Process\t| Arrival Time\t| Burst Time\t| Finish Time\t| Turn Time\t| Waiting Time\t|\n");
        display.append("------------------------------------------------------------------------------------------------------------------------------\n");

        for (int i = 0; i < numberOfProcesses; i++) {
            display.append("| " + i + "\t| " + arrivalTimes.get(i) + "\t| " + burstTimes.get(i) + "\t| " + finishTimes.get(i) + "\t| " + turnTimes.get(i) + "\t| " + waitTimes.get(i) + "\t"+ "|\n");
            display.append("------------------------------------------------------------------------------------------------------------------------------\n");

        }

        float avgturn_time = 0;
        float avgwait_time = 0;
        for (int i = 0; i < numberOfProcesses; i++) {
            avgturn_time += turnTimes.get(i);
            avgwait_time += waitTimes.get(i);
        }

        display.append("Total Turn Around time: " + avgturn_time);
        display.append(String.format("\nAverage Turn Around Time: %.2f\n", (avgturn_time/numberOfProcesses)));
        display.append("Total Waiting time: " + avgwait_time);
        display.append(String.format("\nAverage Waiting Time: %.2f\n\n", (avgwait_time/numberOfProcesses)));
    }

    public void nonpreemptiveSJF() {
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
                chart.add(index);
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
                chart.add(index);
            } else {
                time++;
            }
        }

    }

    public void preemptiveSJF() {
        int time = 0;
        int done = 0;
        int previndex = -1;
        int start = 0;
    
        while (done != numberOfProcesses) {
            int index = -1;
            int min = 1000;
    
            if (start == 0) {
                for (int i = 0; i < numberOfProcesses; i++) {
                    if (arrivalTimes.get(i) <= time && !used.get(i)) {
                        if (burstTimes.get(i) < min) {
                            min = burstTimes.get(i);
                            index = i;
                        }
                    }
                }
                start++;
            } else {
                for (int i = 0; i < numberOfProcesses; i++) {
                    if (arrivalTimes.get(i) <= time && !used.get(i)) {
                        if (burstTimes.get(i) < min) {
                            min = burstleft.get(i);
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

                if (previndex != index) {
                    chart.add(index);
                    previndex = index;
                }

                if (burstleft.get(index) == 0) {
                    finishTimes.set(index, time);
                    int turn = finishTimes.get(index) - arrivalTimes.get(index);
                    turnTimes.set(index, turn);
                    int wait = turnTimes.get(index) - burstTimes.get(index);
                    waitTimes.set(index, wait);

                    used.set(index, true);
                    done++;
                }
            } else {
                time++;
            }
        }

    }

    public void roundrobin() {

        Queue<Integer> processQueue = new LinkedList<>();
        int current_time = 0;
        int completed = 0;
        int index;

        while (completed != numberOfProcesses) {
            for (int i = 0; i < numberOfProcesses; i++) {
                if (burstleft.get(i) > 0 && arrivalTimes.get(i) <= current_time && !used.get(i)) {
                    processQueue.add(i);
                    used.set(i, true);
                }
            }

            if (!processQueue.isEmpty()) {
                index = processQueue.poll();

                if (burstleft.get(index) > quantum) {
                    burstleft.set(index, burstleft.get(index) - quantum);
                    current_time += quantum;
                    chart.add(index);

                } else {
                    current_time += burstleft.get(index);
                    burstleft.set(index, 0);
                    completed++;

                    finishTimes.set(index, current_time);
                    turnTimes.set(index, finishTimes.get(index) - arrivalTimes.get(index));
                    waitTimes.set(index, turnTimes.get(index) - burstTimes.get(index));
                    chart.add(index);
                }

                for (int i = 0; i < numberOfProcesses; i++) {
                    if (burstleft.get(i) > 0 && arrivalTimes.get(i) <= current_time && !used.get(i)) {
                        processQueue.add(i);
                        used.set(i, true);
                    }
                }

                if (burstleft.get(index) > 0) {
                    processQueue.add(index);
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
        new CPU_scheduling_algorithms();
    }
}
